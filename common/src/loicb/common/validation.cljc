(ns loicb.common.validation
  "The schemas can be used for both validation and pull pattern.
   The main difference between validation schema and pull pattern schema is
   that pull pattern schems has all keys optional as we do not want to
   force the client to require any fields.
   However, for validation schema (form inputs for frontend, request params for backend),
   we often need the client to provide some mandatory fields."
  (:require [malli.core :as m]
            [malli.util :as mu]
            [loicb.common.utils :as u]))

;;---------- Validation Schemas ----------

(def user-email-schema
  [:re #"^([a-zA-Z0-9]+)([\.{1}])?([a-zA-Z0-9]+)@gmail.com$"])

(def post-schema
  [:map {:closed true}
   [:post/id :uuid]
   [:post/page :keyword]
   [:post/title :string]
   [:post/css-class {:optional true} :string]
   [:post/creation-date inst?]
   [:post/last-edit-date {:optional true} inst?]
   [:post/show-dates? {:optional true} :boolean]
   [:post/show-authors? {:optional true} :boolean]
   [:post/md-content :string]
   [:post/image-beside
    {:optional true}
    [:map
     [:image/src :string]
     [:image/src-dark :string]
     [:image/alt :string]]]])

(def user-schema
  [:map {:closed true}
   [:user/id :string]
   [:user/email :string]
   [:user/name :string]
   [:user/picture :string]])

(defn all-keys-optional
  "Walk through the given `schema` and set all keys to optional."
  [schema]
  (m/walk
   schema
   (m/schema-walker
    (fn [sch]
      (if (= :map (m/type sch))
        (mu/optional-keys sch)
        sch)))))

;;---------- Pull Schemas ----------

(def api-schema
  "All keys are optional because it is just a data query schema."
  (all-keys-optional
   [:map
    {:closed true}
    [:posts
     [:map
      [:post [:=> [:cat :uuid] post-schema]]
      [:all [:=> [:cat :any] [:vector post-schema]]]
      [:new-post [:=> [:cat post-schema] post-schema]]
      [:removed-post [:=> [:cat :uuid] post-schema]]]]
    [:users
     [:map
      [:user [:=> [:cat :string] user-schema]]
      [:auth [:map
              [:registered [:=> [:cat :string user-email-schema :string :string] user-schema]]
              [:logged [:=> [:cat :any] user-schema]]]]]]]))

;;---------- Front-end validation ----------

(defn validate
  "Validates the given `data` against the given `schema`.
   If the validation passes, returns the data.
   Else, returns the error data."
  [data schema]
  (let [validator (m/validator schema)]
    (if (validator data)
      data
      (mu/explain-data schema data))))

(defn error-msg
  [errors]
  (->> errors
       :errors
       (map #(select-keys % [:path :type]))))

(defn prepare-post
  "Given a `post` from the post form and the `user-id`,
   returns a post matching server format requirements."
  [post]
  (let [temp-id?     (-> post :post/id u/temporary-id?)
        date-field   (if temp-id? :post/creation-date :post/last-edit-date)]
    (-> post
        (dissoc :post/view :post/mode :post/to-delete?)
        (update :post/id (if temp-id? (constantly (u/mk-uuid)) identity))
        (assoc date-field (u/mk-date)))))
