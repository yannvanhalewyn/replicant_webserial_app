(ns app.configurations.db
  (:require
    [app.configurations.configuration :as configuration]
    [app.db :as db]
    [app.device.db :as device.db]
    [app.tools.utils :as u]))

(def ^:private STORAGE_KEY "demo-app-configurations")

(defmethod db/action->effects ::init
  [_ _]
  [[:storage/load STORAGE_KEY ::configurations]])

(defmethod db/action->effects ::update-form-field
  [{:keys [db]} [_ field value]]
  (let [new-config (assoc (::current-configuration db) field value)]
    [[:db/save (assoc db
                 ::current-configuration new-config
                 ::validation-errors (configuration/validate new-config))]]))

(defmethod db/action->effects ::save
  [{:keys [db]} [_ config]]
  (let [errors (configuration/validate config)]
    (if errors
      [[:db/save (assoc db ::validation-errors errors)]]
      (let [new-db (-> db
                     (assoc-in [::configurations (:configuration/id config)] config)
                     (assoc
                       ::current-configuration nil
                       ::validation-errors nil))]
        [[:db/save new-db]
         [:storage/save STORAGE_KEY (::configurations new-db)]
         [:route/push :configurations.routes/index]]))))

(defmethod db/action->effects ::delete
  [{:keys [db]} [_ config-id]]
  (let [new-db (u/dissoc-in db [::configurations config-id])]
    [[:db/save new-db]
     [:storage/save STORAGE_KEY (::configurations new-db)]]))

(defn- serialize-configuration
  "Serializes a configuration to a string format for sending to device."
  [config]
  (str "CONFIG:"
    "MIN_FREQ=" (:configuration/min-frequency config) ","
    "MAX_FREQ=" (:configuration/max-frequency config) ","
    "VOLUME=" (:configuration/volume config)
    "\n"))

(defmethod db/action->effects ::send-to-device
  [{:keys [db]} [_ config]]
  (when (device.db/connected? db)
    (let [serialized (serialize-configuration config)]
      [[::device.db/send-data serialized]])))
