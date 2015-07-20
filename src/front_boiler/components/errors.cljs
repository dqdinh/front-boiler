(ns front-boiler.components.errors
  (:require [front-boiler.state :as state]
            [front-boiler.async :refer [raise!]]
            [front-boiler.components.common :as common]
            [front-boiler.utils :as utils :include-macros true]
            [front-boiler.utils.github :as gh-utils]
            [om.core :as om :include-macros true])
  (:require-macros [front-boiler.utils :refer [html]]))

(defn error-page [app owner]
  (reify
    om/IRender
    (render [_]
      (let [status (get-in app [:navigation-data :status])
            logged-in? (get-in app state/user-path)
            orig-nav-point (get-in app [:original-navigation-point])
            _ (utils/mlog "error-page render with orig-nav-point " orig-nav-point " and logged-in? " (boolean logged-in?))
            maybe-login-page? (some #{orig-nav-point} [:dashboard :build])]
        (html
         [:div.page.error
          [:div.banner
           [:div.container
            [:h1 status]
            [:h3 (str (condp = status
                        401 "Login required"
                        404 "Page not found"
                        500 "Internal server error"
                        "Something unexpected happened"))]]]
          [:div.container
           (condp = status
             401 [:p
                  [:b [:a {:href (gh-utils/auth-url)
                           :on-click #(raise! owner [:track-external-link-clicked
                                                     {:event "login_click"
                                                      :properties {:source "401"
                                                                   :url js/window.location.pathname}
                                                      :path (gh-utils/auth-url)}])}
                       "Log in"]]
                  " here to view this page"]
             404 (if (and (not logged-in?) maybe-login-page?)
                   [:div
                    [:p "We're sorry; either that page doesn't exist or you need to be logged in to view it."]
                    [:p [:b [:a {:href (gh-utils/auth-url)
                                 :on-click #(raise! owner [:track-external-link-clicked
                                                           {:event "login_click"
                                                            :properties {:source "404"
                                                                         :url js/window.location.pathname}
                                                            :path (gh-utils/auth-url)}])} "Log in"] " here to view this page with your GitHub permissions."]]]
                   [:p "We're sorry, but that page doesn't exist."])
             500 [:p "We're sorry, but something broke"]
             "Something completely unexpected happened")]])))))
