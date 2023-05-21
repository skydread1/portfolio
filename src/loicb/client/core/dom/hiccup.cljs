(ns loicb.client.core.dom.hiccup
  (:require [clojure.walk :refer [postwalk]]
            [markdown-to-hiccup.core :as mth]
            [re-frame.core :as rf]))

;; ---------- Post hiccup conversion logic ----------

(defn md-dark-image
  "Extract the dark mode src from the markdown
   and add it to the hiccup props."
  [[tag {:keys [srcdark] :as props} value]] 
  (if (and srcdark (= :dark @(rf/subscribe [:subs/pattern '{:app/theme ?theme}])))
    [tag (assoc props :src (:srcdark props)) value]
    [tag props value]))

(defn link-target
  "Add '_blank' target to open external links in new tab"
  [hiccup]
  [:a
   (-> hiccup second (assoc :rel "noreferrer" :target "_blank"))
   (last hiccup)])

(defn post-hiccup
  "Given the hiccup-info, modify the hiccup."
  [content]
  (postwalk
   (fn [h]
     (cond (and (associative? h) (= :a (first h)))
           (link-target h)

           (and (vector? h) (= :img (first h)))
           (md-dark-image h)

           :else
           h))
   content))

;; ---------- Markdown to Hiccup ----------

(defn md->hiccup
  "Given some markdown as a string, returns the hiccup equivalent."
  [markdown]
  (-> markdown mth/md->hiccup mth/component post-hiccup))