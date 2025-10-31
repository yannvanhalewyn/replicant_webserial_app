(ns dev.preload
  (:require [devtools.core :as devtools]))

(let [{:keys [cljs-land-style]} (devtools/get-prefs)]
  (devtools/set-pref! :cljs-land-style
    (str "filter:invert(1);" cljs-land-style)))

(devtools/install!)
