(ns loicb.common.test-sample-data
  "Sample data that can be used in both backend and frontend tests."
  (:require [loicb.common.utils :as u]))

;;---------- User ----------

(def bob-id "bob-id")

(def bob-user {:user/id "bob-id"
               :user/email "bob@mail.com" 
               :user/name "Bob"
               :user/picture "bob-pic"})

;;---------- Posts ----------

(def post-1-id (u/mk-uuid))
(def post-2-id (u/mk-uuid))
(def post-3-id (u/mk-uuid))
(def post-1-create-date (u/mk-date))
(def post-1-edit-date (u/mk-date))
(def post-2-create-date (u/mk-date))
(def post-3-create-date (u/mk-date))

(def post-1 {:post/id             post-1-id
             :post/page           :home
             :post/title          "Title 1"
             :post/css-class      "post-1"
             :post/md-content     "#Some content 1"
             :post/image-beside   {:image/src "https://some-image.svg"
                                   :image/src-dark "https://some-image-dark-mode.svg"
                                   :image/alt "something"}
             :post/creation-date  post-1-create-date
             :post/last-edit-date post-1-edit-date
             :post/show-dates?    true
             :post/show-authors?  true})
(def post-2 {:post/id            post-2-id
             :post/title         "Title 2"
             :post/page          :home
             :post/css-class     "post-2"
             :post/md-content    "#Some content 2"
             :post/creation-date post-2-create-date})
(def post-3 {:post/id            post-3-id
             :post/title         "Title 3"
             :post/page          :home
             :post/md-content    "Content"
             :post/creation-date post-3-create-date})

(def init-pages-and-posts
  {:posts {:all [post-1 post-2]}
   :users {:auth {:logged bob-user}}})