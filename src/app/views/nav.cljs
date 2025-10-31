(ns app.views.nav
  (:require
    [reitit.frontend.easy :as rfe]))

(defn component []
  [:nav {:style {:margin-bottom "20px"}}
   [:a {:href (rfe/href :route/home)} "Home"]
   " | "
   [:a {:href (rfe/href :route/configurations)} "Configurations"]])
