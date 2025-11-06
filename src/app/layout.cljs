(ns app.layout
  (:require
   [reitit.frontend.easy :as rfe]))

(defn component [_props & children]
  [:div
   [:nav {:style {:margin-bottom "20px"}}
    [:a {:href (rfe/href :home.routes/index)} "Home"]
    " | "
    [:a {:href (rfe/href :configurations.routes/index)} "Configurations"]]
   children])
