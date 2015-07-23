(ns front-boiler.components.app
  (:require [cljs.core.async :as async :refer [>! <! alts! chan sliding-buffer close!]]
            [front-boiler.async :refer [raise!]]
            [front-boiler.components.errors :as errors]
            [front-boiler.components.key-queue :as keyq]
            [front-boiler.components.footer :as footer]
            [front-boiler.components.header :as header]
            [front-boiler.components.landing :as landing]
            [front-boiler.components.common :as common]
            [front-boiler.config :as config]
            [front-boiler.state :as state]
            [front-boiler.utils :as utils :include-macros true]
            [front-boiler.utils.seq :refer [dissoc-in]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [ankha.core :as ankha])
  (:require-macros [front-boiler.utils :refer [html]]))

(def keymap
  (atom nil))

(defn loading [app owner]
  (reify
    om/IRender
    (render [_] (html [:div.loading-spinner common/spinner]))))

(defn dominant-component [app-state owner]
  (case (:navigation-point app-state)
    :loading loading
    :landing landing/home
    :error errors/error-page))

(defn app* [app owner {:keys [reinstall-om!]}]
  (reify
    om/IDisplayName (display-name [_] "App")
    om/IRender
    (render [_]
      (if-not (:navigation-point app)
        (html [:div#app])

        (let [persist-state! #(raise! owner [:state-persisted])
              restore-state! #(do (raise! owner [:state-restored])
                                  ;; Components are not aware of external state changes.
                                  (reinstall-om!))
              logged-in? (get-in app state/user-path)
              ;; simple optimzation for real-time updates when the build is running
              app-without-container-data (dissoc-in app state/container-data-path)
              dom-com (dominant-component app owner)]
          (reset! keymap {["ctrl+s"] persist-state!
                          ["ctrl+r"] restore-state!})
          (html
           (let [inner? (get-in app state/inner?-path)]

             [:div#app {:class (concat [(if inner? "inner" "outer")]
                                       (when-not logged-in? ["aside-nil"])
                                       ;; The following 2 are meant for the landing ab test to hide old heaqder/footer
                                       (when (= :landing (:navigation-point app)) ["landing"])
                                       (when (= :pricing (:navigation-point app)) ["pricing"]))}
              (om/build keyq/KeyboardHandler app-without-container-data
                        {:opts {:keymap keymap
                                :error-ch (get-in app [:comms :errors])}})
              [:main.app-main {:ref "app-main"}
               (om/build header/header app-without-container-data)
               [:div.main-body
                (om/build dom-com app)]
               (when (config/footer-enabled?)
                 [:footer.main-foot
                  (footer/footer)]) ]])))))))

(defn app [app owner opts]
  (reify
    om/IDisplayName (display-name [_] "App Wrapper")
    om/IRender (render [_] (om/build app* (dissoc app :inputs :state-map) {:opts opts}))))
