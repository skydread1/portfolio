(ns loicb.client.core.router
  (:require [loicb.client.core.dom.page :refer [about-page blog-page portfolio-page page-with-a-post]]
            [goog.object :as gobj]
            [reitit.frontend :as rei]
            [reitit.frontend.easy :as rfe]
            [reitit.frontend.history :as rfh]
            [re-frame.core :as rf]))

(def routes
  [["/"
    {:name :home
     :view (constantly [:<>])}]

   ["/portfolio"
    [""
     {:name :portfolio
      :db-page-name :blog
      :post-route :blog/post
      :title "Portfolio"
      :view  portfolio-page}]]

   ["/blog"
    [""
     {:name :blog
      :db-page-name :blog
      :post-route :blog/post
      :title "Blog"
      :view  blog-page}]

    ["/:post-id"
     {:name :blog/post
      :db-page-name :blog
      :title "Blog"
      :view  page-with-a-post}]]

   ["/about"
    {:name :about
     :db-page-name :about
     :post-route :about/post
     :title "About Me"
     :view about-page}]

   ["#footer-contact"
    {:name :contact}]])

(def router
  (rei/router routes))

(defn on-navigate
  "Implementation provided by reitit to enable controllers."
  [new-match]
  (when new-match
    (rf/dispatch [:evt.page/set-current-view new-match])))

(defn ignore-anchor-click?
  "Function provided by reitit doc to ignore reitit routing on anchor link."
  [router e el uri]
  ;; Add additional check on top of the default checks
  (and (rfh/ignore-anchor-click? router e el uri)
       (not= "false" (gobj/get (.-dataset el) "reititHandleClick"))))

(defn init-routes! []
  (rfe/start!
   router
   on-navigate
   {:use-fragment false
    :ignore-anchor-click? ignore-anchor-click?}))
