(ns loicb.client.core.dom.page
  (:require [loicb.client.core.dom.hiccup :as h]
            [loicb.client.core.dom.common.link :refer [internal-link]]
            [re-frame.core :as rf]))

(defn post-view
  [{:post/keys [css-class image hiccup-content]}]
  (let [{:image/keys [src src-dark alt]} image
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
  [{:post/keys [articles css-class date employer image :md-content-short repos title]} link-params]
  (let [{:image/keys [src src-dark alt]} image
        src (if (= :dark @(rf/subscribe [:subs/pattern '{:app/theme ?x}]))
              src-dark src)]
    [:div.vignette-body
     {:class css-class}
     [:h2
      (internal-link
       title
       link-params)]
     [:h5.vignette-info
      (str date " | " (if employer employer "Personal Project"))]
     (when src
       [:div.image
        [:img {:src src :alt alt}]])
     [:div.content
      (h/md->hiccup md-content-short)]
     [:div.repos
      [:h3 "Git Repos"]
      (if repos
        (for [repo repos
              :let [[repo-title repo-link] repo]]
          [:a
           {:key (str "repo-" repo-title) :href repo-link :rel "noreferrer" :target "_blank"}
           [:div.repo
            [:div.img
             [:img
              {:alt "Github Logo"
               :src "/assets/github-mark-logo.png"
               :srcdark "/assets/github-mark-logo-dark-mode.png"
               :title "github"}
              nil]]
            [:div.title repo-title]]])
        "Repositories are private")]
     [:div.articles
      [:h3 "Related Articles"]
      (if articles
        (for [article articles
              :let [[article-title article-link] article]]
          [:a
           {:key (str "article-" article-title) :href article-link :rel "noreferrer" :target "_blank"}
           [:div.article
            [:div.img
             [:img
              {:alt "Loic Blog Logo"
               :src "/assets/loic-blog-logo.png"
               :srcdark "/assets/loic-blog-logo.png"
               :title article-title}
              nil]]
            [:div.title article-title]]])
        "No articles yet")]]))

(defn vignette
  "Short version of a post without the content of the post."
  [{:post/keys [id] :as post} link-params]
  [:div.vignette
   {:key (str "vignette-" id)
    :id (str "vignette-" id)}
   [vignette-view post link-params]])

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
         (vignette post {:post-id (:post/id post)
                         :page-name (or post-route page-name)})))]]))

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