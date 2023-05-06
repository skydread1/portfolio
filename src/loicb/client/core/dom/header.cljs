(ns loicb.client.core.dom.header 
  (:require [loicb.client.core.dom.common.svg :as svg]
            [re-frame.core :as rf]
            [reitit.frontend.easy :as rfe]))

(defn internal-link
  "Reitit internal link for the navbar." 
  [page-name text]
  (let [current-page @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:name ?x}}}])]
    [:a {:href     (rfe/href page-name)
         :on-click #(rf/dispatch [:evt.nav/close-navbar :main])
         :class    (when (= page-name current-page) "active")}
     text]))

(defn theme-link
  "dark/light mode switch"
  [text]
  [:a {:on-click #(rf/dispatch [:evt.app/toggle-theme])}
   text])

(defn navbar-content-browser []
  [:nav.browser
   {:id "browser-nav"}
   [:div.menu
    [:div.menu-top
     (internal-link
      :about
      [:div
       [:div.txt "About Me"]
       [svg/right-arrow]])]
    [:div.menu-center
     (internal-link
      :home
      [:div.menu-left
       [:div.txt "Portfolio"]
       [svg/right-arrow]])
     [:div.menu-mid
      (theme-link
       [svg/diamond])]
     (internal-link
      :blog
      [:div.menu-right
       [svg/right-arrow]
       [:div.txt "Blog"]])]
    (internal-link
     :contact
     [:div.menu-bottom
      [:div
       [svg/right-arrow]
       [:div.txt "Contact"]]])]])

(defn navbar-content-mobile []
  [:nav.mobile
   {:id "mobile-nav"}
   [:div.menu
    (internal-link
     :about
     [:div.menu-right
      [svg/right-arrow]
      [:div.txt "About Me"]])
    (internal-link
     :home
     [:div.menu-right
      [svg/right-arrow]
      [:div.txt "Portfolio"]])
    (internal-link
     :blog
     [:div.menu-right
      [svg/right-arrow]
      [:div.txt "Blog"]])
    (internal-link
     :contact
     [:div.menu-right
      [svg/right-arrow]
      [:div.txt "Contact"]])]])

(defn navbar [navbar-content]
  (if @(rf/subscribe [:subs/pattern '{:nav.main/open? ?x}])
    (assoc-in navbar-content [1 :class] "show")
    (assoc-in navbar-content [1 :class] "hidden")))

(defn header-comp []
  (let [nav-open? @(rf/subscribe [:subs/pattern '{:nav.main/open? ?x}])]
    [:header.container
     (when nav-open? {:class "full-screen"})
     [:div.top
      [:button.nav-btn.hidden
       {:on-click #(rf/dispatch [:evt.nav/toggle :main])}
       [svg/menu]]
      [:div.name
       [:h1 "Loïc Blanchard"]
       (when nav-open? [:h2 "Software Engineer in Functional Programming (Clojure)"])]
      [:button.nav-btn.hidden
       {:on-click #(rf/dispatch [:evt.app/toggle-theme])}
       [svg/diamond]]]
     [navbar (navbar-content-browser)]
     [navbar (navbar-content-mobile)]]))