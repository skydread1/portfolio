(ns loicb.client.core.db.fx
  (:require [cljsjs.highlight]
            [cljsjs.highlight.langs.asciidoc]
            [cljsjs.highlight.langs.bash]
            [cljsjs.highlight.langs.clojure]
            [cljsjs.highlight.langs.clojure-repl]
            [cljsjs.highlight.langs.cpp]
            [cljsjs.highlight.langs.dockerfile]
            [cljsjs.highlight.langs.java]
            [cljsjs.highlight.langs.javascript]
            [cljsjs.highlight.langs.lisp]
            [cljsjs.highlight.langs.markdown]
            [cljsjs.highlight.langs.python]
            [clojure.edn :as edn]
            [loicb.client.core.db.class-utils :as cu]
            [loicb.client.core.db.localstorage :as l-storage]
            [re-frame.core :as rf]
            [reagent.core :as reagent]))

;; ---------- Theme ----------

;; html tag css manipulation

(rf/reg-fx
 :fx.app/update-html-class
 (fn [app-theme]
   (cu/add-class!
    (. js/document -documentElement)
    app-theme)))

(rf/reg-fx
 :fx.app/toggle-css-class
 (fn [[cur-theme next-theme]]
   (cu/toggle-class!
    (. js/document -documentElement)
    cur-theme
    next-theme)))

(rf/reg-fx
 :fx.app/scroll-to
 (fn [fragment]
   (reagent/after-render #(let [el (or (.getElementById js/document fragment)
                                       (.getElementById js/document "app"))]
                            (.scrollIntoView el)))))

;; ---------- Syntax Highlighting ----------

(rf/reg-fx
 :fx.app/highlight-code
 (fn [id]
   (reagent/after-render
    #(do (.configure js/hljs #js {:cssSelector (str "#" id " pre code")
                                  :ignoreUnescapedHTML true})
         (.highlightAll js/hljs)))))

;; ---------- Local Storage ----------

(rf/reg-cofx
 :cofx.app/local-store-theme
 (fn [coeffects local-store-key]
   (assoc coeffects
          :local-store-theme
          (-> local-store-key l-storage/get-item edn/read-string))))

(rf/reg-fx
 :fx.app/set-theme-local-store
 (fn [next-theme]
   (l-storage/set-item :theme next-theme)))
