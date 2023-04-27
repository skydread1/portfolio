(ns loicb.client.web.core.dom.header 
  (:require [loicb.client.web.core.dom.common.svg :as svg]
            [re-frame.core :as rf]
            [reitit.frontend.easy :as rfe]))

(defn internal-link
  "Reitit internal link for the navbar.
   Setting `reitit?` to false allows the use of a regular browser link (good for anchor link)."
  ([page-name text]
   (internal-link page-name text true))
  ([page-name text reitit?]
   (let [current-page @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:name ?x}}}])]
     [:a {:href                     (rfe/href page-name)
          :on-click                 #(rf/dispatch [:evt.nav/close-navbar :main])
          :class                    (when (= page-name current-page) "active")
          :data-reitit-handle-click reitit?}
      text])))

(defn theme-link
  "dark/light mode switch"
  [text]
  [:a {:on-click #(rf/dispatch [:evt.app/toggle-theme])}
   text])

(defn login-link
  "Link to the server for the login/logout of a user."
  []
  (when @(rf/subscribe [:subs/pattern '{:app/user ?x}])
    [:a {:href "" :on-click #(rf/dispatch [:evt.user/logout])} "Logout"]))

(defn navbar-content-browser []
  [:nav.browser
   {:id "browser-nav"}
   [:div.menu
    [:div.menu-top
     (internal-link
      :loicb/about
      [:div
       [:div.txt "About Me"]
       [svg/right-arrow]])]
    [:div.menu-center
     (internal-link
      :loicb/home
      [:div.menu-left
       [:div.txt "Portfolio"]
       [svg/right-arrow]])
     [:div.menu-mid
      (theme-link
       [svg/diamond])]
     (internal-link
      :loicb/blog
      [:div.menu-right
       [svg/right-arrow]
       [:div.txt "Blog"]])]
    (internal-link
     :loicb/contact
     [:div.menu-bottom
      [:div
       [svg/right-arrow]
       [:div.txt "Contact"]]]
     false)]])

(defn navbar-content-mobile []
  [:nav.mobile
   {:id "mobile-nav"}
   [:div.menu
    (theme-link
     [svg/diamond])
    (internal-link
     :loicb/about
     [:div.menu-right
      [svg/right-arrow]
      [:div.txt "About Me"]])
    (internal-link
     :loicb/home
     [:div.menu-right
      [svg/right-arrow]
      [:div.txt "Portfolio"]])
    (internal-link
     :loicb/blog
     [:div.menu-right
      [svg/right-arrow]
      [:div.txt "Blog"]])
    (internal-link
     :loicb/contact
     [:div.menu-right
      [svg/right-arrow]
      [:div.txt "Contact"]]
     false)]])

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
      (when @(rf/subscribe [:subs/pattern '{:app/user ?x}])
        [svg/user-mode-logo])
      (when-let [{:user/keys [name picture]} @(rf/subscribe [:subs/pattern '{:app/user ?x}])]
        [:div
         [:img.user-pic
          {:alt (str name " profile picture")
           :src picture}]])
      [login-link]
      [:button.nav-btn.hidden
       {:on-click #(rf/dispatch [:evt.app/toggle-theme])}
       [svg/diamond]]]
     [navbar (navbar-content-browser)]
     [navbar (navbar-content-mobile)]]))