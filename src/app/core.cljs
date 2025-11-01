(ns app.core
  (:require
    [app.db :as db]
    [app.views.configurations :as configurations]
    [app.views.home :as home]
    [app.views.nav :as nav]
    [app.webserial :as serial]
    [promesa.core :as p]
    [reitit.frontend :as rf]
    [reitit.frontend.easy :as rfe]
    [replicant.dom :as r]))

(defonce state
  (atom {:serial-status "Not Connected"
         ::current-route nil}))

(def routes
  [["/"
    {:name :route/home
     :render home/page}]
   ["/configurations"
    {:name :route/configurations
     :render configurations/page}]])

(defmulti handle-event
  (fn [_event-data event-vec]
    (first event-vec)))

(defmethod handle-event :webserial/connect
  [_event-data _event]
  (-> (serial/connect!)
    (p/then #(do
               (js/console.log "Connected:" %)
               (swap! state assoc
                 :serial-status "Connected"
                 ::db/connection %)))
    (p/catch
      (fn [err]
        (println "Error connecting:" err)
        {:serial-status (str "Error: " (.-message err))}))))

(defmethod handle-event :webserial/disconnect
  [_event-data _event]
  (-> (serial/disconnect! (::db/connection @state))
    (p/then (fn []
              (swap! state
                #(-> %
                   (dissoc ::db/connection)
                   (assoc :serial-status "Not Connected")))))
    (p/catch #(println "Error disconecting:" %))))

(defmethod handle-event :webserial/send-data
  [_event-data event]
  (serial/send-data! (::db/connection @state) (second event)))

(defn handle-events [event-data events]
  (doseq [event events]
    (js/console.log "Handling event" event)
    (handle-event event-data event)))

(defn render! []
  (r/set-dispatch! handle-events)
  (r/render (js/document.getElementById "app")
    [:div
     (nav/component)
     (if-let [render (-> @state ::current-route :data :render)]
       (render state)
       [:div [:h1 "Not Found"]
        [:a {:href (rfe/href :route/home)}
         "Back to home"]])]))

(defn- on-navigate [new-match]
  (swap! state assoc ::current-route new-match))

(defn init! []
  (add-watch state ::render
    (fn [_ _ _ _] (render!)))
  (rfe/start!
    (rf/router routes)
    on-navigate
    {:use-fragment false})
  (render!))

(defn reload! []
  (println "Reloading app...")
  (render!))
