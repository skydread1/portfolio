(ns loicb.client.core
  (:require [loicb.client.core.dom :refer [app]]
            [loicb.client.core.db]
            [loicb.client.core.router :as router]
            [reagent.dom :as rdom]
            [re-frame.core :as rf]))

(defn start-app! []
  (router/init-routes!)
  (rf/dispatch [:evt.app/initialize])
  (rdom/render [app] (. js/document (getElementById "app"))))

(start-app!)