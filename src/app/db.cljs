(ns app.db
  (:require
    [app.tools.storage :as storage]))

(defonce app-db
  (atom {:serial-status "Not Connected"
         ::current-route nil
         ::configurations (storage/load-configurations)
         ::editing-configuration nil
         ::validation-errors nil}))

(defmulti execute-action
  (fn [_event-data event-vec]
    (first event-vec)))

(defmethod execute-action :event/prevent-default
  [event-data _]
  (.preventDefault (:replicant/js-event event-data)))
