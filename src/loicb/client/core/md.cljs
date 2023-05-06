(ns loicb.client.core.md
  (:require-macros [loicb.server.md :as macro]))

(defn load-posts
  "Read posts config and content from the md files"
  []
  (macro/load-posts-macro))