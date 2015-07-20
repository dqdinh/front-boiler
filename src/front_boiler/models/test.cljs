(ns front-boiler.models.test
  (:require [clojure.string :as string]
            [front-boiler.state :as state]
            [front-boiler.utils :as utils :include-macros true]
            [goog.string :as gstring]
            goog.string.format))

(defn source [test]
  (or (:source_type test)
      ;; TODO: this can be removed once source_type is fully deployed
      (:source test)))

(defmulti format-test-name source)

(defmethod format-test-name :default [test]
  (->> [[(:name test)] [(:classname test) (:file test)]]
       (map (fn [s] (some #(when-not (string/blank? %) %) s)))
       (filter identity)
       (string/join " - in ")))

(defmethod format-test-name "lein-test" [test]
  (str (:classname test) "/" (:name test)))

(defmethod format-test-name "cucumber" [test]
  (if (string/blank? (:name test))
    (:classname test)
    (:name test)))

(defn pretty-source [source]
  (case source
    "cucumber" "Cucumber"
    "rspec" "RSpec"
    source))

(defn failed? [test]
  (not= "success" (:result test)))
