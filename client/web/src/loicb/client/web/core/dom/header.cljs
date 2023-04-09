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

(defn login-link
  "Link to the server for the login/logout of a user."
  []
  (if @(rf/subscribe [:subs/pattern '{:app/user ?x}])
    [:a {:href "" :on-click #(rf/dispatch [:evt.user/logout])} "Logout"]
    [:a {:href "oauth/google/login"} "Login"]))

(defn navbar-content []
  [(internal-link :loicb/home "Home")
   (internal-link :loicb/about "About Me")
   (internal-link :loicb/blog "Blog")
   (internal-link :loicb/contact "Contact" false)
   (login-link)])

(defn navbar-mobile []
  (if @(rf/subscribe [:subs/pattern '{:nav/navbar-open? ?x}])
    (->> (navbar-content) (cons :nav.show) vec)
    (->> (navbar-content) (cons :nav.hidden) vec)))

(defn header-comp []
  [:header.container
   [:div.top
    [svg/theme-logo]
    (when @(rf/subscribe [:subs/pattern '{:app/user ?x}])
      [svg/user-mode-logo])
    (when-let [{:user/keys [name picture]} @(rf/subscribe [:subs/pattern '{:app/user ?x}])]
      [:div
       [:img.user-pic
        {:alt (str name " profile picture")
         :src picture}]])
    [:button.burger-btn.hidden {:on-click #(rf/dispatch [:evt.nav/toggle-navbar])}
     svg/burger-icon]]
   [navbar-mobile]])