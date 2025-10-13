(ns loicb.server.md
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [loicb.common.validation :as v]
            [markdown.core :as md]
            [clj-rss.core :as rss]
            [tick.core :as t]))

;; ---------- IO ----------

(def directory "./src/loicb/server/content/")
(def sub-dirs
  "pages to be published."
  ["about" "blog"])

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

;; ---------- Helper Functions ----------

(defn- inst->date-string
  "Convert an inst object to YYYY-MM-DD string."
  [inst-obj]
  (.format (java.text.SimpleDateFormat. "yyyy-MM-dd") inst-obj))

(defn- ->vector
  "Convert a collection to a vector. Returns as-is if already a vector."
  [coll]
  (if (vector? coll) coll (vec coll)))

(defn- namespace-image-keys
  "Convert flat image map keys to :image/ namespaced keys."
  [image-map]
  (reduce-kv
   (fn [acc k v]
     (assoc acc (keyword "image" (name k)) v))
   {}
   image-map))

(defn normalize-metadata
  "Normalize YAML metadata to use :post/ namespaced keys and proper types.
   
   Conversions:
   - All keys → :post/ namespace
   - date (inst or coll of inst) → vector of YYYY-MM-DD strings
   - page (string) → keyword
   - tags, rss-feeds (list) → vector
   - repos, articles (list of lists) → vector of vectors
   - image map → :image/ namespaced keys"
  [metadata]
  (reduce-kv
   (fn [acc k v]
     (let [namespaced-key (if (qualified-keyword? k)
                            k
                            (keyword "post" (name k)))]
       (case namespaced-key
         ;; Image: namespace the keys
         :post/image
         (assoc acc namespaced-key (namespace-image-keys v))

         ;; Date: convert inst to vector of date strings
         :post/date
         (assoc acc namespaced-key
                (cond
                  (inst? v) [(inst->date-string v)]
                  (and (coll? v) (not (map? v))) (mapv #(if (inst? %) (inst->date-string %) %) v)
                  :else [v]))

         ;; Page: ensure keyword
         :post/page
         (assoc acc namespaced-key (if (keyword? v) v (keyword v)))

         ;; Simple vectors: tags, rss-feeds
         :post/tags
         (assoc acc namespaced-key (->vector v))

         :post/rss-feeds
         (assoc acc namespaced-key (->vector v))

         ;; Nested vectors: repos, articles
         :post/repos
         (assoc acc namespaced-key (mapv ->vector v))

         :post/articles
         (assoc acc namespaced-key (mapv ->vector v))

         ;; Default: keep as-is
         (assoc acc namespaced-key v))))
   {}
   metadata))

(defn extract-tldr
  "Extract TLDR/Summary section from markdown content.
   
   Looks for ## TLDR or ## Summary heading (case insensitive).
   Returns a map with :tldr and :full-content keys.
   The TLDR section is removed from full-content to avoid duplication."
  [markdown-content]
  (let [tldr-pattern #"(?i)##\s+(TLDR|Summary)\s*\n([\s\S]*?)(?=\n##|\z)"
        matches (re-find tldr-pattern markdown-content)]
    (if matches
      {:tldr (str/trim (nth matches 2))
       :full-content (str/trim (str/replace markdown-content tldr-pattern ""))}
      {:tldr ""
       :full-content (str/trim markdown-content)})))

(defn load-post
  "Load a markdown file with YAML frontmatter.
   
   Format:
   ---
   title: Post Title
   date: YYYY-MM-DD
   ...
   ---
   
   ## TLDR
   
   Quick summary here.
   
   ## Introduction
   
   Full article content...
   
   Returns a map with :post/ namespaced keys."
  [file-path]
  (try
    (let [raw (slurp file-path)
          ;; Parse YAML frontmatter using markdown-clj
          result (md/md-to-html-string-with-meta raw)
          metadata (or (:metadata result) {})
          ;; Extract markdown content (without YAML frontmatter)
          markdown-content (str/replace-first raw #"(?s)^---.*?---\s*" "")
          ;; Extract TLDR section and full content
          {:keys [tldr full-content]} (extract-tldr markdown-content)
          ;; Build the post map with normalized metadata
          post (-> metadata
                   normalize-metadata
                   (assoc :post/md-content full-content
                          :post/md-content-short tldr))]
      ;; Validate returns the post if valid, or explain-data if invalid
      (-> post
          (v/validate v/post-schema)))
    (catch Exception ex
      {:error (str (ex-data ex))})))

(def load-posts
  (mapv load-post all-files))

;; ---------- Macro ----------

(defmacro load-posts-macro
  []
  `~load-posts)

;; ---------- Markdown to RSS feed ----------

(defn link-relative->absolute
  [text state]
  [(str/replace text #"src=\"/" "src=\"https://www.loicb.dev/") state])

(defn blog-rss-clojure-feed
  []
  (let [clojure-blog-posts (->> (files-of "blog")
                                (mapv load-post)
                                (filter #(some #{"clojure"} (:post/rss-feeds %)))
                                (sort-by #(first (:post/date %)))
                                reverse)
        base-url "https://www.loicb.dev"
        blog-url (str base-url "/blog")
        channel {:title "Loic Blanchard - Clojure Blog Feed"
                 :link "https://www.loicb.dev"
                 :feed-url (str blog-url "/rss/clojure-feed.xml")
                 :description "Articles related to Clojure"
                 :language "en-us"
                 :lastBuildDate (t/now)}
        items (for [{:post/keys [id date md-content md-content-short title]} clojure-blog-posts]
                {:title title
                 :link (str blog-url "/" id)
                 :guid (str blog-url "/" id)
                 :pubDate (-> (t/time "18:00") (t/on (first date)) (t/in "Asia/Singapore") t/instant)
                 :description md-content-short
                 "content:encoded" (str "<![CDATA["
                                        (md/md-to-html-string md-content
                                                              :custom-transformers
                                                              [link-relative->absolute])
                                        "]]>")})]
    (->> (apply rss/channel-xml (conj items channel))
         (spit "resources/public/blog/rss/clojure-feed.xml"))))
