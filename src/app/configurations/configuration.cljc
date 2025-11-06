(ns app.configurations.configuration
  (:refer-clojure :exclude [new])
  (:require
    [app.tools.utils :as u]
    [malli.core :as m]
    [malli.error :as me]
    [malli.util :as mu]))

(def BaseSchema
  [:map
   [:configuration/id :uuid]
   [:configuration/name [:string {:min 1}]]
   [:configuration/min-frequency
    [:int {:min 0
           :max 20000
           :form/unit "Hz"
           :form/label "Min Frequency"}]]
   [:configuration/max-frequency
    [:int {:min 0
           :max 20000
           :form/unit "Hz"
           :form/label "Max Frequency"}]]
   [:configuration/volume
    [:int {:min 0
           :max 100
           :form/unit "%"
           :form/label "Volume"}]]])

(def Configuration
  (m/schema
    [:and
     BaseSchema
     [:fn
      {:error/message "Max frequency must be greater than or equal to min frequency"
       :error/path [:configuration/max-frequency]}
      (fn [{:configuration/keys [min-frequency max-frequency]}]
        (<= min-frequency max-frequency))]]))

(defn- new-configuration-name
  "Generates a default configuration name based on existing configurations.
  Will look for the highest pre-existing 'Configuration N' name and return
  'Configuration N+1'."
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
  (letfn [(schema-prop [schema-key prop-key]
            (-> (mu/get BaseSchema schema-key)
              (m/properties)
              (get prop-key)))]
    {:configuration/id (random-uuid)
     :configuration/name (new-configuration-name configurations)
     :configuration/min-frequency (schema-prop :configuration/min-frequency :min)
     :configuration/max-frequency (schema-prop :configuration/max-frequency :max)
     :configuration/volume (schema-prop :configuration/volume :max)}))

(defn serialize
  "Serializes a configuration to a string format for sending to device."
  [config]
  (str "CONFIG:"
       "MIN_FREQ=" (:configuration/min-frequency config) ","
       "MAX_FREQ=" (:configuration/max-frequency config) ","
       "VOLUME=" (:configuration/volume config)
       "\n"))

(defn valid?
  "Returns true if the configuration is valid."
  [config]
  (m/validate Configuration config))

(defn validate
  "Validates a configuration. Returns nil if valid, or a map with :errors if invalid."
  [config]
  (when-not (valid? config)
    (me/humanize (m/explain Configuration config))))

(comment
  (new-configuration {"1" {:configuration/name "Configuration 4"}})
  (valid? (new-configuration {}))
  (validate (assoc (new-configuration {}) :configuration/min-frequency -1))
  (validate (assoc (new-configuration {})
              :configuration/name ""
              :configuration/min-frequency 1000
              :configuration/max-frequency 10)))
