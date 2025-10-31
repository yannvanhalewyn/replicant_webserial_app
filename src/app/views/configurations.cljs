(ns app.views.configurations)

(defn page [state]
  [:div
   [:h1 "Configurations"]
   [:p "Manage your device configurations here."]

   [:div.status
    [:strong "Serial Status: "]
    (:serial-status state)]

   [:div
    [:p "Configuration management coming soon..."]]])
