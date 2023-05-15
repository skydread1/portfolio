(ns loicb.client.core.dom.page
  (:require [loicb.client.core.dom.hiccup :as h]
            [loicb.client.core.dom.common.svg :as svg]
            [reitit.frontend.easy :as rfe]
            [re-frame.core :as rf]))

(defn internal-link
  "Reitit internal link for the navbar."
  ([page-name text params]
   (internal-link page-name text params false))
  ([page-name text params mobile?]
   [:a {:class                    (if mobile? "mobile-only" "browser-only")
        :href                     (rfe/href page-name params)
        :on-click                 (when mobile? #(rf/dispatch [:evt.nav/close-navbar :left-menu]))
        :key                      (:title params)
        :data-reitit-handle-click true}
    text]))

(defn format-date
  [date]
  (-> (js/Intl.DateTimeFormat. "en-GB")
      (.format date)))

(defn post-authors
  [{:post/keys [show-dates? creation-date last-edit-date]}]
  (when show-dates?
    [:div.post-dates
     (when creation-date
       [:h4 (str (format-date creation-date) " (Audited)")])
     (when last-edit-date
       [:h4 (str (format-date last-edit-date) " (Edited)")])]))

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
              {:post-id id :title title})
             (internal-link
              (or post-route page-name)
              [:div
               (when (= active-post-id id) {:class "active"})
               [svg/right-arrow]
               [:h2 title]]
              {:post-id id :title title}
              true)]))]]
       [:div.left-close
        [:div.menu-title
         [:button.burger-btn
          {:on-click #(rf/dispatch [:evt.nav/toggle :left-menu])}
          svg/burger-icon]
         [:h1 page-title]]])
     [:div.right
      (post active-post)]]))