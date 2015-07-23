;; Controls Controller Notes
;; - helper methods to change global state atom
;;   using cursors
;; - exposed through core/controls-handler
;; - excuted in a alt! function inside a go loop.
;;   alt! function return [value channel]

(ns front-boiler.controllers.controls
  (:require [cljs.core.async :as async :refer [>! <! alts! chan sliding-buffer close!]]
            [cljs.reader :as reader]
            [front-boiler.api :as api]
            [front-boiler.async :refer [put!]]
            [front-boiler.components.forms :refer [release-button!]]
            [front-boiler.state :as state]
            [front-boiler.utils.ajax :as ajax]
            [front-boiler.utils :as utils :include-macros true]
            [front-boiler.utils.seq :refer [dissoc-in]]
            [front-boiler.utils.state :as state-utils]
            [goog.dom]
            [goog.labs.userAgent.engine :as engine]
            goog.style)
  (:require-macros [cljs.core.async.macros :as am :refer [go go-loop alt!]])
  (:import [goog.fx.dom.Scroll]))

;; --- Helper Methods ---
;; TODO Move these methods into another file

(defn container-id [container]
  (int (last (re-find #"container_(\d+)" (.-id container)))))


(defn extract-from
  "Extract data from a nested map. Returns a new nested map comprising only the
  nested keys from `path`.

  user=> (extract-from nil nil)
  nil
  user=> (extract-from nil [])
  nil
  user=> (extract-from nil [:a])
  nil
  user=> (extract-from {} [:a])
  nil
  user=> (extract-from {:a 1} [:a])
  {:a 1}
  user=> (extract-from {:a {:b {:c 1}}, :d 2} [:a :b])
  {:a {:b {:c 1}}}"
  [m path]
  (when (seq path)
    (let [sentinel (js-obj)
          value (get-in m path sentinel)]
      (when-not (identical? value sentinel)
        (assoc-in {} path value)))))

(defn merge-settings
  "Merge new settings from inputs over a subset of project settings."
  [paths project settings]
  (letfn []
    (if (not (seq paths))
      settings
      (utils/deep-merge (apply merge {} (map (partial extract-from project) paths))
                        settings))))

(defn button-ajax
  "An ajax/ajax wrapper that releases the current managed-button after the API
  request.  Exists to faciliate migration away from stateful-button."
  [method url message channel & opts]
  (let [uuid front-boiler.async/*uuid*
        c (chan)]
    (apply ajax/ajax method url message c opts)
    (go-loop []
      (when-let [[_ status _ :as event] (<! c)]
        (when (#{:success :failed} status)
          (release-button! uuid status))
        (>! channel event)
        (recur)))))

;; --- Navigation Multimethod Declarations ---

(defmulti control-event
  ;; target is the DOM node at the top level for the app
  ;; message is the dispatch method (1st arg in the channel vector)
  ;; state is current state of the app
  ;; return value is the new state
  (fn [target message args state] message))

(defmulti post-control-event!
  (fn [target message args previous-state current-state] message))

;; --- Navigation Multimethod Implementations ---

;; (defmethod control-event :default
;;   [target message args state]
;;   (utils/mlog "Unknown controls: " message)
;;   state)
;;
;; (defmethod post-control-event! :default
;;   [target message args previous-state current-state]
;;   (utils/mlog "No post-control for: " message))

