(ns loicb.build.md-test
  (:require
   [clojure.string :as s]
   [clojure.test :refer [deftest is testing]]
   [loicb.build.md :as sut]
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

## TLDR

This is the short description.

## Main Content

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
      (is (clojure.string/includes? html "<h2>Main Content</h2>")))))

;; Tests for normalize-metadata
(deftest normalize-metadata-test
  (testing "namespaces all keys with :post/"
    (let [metadata {:title "Test" :id "test-id"}
          result (sut/normalize-metadata metadata)]
      (is (= "Test" (:post/title result)))
      (is (= "test-id" (:post/id result)))))

  (testing "converts inst date to vector of strings"
    (let [inst-date #inst "2024-05-12T00:00:00.000-00:00"
          result (sut/normalize-metadata {:date inst-date})]
      (is (= ["2024-05-12"] (:post/date result)))))

  (testing "converts collection of inst dates to vector of strings"
    (let [dates (list #inst "2024-05-12" #inst "2024-06-15")
          result (sut/normalize-metadata {:date dates})]
      (is (= ["2024-05-12" "2024-06-15"] (:post/date result)))))

  (testing "converts page string to keyword"
    (let [result (sut/normalize-metadata {:page "blog"})]
      (is (= :blog (:post/page result)))))

  (testing "converts tags list to vector"
    (let [tags (list "Clojure" "Web")
          result (sut/normalize-metadata {:tags tags})]
      (is (vector? (:post/tags result)))
      (is (= ["Clojure" "Web"] (:post/tags result)))))

  (testing "converts repos nested lists to vectors"
    (let [repos (list (list "Repo1" "url1") (list "Repo2" "url2"))
          result (sut/normalize-metadata {:repos repos})]
      (is (= [["Repo1" "url1"] ["Repo2" "url2"]] (:post/repos result)))))

  (testing "namespaces image keys"
    (let [image {:src "/img.png" :src-dark "/img-dark.png" :alt "Image"}
          result (sut/normalize-metadata {:image image})]
      (is (= "/img.png" (:image/src (:post/image result))))
      (is (= "/img-dark.png" (:image/src-dark (:post/image result))))
      (is (= "Image" (:image/alt (:post/image result)))))))

;; Tests for extract-tldr
(deftest extract-tldr-test
  (testing "extracts ## TLDR section"
    (let [md "## TLDR\n\nShort summary.\n\n## Introduction\n\nFull content."
          result (sut/extract-tldr md)]
      (is (= "Short summary." (:tldr result)))
      (is (clojure.string/includes? (:full-content result) "Introduction"))
      (is (not (clojure.string/includes? (:full-content result) "TLDR")))))

  (testing "extracts ## Summary section (case insensitive)"
    (let [md "## summary\n\nQuick overview.\n\n## Content\n\nMain text."
          result (sut/extract-tldr md)]
      (is (= "Quick overview." (:tldr result)))
      (is (clojure.string/includes? (:full-content result) "Content"))))

  (testing "handles missing TLDR section"
    (let [md "## Introduction\n\nNo TLDR here."
          result (sut/extract-tldr md)]
      (is (= "" (:tldr result)))
      (is (= "## Introduction\n\nNo TLDR here." (:full-content result))))))

;; Tests for load-post
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
