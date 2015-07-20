(ns front-boiler.models.user
  (:require [clojure.set :as set]
            [front-boiler.datetime :as datetime]
            [goog.string :as gstring]
            goog.string.format))

(defn missing-scopes [user]
  (let [current-scopes (set (:github_oauth_scopes user))]
    (set/union (when (empty? (set/intersection current-scopes #{"user" "user:email"}))
                 #{"user:email"})
               (when-not (contains? current-scopes "repo")
                 #{"repo"}))))

(defn public-key-scope? [user]
  (some #{"admin:public_key"} (:github_oauth_scopes user)))

(defn unkeyword
  "Converts a keyword in to a string without the leading colon. See server-side function of the same name."
  [kw]
  (.substr (str kw) 1))

(defn project-preferences [user]
  (into {} (for [[vcs-url prefs] (:projects user)]
             [(unkeyword vcs-url) prefs])))
