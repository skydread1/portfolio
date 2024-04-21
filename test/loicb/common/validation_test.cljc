(ns loicb.common.validation-test
  (:require [clojure.test :refer [deftest testing is are]]
            [loicb.common.validation :as sut]
            [malli.util :as mu]))

(deftest date-valid?
  (testing "Returns true when the dates have correct format."
    (are [valid? dates] (= valid? (sut/date-valid? dates))
      ;; single date
      true ["2024-01-06"]
      false ["2018 06 21"]
      false nil
      false []
      false ["01-06"]
      false ["2023"]
      ;; dates interval
      true ["2023-01-01" "2023-02-04"]
      false ["2023-01-01" "2022-02-04"])))

(deftest all-keys-optional
  (testing "All the schema keys are made optional."
    (is (mu/equals
         [:map
          {:closed true}
          [:a {:optional true} [:vector :int]]
          [:b {:optional true} [:map [:c {:optional true}
                                      [:map [:d {:optional true} :keyword]]]]]]
         (sut/all-keys-optional
          [:map
           {:closed true}
           [:a [:vector :int]]
           [:b [:map [:c [:map [:d :keyword]]]]]])))))
