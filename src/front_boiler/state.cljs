(ns front-boiler.state)

(defn initial-state []
  {:error-message nil
   :changelog nil
   :environment "development"
   :settings {:projects {}            ; hash of project-id to settings
              :organizations  {:circleci  {:plan {}}}
              :add-projects {:repo-filter-string ""
                             :selected-org {:login nil
                                            :type :org}
                             :show-fork-accounts false
                             :show-forks false}}
   :selected-home-technology-tab nil
   :modal-video-id nil
   :builds-per-page 30
   :navigation-point nil
   :navigation-data nil
   :navigation-settings {}
   :current-user nil
   :crumbs nil
   :current-repos []
   :render-context nil
   :projects nil
   :recent-builds nil
   :project-settings-subpage nil
   :project-settings-project-name nil
   :org-settings-subpage nil
   :org-settings-org-name nil
   :admin-settings-subpage nil
   :dashboard-data {:branch nil
                    :repo nil
                    :org nil}
   :current-project-data {:project nil
                          :plan nil
                          :settings {}
                          :tokens nil
                          :checkout-keys nil
                          :envvars nil}
   :current-build-data {:build nil
                        :usage-queue-data {:builds nil
                                           :show-usage-queue false}
                        :artifact-data {:artifacts nil
                                        :show-artifacts false}
                        :config-data {:show-config false}
                        :current-container-id 0
                        :container-data {:current-container-id 0
                                         :containers nil}}
   :current-org-data {:plan nil
                      :projects nil
                      :users nil
                      :invoices nil
                      :name nil}
   :invite-data {:dismiss-invite-form nil
                 :github-users nil}
   :instrumentation []
   ;; This isn't passed to the components, it can be accessed though om/get-shared :_app-state-do-not-use
   :inputs nil})

(def user-path [:current-user])

(def build-data-path [:current-build-data])
(def build-path [:current-build-data :build])
(def invite-github-users-path [:invite-data :github-users])
(defn invite-github-user-path [index] (conj invite-github-users-path index))
(def dismiss-invite-form-path [:invite-data :dismiss-invite-form])
(def dismiss-config-errors-path (conj build-data-path :dismiss-config-errors))
(def invite-logins-path (conj build-data-path :invite-data :invite-logins))
(defn invite-login-path [login] (conj invite-logins-path login))

(def usage-queue-path [:current-build-data :usage-queue-data :builds])
(defn usage-queue-build-path [build-index] (conj usage-queue-path build-index))
(def show-usage-queue-path [:current-build-data :usage-queue-data :show-usage-queue])

(def artifacts-path [:current-build-data :artifacts-data :artifacts])
(def show-artifacts-path [:current-build-data :artifacts-data :show-artifacts])

(def tests-path [:current-build-data :tests-data :tests])

(def show-config-path [:current-build-data :config-data :show-config])

(def container-data-path [:current-build-data :container-data])
(def containers-path [:current-build-data :container-data :containers])
(def current-container-path [:current-build-data :container-data :current-container-id])
(def build-header-tab-path [:current-build-data :selected-header-tab])


(defn container-path [container-index] (conj containers-path container-index))
(defn actions-path [container-index] (conj (container-path container-index) :actions))
(defn action-path [container-index action-index] (conj (actions-path container-index) action-index))
(defn action-output-path [container-index action-index] (conj (action-path container-index action-index) :output))
(defn show-action-output-path [container-index action-index] (conj (action-path container-index action-index) :show-output))

(def project-data-path [:current-project-data])
(def project-plan-path (conj project-data-path :plan))
(def project-tokens-path (conj project-data-path :tokens))
(def project-checkout-keys-path (conj project-data-path :checkout-keys))
(def project-envvars-path (conj project-data-path :envvars))
(def project-settings-branch-path (conj project-data-path :settings-branch))
(def project-path (conj project-data-path :project))
(def project-scopes-path (conj project-data-path :project-scopes))
(def page-scopes-path [:page-scopes])

(def project-new-ssh-key-path (conj project-data-path :new-ssh-key))
(def project-new-api-token-path (conj project-data-path :new-api-token))

(def crumbs-path [:crumbs])
(defn project-branch-crumb-path [state]
  (let [crumbs (get-in state crumbs-path)
        project-branch-crumb-index (->> crumbs
                                        (keep-indexed
                                          #(when (= (:type %2) :project-branch)
                                             %1))
                                        first)]
    (conj crumbs-path project-branch-crumb-index)))

;; TODO we probably shouldn't be storing repos in the user...
(def user-organizations-path (conj user-path :organizations))
(def user-tokens-path (conj user-path :tokens))
(def user-collaborators-path (conj user-path :collaborators))

(defn repos-path
  "Path for a given set of repos (e.g. all heavybit repos). Login is the username,
   type is :user or :org"
  [login type] (conj user-path :repos (str login "." type)))

(defn repo-path [login type repo-index]
  (conj (repos-path login type) repo-index))


(def org-data-path [:current-org-data])
(def org-name-path (conj org-data-path :name))
(def org-plan-path (conj org-data-path :plan))
(def org-plan-balance-path (conj org-plan-path :account_balance))
(def stripe-card-path (conj org-data-path :card))
(def org-users-path (conj org-data-path :users))
(def org-projects-path (conj org-data-path :projects))
(def org-loaded-path (conj org-data-path :loaded))
(def org-authorized?-path (conj org-data-path :authorized?))
(def selected-containers-path (conj org-data-path :selected-containers))
;; Map of org login to boolean (selected or not selected)
(def selected-piggyback-orgs-path (conj org-data-path :selected-piggyback-orgs))
(def selected-transfer-org-path (conj org-data-path :selected-transfer-org))
(def org-invoices-path (conj org-data-path :invoices))
(def selected-cancel-reasons-path (conj org-data-path :selected-cancel-reasons))
;; Map of reason to boolean (selected or not selected)
(defn selected-cancel-reason-path [reason] (conj selected-cancel-reasons-path reason))
(def cancel-notes-path (conj org-data-path :cancel-notes))

(def settings-path [:settings])

(def projects-path [:projects])

(def inner?-path [:navigation-data :inner?])

(def instrumentation-path [:instrumentation])

(def browser-settings-path [:settings :browser-settings])
(def show-instrumentation-line-items-path (conj browser-settings-path :show-instrumentation-line-items))
(def show-admin-panel-path (conj browser-settings-path :show-admin-panel))
(def show-all-branches-path (conj browser-settings-path :show-all-branches))
(defn project-branches-collapsed-path [project-id] (conj browser-settings-path :projects project-id :branches-collapsed))
(def show-inspector-path (conj browser-settings-path :show-inspector))

(def account-subpage-path [:account-settings-subpage])
(def new-user-token-path (conj user-path :new-user-token))

(def flash-path [:render-context :flash])

(def error-data-path [:error-data])

(def selected-home-technology-tab-path [:selected-home-technology-tab])

(def modal-video-id-path [:modal-video-id])

(def language-testimonial-tab-path [:selected-language-testimonial-tab])

(def changelog-path [:changelog])

(def build-state-path [:build-state])

(def fleet-state-path [:fleet-state])

(def error-message-path [:error-message])

(def inputs-path [:inputs])

(def docs-data-path [:docs-data])
(def docs-search-path [:docs-query])
(def docs-articles-results-path [:docs-articles-results])
(def docs-articles-results-query-path [:docs-articles-results-query])

(def customer-logo-customer-path [:customer-logo-customer])

(def selected-toolset-path [:selected-toolset])

(def pricing-parallelism-path [:pricing-parallelism])
