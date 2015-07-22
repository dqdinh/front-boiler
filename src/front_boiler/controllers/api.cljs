(ns front-boiler.controllers.api
  (:require [cljs.core.async :refer [close!]]
            [front-boiler.api :as api]
            [front-boiler.async :refer [put!]]
            [front-boiler.models.repo :as repo-model]
            [front-boiler.state :as state]
            [front-boiler.favicon]
            [front-boiler.utils.ajax :as ajax]
            [front-boiler.utils.state :as state-utils]
            [front-boiler.utils.vcs-url :as vcs-url]
            [front-boiler.utils.docs :as doc-utils]
            [front-boiler.utils :as utils :refer [mlog merror]]
            [goog.string :as gstring]))

;; when a button is clicked, the post-controls will make the API call, and the
;; result will be pushed into the api-channel
;; the api controller will do assoc-in
;; the api-post-controller can do any other actions

;; --- API Multimethod Declarations ---

(defmulti api-event
  ;; target is the DOM node at the top level for the app
  ;; message is the dispatch method (1st arg in the channel vector)
  ;; args is the 2nd value in the channel vector)
  ;; state is current state of the app
  ;; return value is the new state
  (fn [target message status args state] [message status]))

(defmulti post-api-event!
  (fn [target message status args previous-state current-state] [message status]))

;; --- API Multimethod Implementations ---

(defmethod api-event :default
  [target message status args state]
  ;; subdispatching for state defaults
  (let [submethod (get-method api-event [:default status])]
    (if submethod
      (submethod target message status args state)
      (do (merror "Unknown api: " message args)
          state))))

(defmethod post-api-event! :default
  [target message status args previous-state current-state]
  ;; subdispatching for state defaults
  (let [submethod (get-method post-api-event! [:default status])]
    (if submethod
      (submethod target message status args previous-state current-state)
      (merror "Unknown api: " message status args))))

;; TODO Too verbose. Needs refactoring.

(defmethod api-event [:default :started]
  [target message status args state]
  (mlog "No api for" [message status])
  state)

(defmethod post-api-event! [:default :started]
  [target message status args previous-state current-state]
  (mlog "No post-api for: " [message status]))

(defmethod api-event [:default :success]
  [target message status args state]
  (mlog "No api for" [message status])
  state)

(defmethod post-api-event! [:default :success]
  [target message status args previous-state current-state]
  (mlog "No post-api for: " [message status]))

(defmethod api-event [:default :failed]
  [target message status args state]
  (mlog "No api for" [message status])
  state)

(defmethod post-api-event! [:default :failed]
  [target message status args previous-state current-state]
  (put! (get-in current-state [:comms :errors]) [:api-error args])
  (mlog "No post-api for: " [message status]))

(defmethod api-event [:default :finished]
  [target message status args state]
  (mlog "No api for" [message status])
  state)

(defmethod post-api-event! [:default :finished]
  [target message status args previous-state current-state]
  (mlog "No post-api for: " [message status]))

;; More Examples
;; (defmethod api-event [:follow-repo :success]
;;   [target message status {:keys [resp context]} state]
;;   (let [{:keys [login type]} context] ; don't pull out :name, to avoid overshadowing
;;     (if-let [repo-index (state-utils/find-repo-index state login type (:name context))]
;;       (assoc-in state (conj (state/repo-path login type repo-index) :following) true)
;;       state)))
;;
;; (defmethod post-api-event! [:follow-repo :success]
;;   [target message status args previous-state current-state]
;;   (api/get-projects (get-in current-state [:comms :api]))
;;   (if-let [first-build (get-in args [:resp :first_build])]
;;     (let [nav-ch (get-in current-state [:comms :nav])
;;           build-path (-> first-build
;;                          :build_url
;;                          (goog.Uri.)
;;                          (.getPath)
;;                          (subs 1))]
;;       (put! nav-ch [:navigate! {:path build-path}]))
;;     (when (repo-model/should-do-first-follower-build? (:context args))
;;       (ajax/ajax :post
;;                  (gstring/format "/api/v1/project/%s" (vcs-url/project-name (:vcs_url (:context args))))
;;                  :start-build
;;                  (get-in current-state [:comms :api])))))
;;
;;
;; (defmethod api-event [:unfollow-repo :success]
;;   [target message status {:keys [resp context]} state]
;;   (let [{:keys [login type]} context] ; don't pull out :name, to avoid overshadowing
;;     (if-let [repo-index (state-utils/find-repo-index state login type (:name context))]
;;       (assoc-in state (conj (state/repo-path login type repo-index) :following) false)
;;       state)))
;;
;;
;; (defmethod post-api-event! [:unfollow-repo :success]
;;   [target message status args previous-state current-state]
;;   (api/get-projects (get-in current-state [:comms :api])))
;;
