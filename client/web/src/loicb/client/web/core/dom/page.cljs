(ns loicb.client.web.core.dom.page
  (:require [loicb.client.web.core.dom.hiccup :as h]
            [loicb.client.web.core.dom.page.post :refer [page-post]]
            [loicb.client.web.core.dom.common.svg :as svg]
            [reitit.frontend.easy :as rfe]
            [re-frame.core :as rf]))

(defn internal-link
  "Reitit internal link for the navbar.
   Setting `reitit?` to false allows the use of a regular browser link (good for anchor link)."
  ([page-name text]
   (internal-link page-name text nil))
  ([page-name text params]
   (internal-link page-name text params true))
  ([page-name text params reitit?]
   [:a {:href                     (rfe/href page-name params)
        :key                      (:title params)
        :data-reitit-handle-click reitit?}
    text]))

(defn page-type-1
  "Page with a left bar that displays only one active post.
   Designed for `portfolio` and `blog` page"
  []
  (let [page-title @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:title ?x}}}])
        page-name @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:name ?x}}}])
        db-page-name @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:db-page-name ?x}}}])
        post-route @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:post-route ?x}}}])
        all-posts       (->> @(rf/subscribe [:subs.post/posts db-page-name])
                             (map #(assoc % :post/hiccup-content (h/md->hiccup (:post/md-content %))))
                             (sort-by :post/order)
                             reverse)
        new-post        {:post/id (uuid "new-post-temp-id") :post/title "New Post"}
        posts           (if @(rf/subscribe [:subs/pattern '{:app/user ?x}])
                          (conj all-posts new-post)
                          all-posts)
        active-post-id  (or @(rf/subscribe [:subs/pattern '{:page/active-post ?x}])
                            (-> all-posts first :post/id))
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
              (or post-route page-name)
              [:div
               (when (= active-post-id id) {:class "active"})
               [svg/right-arrow]
               [:h2 title]]
              {:post-id id :title title})]))]]
       [:div.left-close
        [:div.menu-title
         [:button.burger-btn
          {:on-click #(rf/dispatch [:evt.nav/toggle :left-menu])}
          svg/burger-icon]
         [:h1 page-title]]])
     [:div.right
      (page-post db-page-name active-post)]]))

(defn page-type-2
  "Page without left bar that displays all the posts
   Designed for `about` page"
  []
  (let [page-name @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:name ?x}}}])
        db-page-name @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:db-page-name ?x}}}])
        all-posts       (->> @(rf/subscribe [:subs.post/posts db-page-name])
                             (map #(assoc % :post/hiccup-content (h/md->hiccup (:post/md-content %))))
                             (sort-by :post/order)
                             reverse)
        new-post        {:post/id (uuid "new-post-temp-id") :post/title "New Post"}
        posts           (if @(rf/subscribe [:subs/pattern '{:app/user ?x}])
                          (conj all-posts new-post)
                          all-posts)]
    [:section.container.type-2
     {:id  (name page-name)
      :key (name page-name)}
     [:div.right
      (doall
       (for [post posts]
         (page-post db-page-name post)))]]))