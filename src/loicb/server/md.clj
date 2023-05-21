(ns loicb.server.md
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [loicb.common.validation :as v]))

;; ---------- IO ----------

(def directory "./src/loicb/server/content/")
(def sub-dirs ["about" "blog" "contact" "home"])

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
        [content config] (->> (str/split raw #"\+\+\+")
                              (take 2)
                              reverse)
        post (-> config
                 edn/read-string
                 (assoc :post/md-content content))]
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