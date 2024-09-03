(ns build
  (:require [clojure.tools.build.api :as b]))

;; ---------- Build Client ----------

(def build-dir "target")

(defn clean [_]
  (b/delete {:path build-dir}))

(defn js-bundle
  "Compiles the sources cljs to a single main.js"
  [_]
  (clean nil)
  (b/process {:command-args ["clojure" "-M:web/prod"]})
  (clean nil))

(defn rss-feed
  "Generate RSS feed from the markdown file for the Clojure blog posts."
  [_]
  (b/process {:command-args ["clojure" "-M:rss"]}))
