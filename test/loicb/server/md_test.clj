(ns loicb.server.md-test
  (:require
   [clojure.string :as s]
   [clojure.test :refer [deftest is testing]]
   [loicb.server.md :as sut]
   [markdown.core :as md]))

;; Test data
(def yaml-frontmatter-md
  "---
title: Test Post
date: 2024-05-12
page: blog
id: test-post
tags:
  - Clojure
  - Test
css-class: test-class
image:
  src: /test.png
  src-dark: /test-dark.png
  alt: Test image
---

This is the short description.

+++

# Main Content

This is the main content of the post.")

(deftest markdown-clj-yaml-support
  (testing "markdown-clj can extract YAML metadata"
    (let [metadata (md/md-to-meta yaml-frontmatter-md)]
      (is (map? metadata))
      (is (= "Test Post" (:title metadata)))
      (is (inst? (:date metadata)))))

  (testing "markdown-clj separates metadata from content"
    (let [result (md/md-to-html-string-with-meta yaml-frontmatter-md)
          html (:html result)]
      (is (string? html))
      (is (s/includes? html "<h1>Main Content</h1>")))))

(deftest load-post-yaml-format
  (testing "load-post can parse YAML frontmatter format"
    (let [temp-file (java.io.File/createTempFile "test-yaml" ".md")
          _ (spit temp-file yaml-frontmatter-md)
          result (sut/load-post (.getPath temp-file))]
      (.delete temp-file)
      (is (map? result))
      (is (nil? (:error result)))
      (is (= "Test Post" (:post/title result)))
      (is (= :blog (:post/page result)))
      (is (vector? (:post/date result)))
      (is (= ["Clojure" "Test"] (:post/tags result)))
      (is (map? (:post/image result)))
      (is (string? (:post/md-content result)))
      (is (string? (:post/md-content-short result))))))
