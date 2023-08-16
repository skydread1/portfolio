(ns loicb.client.core.dom.common.svg
  (:require [loicb.client.core.dom.common.svg.nav-diamond :as diamond]
            [loicb.client.core.dom.common.svg.nav-arrow :as arrow]
            [loicb.client.core.dom.common.svg.nav-menu :as menu]))

;; navigation

(def right-arrow arrow/item)
(def diamond diamond/item)
(def menu menu/item)