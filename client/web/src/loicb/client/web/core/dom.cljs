(ns loicb.client.web.core.dom
  (:require [loicb.client.web.core.db]
            [loicb.client.web.core.dom.footer :refer [footer-comp]]
            [loicb.client.web.core.dom.header :refer [header-comp]]
            [re-frame.core :as rf]
            [reitit.frontend.easy :as rfe]))

(defn current-page []
  (let [view @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:view ?view}}}])]
    (if view
      (view)
      (rfe/href :home))))

;; App Component

(defn app []
  [:div
   [header-comp]
   [current-page]
   [footer-comp]])