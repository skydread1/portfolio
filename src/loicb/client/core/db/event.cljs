(ns loicb.client.core.db.event
  (:require [loicb.common.utils :as utils :refer [toggle]]
            [loicb.client.core.md :as md]
            [re-frame.core :as rf]
            [reitit.frontend.controllers :as rfc]
            [reitit.frontend.easy :as rfe]))

;; ---------- App ----------

;; Initialization

(rf/reg-event-fx
 :evt.app/initialize
 [(rf/inject-cofx :cofx.app/local-store-theme :theme)]
 (fn [{:keys [db local-store-theme]} _]
   (let [app-theme    (or local-store-theme :dark)
         current-view (or (:app/current-view db) (rfe/push-state :home))]
     {:db         (assoc
                   db
                   :app/current-view current-view
                   :app/theme        app-theme
                   :nav.main/open? true
                   :app/posts (utils/to-indexed-maps :post/id md/posts))
      :fx         [[:fx.app/update-html-class app-theme]]})))

;; Theme (dark/light)

(rf/reg-event-fx
 :evt.app/toggle-theme
 (fn [{:keys [db]} [_]]
   (let [cur-theme (:app/theme db)
         next-theme (toggle cur-theme [:light :dark])]
     {:db (assoc db :app/theme next-theme)
      :fx [[:fx.app/set-theme-local-store next-theme]
           [:fx.app/toggle-css-class [cur-theme next-theme]]]})))

;; View

(rf/reg-event-db
 :evt.page/set-current-view
 (fn [db [_ new-match]]
   (let [old-match (-> db :app/current-view)
         match (assoc new-match :controllers (rfc/apply-controllers (:controllers old-match) new-match))]
     (assoc db :app/current-view match))))

;; ---------- Navbars ----------

(rf/reg-event-db
 :evt.nav/toggle
 (fn [db _]
   (update db :nav.main/open? not)))

(rf/reg-event-db
 :evt.nav/close-navbar
 (fn [db _]
   (assoc db :nav.main/open? false)))

