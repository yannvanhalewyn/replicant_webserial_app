(ns app.device.views
  (:require
    [app.device.db :as device.db]))

(defn connection-status [state]
  (js/console.log "rendering status" (::device.db/connection state)
    (device.db/connected? state))
  (list
   [:div.status
    [:strong "Serial Status: "]
    (cond
      (device.db/connected? state) "Connected"
      (::device.db/error state) (::device.db/error state)
      :else "Not Connected")]

   [:div
    [:button
     {:on {:click [[::device.db/connect]]}}
     "Connect to Serial Device"]

    (when (device.db/connected? state)
      [:button
       {:on {:click [[::device.db/disconnect]]}}
       "Disconnect"])

    (when (device.db/connected? state)
     [:button
      {:on {:click [[::device.db/send-data "Hello from browser!\n"]]}}
      "Send Test Message"])]))
