(ns loicb.client.core.dom
  (:require [loicb.client.core.db]
            [loicb.client.core.dom.glowing :refer [glowing-particles]]
            [loicb.client.core.dom.footer :refer [footer-comp]]
            [loicb.client.core.dom.header :refer [header-comp]]
            [re-frame.core :as rf]
            [reitit.frontend.easy :as rfe]))

(defn current-page []
  (let [view @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:view ?view}}}])]
    (if view
      (view)
      (rfe/href :home))))

;; App Component

(defn app []
  [:<>
   [glowing-particles]
   [header-comp]
   [current-page]
   [footer-comp]])