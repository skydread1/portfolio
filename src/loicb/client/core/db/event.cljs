(ns loicb.client.core.db.event
  (:require [loicb.common.utils :as utils :refer [toggle]]
            [loicb.client.core.md :as md]
            [re-frame.core :as rf]
            [reitit.frontend.easy :as rfe]))

;; ---------- App ----------

;; Initialization

(rf/reg-event-fx
 :evt.app/initialize
 [(rf/inject-cofx :cofx.app/local-store-theme :theme)]
 (fn [{:keys [db local-store-theme]} _]
   (let [app-theme    (or local-store-theme :dark)
         current-view (or (:app/current-view db) (rfe/push-state :home))
         nav-bar-open? (or (-> db :app/current-view not)
                           (= :home (-> db :app/current-view :data :name)))]
     {:db         (assoc
                   db
                   :app/current-view current-view
                   :app/theme        app-theme
                   :nav.main/open? nav-bar-open?
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

(rf/reg-event-fx
 :evt.page/set-current-view
 (fn [{:keys [db]} [_ new-match]]
   {:db (assoc db :app/current-view new-match)
    :fx [[:fx.app/scroll-to (:fragment new-match)]]}))

;; ---------- Syntax Highlighting ----------

(rf/reg-event-fx
 :evt.app/highlight-code
 (fn [_ [_ html-id]]
   {:fx [[:fx.app/highlight-code html-id]]}))

;; ---------- Navbars ----------

(rf/reg-event-db
 :evt.nav/toggle
 (fn [db _]
   (update db :nav.main/open? not)))

(rf/reg-event-db
 :evt.nav/set-navbar
 (fn [db [_ open?]]
   (assoc db :nav.main/open? open?)))

