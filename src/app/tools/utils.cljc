(ns app.tools.utils)

(defn parse-int [s]
  #?(:cljs (js/parseInt s)
     :clj (Integer/parseInt s)))
