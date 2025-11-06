(ns app.configurations.views
  (:require
    [app.configurations.db :as configurations.db]
    [app.db :as db]
    [reitit.frontend.easy :as rfe]))

(defn- configuration-item [config]
  [:div.bg-white.rounded-lg.shadow-sm.border.border-slate-200.p-5.hover:shadow-md.transition-shadow
   [:div.flex.items-start.justify-between
    [:div.flex-1
     [:h3.text-lg.font-semibold.text-slate-900.mb-3
      (:configuration/name config)]
     [:div.space-y-2
      [:div.flex.items-center.gap-2
       [:span.text-sm.font-medium.text-slate-600 "Frequency Range:"]
       [:span.text-sm.text-slate-900
        (:configuration/min-frequency config) " Hz - "
        (:configuration/max-frequency config) " Hz"]]
      [:div.flex.items-center.gap-2
       [:span.text-sm.font-medium.text-slate-600 "Volume:"]
       [:span.text-sm.text-slate-900
        (:configuration/volume config) "%"]]]]
    [:div.flex.gap-2
     [:a.px-4.py-2.text-sm.font-medium.text-primary-700.bg-primary-50.border.border-primary-200.rounded-md.hover:bg-primary-100.transition-colors
      {:href (rfe/href :configurations.routes/edit
               {:id (:configuration/id config)})}
      "Edit"]
     [:button.px-4.py-2.text-sm.font-medium.text-red-700.bg-red-50.border.border-red-200.rounded-md.hover:bg-red-100.transition-colors
      {:on {:click [[::configurations.db/delete (:configuration/id config)]]}}
      "Delete"]]]])

(defn list-page [state]
  (let [configurations (::db/configurations state)]
    [:div
     [:div.flex.items-center.justify-between.mb-6
      [:div
       [:h1.text-3xl.font-bold.text-slate-900.mb-2 "Configurations"]
       [:p.text-slate-600 "Manage your device configurations"]]
      [:a.px-4.py-2.text-sm.font-medium.text-white.bg-primary-600.rounded-md.hover:bg-primary-700.transition-colors.flex.items-center.gap-2
       {:href (rfe/href :configurations.routes/new)}
       [:span "＋"]
       [:span "New Configuration"]]]

     [:div.space-y-4
      (if (empty? configurations)
        [:div.bg-white.rounded-lg.shadow-sm.border.border-slate-200.p-12.text-center
         [:div.text-4xl.mb-4 "⚙️"]
         [:h3.text-lg.font-medium.text-slate-900.mb-2 "No configurations yet"]
         [:p.text-slate-600.mb-6 "Get started by creating your first configuration"]
         [:a.inline-block.px-4.py-2.text-sm.font-medium.text-white.bg-primary-600.rounded-md.hover:bg-primary-700.transition-colors
          {:href (rfe/href :configurations.routes/new)}
          "Create Configuration"]]
        (for [config (sort-by :configuration/name (vals configurations))]
          ^{:key (:configuration/id config)}
          (configuration-item config)))]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Form

(defn- form
  [{:keys [config validation-errors on-save]}]
  [:form.bg-white.rounded-lg.shadow-sm.border.border-slate-200.p-6
   {:on {:submit (cond-> [[:event/prevent-default]]
                   (empty? validation-errors)
                   (concat on-save))}}

   [:div.space-y-6
    [:div
     [:label.block.text-sm.font-medium.text-slate-900.mb-2
      "Configuration Name"]
     [:input.w-full.px-4.py-2.border.border-slate-300.rounded-md.focus:ring-2.focus:ring-primary-500.focus:border-primary-500.transition-colors
      {:type "text"
       :placeholder "Enter configuration name"
       :value (:configuration/name config)
       :on {:input [[::configurations.db/update-form-field :configuration/name [:event/target.value]]]}}]]

    [:div
     [:label.block.text-sm.font-medium.text-slate-900.mb-2
      [:span "Min Frequency: "]
      [:span.text-primary-600.font-semibold
       (:configuration/min-frequency config) " Hz"]]
     [:input.w-full.h-2.bg-slate-200.rounded-lg.appearance-none.cursor-pointer
      {:type "range"
       :min 0
       :max 20000
       :step 1
       :value (:configuration/min-frequency config)
       :class "range-slider"
       :on {:input [[::configurations.db/update-form-field :configuration/min-frequency [:event/target.value.int]]]}}]
     [:div.flex.justify-between.text-xs.text-slate-500.mt-1
      [:span "0 Hz"]
      [:span "20,000 Hz"]]]

    [:div
     [:label.block.text-sm.font-medium.text-slate-900.mb-2
      [:span "Max Frequency: "]
      [:span.text-primary-600.font-semibold
       (:configuration/max-frequency config) " Hz"]]
     [:input.w-full.h-2.bg-slate-200.rounded-lg.appearance-none.cursor-pointer
      {:type "range"
       :min 0
       :max 20000
       :step 1
       :value (:configuration/max-frequency config)
       :class "range-slider"
       :on {:input [[::configurations.db/update-form-field :configuration/max-frequency [:event/target.value.int]]]}}]
     [:div.flex.justify-between.text-xs.text-slate-500.mt-1
      [:span "0 Hz"]
      [:span "20,000 Hz"]]]

    [:div
     [:label.block.text-sm.font-medium.text-slate-900.mb-2
      [:span "Volume: "]
      [:span.text-primary-600.font-semibold
       (:configuration/volume config) "%"]]
     [:input.w-full.h-2.bg-slate-200.rounded-lg.appearance-none.cursor-pointer
      {:type "range"
       :min 0
       :max 100
       :step 1
       :value (:configuration/volume config)
       :class "range-slider"
       :on {:input [[::configurations.db/update-form-field :configuration/volume [:event/target.value.int]]]}}]
     [:div.flex.justify-between.text-xs.text-slate-500.mt-1
      [:span "0%"]
      [:span "100%"]]]

    (when validation-errors
      [:div.bg-red-50.border.border-red-200.rounded-md.p-4
       [:div.flex.items-start.gap-2
        [:span.text-red-600 "⚠️"]
        [:div
         [:p.text-sm.font-medium.text-red-900 "Validation Error"]
         [:p.text-sm.text-red-700.mt-1
          "Max frequency must be greater than or equal to min frequency"]]]])

    [:div.flex.gap-3.pt-4.border-t.border-slate-200
     [:a.px-6.py-2.text-sm.font-medium.text-slate-700.bg-white.border.border-slate-300.rounded-md.hover:bg-slate-50.transition-colors
      {:href (rfe/href :configurations.routes/index)}
      "Cancel"]
     [:button.px-6.py-2.text-sm.font-medium.text-white.bg-primary-600.rounded-md.hover:bg-primary-700.transition-colors.disabled:bg-slate-300.disabled:cursor-not-allowed
      (cond-> {}
        (seq validation-errors) (assoc :disabled true))
      "Save Configuration"]]]])

(defn new-page [state]
  (let [current-config (::configurations.db/current-configuration state)]
    [:div
     [:h1.text-3xl.font-bold.text-slate-900.mb-6 "New Configuration"]
     (form
       {:config current-config
        :validation-errors (::configurations.db/validation-errors state)
        :on-save [[::configurations.db/save current-config]]})]))

(defn edit-page [state]
  (let [current-config (::configurations.db/current-configuration state)
        validation-errors (::configurations.db/validation-errors state)]
    (if current-config
      [:div
       [:h1.text-3xl.font-bold.text-slate-900.mb-6 "Edit Configuration"]
       (form
         {:config current-config
          :validation-errors validation-errors
          :on-save [[::configurations.db/save current-config]]})]
      [:div.bg-white.rounded-lg.shadow-sm.border.border-slate-200.p-12.text-center
       [:div.text-4xl.mb-4 "❌"]
       [:h3.text-lg.font-medium.text-slate-900.mb-2 "Configuration not found"]
       [:p.text-slate-600.mb-6 "The configuration you're looking for doesn't exist"]
       [:a.inline-block.px-4.py-2.text-sm.font-medium.text-primary-700.bg-primary-50.border.border-primary-200.rounded-md.hover:bg-primary-100.transition-colors
        {:href (rfe/href :configurations.routes/index)}
        "← Back to Configurations"]])))
