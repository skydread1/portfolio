(ns loicb.client.core.dom.common.svg.common
  (:require [re-frame.core :as rf]))

(defn nav-menu-color
  []
  (if (= :dark @(rf/subscribe [:subs/pattern '{:app/theme ?x}]))
    "#ffffff"
    "#08080a"))