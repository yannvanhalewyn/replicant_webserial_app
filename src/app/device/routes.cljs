(ns app.device.routes
  (:require
    [app.device.views :as device.views]))

(def routes
  [["/devices"
    {:name :devices.routes/index
     :breadcrumbs [{:label "Home" :href "/"}
                   {:label "Devices" :route [:devices.routes/index]}]
     :render
     (fn [state]
       [:div
        [:h1.text-3xl.font-bold.text-slate-900.mb-2 "Device Management"]
        [:p.text-slate-600.mb-8 "Connect and manage your WebSerial devices"]

        (device.views/connection-details state)])}]])
