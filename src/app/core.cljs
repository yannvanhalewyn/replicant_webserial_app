(ns app.core
  (:require
    [app.configurations.db :as configurations.db]
    [app.configurations.routes :as configurations.routes]
    [app.db :as db]
    [app.device.routes :as device.routes]
    [app.home.routes :as home.routes]
    [app.layout :as layout]
    [reitit.coercion.malli :as rcm]
    [reitit.frontend :as rf]
    [reitit.frontend.easy :as rfe]
    [replicant.dom :as r]))

(def routes
  (concat
    home.routes/routes
    device.routes/routes
    configurations.routes/routes))

(defn render! []
  (r/set-dispatch! db/dispatch!)
  (let [state @db/app-db]
    (r/render (js/document.getElementById "app")
      (layout/component state
        (if-let [render (-> state ::db/current-route :data :render)]
          (render state)
          (layout/not-found))))))

(defn- on-navigate [new-match]
  (db/dispatch! {} [[:route/on-navigate new-match]]))

(defn init! []
  (db/dispatch! {} [[::configurations.db/init]])
  (add-watch db/app-db ::render
    (fn [_ _ _ _]
      (render!)))
  (rfe/start!
    (rf/router routes {:data {:coercion rcm/coercion}
                       :conflicts nil})
    on-navigate
    {:use-fragment false})
  (render!))

(defn reload! []
  (println "Reloading app...")
  (render!))
