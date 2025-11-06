(ns app.views.configurations
  (:require
    [app.db :as db]
    [app.configuration :as configuration]
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
     [:a {:href (rfe/href :route/configurations-edit
                  {:id (:configuration/id config)})}
      "Edit"]
     [:button {:on {:click [[:configuration/delete (:configuration/id config)]]}}
      "Delete"]]]])

(defn- configuration-form [{:keys [config validation-errors on-save]}]
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
             :on {:input [[:configuration-form/update-field :configuration/name :event/target.value]]}}]]

   [:div {:style {:margin-bottom "1rem"}}
    [:label {:style {:display "block" :margin-bottom "0.5rem"}}
     "Min Frequency: " (:configuration/min-frequency config) " Hz"]
    [:input {:type "range"
             :min 0
             :max 20000
             :step 1
             :value (:configuration/min-frequency config)
             :style {:width "100%"}
             :on {:input [[:configuration-form/update-field :configuration/min-frequency :event/target.value.int]]}}]]

   [:div {:style {:margin-bottom "1rem"}}
    [:label {:style {:display "block" :margin-bottom "0.5rem"}}
     "Max Frequency: " (:configuration/max-frequency config) " Hz"]
    [:input {:type "range"
             :min 0
             :max 20000
             :step 1
             :value (:configuration/max-frequency config)
             :style {:width "100%"}
             :on {:input [[:configuration-form/update-field :configuration/max-frequency :event/target.value.int]]}}]]

   [:div {:style {:margin-bottom "1rem"}}
    [:label {:style {:display "block" :margin-bottom "0.5rem"}}
     "Volume: " (:configuration/volume config) "%"]
    [:input {:type "range"
             :min 0
             :max 100
             :step 1
             :value (:configuration/volume config)
             :style {:width "100%"}
             :on {:input [[:configuration-form/update-field :configuration/volume :event/target.value.int]]}}]]

   ;; TODO display errors on each field
   (when validation-errors
     [:div {:style {:color "red" :margin-bottom "1rem"}}
      [:strong "Validation Error:"]
      [:div "Max frequency must be greater than or equal to min frequency"]])

   [:div {:style {:display "flex" :gap "0.5rem"}}
    [:a {:href (rfe/href :route/configurations)}
     "Cancel"]
    [:button (cond-> {}
               )
     "Save"]]])

(defn new-page [state-atom]
  (let [state @state-atom
        configurations (::db/configurations state)
        editing-config (::db/editing-configuration state)
        validation-errors (::db/validation-errors state)]

    (when-not editing-config
      [:div "Loading..."])

    (when editing-config
      [:div
       [:h1 "New Configuration"]

       (configuration-form
         {:config editing-config
          :validation-errors validation-errors
          :on-save [[:configuration/save editing-config]]})

       [:hr {:style {:margin "2rem 0"}}]

       [:h2 "Existing Configurations"]
       [:div
        (if (empty? configurations)
          [:p "No configurations yet."]
          (for [config (sort-by :configuration/name (vals configurations))]
            ^{:key (:configuration/id config)}
            (configuration-item config)))]])))

(defn edit-page [state-atom]
  (let [state @state-atom
        configurations (::db/configurations state)
        editing-config (::db/editing-configuration state)
        validation-errors (::db/validation-errors state)]

    (when-not editing-config
      [:div "Configuration not found"])

    (when editing-config
      [:div
       [:h1 "Edit Configuration"]

       (configuration-form
         {:config editing-config
          :validation-errors validation-errors
          :on-save [[:configuration/save editing-config]]})

       [:hr {:style {:margin "2rem 0"}}]

       [:h2 "Other Configurations"]
       [:div
        (for [config (sort-by :configuration/name (vals configurations))
              :when (not= (:configuration/id config) (:configuration/id editing-config))]
          ^{:key (:configuration/id config)}
          (configuration-item config))]])))

(defn page [state-atom]
  (let [state @state-atom
        configurations (::db/configurations state)]
    [:div
     [:h1 "Configurations"]
     [:p "Manage your device configurations here."]

     [:div.status
      [:strong "Serial Status: "]
      (:serial-status state)]

     [:div {:style {:margin "2rem 0"}}
      [:a {:href (rfe/href :route/configurations-new)}
       "Add Configuration"]]

     [:div
      (if (empty? configurations)
        [:p "No configurations yet. Click 'Add Configuration' to create one."]
        (for [config (sort-by :configuration/name (vals configurations))]
          ^{:key (:configuration/id config)}
          (configuration-item config)))]]))
