(ns loicb.client.core.dom.header
  (:require [loicb.client.core.dom.common.link :refer [internal-link]]
            [loicb.client.core.dom.common.svg :as svg]
            [re-frame.core :as rf]))

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
      [:div
       [:div.txt "About Me"]
       [svg/right-arrow]]
      {:page-name :about})]
    [:div.menu-center
     (internal-link
      [:div.menu-left
       [:div.txt "Portfolio"]
       [svg/right-arrow]]
      {:page-name :portfolio})
     [:div.menu-mid
      (theme-link
       [svg/diamond])]
     (internal-link
      [:div.menu-right
       [svg/right-arrow]
       [:div.txt "Blog"]]
      {:page-name :blog})]
    (internal-link
     [:div.menu-bottom
      [:div
       [svg/right-arrow]
       [:div.txt "Contact"]]]
     {:page-name :contact
      :with-reitit? false})]])

(defn navbar-content-mobile []
  [:nav.mobile
   {:id "mobile-nav" :class "mobile-only"}
   [:div.menu
    (internal-link
     [:div.menu-right
      [svg/right-arrow]
      [:div.txt "About Me"]]
     {:page-name :about
      :mobile? true})
    (internal-link
     [:div.menu-right
      [svg/right-arrow]
      [:div.txt "Portfolio"]]
     {:page-name :portfolio
      :mobile? true})
    (internal-link
     [:div.menu-right
      [svg/right-arrow]
      [:div.txt "Blog"]]
     {:page-name :blog
      :mobile? true})
    (internal-link
     [:div.menu-right
      [svg/right-arrow]
      [:div.txt "Contact"]]
     {:page-name :contact
      :with-reitit? false})]])

(defn navbar [navbar-content]
  (if @(rf/subscribe [:subs/pattern '{:nav.main/open? ?x}])
    (assoc-in navbar-content [1 :class] "show")
    (assoc-in navbar-content [1 :class] "hidden")))

(defn top-browser
  [nav-open?]
  (let [home-page? (= :home @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:name ?x}}}]))]
    [:div.top
     {:class "browser-only"}
     (when-not home-page?
       [:button.nav-btn.hidden
        {:on-click #(rf/dispatch [:evt.nav/toggle])}
        [svg/menu]])
     [:div.name
      [:h2 "Loïc Blanchard"]
      (when nav-open? [:h3 "Software Engineer in Functional Programming (Clojure)"])]
     (when-not home-page?
       [:button.nav-btn.hidden
        {:on-click #(rf/dispatch [:evt.app/toggle-theme])}
        [svg/diamond]])]))

(defn top-mobile
  [nav-open?]
  (let [home-page? (= :home @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:name ?x}}}]))]
    [:<>
     [:div.top
      {:class "mobile-only"}
      (when-not home-page?
        [:button.nav-btn.hidden
         {:on-click #(rf/dispatch [:evt.nav/toggle])}
         [svg/menu]])
      (when-not nav-open?
        [:div.name
         [:h2 "Loïc Blanchard"]])
      (when-not home-page?
        [:button.nav-btn.hidden
         {:on-click #(rf/dispatch [:evt.app/toggle-theme])}
         [svg/diamond]])]
     (when nav-open?
       [:div.name
        {:class "mobile-only"}
        [:h2 "Loïc Blanchard"]
        [:h3 "Software Engineer in Functional Programming (Clojure)"]])]))

(defn header-comp []
  (let [nav-open? @(rf/subscribe [:subs/pattern '{:nav.main/open? ?x}])]
    [:header.container
     (when nav-open? {:class "full-screen"})
     [top-browser nav-open?]
     [top-mobile nav-open?]
     [navbar (navbar-content-browser)]
     [navbar (navbar-content-mobile)]]))
