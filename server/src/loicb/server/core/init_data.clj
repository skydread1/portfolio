(ns loicb.server.core.init-data
  "Realistic sample data that can be used for api or figwheel developement."
  (:require [loicb.common.utils :as u]
            [clojure.edn :as edn]
            [clojure.java.io :as io]))

;; ---------- Initial Data ----------

(def user
  (vector
   (edn/read-string (or (System/getenv "ADMIN_USER")
                        (slurp "config/admin.edn")))))

(defn slurp-md
  "Slurp the sample files with the markdown."
  [page-name file-name]
  (-> (str "loicb/server/core/init_data/md_content/" page-name "/" file-name)
      io/resource
      slurp))

(def about-posts
  [#:post{:id (u/mk-uuid)
          :page :about
          :title "My Background"
          :css-class "background"
          :creation-date (u/mk-date)
          :md-content (slurp-md "about" "background.md")}
   #:post{:id (u/mk-uuid)
          :page :about
          :title "My Socials"
          :css-class "socials"
          :creation-date (u/mk-date)
          :md-content (slurp-md "about" "socials.md")}])

(def blog-posts
  [#:post{:id (u/mk-uuid)
          :page :blog
          :title "Welcome to my website"
          :css-class "welcome"
          :creation-date (u/mk-date)
          :last-edit-date (u/mk-date)
          :show-dates? true
          :show-authors? true
          :md-content (slurp-md "blog" "welcome.md")}
   #:post{:id (u/mk-uuid)
          :page :blog
          :title "Some md samples to validate good UI behaviour"
          :css-class "md-example"
          :creation-date (u/mk-date)
          :last-edit-date (u/mk-date)
          :show-dates? true
          :show-authors? true
          :md-content (slurp-md "blog" "mdsample.md")
          :image-beside #:image{:src "https://octodex.github.com/images/dojocat.jpg"
                                :src-dark "https://octodex.github.com/images/stormtroopocat.jpg"
                                :alt "Cat Logo"}}])

(def home-posts
  [#:post{:id (u/mk-uuid)
          :page :home
          :title "Project 1"
          :css-class "proj-1"
          :creation-date (u/mk-date)
          :md-content (slurp-md "home" "project1.md")
          :image-beside #:image{:src "assets/clojure-logo.svg"
                                :src-dark "assets/clojure-logo-dark-mode.svg"
                                :alt "Clojure Logo"}}
   #:post{:id (u/mk-uuid)
          :page :home
          :title "Project 2"
          :css-class "proj-2"
          :creation-date (u/mk-date)
          :md-content (slurp-md "home" "project2.md")
          :image-beside #:image{:src "assets/lambda-logo.svg"
                                :src-dark "assets/lambda-logo-dark-mode.svg"
                                :alt "Lambda Logo"}}])

(def posts
  (concat home-posts about-posts blog-posts))

(def init-data
  (concat posts user))