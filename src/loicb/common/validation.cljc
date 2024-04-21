(ns loicb.common.validation
  (:require [malli.core :as m]
            [malli.util :as mu]
            [tick.core :as t]
            [tick.alpha.interval :as t.i]))

(defn date-valid?
  "Returns true if the given the given coll of `dates` is valid."
  [[date1 date2]]
  (try
    (if date2
      (= :precedes (t.i/relation (t/date date1)
                                 (t/date date2)))
      (t/date? (t/date date1)))
    (catch #?(:clj Exception :cljs js/Error) _ false)))

;;---------- Validation Schemas ----------

(def post-schema
  [:map {:closed true}
   [:post/id :string]
   [:post/page :keyword]
   [:post/title :string]
   [:post/date [:and
                [:vector :string]
                [:fn #(date-valid? %)]]]
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

