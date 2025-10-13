(ns loicb.build.rss
  (:require [clojure.string :as str]
            [loicb.build.md :as md]
            [clj-rss.core :as rss]
            [markdown.core :as markdown]
            [tick.core :as t]))

;; ---------- RSS Feed Generation ----------

(defn link-relative->absolute
  "Transform markdown image links from relative to absolute URLs."
  [text state]
  [(str/replace text #"src=\"/" "src=\"https://www.loicb.dev/") state])

(def feed-configs
  "Configuration for different RSS feeds."
  {"clojure" {:title "Loic Blanchard - Clojure Blog Feed"
              :description "Articles related to Clojure"}
   "all"     {:title "Loic Blanchard - Blog Feed"
              :description "All blog articles"}})

(defn rss-feed
  "Generate an RSS feed for a specific feed-name.
   
   Args:
   - feed-name: string identifier for the feed (e.g., \"clojure\", \"all\")
   
   The function:
   1. Loads all blog posts
   2. Filters posts that have feed-name in their :post/rss-feeds
   3. Sorts by date (newest first)
   4. Generates RSS XML
   5. Writes to resources/public/blog/rss/{feed-name}-feed.xml"
  [feed-name]
  (let [config (get feed-configs feed-name)
        _ (when-not config
            (throw (ex-info (str "Unknown feed: " feed-name)
                            {:feed-name feed-name
                             :available-feeds (keys feed-configs)})))
        filtered-posts (->> (md/files-of "blog")
                            (mapv md/load-post)
                            (filter #(some #{feed-name} (:post/rss-feeds %)))
                            (sort-by #(first (:post/date %)))
                            reverse)
        base-url "https://www.loicb.dev"
        blog-url (str base-url "/blog")
        channel {:title (:title config)
                 :link base-url
                 :feed-url (str blog-url "/rss/" feed-name "-feed.xml")
                 :description (:description config)
                 :language "en-us"
                 :lastBuildDate (t/now)}
        items (for [{:post/keys [id date md-content md-content-short title]} filtered-posts]
                {:title title
                 :link (str blog-url "/" id)
                 :guid (str blog-url "/" id)
                 :pubDate (-> (t/time "18:00") (t/on (first date)) (t/in "Asia/Singapore") t/instant)
                 :description md-content-short
                 "content:encoded" (str "<![CDATA["
                                        (markdown/md-to-html-string md-content
                                                                    :custom-transformers
                                                                    [link-relative->absolute])
                                        "]]>")})
        output-path (str "resources/public/blog/rss/" feed-name "-feed.xml")]
    (->> (apply rss/channel-xml (conj items channel))
         (spit output-path))
    (println (str "Generated RSS feed: " output-path " with " (count filtered-posts) " posts"))))

(defn rss-feeds
  "Generate all configured RSS feeds."
  []
  (doseq [feed-name (keys feed-configs)]
    (rss-feed feed-name))
  (println "All RSS feeds generated successfully!"))

(defn -main
  "Generate all configured RSS feeds."
  [& _]
  (rss-feeds))
