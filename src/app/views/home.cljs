(ns app.views.home
  (:require
    [app.db :as db]))

(defn page [state]
  [:div
   [:h1 "Configuration Portal"]
   [:p "Please connect to your device"]

   [:div.status
    [:strong "Serial Status: "]
    (:serial-status state)]

   [:div
    [:button
     {:on {:click [[:webserial/connect]]}}
     "Connect to Serial Device"]

    [:button
     {:disabled (not (db/connected? state))
      :on {:click [[:webserial/disconnect]]}}
     "Disconnect"]

    [:button
     {:disabled (not (db/connected? state))
      :on {:click [[:webserial/send-data "Hello from browser!\n"]]}}
     "Send Test Message"]]])


