(ns front-boiler.controllers.ws
  "Websocket controllers"
  (:require [clojure.set]
            [front-boiler.api :as api]
            [front-boiler.favicon]
            [front-boiler.models.action :as action-model]
            [front-boiler.models.build :as build-model]
            [front-boiler.pusher :as pusher]
            [front-boiler.utils.seq :refer [find-index]]
            [front-boiler.state :as state]
            [front-boiler.utils :as utils :include-macros true])
  (:require-macros [front-boiler.controllers.ws :refer [with-swallow-ignored-build-channels]]))

;; To subscribe to a channel, put a subscribe message in the websocket channel
;; with the channel name and the messages you want to listen to. That will be
;; handled in the post-ws controller.
;; Example: (put! ws-ch [:subscribe {:channel-name "my-channel" :messages [:my-message]}])
;;
;; Unsubscribe by putting an unsubscribe message in the channel with the channel name
;; Exampel: (put! ws-ch [:unsubscribe "my-channel"])
;; the api-post-controller can do any other actions

(defn fresh-channels
  "Returns all of the channels that a user should not be unsubscribed from"
  [state]
  (let [build (get-in state state/build-path)
        user (get-in state state/user-path)
        navigation-point (:navigation-point state)
        navigation-data (:navigation-data state)]
    (set (concat []
                 (when user [(pusher/user-channel user)])
                 (when build [(pusher/build-channel build)])
                 ;; Don't unsubscribe if the build takes a second to load
                 (when (= navigation-point :build)
                   [(pusher/build-channel-from-parts {:project-name (:project navigation-data)
                                                      :build-num (:build-num navigation-data)})])))))

(defn ignore-build-channel?
  "Returns true if we should ignore pusher updates for the given channel-name. This will be
  true if the channel is stale or if the build hasn't finished loading."
  [state channel-name]
  (if-let [build (get-in state state/build-path)]
    (not= channel-name (pusher/build-channel build))
    true))

(defn usage-queue-build-index-from-channel-name [state channel-name]
  "Returns index if there is a usage-queued build showing with the given channel name"
  (when-let [builds (seq (get-in state state/usage-queue-path))]
    (find-index #(= channel-name (pusher/build-channel %)) builds)))

;; --- Navigation Multimethod Declarations ---

(defmulti ws-event
  (fn [pusher-imp message args state] message))

(defmulti post-ws-event!
  (fn [pusher-imp message args previous-state current-state] message))

;; --- Navigation Mutlimethod Implementations ---

(defmethod ws-event :default
  [pusher-imp message args state]
  (utils/mlog "Unknown ws event: " (pr-str message))
  state)

(defmethod post-ws-event! :default
  [pusher-imp message args previous-state current-state]
  (utils/mlog "No post-ws for: " message))

(defmethod ws-event :build/update
  [pusher-imp message {:keys [data channel-name]} state]
  (if-not (ignore-build-channel? state channel-name)
    (update-in state state/build-path merge (utils/js->clj-kw data))
    (if-let [index (usage-queue-build-index-from-channel-name state channel-name)]
      (update-in state (state/usage-queue-build-path index) merge (utils/js->clj-kw data))
      state)))

(defmethod post-ws-event! :build/update
  [pusher-imp message {:keys [data channel-name]} previous-state current-state]
  (when-not (ignore-build-channel? current-state channel-name)
    (front-boiler.favicon/set-color! (build-model/favicon-color (utils/js->clj-kw data)))
    (let [build (get-in current-state state/build-path)]
      (when (and (build-model/finished? build)
                 (empty? (get-in current-state state/tests-path)))
        (api/get-build-tests build
                             (get-in current-state [:comms :api]))))))

(defmethod ws-event :build/new-action
  [pusher-imp message {:keys [data channel-name]} state]
  (with-swallow-ignored-build-channels state channel-name
    (reduce (fn [state {action-index :step container-index :index action-log :log}]
              (-> state
                  (build-model/fill-containers container-index action-index)
                  (assoc-in (state/action-path container-index action-index) action-log)
                  (update-in (state/action-path container-index action-index) action-model/format-latest-output)))
            state (utils/js->clj-kw data))))


(defmethod ws-event :build/update-action
  [pusher-imp message {:keys [data channel-name]} state]
  (with-swallow-ignored-build-channels state channel-name
    (reduce (fn [state {action-index :step container-index :index action-log :log}]
              (-> state
                  (build-model/fill-containers container-index action-index)
                  (update-in (state/action-path container-index action-index) merge action-log)))
            state (utils/js->clj-kw data))))


(defmethod ws-event :build/append-action
  [pusher-imp message {:keys [data channel-name]} state]
  (with-swallow-ignored-build-channels state channel-name
    (reduce (fn [state data]
              (let [container-index (aget data "index")
                    action-index (aget data "step")]
                (if (not= container-index (get-in state state/current-container-path 0))
                  (do (utils/mlog "Ignoring output for inactive container: " container-index)
                      (-> state
                          (build-model/fill-containers container-index action-index)
                          (update-in (state/action-path container-index action-index) assoc :missing-pusher-output true :has_output true)))

                  (let [output (utils/js->clj-kw (aget data "out"))]
                    (-> state
                        (build-model/fill-containers container-index action-index)
                        (update-in (state/action-output-path container-index action-index) vec)
                        (update-in (state/action-output-path container-index action-index) conj output)
                        (update-in (state/action-path container-index action-index) action-model/format-latest-output))))))
            state data)))


(defmethod ws-event :build/add-messages
  [pusher-imp message {:keys [data channel-name]} state]
  (let [build (get-in state state/build-path)
        new-messages (set (utils/js->clj-kw data))]
    (with-swallow-ignored-build-channels state channel-name
      (update-in state (conj state/build-path :messages)
                 (fn [messages] (-> messages
                                    set ;; careful not to add the same message twice
                                    (clojure.set/union new-messages)))))))


(defmethod post-ws-event! :subscribe
  [pusher-imp message {:keys [channel-name messages context]} previous-state current-state]
  (let [ws-ch (get-in current-state [:comms :ws])]
    (utils/mlog "subscribing to " channel-name)
    (pusher/subscribe pusher-imp channel-name ws-ch :messages messages :context context)))


(defmethod post-ws-event! :unsubscribe
  [pusher-imp message channel-name previous-state current-state]
  (pusher/unsubscribe pusher-imp channel-name))

(defmethod post-ws-event! :unsubscribe-stale-channels
  [pusher-imp message _ previous-state current-state]
  (doseq [channel-name (clojure.set/difference (pusher/subscribed-channels pusher-imp)
                                               (fresh-channels current-state))]
    (utils/mlog "unsubscribing from " channel-name)
    (pusher/unsubscribe pusher-imp channel-name)))

(defmethod post-ws-event! :refresh
  [pusher-imp message _ previous-state current-state]
  (let [navigation-point (:navigation-point current-state)
        api-ch (get-in current-state [:comms :api])]
    (api/get-projects api-ch)
    (condp = navigation-point
      :build (when (get-in current-state state/show-usage-queue-path)
               (api/get-usage-queue (get-in current-state state/build-path) api-ch))
      :dashboard (api/get-dashboard-builds (assoc (:navigation-data current-state)
                                             :builds-per-page (:builds-per-page current-state))
                                           api-ch)
      nil)))
