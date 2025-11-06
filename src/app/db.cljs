(ns app.db
  (:require
    [app.tools.storage :as storage]
    [reitit.frontend.easy :as rfe]))

(defonce app-db
  (atom {:serial-status "Not Connected"
         ::configurations (storage/load! "fixme")}))

(defmulti action->effects
  (fn [_coeffects action-vec]
    (first action-vec)))

(defmethod action->effects :event/prevent-default
  [_ _]
  [[:effect/prevent-default]])

(defmulti execute-effect
  (fn [_event-data effect-vec]
    (first effect-vec)))

(defmethod execute-effect :effect/prevent-default
  [event-data _]
  (.preventDefault (:replicant/js-event event-data)))

(defmethod execute-effect :db/save
  [_ [_ new-db]]
  (reset! app-db new-db))

(defmethod execute-effect :storage/save
  [_ [_ storage-key data]]
  (storage/store! storage-key data))

(defmethod execute-effect :route/push
  [_ [_ route-name params query-params]]
  (rfe/push-state route-name params query-params))
