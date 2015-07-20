(ns front-boiler.async
  (:require [cljs.core.async :as async]
            [om.core :as om :include-macros true]))

(def ^:dynamic *uuid* nil)

(defn put! [port val & args]
  (if (and (satisfies? IMeta val) *uuid*)
    (apply async/put! port (vary-meta val assoc :uuid *uuid*) args)
    (apply async/put! port val args)))

(defn raise! [owner val & args]
  (let [c (om/get-shared owner [:comms :controls])]
    (apply put! c val args)))
