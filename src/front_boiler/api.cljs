(ns front-boiler.api
  (:require
            [front-boiler.utils :as utils :include-macros true]
            [front-boiler.utils.ajax :as ajax]
            [front-boiler.utils.vcs-url :as vcs-url]
            [goog.string :as gstring]
            [goog.string.format]
            [secretary.core :as sec]))

(defn get-projects [api-ch]
  (ajax/ajax :get "/api/v1/projects?shallow=true" :projects api-ch))

(defn dashboard-builds-url [{:keys [branch repo org admin deployments query-params builds-per-page]}]
  (let [url (cond admin "/api/v1/admin/recent-builds"
                  deployments "/api/v1/admin/deployments"
                  branch (gstring/format "/api/v1/project/%s/%s/tree/%s" org repo branch)
                  repo (gstring/format "/api/v1/project/%s/%s" org repo)
                  org (gstring/format "/api/v1/organization/%s" org)
                  :else "/api/v1/recent-builds")
        page (get query-params :page 0)]
    (str url "?" (sec/encode-query-params (merge {:shallow true
                                                  :offset (* page builds-per-page)
                                                  :limit builds-per-page}
                                                 query-params)))))

(defn get-action-output [{:keys [vcs-url build-num step index output-url] :as args}
                         api-ch]
  (let [url (or output-url
                (gstring/format "/api/v1/project/%s/%s/output/%s/%s"
                                (vcs-url/project-name vcs-url)
                                build-num
                                step
                                index))]
    (ajax/ajax :get
               url
               :action-log
               api-ch
               :context args)))

