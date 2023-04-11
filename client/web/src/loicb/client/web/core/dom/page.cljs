(ns loicb.client.web.core.dom.page
  (:require [loicb.client.web.core.dom.hiccup :as h]
            [loicb.client.web.core.dom.page.post :refer [page-post]]
            [re-frame.core :as rf]))

(defn page
  "Given the `page-name`, returns the page content."
  [page-name]
  (let [all-posts      (->> @(rf/subscribe [:subs.post/posts page-name])
                            (map #(assoc % :post/hiccup-content (h/md->hiccup (:post/md-content %)))))
        new-post       {:post/id "new-post-temp-id"}
        posts          (conj all-posts new-post)
        active-post-id @(rf/subscribe [:subs/pattern '{:page/post-active ?x}])
        active-post    (->> posts
                            (filter #(= active-post-id (:post/id %)))
                            first)]
    [:section.container
     {:id (name page-name)
      :key   (name page-name)}
     [:div.left
      (doall
       (for [post posts
             :let [{:post/keys [id title]} post]]
         (when title
           [:a
            {:key title
             :on-click #(rf/dispatch [:evt.page/set-active-post id])}
            title])))]
     [:div.right
      (page-post page-name active-post)]]))