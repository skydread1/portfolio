(ns loicb.server.core.handler.middleware
  (:require [reitit.ring.middleware.exception :as exception]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.ssl :refer [wrap-forwarded-scheme]]))

(defn handler [status message exception request]
  {:status status
   :headers {"content-type" "application/edn"}
   :body {:message message
          :data (str (ex-data exception))
          :uri (:uri request)}})

;;TODO: error not thrown if comes from redirect?
(def exception-middleware
  "When a ex-data :type is matched, create a handler with custom status and error message."
  (exception/create-exception-middleware
   {:pattern/schema            (partial handler 407 "Invalid pattern provided") ;; use status 500 for now
    :user/login                (partial handler 408 "Cannot login because user does not exist")
    :post/deletion             (partial handler 409 "Cannot delete post that does not exist")
    :api.google/fetch-user     (partial handler 412 "Could not fecth google user info")
    :authorization             (partial handler 413 "User not logged in")
    ::exception/default        (partial handler 500 "Default")}))

(defn ring-cfg
  [session-store]
  (-> site-defaults
      (assoc-in [:security :anti-forgery] false)
      (assoc-in [:session :store] session-store)
      (assoc-in [:session :cookie-attrs :same-site] :lax)))

(defn wrap-defaults-custom
  [handler session-store]
  (-> handler
      (wrap-defaults
       (ring-cfg session-store))
      (wrap-forwarded-scheme)))