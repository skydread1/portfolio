#:post{:id "clojure-lasagna-pull"
       :order 2
       :page :blog
       :title "Pull Pattern: precisely select data"
       :css-class "lasagna-pull"
       :creation-date "04/12/2022"
       :show-dates? true}
+++
# Pull Pattern: Query in deep nested data structure

[flybot-sg/lasagna-pull](https://github.com/flybot-sg/lasagna-pull) by [Luo Tian](https://github.com/robertluo) aims at precisely select from deep data structure in Clojure.

We use it in our website [flybot.sg](https://github.com/skydread1/flybot.sg) and I use it in my [portfolio website](https://www.loicblanchard.me/) as well.

In this article, I am going to
- highlight the benefits of the `pull-pattern`
- show a real case scenario from our open-source website flybot.sg

## Rational

In Clojure, it is very common to have to precisely select data in nested maps. the Clojure core `select-keys` and `get-in` functions do not allow to easily select in deeper levels of the maps with custom filters or parameters.

One of the libraries of our `lasagna-stack` is [flybot-sg/lasagna-pull](https://github.com/flybot-sg/lasagna-pull). It takes inspiration from the [datomic pull API](https://docs.datomic.com/on-prem/query/pull.html) and the library [redplanetlabs/specter](https://github.com/redplanetlabs/specter).

`lasagna-pull` aims at providing a clearer pattern that the datomic pull API.

It also allow the user to add options on the selected keys (filtering, providing params to values which are functions etc). It supports less features than the `specter` library but the syntax is more intuitive and covers all major use cases you might need to select the data you want.

Finally, a [metosin/malli](https://github.com/metosin/malli) schema can be provided to perform data validation directly using the provided pattern. This allows the client to prevent unnecessary pulling if the pattern does not match the expected shape (such as not providing the right params to a function, querying the wrong type etc).

## A query language to select deep nested structure

Selecting data in nested structure is made intuitive via a pattern that describes the data to be pulled following the shape of the data.

### Simple query cases

Here are some simple cases to showcase the syntax:

- query a map

```clojure
(require '[sg.flybot.pullable :as pull])

((pull/query '{:a ? :b {:b1 ?}})
 {:a 1 :b {:b1 2 :b2 3}})
;=> {&? {:a 1, :b {:b1 2}}}
```

- query a sequence of maps

```clojure
((pull/query '[{:a ? :b {:b1 ?}}])
 [{:a 1 :b {:b1 2 :b2 3}}
   {:a 2 :b {:b1 2 :b2 4}}])
;=> {&? [{:a 1, :b {:b1 2}} {:a 2, :b {:b1 2}}]}
```

- query nested sequences and maps

```clojure
((pull/query '[{:a ?
                :b [{:c ?}]}])
 [{:a 1 :b [{:c 2}]}
  {:a 11 :b [{:c 22}]}])
;=> {&? [{:a 1, :b [{:c 2}]} {:a 11, :b [{:c 22}]}]}
```

Let’s compare datomic pull and lasagna pull query with a simple example:

- datomic pull

```clojure
(def sample-data
  [{:a 1 :b {:b1 2 :b2 3}}
   {:a 2 :b {:b1 2 :b2 4}}])

(pull ?db
      [:a {:b [:b1]}]
      sample-data)
```

- Lasagna pull
```clojure
((pull/query '[{:a ? :b {:b1 ?}}])
 sample-data)
;=> {&? [{:a 1, :b {:b1 2}} {:a 2, :b {:b1 2}}]}
```

A few things to note

- the lasagna-pull uses a map to query a map and surround it with a vector to query a sequence which is very intuitive to use.
- The `?` is just a placeholder on where the value will be after the pull.
- the lasagna-pull returns a map with your pulled data in a key `&?`.

### Query specific keys

You might not want to fetch the whole path down to a leaf key, you might want to query that key and store it in a dedicated var. It is possible to do this providing a var name after the placeholder `?` such as `?a` for instance. The key `?a` will then be added to the result map along side the `&?` that contains the whole data structure.

Let’s have a look at an example.

Let’s say we want to fetch specific keys in addition to the whole data structure:

```clojure
((pull/query '{:a ?a
               :b {:b1 ?b1 :b2 ?}})
 {:a 1 :b {:b1 2 :b2 3}})
=> {?&  {:a 1 :b {:b1 2 :b2 3}} ;; all nested data structure
    ?a  1 ;; var a
    ?b1 2 ;; var b1
    }
```

The results now contain the logical variable we selected via `?a` and `?b1`. Note that the `:b2` key has just a `?` placeholder so it does not appear in the results map keys.

It works also for sequences:

```clojure
;; logical variable for a sequence
((pull/query '{:a [{:b1 ?} ?b1]})
 {:a [{:b1 1 :b2 2} {:b1 2} {}]})
;=> {?b1 [{:b1 1} {:b1 2} {}]
;    &?  {:a [{:b1 1} {:b1 2} {}]}}
```

Note that `'{:a [{:b1 ?b1}]}` does not work because the logical value cannot be the same for all the `b1` keys:

```clojure
((pull/query '{:a [{:b1 ?b1}]})
 {:a [{:b1 1 :b2 2} {:b1 2} {}]})
;=> {&? {:a [{:b1 1} nil nil]}} ;; not your expected result
```

## A query language to select structure with params and filters

Most of the time, just selecting nested keys is not enough. We might want to select the key if certain conditions are met only, or even pass a parameter if the value of the key is a function so we can run the function and get the value.

With library like [redplanetlabs/specter](https://github.com/redplanetlabs/specter), you have different possible transformations using diverse [macros](https://github.com/redplanetlabs/specter/wiki/List-of-Macros) which is an efficient way to select/transform data. The downside is that it introduces yet another syntax to get familiar with.

`lasagna-pull` supports most of the features at a key level.

Instead of just providing just the key you want to pull in the pattern, you can provide a list with the key as first argument and the options as the rest of the list.

The transformation is done at the same time as the selection, the pattern can be enhanced with options:

- not found

```clojure
((pull/query '{(:a :not-found ::not-found) ?}) {:b 5})
;=> {&? {:a :user/not-found}}
```

- when

```clojure
((pull/query {(:a :when even?) '?}) {:a 5})
;=> {&? {}} ;; empty because the value of :a is not even
```

- with

If the value of a query is a function, using `:with` option can invoke it and returns the result instead:

```clojure
((pull/query '{(:a :with [5]) ?}) {:a #(* % 2)})
;=> {&? {:a 10}} ;; the arg 5 was given to #(* % 2) and the result returned
```

- batch

Batched version of :with option:

```clojure
((pull/query '{(:a :batch [[5] [7]]) ?}) {:a #(* % 2)})
;=> {&? {:a (10 14)}}
```

- seq

Apply to sequence value of a query, useful for pagination:

```clojure
((pull/query '[{:a ? :b ?} ? :seq [2 3]]) [{:a 0} {:a 1} {:a 2} {:a 3} {:a 4}])
;=> {&? ({:a 2} {:a 3} {:a 4})}
```

As you can see with the different options above, the transformations are specified within the selected keys. Unlike specter however, we do not have a way to apply transformation to all the keys for instance.

## Pattern validation with Malli schema

We can optionally provide a [metosin/malli](https://github.com/metosin/malli) schema to specify the shape of the data to be pulled.

The client malli schema provided is actually internally "merged" to a internal schema that check the pattern shape so both the pattern syntax and the pattern shape are validated.

## Context

You can provide a context to the query. You can provide a `modifier` and a `finalizer`.

This context can help you gathering information from the query and apply a function on the results.

## Lasagna-pull applied to flybot.sg

We use the `lasagna pull` in both frontend and backend of [flybot.sg](https://github.com/skydread1/flybot.sg).

### Pullable API

In our backend, we have a structure representing all our data:

```clojure
;; BACKEND data structure
(defn pullable-data
  "Path to be pulled with the pull-pattern.
   The pull-pattern `:with` option will provide the params to execute the function
   before pulling it."
  [db]
  {:posts {:all          (fn [] (get-all-posts db))
           :post         (fn [post-id] (get-post db post-id))
           :new-post     (fn [post] (add-post post))
           :removed-post (fn [post-id] (delete-post post-id))}
   :pages {:all       (fn [] (get-all-pages db))
           :page      (fn [page-name] (get-page db page-name))
           :new-page  (fn [page] (add-page page))}
   :users {:all          (fn [] (get-all-users db))
           :user         (fn [id] (get-user db id))
           :removed-user (fn [id] (delete-user db id))
           :auth         {:registered (fn [id email name picture] (register-user db id email name picture))
                          :logged     (fn [id] (login-user db id))}}})
```

This resembles a REST API structure. We will detail more what the functions actually do but let’s assume they returns the pure expected data for now.

Also, the request method does not really matter, we just want to be sure that the proper params are given to the functions.

### Example: pull a post

```clojure
('&?
 (pull/query
  {:posts
   {(list :post :with ["post1-id"])
    {:post/id '?
     :post/page '?
     :post/css-class '?
     :post/creation-date '?
     :post/md-content '?
     :post/image-beside {:image/src '?
                         :image/src-dark '?
                         :image/alt '?}}}}
  (pullable-data db)))

  ;=> {:posts
  ;    {:post
  ;     {:post/id "post1-id"
  ;      :post/page :home
  ;      :post/css-class "post-1"
  ;      :post/creation-date "some-date"
  ;      :post/md-content "#Some content 1"
  ;      :post/image-beside {:image/src "https://some-image.svg"
  ;                          :image/src-dark "https://some-image-dark-mode.svg"
  ;                          :image/alt "something"}}}}
```
It is important to understand that the param `post1-id` in the pattern key `(list :post :with ["post1-id"])` was passed to `(fn [post-id] (get-post db post-id))`. The function returned the post fetched from the db. We decided to fetch all the information of the post in our pattern but we could have just fetch some of the keys only:

```clojure
('&?
 (pull/query
  {:posts
   {(list :post :with ["post1-id"])
    {:post/id '?
     :post/page '?
     :post/css-class '?
     :post/creation-date '?
     :post/md-content '?
     :post/image-beside {:image/src '?
                         :image/src-dark '?
                         :image/alt '?}}}}
  (pullable-data db)))

;=> {:posts
;    {:post
;     {:post/id "post1-id"
;      :post/page :home}}}
```

The function (fn [post-id] (get-post db post-id)) returned all the post keys but we only select the post/id and post/page.

### query with context

In reality, if you have a look at the implementation of `(fn [post-id] (get-post db post-id))`, it does not return the post directly but a map containing `response`, eventual `effects`, `error` and `session` keys:
```clojure
;; returned by get-post
{:response (db/get-post db post-id)}

;; returned by register-user
{:response user
 :effects  {:db {:payload [user]}}
 :session  {:user-id user-id}}
```

So the pattern we saw in the previous section can not work because we would need to add the `response` key in the pattern.

For function with side effects such as the one to register a user, we could write to the DB, update an atom with the session and return the post map so we can use the pattern from the previous section. This is not optimal because:
- we handle side effects while querying the data which is not easy to debug/test
- the side effects are scattered in the code which is bad practice

Our goal is to isolate the side effects (writing to the DB, updating the session) as much as possible. That is why, the functions from our `pullable-data` API returns pure data only:
- `response`: the data we want to return for the pattern key that has a function (which get params via `:with` pattern option)
- `error`: eventual read error, validation error etc returns a pure map as well (not thrown)
- `effects`: could be any side effects description to be executed later (in our case, writing to the DB)
- `session`: data to assoc to the ring session

#### context modifier and finalizer

The question is, how do we gather the different `effects` and `session` data while pulling the data.

The solution is to add a `context` with a `modifier` and a `finalizer`.

- The `modifier` allows us to conj the different `effects` in a vector and assoc the different session values to a map.
- The `finalizer` allows us to assoc the `effects` and `session` to our query result.

Here is the code:

```clojure
(defn mk-query
  "Given the pattern, make an advance query:
   - modifier: gather all the effects description in a coll
   - finalize: assoc all effects descriptions in the results."
  [pattern]
  (let [effects-acc (transient [])
        session-map (transient {})]
    (pull/query
     pattern
     (pull/context-of
      ;; modifier: gather all effects and sessions
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
      ;; finalizer: assoc the gathered side effects and sessions to the query results
      #(assoc %
              :context/effects  (persistent! effects-acc)
              :context/sessions (persistent! session-map))))))
```

#### context example

Thanks to the context we can gather pure data while querying the data and add it to the query results. Here is an example of a post deletion by a certain user:

```clojure
(mk-query
 {:posts
  {(list :removed-post :with ["post-3-id" "bob-id"])
   {:post/id '?}}}
 (pullable-data db))

;=> {'&?               {:posts
;                       {:removed-post
;                        {:post/id "post-1-id"}}}
;    :context/effects  [{:payload [[:db.fn/retractEntity [:post/id "post-1-id"]]]}]
;    :context/sessions {}}
```

Let's recap what happened:

1. The `post-3-id` and `bob-id` params were passed to the function of the key `:removed-post` in the `pullable-data`. The function performed some validation and returned:

```clojure
{:response {:post/id "post-1-id"}
 :effects  {:db {:payload [[:db.fn/retractEntity [:post/id "post-1-id"]]]}}}
```

Note how easy it is to test since all the data is pure with no side effects.

2. The query context `modifier` gathered the effects (db query to be executed to delete the post) in a vector.

3. The query context `finalizer` assoc the effects in a `context/effects` key for easy access later on.

Now that we have the the side effects description in one place, we defined an executor to execute them all at once:

```clojure
(defn mk-executors
  "Make the executor that will execute all the effects.
   Only db executors are supported now.
   - `response`: pure data pulled via the pattern to return as ring response
   - `effects-desc`: pure effects descriptions to be executed."
  [conn]
  (fn [response effects-desc]
    (reduce (fn [resp effects]
              (if (:db effects)
                (db-executor conn resp (:db effects)) ;; perform the datalevin db transaction
                resp))
            response
            effects-desc)))
```

### Integrate well with re-frame

#### with http-xhrio

If you use [day8/re-frame](https://github.com/day8/re-frame) for your frontend state management, you might use [re-frame-http-fx](https://github.com/day8/re-frame-http-fx) to wrap wraps the goog xhrio API of cljs-ajax in a `:http-xhrio` fx.

Here is an example of using the pattern inside a `:http-xhrio` fx:

```clojure
(rf/reg-event-fx
 :evt.post/remove-post
 (fn [{:keys [db]} [_ post-id]]
   (let [user-id (-> db :app/user :user/id)]
     {:http-xhrio {:method          :post
                   :uri             (base-uri "/posts/removed-post")
                   :headers         {:cookie (:user/cookie db)} ;; for react native
                   :params          {:posts
                                     {(list :removed-post :with [post-id user-id])
                                      {:post/id '?}}}
                   :format          (edn-request-format {:keywords? true})
                   :response-format (edn-response-format {:keywords? true})
                   :on-success      [:fx.http/remove-post-success]
                   :on-failure      [:fx.http/failure]}})))
```

#### with reg-sub

Instead of having multiple `re-frame/reg-sub` events, you can use only on that accepts a pull pattern:

```clojure
(rf/reg-sub
 :subs/pattern
 ;; `pattern` is the pull pattern
 ;; if `all?` is true, returns the raw pulled data (i.e. {&? ... var1 ... var2 ...})
 ;; in case of pattenr with a named-var (such as '?my-var), only the named var value is returned
 ;; in case of multiple named-var requested, returns the raw pulled data
 ;; returns nil if no match
 (fn [db [_ pattern all?]]
   (let [data ((pull/query pattern) db)]
     (when (-> data (get '&?) seq)
       (cond all?
             data
             
             (= 1 (-> data keys count))
             nil

             (= 2 (-> data keys count))
             (-> data (dissoc '&?) vals first)

             :else
             data)))))
```

Calling in your code looks like this:

```clojure
@(rf/subscribe [:subs/pattern '{:app/current-view {:data {:name ?x}}}])
```

Just go through the [web/core/dom/header.cljs](https://github.com/skydread1/flybot.sg/blob/master/client/web/src/flybot/client/web/core/dom/header.cljs) in the web frontend to see how using the pattern is convenient to fetch data from your re-frame db.

## Learn more

Feel free to have a look at the `lasagna-pull` [README](https://github.com/flybot-sg/lasagna-pull)

You can also look at the [flybot.sg](https://github.com/skydread1/flybot.sg) repo for a complete example on how to use the pull pattern in a web/mobile app.