(ns loicb.client.core.db.event
  (:require [loicb.common.utils :as utils :refer [toggle]]
            [loicb.client.core.md :as posts]
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
         current-view (or (:app/current-view db) (rfe/push-state :loicb/home))]
     {:db         (assoc
                   db
                   :app/current-view current-view
                   :app/theme        app-theme
                   :nav.main/open? true
                   :nav.left-menu/open? true
                   :app/posts (utils/to-indexed-maps :post/id (posts/load-posts)))
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
 (fn [db [_ navbar]]
   (case navbar
     :main (update db :nav.main/open? not)
     :left-menu (update db :nav.left-menu/open? not))))

(rf/reg-event-db
 :evt.nav/close-navbar
 (fn [db [_ navbar]]
   (case navbar
     :main (assoc db :nav.main/open? false)
     :left-menu (assoc db :nav.left-menu/open? false))))

;; ---------- Post ----------

;; page

(rf/reg-event-db
 :evt.page/set-active-post
 (fn [db [_ post-id]]
   (assoc db :page/active-post post-id)))

(rf/reg-event-db
 :evt.page/clear-active-post
 (fn [db _]
   (dissoc db :page/active-post)))

