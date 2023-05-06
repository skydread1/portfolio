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
          :order 5
          :page :about
          :title "Recap"
          :css-class "recap"
          :creation-date (u/mk-date)
          :md-content (slurp-md "about" "recap.md")}
   #:post{:id (u/mk-uuid)
          :order 4
          :page :about
          :title "Flybot | 2020 - now"
          :css-class "flybot"
          :creation-date (u/mk-date)
          :md-content (slurp-md "about" "flybot.md")}
   #:post{:id (u/mk-uuid)
          :order 3
          :page :about
          :title "Bosch SEA | 2019"
          :css-class "bosch"
          :creation-date (u/mk-date)
          :md-content (slurp-md "about" "bosch.md")}
   #:post{:id (u/mk-uuid)
          :order 2
          :page :about
          :title "Electriduct | 2017"
          :css-class "electriduct"
          :creation-date (u/mk-date)
          :md-content (slurp-md "about" "electriduct.md")}
   #:post{:id (u/mk-uuid)
          :order 1
          :page :about
          :title "CPE Lyon | 2014-2019"
          :css-class "cpe"
          :creation-date (u/mk-date)
          :md-content (slurp-md "about" "cpe.md")}
   #:post{:id (u/mk-uuid)
          :order 0
          :page :about
          :title "Socials"
          :css-class "socials"
          :creation-date (u/mk-date)
          :md-content (slurp-md "about" "socials.md")}])

(def blog-posts
  [#:post{:id (u/mk-uuid)
          :order 0
          :page :blog
          :title "Host Clojure Full Stack on AWS"
          :css-class "clojure-aws"
          :creation-date (u/mk-date)
          :show-dates? true
          :show-authors? true
          :md-content (slurp-md "blog" "deploy_clojure_aws.md")}])

(def contact-posts
  [#:post{:id (u/mk-uuid)
          :order 0
          :page :contact
          :title "Contact me"
          :css-class "contact-me"
          :creation-date (u/mk-date)
          :md-content (slurp-md "contact" "contact_me.md")}])

(def home-posts
  [#:post{:id (u/mk-uuid)
          :order 1
          :page :home
          :title "Flybot Website"
          :css-class "flybot-website"
          :creation-date (u/mk-date)
          :md-content (slurp-md "home" "flybot_website.md")
          :image-beside #:image{:src "https://www.flybot.sg/assets/flybot-logo.png"
                                :src-dark "https://www.flybot.sg/assets/flybot-logo.png"
                                :alt "Flybot Logo"}}
   #:post{:id (u/mk-uuid)
          :order 2
          :page :home
          :title "Flybot Mobile App"
          :css-class "flybot-mobile-app"
          :creation-date (u/mk-date)
          :md-content (slurp-md "home" "flybot_mobile_app.md")
          :image-beside #:image{:src "https://www.flybot.sg/assets/flybot-logo.png"
                                :src-dark "https://www.flybot.sg/assets/flybot-logo.png"
                                :alt "Flybot Logo"}}])

(def posts
  (concat home-posts about-posts blog-posts contact-posts))

(def init-data
  (concat posts user))