(ns loicb.client.core.dom.glowing)

(defn glowing-particles
  []
  [:div.glowing-container
   (for [n (range 4)]
     [:div.glowing
      [:span {:style {"--i" 1}}]
      [:span {:style {"--i" 2}}]
      [:span {:style {"--i" 3}}]])])