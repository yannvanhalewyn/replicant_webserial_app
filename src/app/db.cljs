(ns app.db
  (:require
    [app.tools.storage :as storage]))

(defonce app-db
  (atom {:serial-status "Not Connected"
         ::configurations (storage/load-configurations)}))

(defmulti execute-action
  (fn [_event-data event-vec]
    (first event-vec)))

(defmethod execute-action :event/prevent-default
  [event-data _]
  (.preventDefault (:replicant/js-event event-data)))
