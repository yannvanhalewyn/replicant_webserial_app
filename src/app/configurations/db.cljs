(ns app.configurations.db
  (:require
    [app.configurations.configuration :as configuration]
    [app.db :as db]
    [app.tools.storage :as storage]
    [reitit.frontend.easy :as rfe]))

(defmethod db/execute-action ::update-form-field
  [_event-data [_ field value]]
  (swap! db/app-db
    (fn [s]
      (let [new-config (assoc (::current-configuration s) field value)]
        (assoc s
          ::current-configuration new-config
          ::validation-errors (configuration/validate new-config))))))

(defmethod db/execute-action ::save
  [_event-data [_ config]]
  (let [validation-result (configuration/validate config)]
    (if validation-result
      (swap! db/app-db assoc ::validation-errors validation-result)
      (do
        (swap! db/app-db
          (fn [s]
            (let [configs (assoc (::db/configurations s)
                            (:configuration/id config)
                            config)]
              (storage/save-configurations! configs)
              (-> s
                (assoc-in [::db/configurations (:configuration/id config)] config)
                (assoc
                  ::current-configuration nil
                  ::validation-errors nil)))))
        (rfe/push-state :configurations.routes/index)))))

(defmethod db/execute-action ::delete
  [_event-data [_ config-id]]
  (swap! db/app-db
    (fn [s]
      (let [configs (dissoc (::db/configurations s) config-id)]
        (storage/save-configurations! configs)
        (assoc s ::db/configurations configs)))))
