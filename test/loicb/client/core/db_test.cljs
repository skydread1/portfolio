(ns loicb.client.core.db-test
  "Hot reloading regression tests for the re-frame logic.
   The tests are executed everytime a cljs file is saved.
   The results are displayed in http://localhost:9500/figwheel-extra-main/auto-testing"
  (:require [cljs.test :refer-macros [deftest is testing use-fixtures]]
            [day8.re-frame.test :as rf-test]
            [loicb.client.core.db]
            [loicb.client.core.router :as router]
            [re-frame.core :as rf]))

;; ---------- Fixtures ----------

(use-fixtures :once
  {:before (fn [] (router/init-routes!))})

(defn test-fixtures
  "Set local storage values and initialize DB with sample data."
  []
  ;; Mock local storage store
  (rf/reg-cofx
   :cofx.app/local-store-theme
   (fn [coeffects _]
     (assoc coeffects :local-store-theme :dark)))
  
  ;; Initialize db
  (rf/dispatch [:evt.app/initialize]))

;; ---------- App ----------

(deftest initialize
  (rf-test/run-test-sync
   (test-fixtures)
   (let [current-page      (rf/subscribe [:subs/pattern '{:app/current-view {:data {:name ?x}}}])
         theme             (rf/subscribe [:subs/pattern '{:app/theme ?x}])
         main-navbar-open? (rf/subscribe [:subs/pattern '{:nav.main/open? ?x}])
         left-navbar-open? (rf/subscribe [:subs/pattern '{:nav.left-menu/open? ?x}])
         posts             (rf/subscribe [:subs.post/posts :home])]
     (rf/dispatch [:evt.app/initialize])
     (testing "Initial db state is accurate"
       (is (= :home @current-page))
       (is (= :dark @theme))
       (is (true? @main-navbar-open?))
       (is (true? @left-navbar-open?))
       (is (some? @posts))))))

(deftest theme
  (rf-test/run-test-sync
   (test-fixtures)
   (let [theme (rf/subscribe [:subs/pattern '{:app/theme ?x}])]
     (testing "Initial theme is :dark."
       (is (= :dark @theme)))

     ;; Toggle theme
     (rf/dispatch [:evt.app/toggle-theme])
     (testing "New theme is :light."
       (is (= :light @theme))))))

;; ---------- Navbar ----------

(deftest navbars
  (rf-test/run-test-sync
   (test-fixtures)
   (let [main-navbar-open? (rf/subscribe [:subs/pattern '{:nav.main/open? ?x}])
         left-navbar-open? (rf/subscribe [:subs/pattern '{:nav.left-menu/open? ?x}])]
     (testing "navbars are initially opened"
       (is (true? @main-navbar-open?))
       (is (true? @left-navbar-open?)))

     ;; Close navbar
     (rf/dispatch [:evt.nav/close-navbar :main])
     (testing "main navbar is now closed."
       (is (false? @main-navbar-open?)))

     ;; Toggle navbar
     (rf/dispatch [:evt.nav/toggle :left-menu])
     (testing "left navbar is now closed."
       (is (false? @left-navbar-open?))))))
