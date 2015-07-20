(ns front-boiler.components.statuspage
  (:require [cljs.core.async :as async :refer [<!]]
            [om.core :as om :include-macros true]
            [front-boiler.async :refer [raise!]]
            [front-boiler.datetime :as datetime]
            [front-boiler.utils :as utils :include-macros true]
            [front-boiler.utils.ajax :as ajax])
  (:require-macros [cljs.core.async.macros :as am :refer [go]]
                   [front-boiler.utils :refer [html]]))

(def prod-page "6w4r0ttlx5ft")
(def test-page "qqq3lzwg9vl4")
(def page prod-page)
(def page-url (str "https://" page ".statuspage.io/api/v2/summary.json"))

(comment def sample-status
;; There are 2 methods to set sample statuses when testing the statuspage.io
;; widget.
;; - The most simple is to (def page test-page) and log in to
;;   statuspage.io and change the "CircleCI Test" page status as required.
;; -  The second method is to load one of the sample JSON files listed below
;;   and (def sample-status) to the loaded document. When loading, the JSON
;;   keys should be converted to Cloure keywords, using something like this:
;;   (clojure.data.json/read (clojure.java.io/reader file-name :key-fn keyword))
;; The om state for [:statuspage :summary] should then be set as in the comment
;; in update-statuspage-state
;;
;; The following test data is commited to the repo:
;; test/json/statuspage.io/github-degraded.json
;; test/json/statuspage.io/github-preventing-builds.json
;; test/json/statuspage.io/incident-and-mutliple-components.json
;; test/json/statuspage.io/normal.json
  {})

(defn update-statuspage-state [component]
  (comment om/set-state! component :statuspage {:summary sample-status})
  (go
   (let [res (<! (ajax/managed-ajax :get page-url))]
     (when (= :success (:status res))
       (om/set-state! component :statuspage {:summary (->> res :resp)})))))

(defn incident-markup [incident]
  (let [status (first (:incident_updates incident))]
    (html
      [:a {:href (:shortlink incident)}
       [:h4 (:name incident)]
       [:p (:body status)
        [:br]
        "Updated " (datetime/as-time-since (:updated_at status))]])))

(defn severity-class
  "Given the statuspage data, return one of none, minor, major or critical.
  The statuspage.io API has 2 places that we need to check to get an indication
  of the system health.
  First, there is a global status at :status :indicator
  Seconly, there is an array of active incidents, each of which has a :impact."
  [response]
  ;; Build a set of the available statuses.
  (let [severities (apply hash-set (get-in response [:status :indicator])
                                   (map :impact (:incidents response)))]
    ;; Find the first severity in the vector below that exists in severities
    (or (some severities ["critical" "major" "minor"])
        "none")))

(defn statuspage [app owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [delay-ms (* 60 1000)
            interval-id (js/setInterval #(update-statuspage-state owner)
                                        delay-ms)]
        (om/set-state! owner :statuspage-interval-id interval-id)
        ;; call it once up front rather than wait the whole delay
        ;; period for first call
        (update-statuspage-state owner)))

    om/IWillUnmount
    (will-unmount [_]
      (js/clearInterval (om/get-state owner :statuspage-interval-id)))

    om/IRender
    (render [_]
      (let [summary-response (:summary (om/get-state owner :statuspage))
            desc (get-in summary-response [:status :description])
            components-affected (filter (comp not (partial = "operational") :status)
                                        (:components summary-response))
            class (severity-class summary-response)]
        (html [:div#statuspage-bar {:class class}
               [:div (for [incident (:incidents summary-response)]
                       (incident-markup incident))]
               (when (seq components-affected)
                 [:a {:href (get-in summary-response [:page :url])}
                  [:h4 (str desc " - components affected: "
                            (clojure.string/join ", " (map (comp str :name) components-affected)))]])])))))
