(ns loicb.client.core.dom.footer
  (:require [loicb.client.core.dom.hiccup :as h]))

(defn footer-comp []
  (h/post-hiccup
   [:footer#footer-contact.container
    [:h2 "Contact"]
    [:div.contact-icons
     [:div
      [:a
       {:href "https://github.com/skydread1" :rel "noreferrer" :target "_blank"}
       [:img
        {:alt "Github Logo"
         :src "/assets/github-mark-logo.png"
         :srcdark "/assets/github-mark-logo-dark-mode.png"
         :title "github"}
        nil]]]
     [:div
      [:a
       {:href "https://www.linkedin.com/in/blanchardloic" :rel "noreferrer" :target "_blank"}
       [:img
        {:alt "Linkedin Logo"
         :src "/assets/linkedin-logo.png"}
        nil]]]]]))