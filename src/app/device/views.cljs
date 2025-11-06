(ns app.device.views
  (:require
    [app.device.db :as device.db]))

(defn device-status [state]
  (list
   [:div.status
    [:strong "Serial Status: "]
    (:serial-status state)]

   [:div
    [:button
     {:on {:click [[:webserial/connect]]}}
     "Connect to Serial Device"]

    [:button
     {:disabled (not (device.db/connected? state))
      :on {:click [[:webserial/disconnect]]}}
     "Disconnect"]

    [:button
     {:disabled (not (device.db/connected? state))
      :on {:click [[:webserial/send-data "Hello from browser!\n"]]}}
     "Send Test Message"]]))
