(ns loicb.client.core.dom.common.svg
  (:require [loicb.client.core.dom.common.svg.nav-diamond :as diamond]
            [loicb.client.core.dom.common.svg.nav-arrow :as arrow]
            [loicb.client.core.dom.common.svg.nav-menu :as menu]
            [re-frame.core :as rf]))

;; mobile nav

(def burger-icon
  [:svg.burger
   {:viewBox "0 0 20 20"}
   [:path
    {:clip-rule "evenodd"
     :d
     "M3 5a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 10a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 15a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1z"
     :fill-rule "evenodd"}]])

;; navigation

(def right-arrow arrow/item)
(def diamond diamond/item)
(def menu menu/item)