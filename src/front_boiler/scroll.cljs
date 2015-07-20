(ns front-boiler.scroll
  (:require [front-boiler.disposable :as disposable]
            [front-boiler.utils :as utils :include-macros true]
            [om.core :as om :include-macros true]
            [goog.events]))

(defn register [owner cb]
  (om/set-state! owner ::scroll-id
    (disposable/register
      (goog.events/listen
        js/window
        "scroll"
        (fn [event]
          ;; FIXME: Dispose doesn't actually remove the event listeners
          ;; so we end up leaking event handlers that survive their components
          ;; and throw interesting errors when events are triggered against unmounted components
          (when (om/mounted? owner)
            (utils/swallow-errors (cb event)))))
      (fn dispose [event-key]
        (goog.events/unlistenByKey event-key)))))

(defn dispose [owner]
  (disposable/dispose (om/get-state owner ::scroll-id)))
