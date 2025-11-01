(ns app.storage
  (:require
    [cognitect.transit :as t]))

(def ^:private storage-key "replicant-webserial-configurations")

(def ^:private transit-writer (t/writer :json))
(def ^:private transit-reader (t/reader :json))

(defn load-configurations
  "Loads configurations from LocalStorage. Returns a map of {id configuration} or empty map if none found."
  []
  (try
    (if-let [stored-data (js/localStorage.getItem storage-key)]
      (let [parsed (t/read transit-reader stored-data)]
        (if (map? parsed)
          parsed
          {}))
      {})
    (catch js/Error e
      (js/console.warn "Error loading configurations from LocalStorage:" e)
      {})))

(defn save-configurations!
  "Saves configurations to LocalStorage. Takes a map of {id configuration}."
  [configurations]
  (try
    (let [serialized (t/write transit-writer configurations)]
      (js/localStorage.setItem storage-key serialized))
    (catch js/Error e
      (js/console.error "Error saving configurations to LocalStorage:" e))))
