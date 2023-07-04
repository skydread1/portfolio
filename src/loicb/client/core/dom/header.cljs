(ns loicb.client.core.dom.header 
  (:require [loicb.client.core.dom.common.svg :as svg]
            [re-frame.core :as rf]
            [reitit.frontend.easy :as rfe]))

(defn internal-link
  "Reitit internal link for the navbar."
  ([page-name text]
   (internal-link page-name text true))
  ([page-name text with-reitit?]
   (let [current-page @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:name ?x}}}])]
     [:a {:href     (rfe/href page-name)
          :on-click #(rf/dispatch [:evt.nav/close-navbar :main])
          :class    (when (= page-name current-page) "active")
          :data-reitit-handle-click with-reitit?}
      text])))

(defn theme-link
  "dark/light mode switch"
  [text]
  [:a {:on-click #(rf/dispatch [:evt.app/toggle-theme])}
   text])

(defn navbar-content-browser []
  [:nav.browser
   {:id "browser-nav" :class "browser-only"}
   [:div.menu
    [:div.menu-top
     (internal-link
      :about
      [:div
       [:div.txt "About Me"]
       [svg/right-arrow]])]
    [:div.menu-center
     (internal-link
      :portfolio
      [:div.menu-left
       [:div.txt "Portfolio"]
       [svg/right-arrow]])
     [:div.menu-mid
      (theme-link
       [svg/diamond])]
     [:a {:href "https://blog.loicblanchard.me" :target "_blank"}
      [:div.menu-right
       [svg/right-arrow]
       [:div.txt "Blog"]]]]
    (internal-link
     :contact
     [:div.menu-bottom
      [:div
       [svg/right-arrow]
       [:div.txt "Contact"]]]
     false)]])

(defn navbar-content-mobile []
  [:nav.mobile
   {:id "mobile-nav" :class "mobile-only"}
   [:div.menu
    (internal-link
     :about
     [:div.menu-right
      [svg/right-arrow]
      [:div.txt "About Me"]])
    (internal-link
     :portfolio
     [:div.menu-right
      [svg/right-arrow]
      [:div.txt "Portfolio"]])
    [:a {:href "https://blog.loicblanchard.me" :target "_blank"}
     [:div.menu-right
      [svg/right-arrow]
      [:div.txt "Blog"]]]
    (internal-link
     :contact
     [:div.menu-right
      [svg/right-arrow]
      [:div.txt "Contact"]]
     false)]])

(defn navbar [navbar-content]
  (if @(rf/subscribe [:subs/pattern '{:nav.main/open? ?x}])
    (assoc-in navbar-content [1 :class] "show")
    (assoc-in navbar-content [1 :class] "hidden")))

(defn top-browser
  [nav-open?]
  [:div.top
   {:class "browser-only"}
   [:button.nav-btn.hidden
    {:on-click #(rf/dispatch [:evt.nav/toggle :main])}
    [svg/menu]]
   [:div.name
    [:h1 "Loïc Blanchard"]
    (when nav-open? [:h2 "Software Engineer in Functional Programming (Clojure)"])]
   [:button.nav-btn.hidden
    {:on-click #(rf/dispatch [:evt.app/toggle-theme])}
    [svg/diamond]]])

(defn top-mobile
  [nav-open?]
  [:<>
   [:div.top
    {:class "mobile-only"}
    [:button.nav-btn.hidden
     {:on-click #(rf/dispatch [:evt.nav/toggle :main])}
     [svg/menu]]
    (when-not nav-open?
      [:div.name
       [:h1 "Loïc Blanchard"]])
    [:button.nav-btn.hidden
     {:on-click #(rf/dispatch [:evt.app/toggle-theme])}
     [svg/diamond]]]
   (when nav-open?
     [:div.name
      {:class "mobile-only"}
      [:h1 "Loïc Blanchard"]
      [:h2 "Software Engineer in Functional Programming (Clojure)"]])])

(defn header-comp []
  (let [nav-open? @(rf/subscribe [:subs/pattern '{:nav.main/open? ?x}])]
    [:header.container
     (when nav-open? {:class "full-screen"})
     [top-browser nav-open?]
     [top-mobile nav-open?]
     [navbar (navbar-content-browser)]
     [navbar (navbar-content-mobile)]]))