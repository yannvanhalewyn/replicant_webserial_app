(ns app.db)

(defn connected? [state]
  (contains? state ::connection))
