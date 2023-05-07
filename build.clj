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