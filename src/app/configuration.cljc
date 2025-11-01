(ns app.configuration
  (:refer-clojure :exclude [new])
  (:require
    [app.tools.utils :as u]
    [malli.core :as m]))

(def Configuration
  (m/schema
    [:and
     [:map
      [:configuration/id :uuid]
      [:configuration/name [:string {:min 1}]]
      [:configuration/min-frequency [:int {:min 0 :max 20000}]]
      [:configuration/max-frequency [:int {:min 0 :max 20000}]]
      [:configuration/volume [:int {:min 0 :max 100}]]]
     [:fn
      {:error/message "Max frequency must be greater than or equal to min frequency"
       :error/path [:configuration/min-frequency]}
      (fn [{:configuration/keys [min-frequency max-frequency]}]
        (<= min-frequency max-frequency))]]))

(defn- new-configuration-name
  "Generates a default configuration name based on existing configurations.
  Will look for the highest 'Configuration N' name and return 'Configuration N+1'."
  [configurations]
  (let [existing-numbers
        (into #{}
          (keep #(when-let [match (re-matches #"Configuration (\d+)"
                                    (:configuration/name %))]
                   (u/parse-int (second match))))
          (vals configurations))]
    (str "Configuration " (if (seq existing-numbers)
                            (inc (apply max existing-numbers))
                            1))))

(defn new-configuration [configurations]
  {:configuration/id (random-uuid)
   :configuration/name (new-configuration-name configurations)
   :configuration/min-frequency 0
   :configuration/max-frequency 20000
   :configuration/volume 100})

(defn validate
  "Validates a configuration. Returns nil if valid, or a map with :errors if invalid."
  [config]
  (when-not (m/validate Configuration config)
    (m/explain Configuration config)))

(defn valid?
  "Returns true if the configuration is valid."
  [config]
  (m/validate Configuration config))

(comment
  (new-configuration {"1" {:configuration/name "Configuration 4"}})
  (valid? (new-configuration {}))
  (validate (assoc (new-configuration {}) :configuration/min-frequency -1))
  (validate (assoc (new-configuration {})
              :configuration/min-frequency 1000
              :configuration/max-frequency 10)))
