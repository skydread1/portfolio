(ns loicb.server.md-test
  (:require [clojure.test :refer [deftest testing is]]
            [loicb.server.md :as sut]))

(deftest load-posts-macro
  (testing "All marldown files are loaded successfully"
    (is (->> (sut/load-posts-macro)
             (not-any? :errors)))))
