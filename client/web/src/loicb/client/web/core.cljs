(ns loicb.client.web.core
  (:require [loicb.client.web.core.dom :refer [app]] 
            [loicb.client.web.core.db]
            [loicb.client.web.core.router :as router]
            [reagent.dom :as rdom]
            [re-frame.core :as rf]))

(defn start-app! []
  (router/init-routes!)
  (rf/dispatch [:evt.app/initialize])
  (rdom/render [app] (. js/document (getElementById "app"))))

(start-app!)