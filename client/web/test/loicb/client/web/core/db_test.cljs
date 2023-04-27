(ns loicb.client.web.core.db-test
  "Hot reloading regression tests for the re-frame logic.
   The tests are executed everytime a cljs file is saved.
   The results are displayed in http://localhost:9500/figwheel-extra-main/auto-testing"
  (:require [loicb.client.web.core.db]
            [loicb.client.web.core.router :as router]
            [loicb.common.test-sample-data :as s]
            [loicb.common.utils :as utils]
            [cljs.test :refer-macros [deftest is testing use-fixtures]]
            [day8.re-frame.test :as rf-test]
            [re-frame.core :as rf]
            [re-frame.db :as rf.db]))

