(ns app.db
  (:require
    [app.tools.storage :as storage]
    [app.tools.utils :as u]
    [clojure.walk :as walk]
    [reitit.frontend.easy :as rfe]))

(defonce app-db
  (atom {}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Actions

(defmulti action->effects
  (fn [_coeffects action-vec]
    (first action-vec)))

;; This passthrough allows us to call effects directly from actions
(defmethod action->effects :default
  [_ action-vec]
  [action-vec])

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

(defmethod execute-effect! :event/prevent-default
  [event-data _]
  (.preventDefault (:replicant/js-event event-data)))

(defmethod execute-effect! :db/save
  [_ [_ new-db]]
  (reset! app-db new-db))

(defmethod execute-effect! :db/assoc-in
  [_ [_ path v]]
  (swap! app-db assoc-in path v))

(defmethod execute-effect! :db/dissoc-in
  [_ [_ path]]
  (swap! app-db u/dissoc-in path))

(defmethod execute-effect! :db/assoc-in-batch
  [_ [_ path-vs]]
  (swap! app-db
    (fn [db]
      (reduce
        (fn [acc [path v]]
          (assoc-in acc path v))
        db path-vs))))

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

(defonce placeholders (atom {}))

(defn register-placeholder!
  [k f]
  (swap! placeholders assoc k f))

(register-placeholder! :event/target.value
  (fn [event-data]
    (.. (:replicant/dom-event event-data) -target -value)))

(register-placeholder! :event/target.value.int
  (fn [event-data]
    (u/parse-int (.. (:replicant/dom-event event-data) -target -value))))

(defn- interpolate
  "Replaces the placeholders in the actions with the relevant values"
  [event-data actions]
  (let [placeholders @placeholders]
    (walk/postwalk
      (fn [x]
        (if-let [f (and (vector? x) (get placeholders (first x)))]
          (apply f event-data (rest x))
          x))
      actions)))

(defn dispatch!
  "Interpolates the actions, extrapolates and then runs effects"
  [event-data actions]
  (let [db @app-db
        effects (->> (interpolate event-data actions)
                  (mapcat #(action->effects {:db db} %)))]
    (js/console.log :effects effects)
    (doseq [effect effects]
      (when effect ;; Allow for ignorable nil effects
        (execute-effect! event-data effect)))))
