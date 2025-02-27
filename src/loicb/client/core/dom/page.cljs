(ns loicb.client.core.dom.page
  (:require [loicb.client.core.dom.hiccup :as h]
            [loicb.client.core.dom.common.link :refer [internal-link]]
            [re-frame.core :as rf]))

(defn date-str
  [dates]
  (apply str (interpose " - " dates)))

(defn all-tags
  [tags]
  (h/post-hiccup
   [:div.tags
    (when tags
      (for [tag tags]
        [:div.tag
         {:key (str "tag-" tag)}
         tag]))]))

(defn git-repos
  [repos]
  (when repos
    (h/post-hiccup
     [:div.links
      [:h3 "Git Repos"]
      (for [repo repos
            :let [[repo-title repo-link] repo]]
        [:a
         {:key (str "repo-" repo-title) :href repo-link :rel "noreferrer" :target "_blank"}
         [:div.link
          [:div.img
           [:img
            {:alt "Github Logo"
             :src "/assets/github-mark-logo.png"
             :srcdark "/assets/github-mark-logo-dark-mode.png"
             :title "github"}
            nil]]
          [:div.title repo-title]]])])))

(defn blog-articles
  [articles]
  (when articles
    [:div.links
     [:h3 "Related Articles"]
     (for [article articles
           :let [[article-title article-link] article]]
       [:a
        {:key (str "article-" article-title) :href article-link}
        [:div.link
         [:div.img
          [:img
           {:alt "Loic Blog Logo"
            :src "/assets/loic-blog-logo.png"
            :srcdark "/assets/loic-blog-logo.png"
            :title article-title}
           nil]]
         [:div.title article-title]]])]))

(defn post
  "Representation of a post."
  [{:post/keys [articles css-class date employer id image page md-content repos tags title]}]
  (let [{:image/keys [src src-dark alt]} image
        src (if (= :dark @(rf/subscribe [:subs/pattern '{:app/theme ?x}]))
              src-dark src)]
    (rf/dispatch [:evt.app/highlight-code id])
    [:div.post
     {:key id
      :id id}
     [:h1 title]
     [:div.post-body
      {:class css-class}
      [:h5.info
       (str (date-str date) " | "
            (if (= :blog page)
              "Blog Article"
              (if employer employer "Personal Project")))]
      (when src
        [:div.image
         [:img {:src src :alt alt}]])
      [all-tags tags]
      (when (or repos articles)
        [:div.resources
         [git-repos repos]
         [blog-articles articles]])
      (h/md->hiccup md-content)
      (when (= :blog page)
        [:div.contribute
         [:h2 "Contribute"]
         [:p "Found any typo, errors or parts that need clarification? Feel free to raise a PR on the "
          [:a {:href "https://github.com/skydread1/portfolio" :rel "noreferrer" :target "_blank"}
           "GitHub repo"]
          " and become a contributor."]])]]))

(defn vignette-link
  "Vignette link to a portfolio project article."
  [{:post/keys [articles date employer image md-content-short repos tags title]} link-params]
  (let [{:image/keys [src src-dark alt]} image
        src (if (= :dark @(rf/subscribe [:subs/pattern '{:app/theme ?x}]))
              src-dark src)]
    [:<>
     [:h2
      (internal-link
       (str "🔗 " title)
       link-params)]
     [:h5.info
      (str (date-str date) " | " (if employer employer "Personal Project"))]
     (when src
       [:div.image
        [:img {:src src :alt alt}]])
     [all-tags tags]
     (h/md->hiccup md-content-short)
     [:div.resources
      [git-repos repos]
      [blog-articles articles]]]))

(defn simple-link
  "Simple link to a blog article."
  [{:post/keys [date image tags title]}]
  (let [{:image/keys [src src-dark alt]} image
        src (if (= :dark @(rf/subscribe [:subs/pattern '{:app/theme ?x}]))
              src-dark src)]
    [:<>
     (when src
       [:div.image
        [:img {:src src :alt alt}]])
     [:div.text
      [:div
       [:h2 title]]
      [:div.info
       [:h5.info
        (str (date-str date) " | " "Loic Blanchard")]
       [all-tags tags]]]]))

(defn portfolio-page
  []
  (let [page-title @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:title ?x}}}])
        page-name "featured"
        db-page-name :blog
        post-route @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:post-route ?x}}}])
        posts       (->> @(rf/subscribe [:subs.post/posts db-page-name])
                         (sort-by #(first (:post/date %)))
                         reverse)]
    [:section.container
     {:id  page-name
      :key page-name}
     [:h1 page-title]
     [:div.post-links
      {:class "vignettes"}
      (doall
       (for [{:post/keys [id css-class home-page?] :as post} posts]
         (when home-page?
           [:div.vignette-container
            {:key (str "vignette-container-" id)
             :id (str "vignette-container-" id)}
            [:div.vignette
             {:key (str "vignette-link-" id)
              :id (str "vignette-link-" id)
              :class (str "vignette-link-" css-class)}
             [vignette-link post {:post-id id
                                  :page-name (or post-route "blog")}]]])))]]))

(defn blog-page
  []
  (let [page-title @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:title ?x}}}])
        page-name "blog"
        db-page-name :blog
        post-route @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:post-route ?x}}}])
        posts       (->> @(rf/subscribe [:subs.post/posts db-page-name])
                         (sort-by #(first (:post/date %)))
                         reverse)]
    [:section.container
     {:id  page-name
      :key page-name}
     [:h1 page-title]
     [:div.post-links
      {:class "simple-links"}
      (doall
       (for [{:post/keys [id css-class] :as post} posts]
         (internal-link
          [:div.simple-link
           {:key (str "simple-link-" id)
            :id (str "simple-link-" id)
            :class (str "simple-link-" css-class)}
           [simple-link post]]
          {:post-id id
           :page-name (or post-route "blog")})))]]))

(defn page-with-a-post
  "Page that displays the full post content.
   It contains only one post."
  []
  (let [page-name @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:name ?x}}}])
        db-page-name @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:db-page-name ?x}}}])
        posts       (->> @(rf/subscribe [:subs.post/posts db-page-name])
                         (sort-by #(first (:post/date %)))
                         reverse)
        active-post-id  @(rf/subscribe [:subs/pattern '{:app/current-view {:path-params {:post-id ?x}}}])
        active-post     (->> posts
                             (filter #(= active-post-id (:post/id %)))
                             first)]
    (if active-post
      [:section.container
       {:id  (str (name page-name) "-" active-post-id)
        :key (str (name page-name) "-" active-post-id)}
       (post active-post)]
      [:section.container
       {:id  (str (name page-name) "-" "invalid-post-id")
        :key (str (name page-name) "-" "invalid-post-id")}
       [:div.post.error
        [:h1 "There is no content at this URL"]
        (internal-link
         [:div
          [:p "Go back to Home Page"]]
         {:page-name :home})]])))

(defn about-page
  []
  (let [page-name     @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:name ?x}}}])
        db-page-name  @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:db-page-name ?x}}}])
        about-me-post (first @(rf/subscribe [:subs.post/posts db-page-name]))]
    [:section.container
     {:id  (name page-name)
      :key (name page-name)}
     (post about-me-post)]))
