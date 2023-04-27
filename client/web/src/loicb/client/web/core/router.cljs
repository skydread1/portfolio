(ns loicb.client.web.core.router
  (:require [loicb.client.web.core.dom.page :refer [page]] 
            [goog.object :as gobj]
            [reitit.frontend :as rei]
            [reitit.frontend.easy :as rfe]
            [reitit.frontend.history :as rfh]
            [re-frame.core :as rf]))

(def routes
  [["/"
    {:name :loicb/home
     :page-name :home
     :title "Portfolio"
     :view #(page :home)}]

   ["/about"
    {:name :loicb/about
     :page-name :about
     :title "About me"
     :view #(page :about)}]
   
   ["/blog"
    {:name :loicb/blog
     :page-name :blog
     :title "Blog"
     :view #(page :blog)}] 

   ["#footer-contact"
    {:name :loicb/contact}]])

(def router
  (rei/router routes))

(defn on-navigate [new-match]
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