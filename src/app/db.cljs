(ns app.db
  (:require
    [app.tools.storage :as storage]
    [app.tools.utils :as u]
    [clojure.walk :as walk]
    [reitit.frontend.easy :as rfe]))

(defonce app-db
  (atom {:serial-status "Not Connected"
         ::configurations (storage/load! "fixme")}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Actions

(defmulti action->effects
  (fn [_coeffects action-vec]
    (first action-vec)))

(defmethod action->effects :event/prevent-default
  [_ _]
  [[:effect/prevent-default]])

(defmethod action->effects :route/on-navigate
  [{:keys [db]} [_ new-match]]
  [[:db/save
    (let [on-mount (get-in new-match [:data :on-mount])
          new-db (assoc db ::current-route new-match)]
      (if on-mount
        (on-mount new-match new-db)
        new-db))]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Effects

(defmulti execute-effect!
  (fn [_event-data effect-vec]
    (first effect-vec)))

(defmethod execute-effect! :effect/prevent-default
  [event-data _]
  (.preventDefault (:replicant/js-event event-data)))

(defmethod execute-effect! :db/save
  [_ [_ new-db]]
  (reset! app-db new-db))

(defmethod execute-effect! :route/push
  [_ [_ route-name params query-params]]
  (rfe/push-state route-name params query-params))

(defmethod execute-effect! :storage/save
  [_ [_ storage-key data]]
  (storage/store! storage-key data))

(defmethod execute-effect! :storage/load
  [_ [_ storage-key db-key]]
  (swap! app-db assoc db-key (storage/load! storage-key)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Dispatch

(defn- interpolate
  "Replaces the placeholders in the actions with the relevant values"
  [event-data actions]
  (walk/postwalk
    (fn [x]
      (case x
        :event/target.value (.. (:replicant/dom-event event-data) -target -value)
        :event/target.value.int (u/parse-int (.. (:replicant/dom-event event-data) -target -value))
        x))
    actions))

(defn dispatch!
  "Interpolates the actions, extrapolates and then runs effects"
  [event-data actions]
  (let [db @app-db
        effects (->> (interpolate event-data actions)
                  (mapcat #(action->effects {:db db} %)))]
    (doseq [effect effects]
      (when effect ;; Allow for ignorable nil effects
        (execute-effect! event-data effect)))))
