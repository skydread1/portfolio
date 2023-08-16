(ns loicb.client.core.db.fx
  (:require [loicb.client.core.db.class-utils :as cu]
            [loicb.client.core.db.localstorage :as l-storage]
            [clojure.edn :as edn]
            [re-frame.core :as rf]
            [reagent.core :as reagent]))

;; ---------- Theme ----------

;; html tag css manipulation

(rf/reg-fx
 :fx.app/update-html-class
 (fn [app-theme]
   (cu/add-class!
    (. js/document -documentElement)
    app-theme)))

(rf/reg-fx
 :fx.app/toggle-css-class
 (fn [[cur-theme next-theme]]
   (cu/toggle-class!
    (. js/document -documentElement)
    cur-theme
    next-theme)))

(rf/reg-fx
 :fx.app/scroll-to-top
 (fn []
   (reagent/after-render #(.scrollIntoView (.getElementById js/document "app")))))

;; ---------- Local Storage ----------

(rf/reg-cofx
 :cofx.app/local-store-theme
 (fn [coeffects local-store-key]
   (assoc coeffects
          :local-store-theme
          (-> local-store-key l-storage/get-item edn/read-string))))

(rf/reg-fx
 :fx.app/set-theme-local-store
 (fn [next-theme]
   (l-storage/set-item :theme next-theme)))