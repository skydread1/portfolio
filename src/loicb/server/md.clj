(ns loicb.server.md
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [loicb.common.validation :as v]
            [markdown.core :as md]
            [clj-rss.core :as rss]
            [tick.core :as t]))

;; ---------- IO ----------

(def directory "./src/loicb/server/content/")
(def sub-dirs
  "pages to be published."
  ["about" "blog" "portfolio"])

(defn files-of
  "Returns a map with the
   - dir as key
   - coll of vectors [file-name file-path] as value"
  [dir]
  (let [dir-path   (str directory dir)
        file-names (-> dir-path io/file .listFiles)]
    (map str file-names)))

(def all-files
  "Returns the files from the different dirs."
  (->> sub-dirs
       (map files-of)
       flatten))

;; ---------- Markdown to Hiccup ----------

(defn load-post
  "Slurp the file and extract the config and the markdown.
   Code above `+++` is a clojure map of config.
   Code under `+++` is the post content as md
   Returns a map with the post info"
  [file-path]
  (let [raw (slurp file-path)
        [post-full post-short config] (->> (str/split raw #"\+\+\+")
                                           (take 3)
                                           reverse)
        post (-> config
                 edn/read-string
                 (assoc :post/md-content post-full
                        :post/md-content-short post-short))]
    (try
      (-> post
          (v/validate v/post-schema))
      (catch Exception ex
        {:error (str (ex-data ex))}))))

(comment
  (load-post "./src/loicb/server/content/about/cpe.md"))

(def load-posts
  (mapv load-post all-files))

;; ---------- Macro ----------

(defmacro load-posts-macro
  []
  `~load-posts)

;; ---------- Markdown to RSS feed ----------

(defn link-relative->absolute
  [text state]
  [(str/replace text #"src=\"/" "src=\"https://www.loicblanchard.me/") state])

(defn blog-rss-clojure-feed
  []
  (let [clojure-blog-posts (->> (files-of "blog")
                                (mapv load-post)
                                (filter #(some #{"Clojure"} (:post/tags %)))
                                (sort-by #(first (:post/date %)))
                                reverse)
        base-url "https://deploy-preview-64--loicblanchard.netlify.app" ;; to be replaced before merging to main
        blog-url (str base-url "/blog")
        channel {:title "Loic Blanchard - Clojure Blog Feed"
                 :link "https://www.loicblanchard.me"
                 :feed-url (str blog-url "/rss/clojure-feed.xml")
                 :description "Articles related to Clojure"
                 :language "en-us"
                 :lastBuildDate (t/now)}
        items (for [{:post/keys [id date md-content md-content-short title]} clojure-blog-posts]
                {:title title
                 :link (str blog-url "/" id)
                 :guid (str blog-url "/" id)
                 :pubDate (-> (t/time) (t/on (first date)) (t/in "Asia/Singapore") t/instant)
                 :description md-content-short
                 "content:encoded" (str "<![CDATA["
                                        (md/md-to-html-string md-content
                                                              :custom-transformers
                                                              [link-relative->absolute])
                                        "]]>")})]
    (->> (apply rss/channel-xml (conj items channel))
         (spit "resources/public/blog/rss/clojure-feed.xml"))))
