(ns app.home.routes
  (:require
    [app.configurations.db :as configurations.db]
    [app.device.db :as device.db]
    [app.device.views :as device.views]))

(def routes
  [["/"
    {:name :home.routes/index
     :breadcrumbs [{:label "Home" :href "/"}]
     :render
     (fn [state]
       [:div
        [:h1.text-3xl.font-bold.text-slate-900.mb-2
         "Configuration Panel"]
        [:p.text-slate-600.mb-8 "Connect your device and manage configurations"]

        [:div.grid.gap-6.md:grid-cols-2.lg:grid-cols-3.mb-8
          ;; Quick Stats / Info Cards
         [:div.stat-card
          [:div.flex.items-center.gap-3.mb-2
           [:span.text-2xl "⚙️"]
           [:h3.text-sm.font-medium.text-slate-600 "Configurations"]]
          [:p.text-3xl.font-bold.text-slate-900
           (count (get state ::configurations.db/configurations {}))]]

         [:div.stat-card
          [:div.mb-2.flex.items-center.gap-3
           [:span.text-2xl "🔌"]
           [:h3.text-sm.font-medium.text-slate-600 "Device Status"]]
          [:p.text-lg.font-semibold
           {:class (if (device.db/connected? state)
                     "text-green-600"
                     "text-slate-400")}
           (if (device.db/connected? state)
             "Connected"
             "Not Connected")]]

         [:div.stat-card
          [:div.flex.items-center.gap-3.mb-2
           [:span.text-2xl "📡"]
           [:h3.text-sm.font-medium.text-slate-600 "WebSerial API"]]
          [:p.text-lg.font-semibold.text-primary-600 "Ready"]]]

        (device.views/connection-details state)])}]])
