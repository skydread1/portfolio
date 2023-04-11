(ns loicb.server.core.handler.auth
  (:require [aleph.http :as http]
            [cheshire.core :as cheshire]
            [clj-commons.byte-streams :as bs]
            [clojure.string :as str]
            [clojure.walk :as walk]
            [reitit.oauth2 :as reitit]))

(defn decode-key
  "Converts a train case string/keyword into a snake case keyword."
  [key]
  (if (keyword? key)
    (-> key name (str/replace "_" "-") keyword)
    (keyword (str/replace key "_" "-"))))

(defn to-snake-case
  "Recursively transforms all map keys in coll with the transform-key fn."
  [coll]
  (letfn [(transform [x] (if (map? x)
                           (into {} (map (fn [[k v]] [(decode-key k) v]) x))
                           x))]
    (walk/postwalk transform coll)))

(defn google-api-fetch-user
  [access-tokens]
  (let [google-api-url      "https://www.googleapis.com/oauth2/v2/userinfo"
        google-access-token   (-> access-tokens :google :token)
        {:keys [status body]} (try
                                @(http/request
                                  {:content-type  :json
                                   :accept        :json
                                   :url           google-api-url
                                   :method        :get
                                   :oauth-token   google-access-token})
                                (catch Exception e
                                  (ex-data e)))]
    (if (= status 200)
      (-> body
          bs/to-string
          (cheshire/parse-string true)
          to-snake-case)
       {:error {:type           :api.google/fetch-user
                :google-api-url google-api-url}})))

(defn redirect-302
  [resp landing-uri]
  (-> resp
      (assoc :status 302)
      (assoc-in [:headers "Location"] landing-uri)))

(defn authentification-middleware
  "Uses the access token returned from google oauth2 to fetch the user-info"
  [ring-handler]
  (fn [{:keys [session] :as request}]
    #_(println "======= session in authen ===========")
    #_(println session)
    (let [user-info (google-api-fetch-user (-> session :oauth2/access-tokens))
          {:keys [id email name picture]} user-info
          pattern {:users
                   {:auth
                    {(list :registered :with [id email name picture])
                     {:user/id '?}}}}
          resp (ring-handler (assoc request :params pattern))]
      (redirect-302 resp "/"))))

(defn session-user-id
  [request]
  (->> request :session :user-id))

(defn authorization-middleware
  [ring-handler]
  (fn [request]
    #_(println "======= session in autho ===========")
    #_(println (:session request))
    (let [user-id (session-user-id request)]
      (if user-id
        (ring-handler request)
        (throw (ex-info "Authorization error" {:type    :authorization
                                               :user-id user-id}))))))

(defn logout-handler
  []
  (fn [_]
    (-> {:session nil} ;; delete session
        (redirect-302 "/"))))

(defn auth-routes
  [oauth2-config]
  (reitit/reitit-routes oauth2-config))