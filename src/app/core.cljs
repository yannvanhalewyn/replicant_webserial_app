(ns app.core
  (:require
    [app.webserial :as serial]
    [promesa.core :as p]
    [replicant.dom :as r]))

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

(defmulti handle-event (fn [_event-data handler-data] (first handler-data)))

(defmethod handle-event :webserial/connect
  [_event-data]
  (-> (serial/connect!)
    (p/then #(swap! state assoc ::connection %))
    (p/catch
      (fn [err]
        (println "Error connecting:" err)
        {::error (.-message err)}))))


(defmethod handle-event :webserial/disconnect
  [_event-data event]
  (-> (serial/disconnect! (::connection @state))
    (p/then (fn [] (swap! state dissoc ::connection)))
    (p/catch (fn [err]
               (println "Error disconecting:" err)))))

(defmethod handle-event :webserial/send-data
  [_event-data event]
  (serial/send-data! (::connection @state) (second event)))

;; TODO what to name 'event-data'?
(defn handle-events [event-data events]
  (doseq [event events]
    (js/console.log "Handling event" event)
    (handle-event event-data event)))

(defn render! []
  (r/set-dispatch! handle-events)
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
