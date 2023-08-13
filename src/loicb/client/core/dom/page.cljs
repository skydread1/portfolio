(ns loicb.client.core.dom.page
  (:require [loicb.client.core.dom.hiccup :as h]
            [loicb.client.core.dom.common.link :refer [internal-link]]
            [loicb.client.core.dom.common.svg :as svg]
            [re-frame.core :as rf]))

(defn post-view
  [{:post/keys [css-class image-beside hiccup-content]}]
  (let [{:image/keys [src src-dark alt]} image-beside
        src (if (= :dark @(rf/subscribe [:subs/pattern '{:app/theme ?x}]))
              src-dark src)]
    [:div.post-body
     {:class css-class}
     (when src
       [:div.image
        [:img {:src src :alt alt}]])
     [:div.text
      hiccup-content]]))

(defn post
  "Full post including the post content."
  [{:post/keys [id]
    :as post}]
  [:div.post
   {:key id
    :id id}
   [post-view post]])

(defn vignette-view
  [{:post/keys [css-class title image-beside]}]
  (let [{:image/keys [src src-dark alt]} image-beside
        src (if (= :dark @(rf/subscribe [:subs/pattern '{:app/theme ?x}]))
              src-dark src)]
    [:div.post-body
     {:class css-class}
     [:h2 title]
     (when src
       [:div.image
        [:img {:src src :alt alt}]])]))

(defn vignette
  "Short version of a post without the content of the post."
  [{:post/keys [id] :as post} post-route page-name]
  [:div.vignette
   {:key id
    :id id}
   (internal-link
    [vignette-view post]
    {:post-id id
     :page-name (or post-route page-name)})])

(defn page-with-vignettes
  "Page with post vignettes."
  []
  (let [page-title @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:title ?x}}}])
        page-name @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:name ?x}}}])
        db-page-name @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:db-page-name ?x}}}])
        post-route @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:post-route ?x}}}])
        posts       (->> @(rf/subscribe [:subs.post/posts db-page-name])
                             (map #(assoc % :post/hiccup-content (h/md->hiccup (:post/md-content %))))
                             (sort-by :post/order)
                             reverse)]
    [:section.container
     {:id  (name page-name)
      :key (name page-name)}
     [:h1 page-title]
     [:div.vignettes
      (doall
       (for [post posts]
         [vignette post post-route page-name]))]]))

(defn page-with-a-post
  "Page that displays the full post content.
   It contains only one post."
  []
  (let [page-name @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:name ?x}}}])
        db-page-name @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:db-page-name ?x}}}])
        posts       (->> @(rf/subscribe [:subs.post/posts db-page-name])
                         (map #(assoc % :post/hiccup-content (h/md->hiccup (:post/md-content %))))
                         (sort-by :post/order)
                         reverse)
        active-post-id  (or @(rf/subscribe [:subs/pattern '{:app/current-view {:path-params {:post-id ?x}}}])
                            (-> posts first :post/id))
        active-post     (->> posts
                             (filter #(= active-post-id (:post/id %)))
                             first)]
    [:section.container
     {:id  (name page-name)
      :key (name page-name)}
     [:div.right
      (post active-post)]]))