(ns loicb.common.validation
  "The schemas can be used for both validation and pull pattern.
   The main difference between validation schema and pull pattern schema is
   that pull pattern schems has all keys optional as we do not want to
   force the client to require any fields.
   However, for validation schema (form inputs for frontend, request params for backend),
   we often need the client to provide some mandatory fields."
  (:require [malli.core :as m]
            [malli.util :as mu]))

;;---------- Validation Schemas ----------

(def post-schema
  [:map {:closed true}
   [:post/id :string]
   [:post/order :int]
   [:post/page :keyword]
   [:post/title :string]
   [:post/date :string]
   [:post/employer {:optional true} :string]
   [:post/css-class {:optional true} :string]
   [:post/md-content :string]
   [:post/md-content-short :string]
   [:post/tags {:optional true} [:vector :string]]
   [:post/repos {:optional true} [:vector [:vector :string]]]
   [:post/articles {:optional true} [:vector [:vector :string]]]
   [:post/image
    {:optional true}
    [:map
     [:image/src :string]
     [:image/src-dark :string]
     [:image/alt :string]]]])

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

(defn validate
  "Validates the given `data` against the given `schema`.
   If the validation passes, returns the data.
   Else, returns the error data."
  [data schema]
  (let [validator (m/validator schema)]
    (if (validator data)
      data
      (mu/explain-data schema data))))

