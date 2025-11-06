(ns app.layout
  (:require
    [app.db :as db]
    [app.device.db :as device.db]
    [reitit.frontend.easy :as rfe]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Sidebar Navigation

(defn- nav-item
  [{:keys [href icon label active?]}]
  [:a {:href href
       :class (concat
                '[flex items-center gap-3 px-4 py-3 text-sm font-medium rounded-lg transition-colors ]
                (if active?
                  '[bg-primary-600 text-white border-l-4 border-white]
                  '[text-slate-300 hover:bg-slate-700 hover:text-white border-l-4 border-transparent]))}
   [:span.text-lg icon]
   [:span label]])

(defn- sidebar
  [{:keys [current-route]}]
  (let [route-name (get-in current-route [:data :name])]
    [:aside.fixed.left-0.top-0.h-screen.w-64.bg-slate-800.shadow-lg.flex.flex-col
     [:div.flex.items-center.gap-2.px-4.py-6.border-b.border-slate-700
      [:div.w-10.h-10.bg-primary-600.rounded-lg.flex.items-center.justify-center
       [:span.text-white.text-xl.font-bold "R"]]
      [:div
       [:h1.text-white.text-lg.font-bold.leading-none "Configurator"]
       [:p.text-slate-400.text-xs "WebSerial"]]]

     [:nav.flex-1.px-3.py-6.space-y-1
      (nav-item {:href (rfe/href :home.routes/index)
                 :icon "🏠"
                 :label "Home"
                 :active? (= route-name :home.routes/index)})
      (nav-item {:href (rfe/href :devices.routes/index)
                 :icon "🔌"
                 :label "Devices"
                 :active? (= route-name :devices.routes/index)})
      (nav-item {:href (rfe/href :configurations.routes/index)
                 :icon "⚙️"
                 :label "Configurations"
                 :active? (contains? #{:configurations.routes/index
                                       :configurations.routes/new
                                       :configurations.routes/edit}
                            route-name)})]

     [:div.px-4.py-4.border-t.border-slate-700
      [:div.text-xs.text-slate-500 "v0.0.1-alpha"]]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Breadcrumbs

(defn- breadcrumb-item
  [{:keys [label href route last?]}]
  (if last?
    [:span.text-slate-900.font-medium label]
    [:span.flex.items-center.gap-2
     [:a.text-slate-600.hover:text-slate-900.transition-colors
      {:href (if route
               (apply rfe/href route)
               href)}
      label]
     [:span.text-slate-400 "/"]]))

(defn- breadcrumbs
  [{:keys [current-route]}]
  (let [crumbs (get-in current-route [:data :breadcrumbs] [])]
    (when (seq crumbs)
      [:nav.flex.items-center.gap-2.text-sm
       (for [[idx crumb] (map-indexed vector crumbs)]
         ^{:key idx}
         (breadcrumb-item (assoc crumb :last? (= idx (dec (count crumbs))))))])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Connection Status

(defn- connection-status-badge
  [state]
  (let [connected? (device.db/connected? state)
        error (::device.db/error state)]
    [:div.flex.items-center.gap-3
     [:div.flex.items-center.gap-2
      [:div {:class (concat '[w-2.5 h-2.5 rounded-full]
                      (cond
                        connected? '[bg-green-500 animate-pulse]
                        error '[bg-red-500]
                        :else '[bg-slate-300]))}]
      [:span.text-sm.font-medium.text-slate-700
       (cond
         connected? "Connected"
         error "Error"
         :else "Not Connected")]]

     (if connected?
       [:button.btn.btn-secondary.px-3.py-1.text-xs
        {:on {:click [[::device.db/disconnect]]}}
        "Disconnect"]
       [:button.btn.btn-primary.px-3.py-1.text-xs
        {:on {:click [[::device.db/connect]]}}
        "Connect Device"])]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Main Layout

(defn component [state & children]
  (let [current-route (::db/current-route state)]
    [:div.flex.h-screen.bg-slate-50
     (sidebar {:current-route current-route})

     [:div.flex-1.ml-64.flex.flex-col
      ;; Header
      [:header.bg-white.border-b.border-slate-200.px-6.py-4
       [:div.flex.items-center.justify-between
        (breadcrumbs {:current-route current-route})
        (connection-status-badge state)]]

      ;; Page Content
      [:main.flex-1.overflow-y-auto
       [:div.max-w-7xl.mx-auto.px-6.py-8
        children]]]]))
