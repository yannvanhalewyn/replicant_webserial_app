(ns app.configurations.routes
  (:require
   [app.configurations.configuration :as configuration]
   [app.configurations.views :as configurations.views]
   [app.db :as db]))

(def routes
  [["/configurations"
    {:name :configurations.routes/index
     :render configurations.views/list-page}]

   ["/configurations/new"
    {:name :configurations.routes/new
     :render configurations.views/new-page
     :on-mount
     (fn [_match state]
       (assoc state
         ::db/editing-configuration
         (configuration/new-configuration (::db/configurations state))
         ::db/validation-errors nil))}]

   ["/configurations/:id/edit"
    {:name :configurations.routes/edit
     :render configurations.views/edit-page
     :parameters {:path [:map [:id :uuid]]}
     :on-mount
     (fn [match state]
       (let [id (get-in match [:parameters :path :id])]
         (assoc state
           ::db/editing-configuration
           (get-in state [::db/configurations id])
           ::db/validation-errors nil)))}]])
