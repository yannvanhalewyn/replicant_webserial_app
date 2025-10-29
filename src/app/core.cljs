(ns app.core
  (:require [replicant.dom :as r]
            [app.webserial :as serial]))

(defonce state
  (atom {:serial-connected false
         :serial-status "Not connected"}))

(defn view [state]
  [:div
   [:h1 "Configuration Portal"]
   [:p "Please connect to your device"]

   [:div.status
    [:strong "Serial Status: "] (:serial-status @state)]

   [:div
    [:button
     {:on {:click [[:webserial/connect]]}}
     "Connect to Serial Device"]

    [:button
     {:disabled (not (:serial-connected @state))
      :on {:click [[:webserial/disconnect]]}}
     "Disconnect"]

    [:button
     {:disabled (not (:serial-connected @state))
      :on {:click [[:webserial/send-data "Hello from browser!\n"]]}}
     "Send Test Message"]]])

(defn handle-event [_event-data handler-data]
  (js/console.log "Events fired:" handler-data)
  (doseq [event handler-data]
   (case (first event)
     :webserial/connect
     (serial/connect! state)
     :webserial/disconnect (serial/disconnect! state)
     :webserial/send-data  (serial/send-data! (second event)))))

(defn render! []
  (r/set-dispatch! handle-event)
  (r/render (js/document.getElementById "app")
    (view state)))

(defn init! []
  (println "Initializing app...")
  (js/console.log [1 2 :foo/bar])
  (add-watch state ::render
    (fn [_ _ _ _] (render!)))
  (render!))

(defn reload! []
  (println "Reloading app...")
  (render!))
