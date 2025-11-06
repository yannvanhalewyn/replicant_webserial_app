(ns app.db)

(defn connected? [state]
  (contains? state ::connection))

(defn path-params
  ([state]
   (get-in state [::current-route :parameters :path]))
  ([state k]
   (get-in state [::current-route :parameters :path k])))
