(ns front-boiler.components.app
  (:require [cljs.core.async :as async :refer [>! <! alts! chan sliding-buffer close!]]
            [front-boiler.async :refer [raise!]]
            ;; [front-boiler.components.account :as account]
            ;; [front-boiler.components.about :as about]
            ;; [front-boiler.components.admin :as admin]
            ;; [front-boiler.components.aside :as aside]
            ;; [front-boiler.components.build :as build-com]
            ;; [front-boiler.components.dashboard :as dashboard]
            ;; [front-boiler.components.documentation :as docs]
            ;; [front-boiler.components.features :as features]
            ;; [front-boiler.components.mobile :as mobile]
            ;; [front-boiler.components.press :as press]
            ;; [front-boiler.components.add-projects :as add-projects]
            ;; [front-boiler.components.invites :as invites]
            ;; [front-boiler.components.changelog :as changelog]
            ;; [front-boiler.components.enterprise :as enterprise]
            [front-boiler.components.errors :as errors]
            [front-boiler.components.footer :as footer]
            [front-boiler.components.header :as header]
            [front-boiler.components.inspector :as inspector]
            ;; [front-boiler.components.integrations :as integrations]
            ;; [front-boiler.components.jobs :as jobs]
            ;; [front-boiler.components.key-queue :as keyq]
            ;; [front-boiler.components.placeholder :as placeholder]
            ;; [front-boiler.components.pricing :as pricing]
            ;; [front-boiler.components.privacy :as privacy]
            ;; [front-boiler.components.project-settings :as project-settings]
            ;; [front-boiler.components.security :as security]
            ;; [front-boiler.components.shared :as shared]
            ;; [front-boiler.components.stories :as stories]
            ;; [front-boiler.components.language-landing :as language-landing]
            [front-boiler.components.landing :as landing]
            ;; [front-boiler.components.org-settings :as org-settings]
            [front-boiler.components.common :as common]
            [front-boiler.config :as config]
            [front-boiler.instrumentation :as instrumentation]
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
    :build build-com/build
    :dashboard dashboard/dashboard
    :add-projects add-projects/add-projects
    :invite-teammates invites/teammates-invites
    :project-settings project-settings/project-settings
    :org-settings org-settings/org-settings
    :account account/account

    :admin-settings admin/admin-settings
    :build-state admin/build-state
    :switch admin/switch

    :loading loading

    :landing landing/home
    :about about/about
    :contact about/contact
    :team about/team
    :features features/features
    :pricing pricing/pricing
    :jobs jobs/jobs
    :press press/press
    :privacy privacy/privacy
    :security security/security
    :security-hall-of-fame security/hall-of-fame
    :enterprise enterprise/enterprise
    :azure enterprise/enterprise-azure
    :aws enterprise/enterprise-aws
    :stories stories/story
    :language-landing language-landing/language-landing
    :integrations integrations/integration
    :changelog changelog/changelog
    :documentation docs/documentation
    :mobile mobile/mobile
    :ios mobile/ios
    :android mobile/android

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
              show-inspector? (get-in app state/show-inspector-path)
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
              (when (and inner? logged-in?)
                (om/build aside/aside (dissoc app-without-container-data :current-build-data)))
              [:main.app-main {:ref "app-main"}
               (when show-inspector?
                 ;; TODO inspector still needs lots of work. It's slow and it defaults to
                 ;;     expanding all datastructures.
                 (om/build inspector/inspector app))
               (om/build header/header app-without-container-data)
               [:div.main-body
                (om/build dom-com app)]
               (when (config/footer-enabled?)
                 [:footer.main-foot
                  (footer/footer)])
               (when (and (config/help-tab-enabled?) (not logged-in?))
                 (om/build shared/sticky-help-link app))]])))))))


(defn app [app owner opts]
  (reify
    om/IDisplayName (display-name [_] "App Wrapper")
    om/IRender (render [_] (om/build app* (dissoc app :inputs :state-map) {:opts opts}))))
