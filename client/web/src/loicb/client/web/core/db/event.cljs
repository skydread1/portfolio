(ns loicb.client.web.core.db.event
  (:require [loicb.common.utils :as utils :refer [toggle]]
            [loicb.common.validation :as valid]
            [ajax.edn :refer [edn-request-format edn-response-format]]
            [day8.re-frame.http-fx]
            [re-frame.core :as rf]
            [reitit.frontend.easy :as rfe]))

;; ---------- http success/failure ----------

(rf/reg-event-db
 :fx.http/failure
 [(rf/path :app/errors)]
 (fn [errors [_ result]]
    ;; result is a map containing details of the failure
   (assoc errors :failure-http-result result)))

(rf/reg-event-fx
 :fx.http/all-success
 (fn [{:keys [db]} [_ {:keys [posts users]}]]
   (let [user (-> users :auth :logged)]
     {:db (merge db {:app/posts (->> posts
                                     :all
                                     (map #(assoc % :post/mode :read))
                                     (utils/to-indexed-maps :post/id))
                     :app/user  (when (seq user) user)})
      :fx [[:fx.log/message "Got all the posts and all the Pages configurations."]
           [:fx.log/message [(if (seq user)
                               (str "User " (:user/name user) " logged in.")
                               (str "No user logged in"))]]]})))

(rf/reg-event-fx
 :fx.http/post-success
 (fn [{:keys [db]} [_ {:keys [posts]}]]
   (let [post (:post posts)]
     {:db (assoc db :form/fields post)
      :fx [[:fx.log/message ["Got the post " (:post/id post)]]]})))

(rf/reg-event-fx
 :fx.http/send-post-success
 (fn [_ [_ {:keys [posts]}]]
   (let [{:post/keys [id] :as post} (:new-post posts)]
     {:fx [[:dispatch [:evt.post/add-post post]]
           [:dispatch [:evt.post.form/clear-form]]
           [:dispatch [:evt.error/clear-errors]]
           [:dispatch [:evt.post/set-modes :read]]
           [:fx.log/message ["Post " id " sent."]]]})))

(rf/reg-event-fx
 :fx.http/remove-post-success
 (fn [{:keys [db]} [_ {:keys [posts]}]]
   (let [post-id   (-> posts :removed-post :post/id)
         user-name (-> db :app/user :user/name)]
     {:fx [[:dispatch [:evt.post/delete-post post-id]]
           [:dispatch [:evt.post.form/clear-form]]
           [:dispatch [:evt.error/clear-errors]]
           [:fx.log/message ["Post " post-id " deleted by " user-name "."]]]})))

(rf/reg-event-fx
 :fx.http/logout-success
 (fn [{:keys [db]} [_ _]]
   {:db (-> db (dissoc :app/user :user/cookie) (assoc :user/mode :reader))
    :fx [[:fx.log/message ["User logged out."]]]}))

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
                   :user/mode        :reader
                   :nav.main/open? true
                   :nav.left-menu/open? true)
      :http-xhrio {:method          :post
                   :uri             "/posts/all"
                   :params {:posts
                            {(list :all :with [nil])
                             [{:post/id '?
                               :post/order '?
                               :post/page '?
                               :post/title '?
                               :post/css-class '?
                               :post/creation-date '?
                               :post/last-edit-date '?
                               :post/show-authors? '?
                               :post/show-dates? '?
                               :post/md-content '?
                               :post/image-beside {:image/src '?
                                                   :image/src-dark '?
                                                   :image/alt '?}}]}
                            :users
                            {:auth
                             {(list :logged :with [nil])
                              {:user/id '?
                               :user/email '?
                               :user/name '?
                               :user/picture '?}}}}
                   :format          (edn-request-format {:keywords? true})
                   :response-format (edn-response-format {:keywords? true})
                   :on-success      [:fx.http/all-success]
                   :on-failure      [:fx.http/failure]}
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
   (-> db
       (assoc :app/current-view new-match)
       (dissoc :page/active-post))))

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

;; ---------- User ----------

(rf/reg-event-db
 :evt.user/toggle-mode
 [(rf/path :user/mode)]
 (fn [user-mode _]
   (toggle user-mode [:reader :editor])))

(rf/reg-event-fx
 :evt.user/logout
 (fn [_ _]
   {:http-xhrio {:method          :get
                 :uri             "/users/logout"
                 :response-format (edn-response-format {:keywords? true})
                 :on-success      [:fx.http/logout-success]
                 :on-failure      [:fx.http/failure]}}))

;; ---------- Post ----------

;; page

(rf/reg-event-db
 :evt.page/set-active-post
 (fn [db [_ post-id]]
   (assoc db :page/active-post post-id)))

;; Mode

(defn set-post-modes
  [posts mode]
  (loop [all-posts posts
         post-ids  (keys posts)]
    (if (seq post-ids)
      (recur (assoc-in all-posts [(first post-ids) :post/mode] mode)
             (rest post-ids))
      all-posts)))

(rf/reg-event-db
 :evt.post/set-modes
 [(rf/path :app/posts)]
 (fn [posts [_ mode]]
   (set-post-modes posts mode)))

(rf/reg-event-fx
 :evt.post/toggle-edit-mode
 (fn [{:keys [db]} [_ post-id]]
   (let [post (-> db :app/posts (get post-id))]
     (if (= :edit (:post/mode post))
       {:db (assoc-in db [:app/posts post-id :post/mode] :read)
        :fx [[:dispatch [:evt.post.form/clear-form]]
             [:dispatch [:evt.error/clear-errors]]]}
       {:db (assoc-in db [:app/posts post-id :post/mode] :edit)
        :fx [[:dispatch [:evt.post.form/autofill post-id]]]}))))

(rf/reg-event-db
 :evt.post/add-post
 [(rf/path :app/posts)]
 (fn [posts [_ {:post/keys [id] :as post}]]
   (assoc posts id post)))

(rf/reg-event-db
 :evt.post/delete-post
 [(rf/path :app/posts)]
 (fn [posts [_ post-id]]
   (dissoc posts post-id)))

(rf/reg-event-fx
 :evt.post/remove-post
 (fn [{:keys [db]} [_ post-id]]
   {:http-xhrio {:method          :post
                 :uri             "/posts/removed-post"
                 :params          {:posts
                                   {(list :removed-post :with [post-id])
                                    {:post/id '?}}}
                 :format          (edn-request-format {:keywords? true})
                 :response-format (edn-response-format {:keywords? true})
                 :on-success      [:fx.http/remove-post-success]
                 :on-failure      [:fx.http/failure]}}))

;; ---------- Post Form ----------

;; Form header

(rf/reg-event-db
 :evt.post.form/toggle-preview
 [(rf/path :form/fields :post/view)]
 (fn [post-view _]
   (toggle post-view [:preview :edit])))

(rf/reg-event-fx
 :evt.post.form/send-post
 (fn [{:keys [db]} _]
   (let [post (-> db :form/fields (valid/prepare-post) (valid/validate valid/post-schema))]
     (if (:errors post)
       {:fx [[:dispatch [:evt.error/set-validation-errors (valid/error-msg post)]]]}
       {:http-xhrio {:method          :post
                     :uri             "/posts/new-post"
                     :params          {:posts
                                       {(list :new-post :with [post])
                                        {:post/id '?
                                         :post/order '?
                                         :post/page '?
                                         :post/title '?
                                         :post/css-class '?
                                         :post/creation-date '?
                                         :post/last-edit-date '?
                                         :post/show-authors? '?
                                         :post/show-dates? '?
                                         :post/md-content '?
                                         :post/image-beside {:image/src '?
                                                             :image/src-dark '?
                                                             :image/alt '?}}}}
                     :format          (edn-request-format {:keywords? true})
                     :response-format (edn-response-format {:keywords? true})
                     :on-success      [:fx.http/send-post-success]
                     :on-failure      [:fx.http/failure]}}))))

;; Form body

(rf/reg-event-fx
 :evt.post.form/autofill
 (fn [{:keys [db]} [_ post-id]]
   (if (utils/temporary-id? post-id)
     {:db         (assoc db :form/fields
                         {:post/id   post-id
                          :post/order 0
                          :post/page (-> db :app/current-view :data :page-name)
                          :post/mode :edit
                          :post/creation-date (utils/mk-date)})}
     {:http-xhrio {:method          :post
                   :uri             "/posts/post"
                   :params          {:posts
                                     {(list :post :with [post-id])
                                      {:post/id '?
                                       :post/order '?
                                       :post/page '?
                                       :post/title '?
                                       :post/css-class '?
                                       :post/creation-date '?
                                       :post/last-edit-date '?
                                       :post/show-authors? '?
                                       :post/show-dates? '?
                                       :post/md-content '?
                                       :post/image-beside {:image/src '?
                                                           :image/src-dark '?
                                                           :image/alt '?}}}}
                   :format          (edn-request-format {:keywords? true})
                   :response-format (edn-response-format {:keywords? true})
                   :on-success      [:fx.http/post-success]
                   :on-failure      [:fx.http/failure]}
      :fx [[:dispatch [:evt.error/clear-errors]]]})))

(rf/reg-event-db
 :evt.post.form/set-field
 [(rf/path :form/fields)]
 (fn [post [_ id value]]
   (assoc post id value)))

(rf/reg-event-db
 :evt.form.image/set-field
 [(rf/path :form/fields :post/image-beside)]
 (fn [post [_ id value]]
   (assoc post id value)))

(rf/reg-event-db
 :evt.post.form/clear-form
 (fn [db _]
   (dissoc db :form/fields)))

;; post deletion

(rf/reg-event-db
 :evt.post-form/show-deletion
 [(rf/path :form/fields)]
 (fn [post [_ show?]]
   (merge (assoc post :post/to-delete? show?)
          (when show? {:post/view :preview}))))

;; ---------- Errors ----------

(rf/reg-event-db
 :evt.error/set-validation-errors
 [(rf/path :app/errors)]
 (fn [errors [_ validation-err]]
   (assoc errors :validation-errors validation-err)))

(rf/reg-event-db
 :evt.error/clear-errors
 (fn [db _]
   (dissoc db :app/errors)))

