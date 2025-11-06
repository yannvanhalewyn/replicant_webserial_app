(ns app.configurations.views
  (:require
   [app.configurations.configuration :as configuration]
   [app.configurations.db :as configurations.db]
   [app.device.db :as device.db]
   [malli.core :as m]
   [malli.util :as mu]
   [reitit.frontend.easy :as rfe]))

(defn- configuration-item [config]
  [:div.card-hover
   [:a
    {:href (rfe/href :configurations.routes/show
             {:id (:configuration/id config)})}
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
      [:a.btn.btn-ghost
       {:href (rfe/href :configurations.routes/edit
                {:id (:configuration/id config)})
        :on {:click [[:event/stop-propagation]]}}
       "Edit"]
      [:button.btn.btn-danger
       {:on {:click [[::configurations.db/delete (:configuration/id config)]
                     [:event/stop-propagation]]}}
       "Delete"]]]]])

(defn list-page [state]
  (let [configurations (::configurations.db/configurations state)]
    [:div
     [:div.flex.items-center.justify-between.mb-6
      [:div
       [:h1.text-3xl.font-bold.text-slate-900.mb-2 "Configurations"]
       [:p.text-slate-600 "Manage your device configurations"]]
      [:a.btn.btn-primary.flex.items-center.gap-2
       {:href (rfe/href :configurations.routes/new)}
       [:span "＋"]
       [:span "New Configuration"]]]

     [:div.space-y-4
      (if (empty? configurations)
        [:div.empty-state
         [:div.text-4xl.mb-4 "⚙️"]
         [:h3.text-lg.font-medium.text-slate-900.mb-2 "No configurations yet"]
         [:p.text-slate-600.mb-6 "Get started by creating your first configuration"]
         [:a.btn.btn-primary.inline-block
          {:href (rfe/href :configurations.routes/new)}
          "Create Configuration"]]
        (for [config (sort-by :configuration/name (vals configurations))]
          ^{:key (:configuration/id config)}
          (configuration-item config)))]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Form

(defn- format-number [n]
  (.format (js/Intl.NumberFormat "en-US") n))

(defn- range-slider [{:keys [form-data errors key]}]
  (let [schema-props (m/properties (mu/get configuration/BaseSchema key))
        unit (:form/unit schema-props)]
   [:div
    [:label.label
     [:span (:form/label schema-props) " "]
     [:span.text-primary-600.font-semibold
      (format-number (get form-data key)) " " unit]]
    [:input.w-full.h-2.bg-slate-200.rounded-lg.appearance-none.cursor-pointer
     {:type "range"
      :min (:min schema-props)
      :max (:max schema-props)
      :step 1
      :value (get form-data key)
      :class "range-slider"
      :on {:input [[::configurations.db/update-form-field key [:event/target.value.int]]]}}]
    [:div.flex.justify-between.text-xs.text-slate-500.mt-1
     [:span (str (format-number (:min schema-props)) " " unit)]
     [:span (str (format-number (:max schema-props)) " " unit)]]
    (when-let [field-errors (get errors key)]
      (for [error field-errors]
        [:p.text-sm.text-red-600.mt-1 error]))]))

(defn- form
  [{:keys [config errors on-save]}]
  [:form.card
   {:on {:submit (cond-> [[:event/prevent-default]]
                   (empty? errors)
                   (concat on-save))}}

   [:div.space-y-6
    [:div
     [:label.label
      "Configuration Name"]
     [:input.input
      {:type "text"
       :placeholder "Enter configuration name"
       :value (:configuration/name config)
       :on {:input [[::configurations.db/update-form-field :configuration/name [:event/target.value]]]}}]
     (when-let [field-errors (:configuration/name errors)]
       (for [error field-errors]
         [:p.text-sm.text-red-600.mt-1 error]))]

    (range-slider
      {:form-data config
       :key :configuration/min-frequency
       :errors errors})

    (range-slider
      {:form-data config
       :key :configuration/max-frequency
       :errors errors})

    (range-slider
      {:form-data config
       :key :configuration/volume
       :errors errors})

    (when errors
      [:div.alert-error
       [:div.flex.items-start.gap-2
        [:span.text-red-600 "⚠️"]
        [:div
         [:p.text-sm.font-medium.text-red-900 "Validation Errors"]
         [:ul
          (for [[k messages] errors
                msg messages]
           [:li.text-sm.text-red-700.mt-1
            (str "- " (name k) ": " msg)])]]]])

    [:div.pt-4.flex.gap-3.border-t.border-slate-200
     [:a.btn.btn-secondary
      {:href (rfe/href :configurations.routes/index)}
      "Cancel"]
     [:button.btn.btn-primary
      (cond-> {}
        (seq errors) (assoc :disabled true))
      "Save Configuration"]]]])

(defn new-page [state]
  (let [current-config (::configurations.db/current-configuration state)]
    [:div
     [:h1.text-3xl.font-bold.text-slate-900.mb-6 "New Configuration"]
     (form
       {:config current-config
        :errors (::configurations.db/validation-errors state)
        :on-save [[::configurations.db/save current-config]]})]))

(defn edit-page [state]
  (let [current-config (::configurations.db/current-configuration state)
        validation-errors (::configurations.db/validation-errors state)]
    (if current-config
      [:div
       [:h1.text-3xl.font-bold.text-slate-900.mb-6 "Edit Configuration"]
       (form
         {:config current-config
          :errors validation-errors
          :on-save [[::configurations.db/save current-config]]})]
      [:div.empty-state
       [:div.text-4xl.mb-4 "❌"]
       [:h3.text-lg.font-medium.text-slate-900.mb-2 "Configuration not found"]
       [:p.text-slate-600.mb-6 "The configuration you're looking for doesn't exist"]
        [:a.btn-ghost.inline-block
         {:href (rfe/href :configurations.routes/index)}
         "← Back to Configurations"]])))

(defn show-page [state]
  (let [current-config (::configurations.db/current-configuration state)
        device-connected? (device.db/connected? state)]
    (if current-config
      [:div
       [:div.flex.items-center.justify-between.mb-6
        [:h1.text-3xl.font-bold.text-slate-900
         (:configuration/name current-config)]
        [:div.flex.gap-2
         [:a.btn.btn-ghost
          {:href (rfe/href :configurations.routes/edit
                   {:id (:configuration/id current-config)})}
          "Edit"]
         (when device-connected?
           [:button.btn.btn-primary
            {:on {:click [[::configurations.db/send-to-device current-config]]}}
            "Send to Device"])]]

       [:div.card
        [:h2.text-xl.font-semibold.text-slate-900.mb-4 "Configuration Details"]
        [:div.space-y-4
         [:div.flex.items-center.justify-between.border-b.border-slate-200.pb-3
          [:span.text-sm.font-medium.text-slate-600 "Min Frequency"]
          [:span.text-sm.text-slate-900.font-semibold
           (:configuration/min-frequency current-config) " Hz"]]
         [:div.flex.items-center.justify-between.border-b.border-slate-200.pb-3
          [:span.text-sm.font-medium.text-slate-600 "Max Frequency"]
          [:span.text-sm.text-slate-900.font-semibold
           (:configuration/max-frequency current-config) " Hz"]]
         [:div.flex.items-center.justify-between
          [:span.text-sm.font-medium.text-slate-600 "Volume"]
          [:span.text-sm.text-slate-900.font-semibold
           (:configuration/volume current-config) " %"]]]]]
      [:div.empty-state
       [:div.text-4xl.mb-4 "❌"]
       [:h3.text-lg.font-medium.text-slate-900.mb-2 "Configuration not found"]
       [:p.text-slate-600.mb-6 "The configuration you're looking for doesn't exist"]
       [:a.btn-ghost.inline-block
        {:href (rfe/href :configurations.routes/index)}
        "← Back to Configurations"]])))

