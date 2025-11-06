(ns app.core
  (:require
    [app.configurations.routes :as configurations.routes]
    [app.db :as db]
    [app.home.routes :as home.routes]
    [app.layout :as layout]
    [app.tools.utils :as u]
    [clojure.walk :as walk]
    [reitit.coercion.malli :as rcm]
    [reitit.frontend :as rf]
    [reitit.frontend.easy :as rfe]
    [replicant.dom :as r]))

(def routes
  (concat
    home.routes/routes
    configurations.routes/routes))

(defn interpolate [event-data actions]
  (walk/postwalk
    (fn [x]
      (case x
        :event/target.value (.. (:replicant/dom-event event-data) -target -value)
        :event/target.value.int (u/parse-int (.. (:replicant/dom-event event-data) -target -value))
        x))
    actions))

(defn handle-actions [event-data actions]
  (let [db @db/app-db
        effects (->> (interpolate event-data actions)
                  (mapcat #(db/action->effects {:db db} %)))]
    (doseq [effect effects]
      (when effect ;; Allow for ignorable nil effects
        (db/execute-effect event-data effect)))))

(defn render! []
  (r/set-dispatch! handle-actions)
  (r/render (js/document.getElementById "app")
    (layout/component {}
      (if-let [render (-> @db/app-db ::db/current-route :data :render)]
        (render @db/app-db)
        [:div [:h1 "Not Found"]
         [:a {:href "/"}
          "Back to home"]]))))

(defn- on-navigate [new-match]
  (swap! db/app-db
    (fn [s]
      (let [on-mount (get-in new-match [:data :on-mount])
            s' (assoc s ::db/current-route new-match)]
        (if on-mount
          (on-mount new-match s')
          s')))))

(defn init! []
  (add-watch db/app-db ::render
    (fn [_ _ _ _] (render!)))
  (rfe/start!
    (rf/router routes {:data {:coercion rcm/coercion}})
    on-navigate
    {:use-fragment false})
  (render!))

(defn reload! []
  (println "Reloading app...")
  (render!))
