(ns loicb.server.core.handler
  (:require [clojure.java.io :as io]
            [datalevin.core :as d]
            [loicb.common.validation :as v]
            [loicb.server.core.handler.auth :as auth]
            [loicb.server.core.handler.middleware :as mw]
            [loicb.server.core.handler.operation :as op]
            [muuntaja.core :as m]
            [reitit.ring :as reitit]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [sg.flybot.pullable :as pull]))

(defn db-executor
  "Db transaction executor
   Execute the tranaction given
   - `conn`: db connection
   - `resp`: pure `resp` to eventually merge txn results with
   - `payload`: to be given to the datomic transaction
   - `f-merge`: fn to add the effects results to the pure `response`.
   Returns a response with eventual effects results in it."
  [conn resp {:keys [payload f-merge]}]
  (let [txn-result @(d/transact conn payload)]
    (if f-merge
      (f-merge resp txn-result)
      resp)))

(defn mk-executors
  "Make the executor that will execute all the effects.
   Only db executors are supported now.
   - `response`: pure data pulled via the pattern to return as ring response
   - `effects-desc`: pure effects desciptions to be executed."
  [conn]
  (fn [response effects-desc]
    (reduce (fn [resp effects]
              (if (:db effects)
                (db-executor conn resp (:db effects))
                resp))
            response
            effects-desc)))

(defn mk-query
  "Given the pattern, make an advance query:
   - query-wrapper: gather all the effects description in a coll
   - finalize: assoc all effects descriptions in the second value of pattern."
  [pattern]
  (let [effects-acc (transient [])
        session-map (transient {})]
    (pull/query
     pattern
     (pull/context-of
      (fn [_ [k {:keys [response effects session error] :as v}]]
        (when error
          (throw (ex-info "executor-error" error)))
        (when session
          (reduce
           (fn [res [k v]] (assoc! res k v))
           session-map
           session))
        (when effects
          (conj! effects-acc effects))
        (if response
          [k response]
          [k v]))
      #(assoc %
              :context/effects  (persistent! effects-acc)
              :context/sessions (persistent! session-map))))))

(defn saturn-handler
  "A saturn handler takes a ring request enhanced with additional keys form the injectors.
   The saturn handler is purely functional.
   The description of the side effects to be performed are returned and they will be executed later on in the executors."
  [{:keys [params body-params session db]}]
  (let [pattern (if (seq params) params body-params)
        data    (op/pullable-data db session)
        {:context/keys [effects sessions] :as resp}
        (pull/with-data-schema
          v/api-schema
          ((mk-query pattern) data))]
    {:response     (get resp '&?)
     :effects-desc effects
     :session      (merge session sessions)}))

(defn mk-ring-handler
  "Takes a seq of `injectors`, a `saturn-handler` and a seq `executors`.
   As for now, only supports db injector and db executor.
   Returns a ring-handler."
  [injectors saturn-handler executors]
  (fn [req]
    (let [sat-req (merge req ((first injectors)))
          {:keys [response effects-desc session]} (saturn-handler sat-req)
          resp (if (seq effects-desc)
                 ((first executors) response effects-desc)
                 response)]
      {:body    resp
       :headers {"content-type" "application/edn"}
       :session session})))

(defn index-handler [_]
  {:headers {"Content-Type" "text/html"}
   :body    (slurp (io/resource "public/index.html"))})

(defn app-routes
  "API routes, returns a ring-handler."
  [ring-handler oauth2-config session-store]
  (reitit/ring-handler
   (reitit/router
    (into (auth/auth-routes oauth2-config)
          [["/posts"
            ["/all"          {:post ring-handler}]
            ["/post"         {:post ring-handler}]
            ["/new-post"     {:post       ring-handler
                              :middleware [[auth/authorization-middleware]]}]
            ["/removed-post" {:post       ring-handler
                              :middleware [[auth/authorization-middleware]]}]]
           ["/users"
            ["/logout"         {:get (auth/logout-handler)}]
            ["/user"           {:post ring-handler}]
            ["/logged-in-user" {:post ring-handler}]]
           ["/oauth/google/success" {:get        ring-handler
                                     :middleware [[auth/authentification-middleware]]}]
           ["/*" {:get {:handler index-handler}}]])
    {:conflicts (constantly nil)
     :data      {:muuntaja   m/instance
                 :middleware [muuntaja/format-middleware
                              [mw/wrap-defaults-custom session-store]
                              mw/exception-middleware]}})))