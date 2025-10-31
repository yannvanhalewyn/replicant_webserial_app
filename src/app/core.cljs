(ns app.core
  (:require
    [app.webserial :as serial]
    [promesa.core :as p]
    [replicant.dom :as r]))

(defonce state
  (atom {:serial-status "Not Connected"}))

(defn- connected? [state]
  (contains? state ::connection))

(defn view [state]
  [:div
   [:h1 "Configuration Portal"]
   [:p "Please connect to your device"]

   [:div.status
    [:strong "Serial Status: "]
    (:serial-status @state)]

   [:div
    [:button
     {:on {:click [[:webserial/connect]]}}
     "Connect to Serial Device"]

    [:button
     {:disabled (not (connected? @state))
      :on {:click [[:webserial/disconnect]]}}
     "Disconnect"]

    [:button
     {:disabled (not (connected? @state))
      :on {:click [[:webserial/send-data "Hello from browser!\n"]]}}
     "Send Test Message"]]])

(defmulti handle-event (fn [_event-data handler-data]
                         (first handler-data)))

(defmethod handle-event :webserial/connect
  [_event-data]
  (-> (serial/connect!)
    (p/then #(do
               (js/console.log "Connected:" %)
               (swap! state assoc
                 :serial-status "Connected"
                 ::connection %)))
    (p/catch
      (fn [err]
        (println "Error connecting:" err)
        {::error (.-message err)
         :serial-status (str "Error: " (.-message err))}))))

(defmethod handle-event :webserial/disconnect
  [_event-data _event]
  (-> (serial/disconnect! (::connection @state))
    (p/then (fn []
              (swap! state
                #(-> %
                   (dissoc ::connection)
                   (assoc :serial-status "Not Connected")))))
    (p/catch (fn [err]
               (println "Error disconecting:" err)))))

(defmethod handle-event :webserial/send-data
  [_event-data event]
  (serial/send-data! (::connection @state) (second event)))

;; TODO what to name 'event-data'?
(defn handle-events [event-data events]
  (js/console.log "Event"
    {:event-data event-data
     :events events})
  (doseq [event events]
    (js/console.log "Handling event" event)
    (handle-event event-data event)))

(defn render! []
  (r/set-dispatch! handle-events)
  (r/render (js/document.getElementById "app")
    (view state)))

(defn init! []
  (println "Initializing app...")
  (add-watch state ::render
    (fn [_ _ _ _] (render!)))
  (render!))

(defn reload! []
  (println "Reloading app...")
  (render!))
