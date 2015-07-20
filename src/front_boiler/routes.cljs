(ns front-boiler.routes
  (:require [cljs.core.async :as async :refer [>! <! alts! chan sliding-buffer close!]]
            [clojure.string :as str]
            [front-boiler.async :refer [put!]]
            [goog.events :as events]
            [front-boiler.models.project :as proj-mod]
            [front-boiler.utils.docs :as doc-utils]
            [front-boiler.utils :as utils :include-macros true]
            [secretary.core :as sec :include-macros true :refer-macros [defroute]])
  (:require-macros [cljs.core.async.macros :as am :refer [go go-loop alt!]]))


(defn open-to-inner! [nav-ch navigation-point args]
  (put! nav-ch [navigation-point (assoc args :inner? true)]))

(defn open-to-outer! [nav-ch navigation-point args]
  (put! nav-ch [navigation-point (assoc args :inner? false)]))

(defn logout! [nav-ch]
  (put! nav-ch [:logout]))

(defn v1-build-path
  "Temporary helper method for v1-build until we figure out how to make
   secretary's render-route work for regexes"
  [org repo build-num]
  (str "/gh/" org "/" repo "/" build-num))

(defn v1-dashboard-path
  "Temporary helper method for v1-*-dashboard until we figure out how to
   make secretary's render-route work for multiple pages"
  [{:keys [org repo branch page]}]
  (let [url (cond branch (str "/gh/" org "/" repo "/tree/" branch)
                  repo (str "/gh/" org "/" repo)
                  org (str "/gh/" org)
                  :else "/")]
    (str url (when page (str "?page=" page)))))

(defn define-admin-routes! [nav-ch]
  (defroute v1-admin-switch "/admin/switch" []
    (open-to-inner! nav-ch :switch {:admin true}))
  (defroute v1-admin-recent-builds "/admin/recent-builds" []
    (open-to-inner! nav-ch :dashboard {:admin true}))
  (defroute v1-admin-deployments "/admin/deployments" []
    (open-to-inner! nav-ch :dashboard {:deployments true}))
  (defroute v1-admin-build-state "/admin/build-state" []
    (open-to-inner! nav-ch :build-state {:admin true}))

  (defroute v1-admin "/admin" []
    (open-to-inner! nav-ch :admin-settings {:admin true
                                            :subpage nil}))
  (defroute v1-admin-fleet-state "/admin/fleet-state" []
    (open-to-inner! nav-ch :admin-settings {:admin true
                                            :subpage :fleet-state})))


;; Creates a route that will ignore fragments and add them to params as {:_fragment "#fragment"}
(defrecord FragmentRoute [route]
  sec/IRenderRoute
  (render-route [this]
    (sec/render-route route))
  (render-route [this params]
    (sec/render-route route params)))

(extend-protocol sec/IRouteMatches
  FragmentRoute
  (route-matches [this route]
    (let [[normal-route fragment] (str/split route #"#" 2)]
      (when-let [match (sec/route-matches (sec/compile-route (:route this)) normal-route)]
        (merge match
               (when fragment {:_fragment fragment}))))))


(defn define-user-routes! [nav-ch authenticated?]
  (defroute v1-org-settings "/gh/organizations/:org/settings"
    [org]
    (open-to-inner! nav-ch :org-settings {:org org :subpage nil}))
  (defroute v1-org-settings-subpage "/gh/organizations/:org/settings#:subpage"
    [org subpage]
    (open-to-inner! nav-ch :org-settings {:org org :subpage (keyword subpage)}))
  (defroute v1-org-dashboard-alternative "/gh/organizations/:org" {:as params}
    (open-to-inner! nav-ch :dashboard params))
  (defroute v1-org-dashboard "/gh/:org" {:as params}
    (open-to-inner! nav-ch :dashboard params))
  (defroute v1-project-dashboard "/gh/:org/:repo" {:as params}
    (open-to-inner! nav-ch :dashboard params))
  (defroute v1-project-branch-dashboard #"/gh/([^/]+)/([^/]+)/tree/(.+)" ; workaround secretary's annoying auto-decode
    [org repo branch args]
    (open-to-inner! nav-ch :dashboard (merge args {:org org :repo repo :branch branch})))
  (defroute v1-build #"/gh/([^/]+)/([^/]+)/(\d+)"
    [org repo build-num]
    (open-to-inner! nav-ch :build {:project-name (str org "/" repo)
                                   :build-num (js/parseInt build-num)
                                   :org org
                                   :repo repo}))
  (defroute v1-project-settings "/gh/:org/:repo/edit"
    [org repo]
    (open-to-inner! nav-ch :project-settings {:project-name (str org "/" repo)
                                              :subpage nil
                                              :org org
                                              :repo repo}))
  (defroute v1-project-settings-subpage "/gh/:org/:repo/edit#:subpage"
    [org repo subpage]
    (open-to-inner! nav-ch :project-settings {:project-name (str org "/" repo)
                                              :subpage (keyword subpage)
                                              :org org
                                              :repo repo}))
  (defroute v1-add-projects "/add-projects" []
    (open-to-inner! nav-ch :add-projects {}))
  (defroute v1-invite-teammates "/invite-teammates" []
    (open-to-inner! nav-ch :invite-teammates {}))
  (defroute v1-invite-teammates-org "/invite-teammates/organization/:org" [org]
    (open-to-inner! nav-ch :invite-teammates {:org org}))
  (defroute v1-account "/account" []
    (open-to-inner! nav-ch :account {:subpage nil}))
  (defroute v1-account-subpage "/account/:subpage" [subpage]
    ;; TODO: make it possible to call (v1-account-subpage {:subpage subpage}) with a fragment route then remove this hack
    (open-to-inner! nav-ch :account {:subpage (keyword (first (str/split subpage #"#")))}))
  (defroute v1-logout "/logout" []
    (logout! nav-ch))

  (defroute v1-doc "/docs" []
    (open-to-outer! nav-ch :documentation {}))
  (defroute v1-doc-subpage (FragmentRoute. "/docs/:subpage") {:keys [subpage] :as params}
    (open-to-outer! nav-ch :documentation (assoc params :subpage (keyword subpage))))

  (defroute v1-about (FragmentRoute. "/about") {:as params}
    (open-to-outer! nav-ch :about (assoc params
                                    :_title "About Us"
                                    :_description "Learn more about the CircleCI story and why we're building the leading Continuous Integration and Deployment platform.")))

  (defroute v1-team (FragmentRoute. "/about/team") {:as params}
    (open-to-outer! nav-ch :team (assoc params
                                    :_title "About the Team"
                                    :_description "Meet the team behind CircleCI, the state-of-the-art automated testing, continuous integration, and continuous deployment tool made for developers.")))

  (defroute v1-contact (FragmentRoute. "/contact") {:as params}
    (open-to-outer! nav-ch :contact (assoc params
                                      :_title "Contact Us"
                                      :_description "Get in touch with CircleCI.")))

  (defroute v1-mobile (FragmentRoute.  "/mobile") {:as params}
    (open-to-outer! nav-ch :mobile (assoc params
                                     :_title "Mobile Continuous Integration and Mobile App Testing"
                                     :_description "Build 5-star mobile apps with Mobile Continuous Integration by automating your build, test, and deployment workflow on iOS and Android. ")))

  (defroute v1-ios (FragmentRoute.  "/mobile/ios") {:as params}
    (open-to-outer! nav-ch :ios (assoc params
                                  :_title "Apple iOS App Testing"
                                  :_description "Build 5-star iOS apps by automating your development workflow with Mobile Continuous Integration and Delivery.")))

  (defroute v1-android (FragmentRoute.  "/mobile/android") {:as params}
    (open-to-outer! nav-ch :android (assoc params
                                      :_title "Android App Testing"
                                      :_description "Build better Android apps with Mobile Continuous Integration. Get testing today!")))

  (defroute v1-pricing (FragmentRoute. "/pricing") {:as params}
    (if authenticated?
      (open-to-inner! nav-ch :account {:subpage :plans})
      (open-to-outer! nav-ch :pricing (assoc params
                                        :_analytics-page "View Pricing Outer"
                                        :_title "Pricing and Information"
                                        :_description "Save time and cost by making your engineering team more efficient. Get started for free and see how many containers and parallelism you need to scale with your team."))))

  (defroute v1-jobs (FragmentRoute. "/jobs") {:as params}
    (open-to-outer! nav-ch :jobs (assoc params
                                   :_analytics-page "View jobs"
                                   :_title "Search for Jobs at CircleCI"
                                   :_description "Come work with us. Join our amazing team of highly technical engineers and business leaders to help us build great developer tools.")))

  (defroute v1-privacy (FragmentRoute. "/privacy") {:as params}
    (open-to-outer! nav-ch :privacy (assoc params
                                      :_analytics-page "View Privacy"
                                      :_title "Privacy Policy"
                                      :_description "Read our privacy policy to understand how we collect and use information about you.")))

  (defroute v1-security (FragmentRoute. "/security") {:as params}
    (open-to-outer! nav-ch :security (assoc params
                                       :_analytics-page "View Security"
                                       :_title "Security Policy"
                                       :_description "Read our security policy and guidelines and see how your data is safe with CircleCI.")))

  (defroute v1-security-hall-of-fame (FragmentRoute. "/security/hall-of-fame") {:as params}
    (open-to-outer! nav-ch :security-hall-of-fame (assoc params
                                                    :_title "Security Hall of Fame"
                                                    :_description "Join our Security Hall of Fame by helping us make our platform more secure."
                                                    :_analytics-page "View Security Hall of Fame")))

  (defroute v1-enterprise (FragmentRoute. "/enterprise") {:as params}
    (open-to-outer! nav-ch :enterprise (assoc params
                                         :_title "Enterprise Continuous Integration and Deployment"
                                         :_description "Reduce risk with Enterprise Continuous Integration from CircleCI. Integrates seamlessly with Github Enterprise and the rest of your technology stack.")))

  (defroute v1-enterprise-aws (FragmentRoute. "/enterprise/aws") {:as params}
    (open-to-outer! nav-ch :aws (assoc params
                                         :_title "CircleCI Enterprise on AWS"
                                         :_description "Run CircleCI Enterprise on the same AWS infrastructure you use for everything else. Integrates seamlessly with GitHub Enterprise on AWS.")))

  (defroute v1-enterprise-azure (FragmentRoute. "/enterprise/azure") {:as params}
    (open-to-outer! nav-ch :azure (assoc params
                                         :_title "CircleCI Enterprise on Azure"
                                         :_description "Install CircleCI Enterprise in your own Microsoft Azure account. Integrates with GitHub Enterprise on Azure and Active Directory authentication.")))

  (defroute v1-stories (FragmentRoute. "/stories/:story") [story]
    (open-to-outer! nav-ch :stories {:story (keyword story)}))

  (defroute v1-features (FragmentRoute. "/features") {:as params}
    (open-to-outer! nav-ch :features (assoc params
                                       :_title "Continuous Integration Product and Features"
                                       :_description "Build a better product and let CircleCI handle your testing. CircleCI helps your team improve productivity with faster development, reduced risk, and better code.")))

  (defroute v1-languages (FragmentRoute. "/features/:language") {:as params}
    (open-to-outer! nav-ch :language-landing params))

  (defroute v1-integrations (FragmentRoute. "/integrations/:integration") [integration]
    (open-to-outer! nav-ch :integrations {:integration (keyword integration)}))

  (defroute v1-changelog-individual (FragmentRoute. "/changelog/:id") {:as params}
    (open-to-outer! nav-ch :changelog params))

  (defroute v1-changelog (FragmentRoute. "/changelog") {:as params}
    (open-to-outer! nav-ch :changelog params))

  (defroute v1-root (FragmentRoute. "/") {:as params}
    (if authenticated?
      (open-to-inner! nav-ch :dashboard params)
      (open-to-outer! nav-ch :landing (assoc params :_canonical "/"))))

  (defroute v1-home (FragmentRoute. "/home") {:as params}
    (open-to-outer! nav-ch :landing (assoc params :_canonical "/")))

  (defroute v1-press (FragmentRoute. "/press") {:as params}
    (open-to-outer! nav-ch :press (assoc params
                                         :_title "Press Releases and Updates"
                                         :_description "Find the latest CircleCI news and updates here."))))

(defn define-spec-routes! [nav-ch]
  (defroute trailing-slash #"(.+)/$" [path]
    (put! nav-ch [:navigate! {:path path :replace-token? true}]))
  (defroute v1-not-found "*" []
    (open-to-outer! nav-ch :error {:status 404})))

(defn define-routes! [state]
  (let [nav-ch (get-in @state [:comms :nav])
        authenticated? (boolean (get-in @state [:current-user]))]
    (define-user-routes! nav-ch authenticated?)
    (when (get-in @state [:current-user :admin])
      (define-admin-routes! nav-ch))
    (define-spec-routes! nav-ch)))
