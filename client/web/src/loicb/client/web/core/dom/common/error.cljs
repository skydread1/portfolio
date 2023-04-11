(ns loicb.client.web.core.dom.common.error
  (:require [re-frame.core :as rf]))

(defn errors
  [comp-id err-ids]
  (when @(rf/subscribe [:subs/pattern '{:app/errors ?}])
    [:div.errors
     {:key comp-id}
     (doall
      (for [id err-ids]
        (when-let [error @(rf/subscribe [:subs/pattern {:app/errors {id '?id}}])]
          [:div.error
           {:key id}
           error])))]))