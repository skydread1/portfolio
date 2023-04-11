(ns loicb.client.web.core.dom.page
  (:require [loicb.client.web.core.dom.hiccup :as h]
            [loicb.client.web.core.dom.page.post :refer [page-post]]
            [re-frame.core :as rf]))

(defn page
  "Given the `page-name`, returns the page content."
  [page-name]
  (let [all-posts (->> @(rf/subscribe [:subs.post/posts page-name])
                       (map #(assoc % :post/hiccup-content (h/md->hiccup (:post/md-content %)))))
        new-post  {:post/id "new-post-temp-id"}
        posts     (conj all-posts new-post)]
    [:section.container
     {:id (name page-name)
      :key   (name page-name)}
     (doall
      (for [post posts]
        (page-post page-name post)))]))