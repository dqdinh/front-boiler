(ns front-boiler.controllers.navigation
  (:require [cljs.core.async :as async :refer [>! <! alts! chan sliding-buffer close!]]
            [clojure.string :as str]
            [front-boiler.async :refer [put!]]
            [front-boiler.api :as api]
            [front-boiler.favicon]
            [front-boiler.state :as state]
            [front-boiler.utils.ajax :as ajax]
            [front-boiler.utils.state :as state-utils]
            [front-boiler.utils :as utils :refer [mlog merror set-page-title! set-page-description! scroll-to-id! scroll!]]
            [goog.string :as gstring])
  (:require-macros
                   [cljs.core.async.macros :as am :refer [go go-loop alt!]]))

;; TODO we could really use some middleware here, so that we don't forget to
;;      assoc things in state on every handler
;;      We could also use a declarative way to specify each page.


;; --- Navigation Multimethod Declarations ---

(defmulti navigated-to
  (fn [history-imp navigation-point args state] navigation-point))

(defmulti post-navigated-to!
  (fn [history-imp navigation-point args previous-state current-state]
    (front-boiler.favicon/reset!)
    (put! (get-in current-state [:comms :ws]) [:unsubscribe-stale-channels])
    navigation-point))

;; --- Navigation Multimethod Implementations ---

(defn navigated-default [navigation-point args state]
  (-> state
      state-utils/clear-page-state
      (assoc :navigation-point navigation-point
             :navigation-data args)))

(defmethod navigated-to :default
  [history-imp navigation-point args state]
  (navigated-default navigation-point args state))

(defn post-default [navigation-point args]
  (set-page-title! (or (:_title args)
                       (str/capitalize (name navigation-point))))
  (when :_description args
        (set-page-description! (:_description args)))
  (scroll! args))

(defmethod post-navigated-to! :default
  [history-imp navigation-point args previous-state current-state]
  (post-default navigation-point args))

(defmethod navigated-to :navigate!
  [history-imp navigation-point args state]
  state)

(defmethod post-navigated-to! :navigate!
  [history-imp navigation-point {:keys [path replace-token?]} previous-state current-state]
  (let [path (if (= \/ (first path))
               (subs path 1)
               path)]
    (if replace-token? ;; Don't break the back button if we want to redirect someone
      (.replaceToken history-imp path)
      (.setToken history-imp path))))

;; More Examples
;; (defmethod navigated-to :dashboard
;;   [history-imp navigation-point args state]
;;   (-> state
;;       state-utils/clear-page-state
;;       (assoc :navigation-point navigation-point
;;              :navigation-data args
;;              :recent-builds nil)
;;       (state-utils/set-dashboard-crumbs args)
;;       state-utils/reset-current-build
;;       state-utils/reset-current-project))
;;
;; (defmethod post-navigated-to! :dashboard
;;   [history-imp navigation-point args previous-state current-state]
;;   (let [api-ch (get-in current-state [:comms :api])
;;         projects-loaded? (seq (get-in current-state state/projects-path))
;;         current-user (get-in current-state state/user-path)]
;;     (mlog (str "post-navigated-to! :dashboard with current-user? " (not (empty? current-user))
;;                " projects-loaded? " (not (empty? projects-loaded?))))
;;     (when (and (not projects-loaded?)
;;                (not (empty? current-user)))
;;       (api/get-projects api-ch))
;;     (go (let [builds-url (api/dashboard-builds-url (assoc (:navigation-data current-state)
;;                                                      :builds-per-page (:builds-per-page current-state)))
;;               api-resp (<! (ajax/managed-ajax :get builds-url))
;;               scopes (:scopes api-resp)
;;               _ (mlog (str "post-navigated-to! :dashboard, " builds-url " scopes " scopes))
;;               comms (get-in current-state [:comms])]
;;           (condp = (:status api-resp)
;;             :success (put! (:api comms) [:recent-builds :success (assoc api-resp :context args)])
;;             :failed (put! (:nav comms) [:error {:status (:status-code api-resp) :inner? false}])
;;             (put! (:errors comms) [:api-error api-resp]))
;;           (when (and (:repo args) (:read-settings scopes))
;;             (ajax/ajax :get
;;                        (gstring/format "/api/v1/project/%s/%s/settings" (:org args) (:repo args))
;;                        :project-settings
;;                        api-ch
;;                        :context {:project-name (str (:org args) "/" (:repo args))})
;;             (ajax/ajax :get
;;                        (gstring/format "/api/v1/project/%s/%s/plan" (:org args) (:repo args))
;;                        :project-plan
;;                        api-ch
;;                        :context {:project-name (str (:org args) "/" (:repo args))})))))
;;   (set-page-title!))
