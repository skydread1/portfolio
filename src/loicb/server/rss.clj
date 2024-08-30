(ns loicb.server.rss
  (:require [loicb.server.md :as md]))

(defn -main
  [& _]
  (md/blog-rss-clojure-feed))
