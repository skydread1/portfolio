(ns loicb.server.core.handler.operation-test
  (:require [loicb.server.core :as core]
            [loicb.server.core.handler.operation :as sut] 
            [loicb.server.systems :as sys]
            [loicb.common.test-sample-data :as s]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [datalevin.core :as d]
            [robertluo.fun-map :refer [halt! touch]]))

(def test-data [s/post-1 s/post-2 s/bob-user])
(def test-system
  (-> (sys/system-config :test)
      core/system
      (dissoc :oauth2-config)
      (assoc :db-conn (sys/db-conn-system test-data))))

(defn system-fixture [f]
  (touch test-system)
  (f)
  (halt! test-system))

(use-fixtures :once system-fixture)

(deftest delete-post
  (let [db-conn (-> test-system :db-conn :conn)]
    (testing "User exits so returns post delete effects."
      (is (= {:response {:post/id s/post-1-id}
              :effects  {:db {:payload [[:db.fn/retractEntity [:post/id s/post-1-id]]]}}}
             (sut/delete-post (d/db db-conn) s/post-1-id))))))

(deftest login-user
  (let [db-conn (-> test-system :db-conn :conn)]
    (testing "No user-id so returns nil"
      (is (not (sut/login-user (d/db db-conn) nil))))
    (testing "The user exists so returns it and add to session."
      (is (= {:response s/bob-user
              :session  {:user-id    s/bob-id}}
           (sut/login-user (d/db db-conn) s/bob-id))))
    (testing "User does not exist so returns error map."
      (is (= {:error {:type    :user/login
                      :user-id ::UNKNOWN-USER}}
             (sut/login-user (d/db db-conn) ::UNKNOWN-USER))))))

(deftest register-user
  (let [db-conn (-> test-system :db-conn :conn)]
    (testing "The user exists so update it and add to session."
      (let [new-bob (assoc s/bob-user :user/name "Bobby" :user/picture "bob-new-pic")]
        (is (= {:response new-bob
                :effects  {:db {:payload [new-bob]}}
                :session  {:user-id    s/bob-id}}
               (sut/register-user (d/db db-conn) s/bob-id ::NOUSE "Bobby" "bob-new-pic")))))
    (testing "User does not exist so returns effect to add the user to db."
      (let [{:user/keys [id email name picture] :as alice} #:user{:id    "alice-id"
                                                     :email "alice@gmail.com"
                                                     :name  "Alice"
                                                     :picture "alice-pic"}]
        (is (= {:response alice
                :effects  {:db {:payload [alice]}}
                :session  {:user-id id}}
               (sut/register-user (d/db db-conn) id email name picture)))))))