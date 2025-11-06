(ns app.configurations.views
  (:require
    [app.configurations.db :as configurations.db]
    [app.db :as db]
    [reitit.frontend.easy :as rfe]))

(defn- configuration-item [config]
  [:div.card-hover
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
     [:a.btn-ghost
      {:href (rfe/href :configurations.routes/edit
               {:id (:configuration/id config)})}
      "Edit"]
     [:button.btn-danger
      {:on {:click [[::configurations.db/delete (:configuration/id config)]]}}
      "Delete"]]]])

(defn list-page [state]
  (let [configurations (::configurations.db/configurations state)]
    [:div
     [:div.flex.items-center.justify-between.mb-6
      [:div
       [:h1.text-3xl.font-bold.text-slate-900.mb-2 "Configurations"]
       [:p.text-slate-600 "Manage your device configurations"]]
      [:a.btn-primary.flex.items-center.gap-2
       {:href (rfe/href :configurations.routes/new)}
       [:span "＋"]
       [:span "New Configuration"]]]

     [:div.space-y-4
      (if (empty? configurations)
        [:div.empty-state
         [:div.text-4xl.mb-4 "⚙️"]
         [:h3.text-lg.font-medium.text-slate-900.mb-2 "No configurations yet"]
         [:p.text-slate-600.mb-6 "Get started by creating your first configuration"]
         [:a.btn-primary.inline-block
          {:href (rfe/href :configurations.routes/new)}
          "Create Configuration"]]
        (for [config (sort-by :configuration/name (vals configurations))]
          ^{:key (:configuration/id config)}
          (configuration-item config)))]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Form

(defn- format-number [n]
  (.format (js/Intl.NumberFormat "en-US") n))

(defn- range-slider [{:keys [form-data errors key label unit min max]}]
  [:div
   [:label.label
    [:span label]
    [:span.text-primary-600.font-semibold
     (format-number (get form-data key)) " " unit]]
   [:input.w-full.h-2.bg-slate-200.rounded-lg.appearance-none.cursor-pointer
    {:type "range"
     :min min
     :max max
     :step 1
     :value (get form-data key)
     :class "range-slider"
     :on {:input [[::configurations.db/update-form-field key [:event/target.value.int]]]}}]
   [:div.flex.justify-between.text-xs.text-slate-500.mt-1
    [:span (str (format-number min) " " unit)]
    [:span (str (format-number max) " " unit)]]
   (when-let [field-errors (get errors key)]
     (for [error field-errors]
       [:p.text-sm.text-red-600.mt-1 error]))])

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
       :errors errors
       :label "Min Frequency: "
       :unit "Hz"
       :min 0
       :max 20000})

    (range-slider
      {:form-data config
       :key :configuration/max-frequency
       :errors errors
       :label "Max Frequency: "
       :unit "Hz"
       :min 0
       :max 20000})

    (range-slider
      {:form-data config
       :key :configuration/volume
       :errors errors
       :label "Volume: "
       :unit "%"
       :min 0
       :max 100})

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

    [:div.flex.gap-3.pt-4.border-t.border-slate-200
     [:a.btn-secondary.px-6.py-2
      {:href (rfe/href :configurations.routes/index)}
      "Cancel"]
     [:button.btn-primary.px-6.py-2.disabled:bg-slate-300.disabled:cursor-not-allowed
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
