(ns loicb.server.core
  (:require [clojure.java.io :as io]))

(defn handler [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body    (slurp (io/resource "public/index.html"))})