(ns loicb.client.test-runner
  (:require
   [figwheel.main.testing :refer-macros [run-tests-async]]
    ;; require all the namespaces that have tests in them
   [loicb.client.core.db-test]))

(defn -main [& _]
  ;; this needs to be the last statement in the main function so that it can
  ;; return the value `[:figwheel.main.async-result/wait 10000]`
  (run-tests-async 10000))