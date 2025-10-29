(ns app.preload
  (:require [devtools.core :as devtools]))

(devtools/install!)
(js/console.log "Hello from preload!")
(js/console.log [1 2 :foo/bar])
