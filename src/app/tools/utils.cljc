(ns app.tools.utils)

(defn parse-int [s]
  #?(:cljs (js/parseInt s)
     :clj (Integer/parseInt s)))

(defn dissoc-in
  "Dissoc's the element at path in coll"
  [coll path]
  (if (= 1 (count path))
    (dissoc coll (first path))
    (update-in coll (butlast path) dissoc (last path))))
