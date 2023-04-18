(ns loicb.server.core.handler.operation
  (:require [loicb.server.core.handler.operation.db :as db]))

;;---------- No Effect Ops ----------

(defn get-post
  [db post-id]
  {:response (db/get-post db post-id)})

(defn get-all-posts
  [db]
  {:response (db/get-all-posts db)})

(defn get-user
  [db user-id]
  {:response (db/get-user db user-id)})

;;---------- Ops with effects ----------

(defn add-post
  [post]
  {:response post
   :effects  {:db {:payload [post]}}})

(defn delete-post
  "Delete the post if
   - `user-id` is author of `post-id`
   - `user-id` has admin role"
  [db post-id]
  (let [post (db/get-post db post-id)]
    (if (seq post)
      {:response {:post/id post-id}
       :effects  {:db {:payload [[:db.fn/retractEntity [:post/id post-id]]]}}}
      {:error {:type    :post/deletion
               :post-id post-id}})))

(defn login-user
  [db user-id]
  (when user-id
    (if-let [{:user/keys [id] :as user} (db/get-user db user-id)]
      {:response user
       :session  {:user-id id}}
      {:error {:type    :user/login
               :user-id user-id}})))

(defn register-user
  [db user-id email name picture]
  (if-let [{:user/keys [id] :as user} (db/get-user db user-id)]
    (let [updated-user (assoc user :user/name name :user/picture picture)]
    ;; already in db so update user (name or picture could have changed).
      {:response updated-user
       :effects  {:db {:payload [updated-user]}}
       :session  {:user-id id}})
    ;; first login so create user
    (let [user #:user{:id      user-id
                      :email   email
                      :name    name
                      :picture picture}]
      {:response user
       :effects  {:db {:payload [user]}}
       :session  {:user-id user-id}})))

;;---------- Pullable data ----------

(defn pullable-data
  "Path to be pulled with the pull-pattern.
   The pull-pattern `:with` option will provide the params to execute the function
   before pulling it."
  [db session]
  {:posts {:all          (fn [_] (get-all-posts db))
           :post         (fn [post-id] (get-post db post-id))
           :new-post     (fn [post] (add-post post))
           :removed-post (fn [post-id] (delete-post db post-id))}
   :users {:user         (fn [id] (get-user db id))
           :auth         {:registered (fn [id email name picture] (register-user db id email name picture))
                          :logged     (fn [_] (login-user db (:user-id session)))}}})