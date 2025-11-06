(ns app.device.views
  (:require
    [app.device.db :as device.db]))

(defn connection-details [state]
  (let [connected? (device.db/connected? state)
        error (::device.db/error state)]
    [:div.card
     [:h2.text-lg.font-semibold.text-slate-900.mb-4 "Device Connection"]

     [:div.space-y-4
      [:div.flex.items-center.justify-between
       [:div.flex.items-center.gap-3
        [:div {:class (concat '[w-3 h-3 rounded-full ]
                        (cond
                          connected? '[bg-green-500]
                          error '[bg-red-500]
                          :else '[bg-slate-300]))}]
        [:div
         [:p.text-sm.font-medium.text-slate-900
          (cond
            connected? "Connected"
            error "Connection Error"
            :else "Not Connected")]
         (when error
           [:p.text-xs.text-red-600.mt-1 (str error)])]]

       [:div.flex.gap-2
        (if connected?
          [:button.btn-secondary
           {:on {:click [[::device.db/disconnect]]}}
           "Disconnect"]
          [:button.btn-primary
           {:on {:click [[::device.db/connect]]}}
           "Connect Device"])]]

      (when connected?
        [:div.pt-4.border-t.border-slate-200
         [:h3.text-sm.font-medium.text-slate-900.mb-2 "Test Communication"]
         [:button.btn-ghost
          {:on {:click [[::device.db/send-data "Hello from browser!\n"]]}}
          "Send Test Message"]])]]))
