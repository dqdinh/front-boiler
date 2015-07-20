(ns front-boiler.core
    (:require
      [om.core :as om :include-macros true]
      [sablono.core :refer-macros [html]]))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload
(defonce app-state
  (atom nil))

(defn render! []
  (om/root
    (fn [data owner]
      (reify
        om/IRender
        (render [_]
          (html
            [:div.aside-left-menu
              ;; (om/build classes-view data)
              ]))))
    app-state
    {:target (. js/document (getElementById "app"))}))

(render!)

(defn on-js-reload [] (render!))

