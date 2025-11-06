(ns app.core
  (:require
    [app.db :as db]
    [app.configuration :as configuration]
    [app.storage :as storage]
    [app.views.configurations :as configurations]
    [app.views.home :as home]
    [app.views.nav :as nav]
    [app.webserial :as serial]
    [clojure.walk :as walk]
    [promesa.core :as p]
    [reitit.coercion.malli :as rcm]
    [reitit.frontend :as rf]
    [reitit.frontend.easy :as rfe]
    [replicant.dom :as r]
    [app.tools.utils :as u]))

(defonce state
  (atom {:serial-status "Not Connected"
         ::db/current-route nil
         ::db/configurations (storage/load-configurations)
         ::db/editing-configuration nil
         ::db/validation-errors nil}))

(def routes
  [["/"
    {:name :route/home
     :render home/page}]
   ["/configurations"
    {:name :route/configurations
     :render configurations/page}]
   ["/configurations/new"
    {:name :route/configurations-new
     :render configurations/new-page
     :on-mount
     (fn [match state]
       (assoc state
         ::db/editing-configuration
         (configuration/new-configuration (::db/configurations state))
         ::db/validation-errors nil))}]
   ["/configurations/:id/edit"
    {:name :route/configurations-edit
     :render configurations/edit-page
     :parameters {:path [:map [:id :uuid]]}
     :on-mount
     (fn [match state]
       (let [id (get-in match [:parameters :path :id])]
         (assoc state
           ::db/editing-configuration
           (get-in state [::db/configurations id])
           ::db/validation-errors nil)))}]])

(defmulti execute-action
  (fn [_event-data event-vec]
    (first event-vec)))

(defmethod execute-action :event/prevent-default
  [event-data _]
  (.preventDefault (:replicant/js-event event-data)))

(defmethod execute-action :webserial/connect
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

(defmethod execute-action :webserial/disconnect
  [_event-data _event]
  (-> (serial/disconnect! (::db/connection @state))
    (p/then (fn []
              (swap! state
                #(-> %
                   (dissoc ::db/connection)
                   (assoc :serial-status "Not Connected")))))
    (p/catch #(println "Error disconecting:" %))))

(defmethod execute-action :webserial/send-data
  [_event-data event]
  (serial/send-data! (::db/connection @state) (second event)))

(defmethod execute-action :configuration/new
  [_event-data _event]
  (swap! state assoc
    ::db/editing-configuration
    (configuration/new-configuration (::db/configurations @state))
    ::db/validation-errors nil))

(defmethod execute-action :configuration/edit
  [_event-data [_ config]]
  (swap! state assoc
    ::db/editing-configuration config
    ::db/validation-errors nil))

(defmethod execute-action :configuration/cancel-edit
  [_event-data _event]
  (swap! state assoc
    ::db/editing-configuration nil
    ::db/validation-errors nil)
  (rfe/push-state :route/configurations))

(defmethod execute-action :configuration-form/update-field
  [_event-data [_ field value]]
  (swap! state
    (fn [s]
      (let [new-config (assoc (::db/editing-configuration s) field value)]
        (assoc s
          ::db/editing-configuration new-config
          ::db/validation-errors (configuration/validate new-config))))))

(defmethod execute-action :configuration/save
  [_event-data [_ config]]
  (let [validation-result (configuration/validate config)]
    (if validation-result
      (swap! state assoc ::db/validation-errors validation-result)
      (do
        (swap! state
          (fn [s]
            (let [configs (assoc (::db/configurations s)
                            (:configuration/id config)
                            config)]
              (storage/save-configurations! configs)
              (-> s
                (assoc-in [::db/configurations (:configuration/id config)] config)
                (assoc ::db/editing-configuration nil)
                (assoc ::db/validation-errors nil)))))
        (rfe/push-state :route/configurations)))))

(defmethod execute-action :configuration/delete
  [_event-data [_ config-id]]
  (swap! state
    (fn [s]
      (let [configs (dissoc (::db/configurations s) config-id)]
        (storage/save-configurations! configs)
        (assoc s ::db/configurations configs)))))

(defn interpolate [event-data events]
  (walk/postwalk
    (fn [x]
      (case x
        :event/target.value (.. (:replicant/dom-event event-data) -target -value)
        :event/target.value.int (u/parse-int (.. (:replicant/dom-event event-data) -target -value))
        x))
    events))

(defn handle-events [event-data actions]
  (doseq [action (interpolate event-data actions)]
    (when action ;; Allow for ignorable nil actions
      (js/console.log "Handling action" action)
      (execute-action event-data action))))

(defn render! []
  (r/set-dispatch! handle-events)
  (r/render (js/document.getElementById "app")
    [:div
     (nav/component)
     (if-let [render (-> @state ::db/current-route :data :render)]
       (render state)
       [:div [:h1 "Not Found"]
        [:a {:href (rfe/href :route/home)}
         "Back to home"]])]))

(defn- on-navigate [new-match]
  (swap! state
    (fn [s]
      (let [on-mount (get-in new-match [:data :on-mount])
            s' (assoc s ::db/current-route new-match)]
        (if on-mount
          (on-mount new-match s')
          ;; Clear editing state for routes without on-mount
          (assoc s'
            ::db/editing-configuration nil
            ::db/validation-errors nil))))))

(defn init! []
  (add-watch state ::render
    (fn [_ _ _ _] (render!)))
  (rfe/start!
    (rf/router routes {:data {:coercion rcm/coercion}})
    on-navigate
    {:use-fragment false})
  (render!))

(defn reload! []
  (println "Reloading app...")
  (render!))
