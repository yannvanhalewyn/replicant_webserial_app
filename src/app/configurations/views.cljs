(ns app.configurations.views
  (:require
    [app.configurations.db :as configurations.db]
    [app.db :as db]
    [app.device.views :as device.views]
    [reitit.frontend.easy :as rfe]))

(defn- configuration-item [config]
  [:div {:style {:border "1px solid #ccc"
                 :padding "1rem"
                 :margin "0.5rem 0"
                 :border-radius "4px"}}
   [:div {:style {:display "flex"
                  :justify-content "space-between"
                  :align-items "center"}}
    [:div
     [:h3 {:style {:margin "0 0 0.5rem 0"}}
      (:configuration/name config)]
     [:div
      [:strong "Frequency Range: "]
      (:configuration/min-frequency config) " Hz - "
      (:configuration/max-frequency config) " Hz"]
     [:div
      [:strong "Volume: "]
      (:configuration/volume config) "%"]]
    [:div {:style {:display "flex" :gap "0.5rem"}}
     [:a {:href (rfe/href :configurations.routes/edit
                  {:id (:configuration/id config)})}
      "Edit"]
     [:button {:on {:click [[::configurations.db/delete (:configuration/id config)]]}}
      "Delete"]]]])

(defn list-page [state]
  (let [configurations (::db/configurations state)]
    [:div
     [:h1 "Configurations"]
     [:p "Manage your device configurations here."]

     (device.views/device-status state)

     [:div {:style {:margin "2rem 0"}}
      [:a {:href (rfe/href :configurations.routes/new)}
       "Add Configuration"]]

     [:div
      (if (empty? configurations)
        [:p "No configurations yet. Click 'Add Configuration' to create one."]
        (for [config (sort-by :configuration/name (vals configurations))]
          ^{:key (:configuration/id config)}
          (configuration-item config)))]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Form

(defn- form
  [{:keys [config validation-errors on-save]}]
  [:form
   {:on {:submit (cond-> [[:event/prevent-default]]
                   (empty? validation-errors)
                   (concat on-save))}}
   [:h2 "Configuration"]

   [:div {:style {:margin-bottom "1rem"}}
    [:label {:style {:display "block" :margin-bottom "0.5rem"}}
     "Name"]
    [:input {:type "text"
             :value (:configuration/name config)
             :style {:width "100%" :padding "0.5rem"}
             :on {:input [[::configurations.db/update-form-field :configuration/name :event/target.value]]}}]]

   [:div {:style {:margin-bottom "1rem"}}
    [:label {:style {:display "block" :margin-bottom "0.5rem"}}
     "Min Frequency: " (:configuration/min-frequency config) " Hz"]
    [:input {:type "range"
             :min 0
             :max 20000
             :step 1
             :value (:configuration/min-frequency config)
             :style {:width "100%"}
             :on {:input [[::configurations.db/update-form-field :configuration/min-frequency :event/target.value.int]]}}]]

   [:div {:style {:margin-bottom "1rem"}}
    [:label {:style {:display "block" :margin-bottom "0.5rem"}}
     "Max Frequency: " (:configuration/max-frequency config) " Hz"]
    [:input {:type "range"
             :min 0
             :max 20000
             :step 1
             :value (:configuration/max-frequency config)
             :style {:width "100%"}
             :on {:input [[::configurations.db/update-form-field :configuration/max-frequency :event/target.value.int]]}}]]

   [:div {:style {:margin-bottom "1rem"}}
    [:label {:style {:display "block" :margin-bottom "0.5rem"}}
     "Volume: " (:configuration/volume config) "%"]
    [:input {:type "range"
             :min 0
             :max 100
             :step 1
             :value (:configuration/volume config)
             :style {:width "100%"}
             :on {:input [[::configurations.db/update-form-field :configuration/volume :event/target.value.int]]}}]]

   ;; TODO display errors on each field
   (when validation-errors
     [:div {:style {:color "red" :margin-bottom "1rem"}}
      [:strong "Validation Error:"]
      [:div "Max frequency must be greater than or equal to min frequency"]])

   [:div {:style {:display "flex" :gap "0.5rem"}}
    [:a {:href (rfe/href :configurations.routes/index)}
     "Cancel"]
    [:button (when (seq validation-errors)
               {:disabled true})
     "Save"]]])

(defn new-page [state]
  (let [current-config (::db/editing-configuration state)]
    [:div
     [:h1 "New Configuration"]
     (form
       {:config current-config
        :validation-errors (::db/validation-errors state)
        :on-save [[::configurations.db/save current-config]]})]))

(defn edit-page [state]
  (let [editing-config (::db/editing-configuration state)
        validation-errors (::db/validation-errors state)]
    (if editing-config
      [:div
       [:h1 "Edit Configuration"]
       (form
         {:config editing-config
          :validation-errors validation-errors
          :on-save [[::configurations.db/save editing-config]]}) ]
      [:div "Configuration not found"])))
