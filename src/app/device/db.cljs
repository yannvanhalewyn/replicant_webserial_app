(ns app.device.db
  (:require
    [app.db :as db]
    [app.tools.webserial :as serial]
    [promesa.core :as p]))

(defn connected? [state]
  (contains? state ::connection))

(defmethod db/execute-action :webserial/connect
  [_event-data _event]
  (-> (serial/connect!)
    (p/then #(do
               (js/console.log "Connected:" %)
               (swap! db/app-db assoc
                 :serial-status "Connected"
                 ::db/connection %)))
    (p/catch
      (fn [err]
        (println "Error connecting:" err)
        {:serial-status (str "Error: " (.-message err))}))))

(defmethod db/execute-action :webserial/disconnect
  [_event-data _event]
  (-> (serial/disconnect! (::db/connection @db/app-db))
    (p/then (fn []
              (swap! db/app-db
                #(-> %
                   (dissoc ::db/connection)
                   (assoc :serial-status "Not Connected")))))
    (p/catch #(println "Error disconecting:" %))))

(defmethod db/execute-action :webserial/send-data
  [_event-data event]
  (serial/send-data! (::db/connection @db/app-db) (second event)))


