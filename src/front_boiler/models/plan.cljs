(ns front-boiler.models.plan
  (:require [front-boiler.utils :as utils :include-macros true]
            [goog.string :as gstring]
            [cljs-time.core :as time]
            [cljs-time.format :as time-format]))

;; The template tells how to price the plan
;; TODO: fix this to work from the backend db; how?
(def default-template-properties {:price 0 :container_cost 50 :id
                                  "p18" :max_containers 1000 :free_containers 0})

(def oss-containers 3)

(defn max-parallelism
  "Maximum parallelism that the plan allows (usually 16x)"
  [plan]
  (:max_parallelism plan))

(defn piggieback? [plan org-name]
  (not= (:org_name plan) org-name))

(defn freemium? [plan]
  (boolean (:free plan)))

(defn paid? [plan]
  (boolean (:paid plan)))

(defn trial? [plan]
  (boolean (:trial plan)))

(defn trial-over? [plan]
  (when (trial? plan)
    (or (nil? (:trial_end plan))
        (time/after? (time/now) (time-format/parse (:trial_end plan))))))

(defn in-trial? [plan]
  (and (trial? plan) (not (trial-over? plan))))

(defn enterprise? [plan]
  (boolean (:enterprise plan)))

(defn suspended? [plan]
  (boolean (some-> plan :paid :suspended)))

(defn freemium-containers [plan]
  (or (get-in plan [:free :template :free_containers]) 0))

(defn paid-containers [plan]
  (if (paid? plan)
    (max (:containers_override plan)
         (:containers plan)
         (get-in plan [:paid :template :free_containers]))
    0))

(defn trial-containers
  "How many containers are provided by this plan's trial? Note that this finds out how many
  containers were provided by expired trials, too. You'll need to guard by `in-trial?` if you
  don't want this behavior."
  [plan]
  (max 0
       ;; Subtract the amount of paid containers from the number of trial containers,
       ;; so that paid-containers + trial-containers = usable-containers for plans
       ;; with only :paid and :trial.
       ;; see `circle.model.plan/usable-containers` on the backend.
       ;; TODO: fix this once that behavior changes.
       (- (get-in plan [:trial :template :free_containers] 0)
          (paid-containers plan))))

(defn enterprise-containers [plan]
  (if (:enterprise plan)
    (:containers plan)
    0))

(defn paid-plan-min-containers
  "A plan has a price, and a free container count. If you are paying for
   the plan, you can't go below the free container count."
  [plan]
  (or (get-in plan [:paid :template :free_containers]) 0))

(defn default-plan-min-containers
  []
  (:free_containers default-template-properties))

(defn usable-containers
  "Maximum containers that the plan has available to it"
  [plan]
  (+ (freemium-containers plan)
     (enterprise-containers plan)
     (trial-containers plan)
     (paid-containers plan)))

(defn can-edit-plan? [plan org-name]
  ;; kill plan pricing page for trial plans by making
  ;; can-edit-plan?' return true for them
  (not (piggieback? plan org-name)))

(defn transferrable-or-piggiebackable-plan? [plan]
  (or (paid? plan)
      ;; Trial plans shouldn't really be transferrable
      ;; but they are piggiebackable and the UI mixes the two :(
      (trial? plan)))

;; true  if the plan has an active Stripe discount coupon.
;; false if the plan is nil (not loaded yet) or has no discount applied
(defn has-active-discount? [plan]
  (get-in plan [:discount :coupon :valid]))

(defn days-left-in-trial
  "Returns number of days left in trial, can be negative."
  [plan]
  (let [trial-end (when (:trial_end plan)
                    (time-format/parse (:trial_end plan)))
        now (time/now)]
    (when (not (nil? trial-end))
      (if (time/after? trial-end now)
        ;; count partial days as a full day
        (inc (time/in-days (time/interval now trial-end)))
        (- (time/in-days (time/interval trial-end now)))))))

(defn pretty-trial-time [plan]
  (when-not (nil? (:trial_end plan))
    (let [trial-interval (time/interval (time/now) (time-format/parse (:trial_end plan)))
          hours-left (time/in-hours trial-interval)]
      (cond (< 24 hours-left)
            (str (days-left-in-trial plan) " days")

            (< 1 hours-left)
            (str hours-left " hours")

            :else
            (str (time/in-minutes trial-interval) " minutes")))))

(defn per-container-cost [plan]
  (let [template-properties (or (-> plan :paid :template)
                                (-> plan :enterprise :template)
                                default-template-properties)]
    (:container_cost template-properties)))

(defn container-cost [plan containers]
  (let [template-properties (or (-> plan :paid :template)
                                (-> plan :enterprise :template)
                                default-template-properties)
        {:keys [free_containers container_cost]} template-properties]
    (max 0 (* container_cost (- containers free_containers)))))

(defn cost [plan containers & {:keys [include-trial?]}]
  (let [paid-plan-template (or (-> plan :paid :template)
                               (-> plan :enterprise :template)
                               default-template-properties)
        plan-base-price (:price paid-plan-template)
        paid-plan-containers (- containers
                                (freemium-containers plan)
                                (if-not include-trial?
                                  0
                                  (trial-containers plan)))]
    (if (< paid-plan-containers 1)
      0
      (+ plan-base-price
         (container-cost plan paid-plan-containers)))))

(defn stripe-cost
  "Normalizes the Stripe amount on the plan to dollars."
  [plan]
  (/ (:amount plan) 100))

(defn grandfathered? [plan]
  (and (paid? plan)
       (< (stripe-cost plan)
          (cost plan (usable-containers plan) :include-trial? true))))

(defn admin?
  "Whether the logged-in user is an admin for this plan."
  [plan]
  (boolean (:admin plan)))
