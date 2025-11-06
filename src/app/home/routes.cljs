(ns app.home.routes
  (:require
    [app.device.views :as device.views]))

(def routes
  [["/"
    {:name :home.routes/index
     :render
     (fn [state]
       [:div
        [:h1 "Configuration Panel"]
        [:p "Please connect to your device"]
        (device.views/connection-status state)])}]])
