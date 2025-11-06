(ns app.device.routes
  (:require
    [app.device.views :as device.views]))

(def routes
  [["/devices"
    {:name :devices.routes/index
     :breadcrumbs [{:label "Home" :href "/"}
                   {:label "Devices" :href "/devices"}]
     :render
     (fn [state]
       [:div
        [:h1.text-3xl.font-bold.text-slate-900.mb-2 "Device Management"]
        [:p.text-slate-600.mb-8 "Connect and manage your WebSerial devices"]

        (device.views/connection-details state)

        [:div.mt-8
         [:h2.text-xl.font-semibold.text-slate-900.mb-4 "About WebSerial"]
         [:div.card.space-y-3
          [:p.text-sm.text-slate-700
           "The Web Serial API allows web applications to communicate with serial devices. "
           "This feature enables direct hardware interaction through your browser."]
          [:div.space-y-2
           [:h3.text-sm.font-semibold.text-slate-900 "Requirements:"]
           [:ul.list-disc.list-inside.text-sm.text-slate-700.space-y-1
            [:li "Chrome, Edge, or Opera browser (version 89+)"]
            [:li "HTTPS connection or localhost"]
            [:li "User permission to access serial ports"]]]
          [:div.space-y-2
           [:h3.text-sm.font-semibold.text-slate-900 "Supported Devices:"]
           [:ul.list-disc.list-inside.text-sm.text-slate-700.space-y-1
            [:li "Arduino boards"]
            [:li "USB-to-Serial adapters"]
            [:li "Custom microcontroller projects"]
            [:li "Any device with a serial interface"]]]]]])}]])
