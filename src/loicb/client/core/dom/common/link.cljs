(ns loicb.client.core.dom.common.link
  (:require [reitit.frontend.easy :as rfe]
            [re-frame.core :as rf]))

(defn internal-link
  "Reitit internal link for the navbar.
   - mobile? is to allow separate keys in case we use different components for web and mobile."
  [content {:keys [page-name with-reitit? mobile? post-id]
            :or [with-reitit? true]}]
  [:a {:class                    (if mobile? "mobile-only" "browser-only")
       :href                     (rfe/href page-name {:post-id post-id})
       :on-click                 #(rf/dispatch [:evt.nav/set-navbar false])
       :key                      (str (when mobile? "phone-") (or post-id page-name))
       :data-reitit-handle-click with-reitit?}
   content])
