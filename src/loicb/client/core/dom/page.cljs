(ns loicb.client.core.dom.page
  (:require [loicb.client.core.dom.hiccup :as h]
            [loicb.client.core.dom.common.link :refer [internal-link]]
            [loicb.client.core.dom.common.svg :as svg]
            [re-frame.core :as rf]))

(defn post-authors
  [{:post/keys [show-dates? creation-date last-edit-date]}]
  (when show-dates?
    [:div.post-dates
     (when creation-date
       [:h4 (str creation-date " (Audited)")])
     (when last-edit-date
       [:h4 (str last-edit-date " (Edited)")])]))

(defn post-view
  [{:post/keys [css-class image-beside hiccup-content] :as post}]
  (let [{:image/keys [src src-dark alt]} image-beside
        src (if (= :dark @(rf/subscribe [:subs/pattern '{:app/theme ?x}]))
              src-dark src)]
    [:div.post-body
     {:class css-class}
     (when src
       [:div.image
        [:img {:src src :alt alt}]])
     [:div.text
      [post-authors post]
      hiccup-content]]))

(defn post
  "Post without any possible interaction."
  [{:post/keys [id]
    :as post}]
  [:div.post
   {:key id
    :id id}
   [post-view post]])

(defn page-type-1
  "Page with a left bar that displays only one active post.
   Designed for `portfolio` and `blog` page"
  []
  (let [page-title @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:title ?x}}}])
        page-name @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:name ?x}}}])
        db-page-name @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:db-page-name ?x}}}])
        post-route @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:post-route ?x}}}])
        posts       (->> @(rf/subscribe [:subs.post/posts db-page-name])
                             (map #(assoc % :post/hiccup-content (h/md->hiccup (:post/md-content %))))
                             (sort-by :post/order)
                             reverse)
        active-post-id  (or @(rf/subscribe [:subs/pattern '{:app/current-view {:path-params {:post-id ?x}}}])
                            (-> posts first :post/id))
        active-post     (->> posts
                             (filter #(= active-post-id (:post/id %)))
                             first)
        left-menu-open? @(rf/subscribe [:subs/pattern '{:nav.left-menu/open? ?x}])]
    [:section.container.type-1
     {:id  (name page-name)
      :key (name page-name)}
     (if left-menu-open?
       [:div.left
        [:div.menu-title
         [:button.burger-btn
          {:on-click #(rf/dispatch [:evt.nav/toggle :left-menu])}
          svg/burger-icon]
         [:h1 page-title]]
        [:ul
         (doall
          (for [post posts
                :let [{:post/keys [id title]} post]]
            [:li {:key title}
             (internal-link
              [:div
               (when (= active-post-id id) {:class "active"})
               [svg/right-arrow]
               [:h2 title]]
              {:post-id id
               :page-name (or post-route page-name)})
             (internal-link
              [:div
               (when (= active-post-id id) {:class "active"})
               [svg/right-arrow]
               [:h2 title]]
              {:post-id id
               :page-name (or post-route page-name)
               :mobile? true})]))]]
       [:div.left-close
        [:div.menu-title
         [:button.burger-btn
          {:on-click #(rf/dispatch [:evt.nav/toggle :left-menu])}
          svg/burger-icon]
         [:h1 page-title]]])
     [:div.right
      (post active-post)]]))