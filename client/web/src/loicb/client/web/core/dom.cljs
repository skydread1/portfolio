(ns loicb.client.web.core.dom
  (:require [loicb.client.web.core.dom.header :refer [header-comp]]
            [loicb.client.web.core.dom.page :refer [page]]
            [loicb.client.web.core.dom.footer :refer [footer-comp]]
            [loicb.client.web.core.db]
            [re-frame.core :as rf]))

(defn current-page []
  (let [view @(rf/subscribe [:subs/pattern '{:app/current-view {:data {:view ?view}}}])]
    (if view
      (view)
      (page :home))))

;; App Component

(defn app []
  [:div
   [header-comp]
   [current-page]
   [footer-comp]])