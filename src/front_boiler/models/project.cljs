(ns front-boiler.models.project
  (:require [clojure.string :refer [lower-case split join]]
            [front-boiler.utils :as utils :include-macros true]
            [goog.string :as gstring]
            [front-boiler.models.plan :as plan-model]))

(defn project-name [project]
  (->> (split (:vcs_url project) #"/")
       (take-last 2)
       (join "/")))

(defn path-for [project & [branch]]
  (str "/gh/" (project-name project)
       (when branch
         (str "/tree/" (gstring/urlEncode branch)))))

(defn settings-path [project]
  (str "/gh/" (project-name project) "/edit"))

(defn default-branch? [branch-name project]
  (= (name branch-name) (:default_branch project)))

(defn personal-branch? [user project branch-data]
  (let [[branch-name build-info] branch-data]
    (or (default-branch? branch-name project)
        (some #{(:login user)} (:pusher_logins build-info)))))

(defn branch-builds [project branch-name-kw]
  (let [build-data (get-in project [:branches branch-name-kw])]
    (sort-by :build_num (concat (:running_builds build-data)
                                (:recent_builds build-data)))))

(defn master-builds
  "Returns branch builds for the project's default branch (usually master)"
  [project]
  (branch-builds project (keyword (:default_branch project))))

(def hipchat-keys [:hipchat_room :hipchat_api_token :hipchat_notify :hipchat_notify_prefs])
(def slack-keys [:slack_channel :slack_subdomain :slack_api_token :slack_notify_prefs :slack_webhook_url])
(def campfire-keys [:campfire_room :campfire_token :campfire_subdomain :campfire_notify_prefs])
(def flowdock-keys [:flowdock_api_token])
(def irc-keys [:irc_server :irc_channel :irc_keyword :irc_username :irc_password :irc_notify_prefs])

(def notification-keys (concat hipchat-keys slack-keys campfire-keys flowdock-keys irc-keys))

(defn notification-settings [project]
  (select-keys project notification-keys))

(defn last-master-build
  "Gets the last finished master build on the branch"
  [project]
  (first (get-in project [:branches (keyword (:default_branch project)) :recent_builds])))

(defn sidebar-sort [l, r]
  (let [l-last-build (last-master-build l)
        r-last-build (last-master-build r)]
    (cond (and l-last-build r-last-build)
          (compare (:build_num r-last-build)
                   (:build_num l-last-build))

          l-last-build -1
          r-last-build 1
          :else (compare (lower-case (:vcs_url l)) (lower-case (:vcs_url r))))))

(defn id [project]
  (:vcs_url project))

;; I was confused, on the backend, we now default to oss if
;; github-info shows it to be public and no one sets the flag
;; explicitly to false. But the read-api always delivers the front-boiler
;; a feature_flags oss set to true for those cases.
(defn oss? [project]
  (get-in project [:feature_flags :oss]))

(defn osx? [project]
  (get-in project [:feature_flags :osx]))

(defn usable-containers [plan project]
  (+ (plan-model/usable-containers plan)
     (if (oss? project) plan-model/oss-containers 0)))

(defn buildable-parallelism [plan project]
  (min (plan-model/max-parallelism plan)
       (usable-containers plan project)))

(defn can-read-settings? [project]
  (-> project :scopes :read-settings))
