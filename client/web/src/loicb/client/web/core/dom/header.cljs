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
          :on-click                 #(rf/dispatch [:evt.nav/close-navbar])
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
  (if @(rf/subscribe [:subs/pattern '{:app/user ?x}])
    [:a {:href "" :on-click #(rf/dispatch [:evt.user/logout])} "Logout"]
    [:a {:href "oauth/google/login"} "Login"]))

(defn navbar-content []
  [[:div.menu
    [:div.menu-top
     (internal-link
      :loicb/about
      [:div
       [:div.txt "About Me"]
       [:img
        {:alt "nav top"
         :src "assets/nav-arrow.png"}]])]
    [:div.menu-center
     (internal-link
      :loicb/home
      [:div.menu-left
       [:div.txt "Portfolio"]
       [:img
        {:alt "nav left"
         :src "assets/nav-arrow.png"}]])
     [:div.menu-mid
      (theme-link
       [:img
        {:alt "nav mid"
         :src "assets/nav-center.png"}])]
     (internal-link
      :loicb/blog
      [:div.menu-right
       [:img
        {:alt "nav right"
         :src "assets/nav-arrow.png"}]
       [:div.txt "Blog"]])]
    (internal-link
     :loicb/contact
     [:div.menu-bottom
      [:div
       [:img
        {:alt "nav bottom"
         :src "assets/nav-arrow.png"}]
       [:div.txt "Contact"]]]
     false)]])

(defn navbar []
  (->> (navbar-content) (cons :nav.show) vec)
  (if @(rf/subscribe [:subs/pattern '{:nav/navbar-open? ?x}])
    (->> (navbar-content) (cons :nav.show) vec)
    (->> (navbar-content) (cons :nav.hidden) vec)))


(defn header-comp []
  [:header.container
   [:div.top
    (when @(rf/subscribe [:subs/pattern '{:app/user ?x}])
      [svg/user-mode-logo])
    (when-let [{:user/keys [name picture]} @(rf/subscribe [:subs/pattern '{:app/user ?x}])]
      [:div
       [:img.user-pic
        {:alt (str name " profile picture")
         :src picture}]])
    (when-not @(rf/subscribe [:subs/pattern '{:nav/navbar-open? ?x}])
      [:button.nav-btn.hidden
       {:on-click #(rf/dispatch [:evt.nav/toggle-navbar])}
       [:img
        {:alt "nav toggle"
         :src "assets/nav-menu.png"}]])]
   [navbar]])