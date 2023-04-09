(ns loicb.client.web.core.dom.footer)

(defn footer-comp []
  [:footer#footer-contact.container
   [:div
    [:h2 "Contact"]
    [:a
     {:rel "noreferrer",
      :target "_blank",
      :href "https://www.linkedin.com/in/blanchardloic"}
     "LinkedIn"]
    [:a
     {:rel "noreferrer",
      :target "_blank",
      :href "https://github.com/skydread1"}
     "GitHub"]]])