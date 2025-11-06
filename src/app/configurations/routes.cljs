(ns app.configurations.routes
  (:require
    [app.configurations.configuration :as configuration]
    [app.configurations.db :as configurations.db]
    [app.configurations.views :as configurations.views]))

(def routes
  [["/configurations"
    {:name :configurations.routes/index
     :breadcrumbs [{:label "Home" :href "/"}
                   {:label "Configurations" :route [:configurations.routes/index]}]
     :render configurations.views/list-page}]

   ["/configurations/new"
    {:name :configurations.routes/new
     :breadcrumbs [{:label "Home" :href "/"}
                   {:label "Configurations" :route [:configurations.routes/index]}
                   {:label "New" :href "/configurations/new"}]
     :render configurations.views/new-page
     :on-mount
     (fn [_match state]
       (assoc state
         ::configurations.db/current-configuration
         (configuration/new-configuration (::configurations.db/configurations state))
         ::configurations.db/validation-errors nil))}]

   ["/configurations/:id/edit"
    {:name :configurations.routes/edit
     :breadcrumbs [{:label "Home" :href "/"}
                   {:label "Configurations" :route [:configurations.routes/index]}
                   {:label "Edit"}]
     :render configurations.views/edit-page
     :parameters {:path [:map [:id :uuid]]}
     :on-mount
     (fn [match state]
       (let [id (get-in match [:parameters :path :id])]
         (assoc state
           ::configurations.db/current-configuration
           (get-in state [::configurations.db/configurations id])
           ::configurations.db/validation-errors nil)))}]])
