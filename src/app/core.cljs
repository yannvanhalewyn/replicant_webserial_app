(ns app.core
  (:require [replicant.dom :as d]
            [app.webserial :as serial]))

(defonce state (atom {:message "Hello, Replicant!"
                      :serial-connected false
                      :serial-status "Not connected"}))

(defn view [state]
  (d/html
   [:div
    [:h1 (:message state)]
    [:p "This is a Replicant app with WebSerial API support."]
    
    [:div.status
     [:strong "Serial Status: "] (:serial-status state)]
    
    [:div
     [:button
      {:on-click #(serial/connect! state)}
      "Connect to Serial Device"]
     
     [:button
      {:disabled (not (:serial-connected state))
       :on-click #(serial/disconnect! state)}
      "Disconnect"]
     
     [:button
      {:disabled (not (:serial-connected state))
       :on-click #(serial/send-data! "Hello from browser!\n")}
      "Send Test Message"]]]))

(defn render! []
  (d/render (js/document.getElementById "app")
            (view @state)))

(defn init! []
  (println "Initializing app...")
  (add-watch state ::render
             (fn [_ _ _ _] (render!)))
  (render!))

(defn reload! []
  (println "Reloading app...")
  (render!))
