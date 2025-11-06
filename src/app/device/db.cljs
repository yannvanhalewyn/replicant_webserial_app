(ns app.device.db
  (:require
    [app.db :as db]
    [app.tools.webserial :as serial]
    [promesa.core :as p]))

(defn connected? [state]
  (contains? state ::connection))

(db/register-placeholder! ::new-connection ::connection)

(defmethod db/action->effects ::connect
  [_ _]
  [[::connect
    {:on-success [[:db/assoc-in [::connection] [::new-connection]]]}]])

(defmethod db/execute-effect! ::connect
  [_event-data [_ {:keys [on-success]}]]
  (-> (serial/connect!)
    (p/then #(db/dispatch! {::connection %} on-success))
    (p/catch #(db/dispatch! {} [[:db/assoc-in [::error] %]]))))

(defmethod db/action->effects ::disconnect
  [{:keys [db]} _event]
  (js/console.log :action "connection:" (::connection db))
  [[::disconnect (::connection db)
    {:on-success [[:db/dissoc-in [::connection]]]}]])

(defmethod db/execute-effect! ::disconnect
  [_ [_ connection {:keys [on-success]}]]
  (-> (serial/disconnect! connection)
    (p/then #(db/dispatch! {::connection %} on-success))
    (p/catch #(db/dispatch! {} [[:db/assoc-in [::error] %]]))))

(defmethod db/execute-effect! ::send-data
  [_event-data effect-vec]
  (serial/send-data! (::connection @db/app-db) (second effect-vec)))
