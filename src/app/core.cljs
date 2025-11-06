(ns app.core
  (:require
    [app.configurations.routes :as configurations.routes]
    [app.db :as db]
    [app.home.routes :as home.routes]
    [app.layout :as layout]
    [reitit.coercion.malli :as rcm]
    [reitit.frontend :as rf]
    [reitit.frontend.easy :as rfe]
    [replicant.dom :as r]))

(def routes
  (concat
    home.routes/routes
    configurations.routes/routes))

(defn render! []
  (r/set-dispatch! db/dispatch!)
  (r/render (js/document.getElementById "app")
    (layout/component {}
      (if-let [render (-> @db/app-db ::db/current-route :data :render)]
        (render @db/app-db)
        [:div [:h1 "Not Found"]
         [:a {:href "/"}
          "Back to home"]]))))

(defn- on-navigate [new-match]
  (db/dispatch! {} [[:route/on-navigate new-match]]))

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
