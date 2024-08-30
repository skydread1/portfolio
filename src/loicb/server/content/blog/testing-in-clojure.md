#:post{:id "testing-in-clojure"
       :page :blog
       :date ["2024-08-10"]
       :title "Testing in Clojure"
       :css-class "testing-in-clojure"
       :tags ["Clojure" "Kaocha" "Malli" "Rich Comment Tests" "Instrumentation" "Data validation/generation"]
       :image #:image{:src "/assets/loic-blog-logo.png"
                      :src-dark "/assets/loic-blog-logo.png"
                      :alt "Logo referencing Aperture Science"}}
+++
Introducing some popular testing tools to developers new to Clojure. Highlight solutions for how to do unit testing with Rich Comment Tests, data validation and generative testing with Malli, running test suites and metrics with Kaocha and how to do integration testing using external containerized services.
+++
## Introduction

This article introduces effective testing libraries and methods for those new to Clojure.

We'll explore using the [kaocha](https://github.com/lambdaisland/kaocha) test runner in both REPL and terminal, along with configurations to enhance feedback. Then we will explain how tests as documentation can be done using [rich-comment-tests](https://github.com/matthewdowney/rich-comment-tests).

We will touch on how to do data validation, generation and instrumentation using [malli](https://github.com/metosin/malli).

Finally, I will talk about how I manage integrations tests with eventual external services involved.

## Test good code

### Pure functions

First of all, always remember that it is important to have as many pure functions as possible. It means, the same input passed to a function always returns the same output. This will simplify the testing and make your code more robust.

Here is an example of unpredictable **impure** logic:

```clojure
(defn fib
  "Read the Fibonacci list length to be returned from a file,
   Return the Fibonacci sequence."
  [variable]
  (when-let [n (-> (slurp "config/env.edn") edn/read-string (get variable) :length)]
    (->> (iterate (fn [[a b]] [b (+' a b)])
                  [0 1])
         (map first)
         (take n))))

(comment
  ;; env.edn has the content {:FIB 10}
  (fib :FIB) ;=> 10
  ;; env.edn is empty
  (fib :FIB) ;=> nil
  )
```

For instance, reading the `length` value from a file before computing the Fibonacci sequence is **unpredictable** for several reasons:

- the file could not have the expected value
- the file could be missing
- in prod, the env variable would be read from the system not a file so the function would always return `nil`
- what if the FIB value from the file has the wrong format.

We would need to test too many cases unrelated to the Fibonacci logic itself, which is bad practice.

The solution is to **isolate** the impure code:

```clojure
(defn fib
  "Return the Fibonacci sequence with a lenght of `n`."
  [n]
  (->> (iterate (fn [[a b]] [b (+' a b)])
                [0 1])
       (map first)
       (take n)))

^:rct/test
(comment
  (fib 10) ;=> [0 1 1 2 3 5 8 13 21 34]
  (fib 0) ;=> []
  )

(defn config<-file
  "Reads the `config/env.edn` file, gets the value of the given key `variable`
   and returns it as clojure data."
  [variable]
  (-> (slurp "config/env.edn") edn/read-string (get variable)))

(comment
  ;; env.edn contains :FIB key with value {:length 10}
  (config<-file :FIB) ;=> {:length 10}
  ;; env.edn is empty
  (config<-file :FIB) ;=> {:length nil}
  )
```

The `fib` function is now **pure** and the same input will always yield the same output. I can therefore write my unit tests and be confident of the result. You might have noticed I added `^:rct/test` above the comment block which is actually a unit test that can be run with RCT (more on this later).

The **impure** code is isolated in the `config<-file` function, which handles reading the environment variable from a file.

This may seem basic, but it's the essential first step in testing: ensuring the code is as pure as possible for easier testing is one of the strengths of **data-oriented** programming!

## Test runner: Kaocha

For all my personal and professional projects, I have used [kaocha](https://github.com/lambdaisland/kaocha) as a test-runner. 

There are 2 main ways to run the tests that developers commonly use:

- Within the **REPL** as we implement our features or fix bugs
- In the **terminal**: to verify that all tests pass or to target a specific group of tests

Here is the `deps.edn` I will use in this example:

```clojure
{:deps {org.clojure/clojure {:mvn/version "1.11.3"}
        org.slf4j/slf4j-nop {:mvn/version "2.0.15"}
        metosin/malli       {:mvn/version "0.16.1"}}
 :paths ["src"]
 :aliases
 {:dev {:extra-paths ["config" "test" "dev"]
        :extra-deps {io.github.robertluo/rich-comment-tests {:git/tag "v1.1.1", :git/sha "3f65ecb"}}}
  :test {:extra-paths ["test"]
         :extra-deps  {lambdaisland/kaocha           {:mvn/version "1.91.1392"}
                       lambdaisland/kaocha-cloverage {:mvn/version "1.1.89"}}
         :main-opts   ["-m" "kaocha.runner"]}
  :jib {:paths ["jibbit" "src"]
        :deps {io.github.atomisthq/jibbit {:git/url "https://github.com/skydread1/jibbit.git"
                                           :git/sha "bd873e028c031dbbcb95fe3f64ff51a305f75b54"}}
        :ns-default jibbit.core
        :ns-aliases {jib jibbit.core}}
  :outdated {:deps {com.github.liquidz/antq {:mvn/version "RELEASE"}}
             :main-opts ["-m" "antq.core"]}
  :cljfmt {:deps       {io.github.weavejester/cljfmt {:git/tag "0.12.0", :git/sha "434408f"}}
           :ns-default cljfmt.tool}}}
```

### Kaocha in REPL

Regarding the bindings to run the tests From the REPL, refer to your IDE documentation. I have experience using both Emacs (spacemacs distribution) and VSCode and running my tests was always straight forward. If you are starting to learn Clojure, I recommend using VSCode, as the Clojure extension [calva](https://github.com/BetterThanTomorrow/calva) is of very good quality and well documented. I’ll use VSCode in the following example.

Let’s say we have the following test namespace:

```clojure
(ns my-app.core.fib-test
  (:require [clojure.test :refer [deftest is testing]]
            [my-app.core :as sut]))

(deftest fib-test
  (testing "The Fib sequence is returned."
    (is (= [0 1 1 2 3 5 8 13 21 34]
           (sut/fib 10)))))
```

After I `jack-in` using my *dev* alias form the `deps.edn` file, I can load the `my-app.core-test` namespace and run the tests. Using Calva, the flow will be like this:

1. *ctrl+alt+c* *ctrl+alt+j*: jack-in (select the `dev` alias in my case)
2. *ctrl+alt+c* *enter* (in the `fib-test` namespace): load the ns in the REPL
3. *ctrl+alt+c* *t* (in the `fib-test` namespace): run the tests

In the REPL, we see:

```clojure
clj꞉user꞉>
; Evaluating file: fib_test.clj
#'my-app.core.fib-test/system-test
clj꞉my-app.core.fib-test꞉> 
; Running tests for the following namespaces:
;   my-app.core.fib-test
;   my-app.core.fib

; 1 tests finished, all passing 👍, ns: 1, vars: 1
```

### Kaocha in terminal

Before committing code, it's crucial to run all project tests to ensure new changes haven't broken existing functionalities.

I added a few other namespaces and some tests.

Let’s run all the tests in the terminal:

```clojure
clj -M:dev:test
Loading namespaces:  (my-app.core.cfg my-app.core.env my-app.core.fib my-app.core)
Test namespaces:  (:system :unit)
Instrumented my-app.core.cfg
Instrumented my-app.core.env
Instrumented my-app.core.fib
Instrumented my-app.core
Instrumented 4 namespaces in 0.4 seconds.
malli: instrumented 1 function vars
malli: dev-mode started
[(.)][(()(..)(..)(..))(.)(.)]
4 tests, 9 assertions, 0 failures.
```

Note the `Test namespaces: (:system :unit)`.  By default, Kaocha runs all tests. When no metadata is specified on the `deftest`, it is considered in the Kaocha `:unit` group. However, as the project grows, we might have slower tests that are system tests, load tests, stress tests etc. We can add metadata to their `deftest` in order to group them together. For instance:

```clojure
(ns my-app.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [malli.dev :as dev]
            [malli.dev.pretty :as pretty]
            [my-app.core :as sut]))

(dev/start! {:report (pretty/reporter)})

(deftest ^:system system-test ;; metadata to add this test in the `system` kaocha test group 
  (testing "The Fib sequence is returned."
    (is (= [0 1 1 2 3 5 8 13 21 34]
           (sut/system #:cfg{:app #:app{:name "app" :version "1.0.0"}
                             :fib #:fib{:length 10}})))))
```

We need to tell Kaocha when and how to run the system test. Kaocha configurations are provided in a `tests.edn` file:

```clojure
#kaocha/v1
 {:tests [{:id :system :focus-meta [:system]} ;; only system tests
          {:id :unit}]} ;; all tests
```

Then in the terminal:

```bash
clj -M:dev:test --focus :system
malli: instrumented 1 function vars
malli: dev-mode started
[(.)]
1 tests, 1 assertions, 0 failures.
```

We can add a bunch of metrics on top of the tests results. These metrics can be added via the `:plugins` keys:

```clojure
#kaocha/v1
 {:tests [{:id :system :focus-meta [:system]}
          {:id :unit}]
  :plugins [:kaocha.plugin/profiling
            :kaocha.plugin/cloverage]}
```

If I run the tests again:

```clojure
clj -M:dev:test --focus :system
Loading namespaces:  (my-app.core.cfg my-app.core.env my-app.core.fib my-app.core)
Test namespaces:  (:system :unit)
Instrumented my-app.core.cfg
Instrumented my-app.core.env
Instrumented my-app.core.fib
Instrumented my-app.core
Instrumented 4 namespaces in 0.4 seconds.
malli: instrumented 1 function vars
malli: dev-mode started
[(.)]
1 tests, 1 assertions, 0 failures.

Top 1 slowest kaocha.type/clojure.test (0.02208 seconds, 97.0% of total time)
  system
    0.02208 seconds average (0.02208 seconds / 1 tests)

Top 1 slowest kaocha.type/ns (0.01914 seconds, 84.1% of total time)
  my-app.core-test
    0.01914 seconds average (0.01914 seconds / 1 tests)

Top 1 slowest kaocha.type/var (0.01619 seconds, 71.1% of total time)
  my-app.core-test/system-test
    0.01619 seconds my_app/core_test.clj:9
Ran tests.
Writing HTML report to: /Users/loicblanchard/workspaces/clojure-proj-template/target/coverage/index.html

|-----------------+---------+---------|
|       Namespace | % Forms | % Lines |
|-----------------+---------+---------|
|     my-app.core |   44.44 |   62.50 |
| my-app.core.cfg |   69.57 |   74.07 |
| my-app.core.env |   11.11 |   44.44 |
| my-app.core.fib |  100.00 |  100.00 |
|-----------------+---------+---------|
|       ALL FILES |   55.26 |   70.59 |
|-----------------+---------+---------|
```

### Kaocha in terminal with options

There are a bunch of options to enhance the development experience such as:

```bash
clj -M:dev:test --watch --fail-fast
```

- `watch` mode makes Kaocha rerun the tests on file save.
- `fail-fast` option makes Kaocha stop running the tests when it encounters a failing test

These 2 options are very convenient for unit testing.

However, when a code base contains slower tests, if the slower tests are run first, the watch mode is not so convenient because it won’t provide instant feedback.

We saw that we can `focus` on tests with a specific metadata tag, we can also `skip` tests. Let’s pretend our `system` test is slow and we want to skip it to only run unit tests:

```bash
 clj -M:dev:test --watch --fail-fast --skip-meta :system
```

Finally, I don’t want to use the `plugins` (profiling and code coverage) on watch mode as it clutter the space in the terminal, so I want to exclude them from the report.

We can actually create another kaocha config file for our watch mode.

`tests-watch.edn`:

```clojure
#kaocha/v1
 {:tests [{:id :unit-watch :skip-meta [:system]}] ;; ignore system tests
  :watch? true ;; watch mode on
  :fail-fast? true} ;; stop running on first failure
```

Notice that there is no plugins anymore, and watch mode and fail fast options are enabled. Also, the `system` tests are skipped.

```clojure
clj -M:dev:test --config-file tests_watch.edn
SLF4J(I): Connected with provider of type [org.slf4j.nop.NOPServiceProvider]
malli: instrumented 1 function vars
malli: dev-mode started
[(.)(()(..)(..)(..))]
2 tests, 7 assertions, 0 failures.
```

We can now leave the terminal always on, change a file and save it and the tests will be rerun using all the options mentioned above.

## Documentation as unit tests: Rich Comment Tests

Another approach to unit testing is to enhance the `comment` blocks to contain tests. This means that we don’t need a test file, we can just write our tests right below our functions and it serves as both documentation and unit tests.

Going back to our first example:

```clojure
(ns my-app.core.fib)

(defn fib
  "Return the Fibonacci sequence with a lenght of `n`."
  [n]
  (->> (iterate (fn [[a b]] [b (+' a b)])
                [0 1])
       (map first)
       (take n)))

^:rct/test
(comment
  (fib 10) ;=> [0 1 1 2 3 5 8 13 21 34]
  (fib 0) ;=> []
  )
```

The `comment` block showcases example of what the `fib` could return given some inputs and the values after `;=>` are actually verified when the tests are run.

### RC Tests in the REPL

We just need to evaluate `(com.mjdowney.rich-comment-tests/run-ns-tests! *ns*)` in the namespace we want to test:

```clojure
clj꞉my-app.core-test꞉> 
; Evaluating file: fib.clj
nil
clj꞉my-app.core.fib꞉> 
(com.mjdowney.rich-comment-tests/run-ns-tests! *ns*)
; 
; Testing my-app.core.fib
; 
; Ran 1 tests containing 2 assertions.
; 0 failures, 0 errors.
{:test 1, :pass 2, :fail 0, :error 0}
```

### RC Tests in the terminal

You might wonder how to run all the RC Tests of the project. Actually, we already did that, when we ran Kaocha unit tests in the terminal.

This is possible by wrapping the RC Tests in a deftest like so:

```clojure
(ns my-app.rc-test
  "Rich Comment tests"
  (:require [clojure.test :refer [deftest testing]]
            [com.mjdowney.rich-comment-tests.test-runner :as rctr]))

(deftest ^rct rich-comment-tests
  (testing "all white box small tests"
    (rctr/run-tests-in-file-tree! :dirs #{"src"})))
```

And if we want to run just the `rct` tests, we can focus on the metadata (see the metadata in the deftest above).

```clojure
clj -M:dev:test --focus-meta :rct
```

It is possible to run the RC Tests without using Kaocha of course, refer to their doc for that.

## clojure.test vs RCT?

I personally use a mix of both. When the function is not too complex and internal (not supposed to be called by the client), I would use RCT.

For system tests, which inevitably often involve side-effects, I have a dedicated test namespace. Using `fixture` is often handy and also the tests are way more verbose which would have polluted the src namespaces with a `comment` block.

In the short example I used in this article, the project tree is as follow:

```bash
├── README.md
├── config
│   └── env.edn
├── deps.edn
├── dev
│   └── user.clj
├── jib.edn
├── project.edn
├── src
│   └── my_app
│       ├── core
│       │   ├── cfg.clj
│       │   ├── env.clj
│       │   └── fib.clj
│       └── core.clj
├── test
│   └── my_app
│       ├── core_test.clj
│       └── rc_test.clj
├── tests.edn
└── tests_watch.edn
```

`cfg.clj`, `env.clj` and `fib.clj` have RCT and `core_test.clj` has regular deftest.

A rule of thumb could be: use regular deftest if the tests require at least one of the following:

- fixtures: start and tear down resources (db, kafka, entire system etc)
- verbose setup (configs, logging etc)
- side-effects (testing the entire system, load tests, stress tests etc)

When the implementation is easy to test, using RCT is good for a combo doc+test.

## Data Validation and Generative testing

There are 2 main libraries I personally used for data validation an generative testing: [clojure/spec.alpha](https://github.com/clojure/spec.alpha) and [malli](https://github.com/metosin/malli). I will not explain in details how both work because that could be a whole article on its own. However, you can guess which one I used in my example project as you might have noticed the `instrumentation` logs when I ran the Kaocha tests: Malli.

### Malli: Data validation

Here is the config namespace that is responsible to validate the env variables passed to our hypothetical app:

```clojure
(ns my-app.core.cfg
  (:require [malli.core :as m]
            [malli.registry :as mr]
            [malli.util :as mu]))

;; ---------- Schema Registry ----------

(def domain-registry
  "Registry for malli schemas."
  {::app
   [:map {:closed true}
    [:app/name :string]
    [:app/version :string]]
   ::fib
   [:map {:closed true}
    [:fib/length :int]]})

;; ---------- Validation ----------

(mr/set-default-registry!
 (mr/composite-registry
  (m/default-schemas)
  (mu/schemas)
  domain-registry))

(def cfg-sch
  [:map {:closed true}
   [:cfg/app ::app]
   [:cfg/fib ::fib]])

(defn validate
  "Validates the given `data` against the given `schema`.
   If the validation passes, returns the data.
   Else, returns the error data."
  [data schema]
  (let [validator (m/validator schema)]
    (if (validator data)
      data
      (throw
       (ex-info "Invalid Configs Provided"
                (m/explain schema data))))))

(defn validate-cfg
  [cfg]
  (validate cfg cfg-sch))

^:rct/test
(comment
  (def cfg #:cfg{:app #:app{:name "my-app"
                            :version "1.0.0-RC1"}
                 :fib #:fib{:length 10}})

  (validate-cfg cfg) ;=>> cfg
  (validate-cfg (assoc cfg :cfg/wrong 2)) ;throws=>> some?
  )
```

Not going into too much details here but you can see that we define a `schema` that follows our data structure. In this case, my data structure I want to spec is my config map.

### Malli: Data Generation

Let’s have a look at a simple example of a test of our system which randomly generates a length and verifies that the result is indeed a sequence of numbers with `length` element:

```clojure
(ns my-app.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [malli.dev :as dev]
            [malli.dev.pretty :as pretty]
            [malli.generator :as mg]
            [my-app.core :as sut]
            [my-app.core.cfg :as cfg]))

(dev/start! {:report (pretty/reporter)})

(deftest ^:system system-test
  (testing "The Fib sequence is returned."
    (is (= [0 1 1 2 3 5 8 13 21 34]
           (sut/system #:cfg{:app #:app{:name "app" :version "1.0.0"}
                             :fib #:fib{:length 10}}))))
  (testing "No matter the length of the sequence provided, the system returns the Fib sequence."
    (let [length (mg/generate pos-int? {:size 10})
          cfg #:cfg{:app #:app{:name "app" :version "1.0.0"}
                    :fib #:fib{:length length}}
          rslt (sut/system cfg)]
      (is (cfg/validate
           rslt
           [:sequential {:min length :max length} :int])))))
```

The second `testing` highlights both data generation (the `length`) and data validation (result must be a sequence of `int` with `length` elements).

The `dev/start!` starts malli instrumentation. It automatically detects functions which have malli specs and validate it. Let’s see what it does exactly in the next section.

### Malli: Instrumentation

Earlier, we saw tests for the `core/system` functions. Here is the core namespace:

```clojure
(ns my-app.core
  (:require [my-app.core.cfg :as cfg]
            [my-app.core.env :as env]
            [my-app.core.fib :as fib]))

(defn system
  {:malli/schema
   [:=> [:cat cfg/cfg-sch] [:sequential :int]]}
  [cfg]
  (let [length (-> cfg :cfg/fib :fib/length)]
    (fib/fib length)))

(defn -main [& _]
  (let [cfg (cfg/validate-cfg #:cfg{:app (env/config<-env :APP)
                                    :fib (env/config<-env :FIB)})]
    (system cfg)))
```

The `system` function is straight forward. It takes a config map and returns the fib sequence.

Note the metadata of that function:

```clojure
{:malli/schema
   [:=> [:cat cfg/cfg-sch] [:sequential :int]]}
```

The arrow `:=>` means it is a function schema. So in this case, we expect a config as unique argument and we expect a sequence of int as returned value.

When we `instrument` our namespace, we tell malli to check the given argument and returned value and to throw an error if they do not respect the schema in the metadata. It is very convenient.

To enable the instrumentation, we call `malli.dev/start!` as you can see in the `core-test` namespace code snippet.

### When to use data validation/generation/instrumentation

Clojure is a dynamically typed language, allowing us to write functions without being constrained by rigid type definitions. This flexibility encourages rapid development, experimentation, and iteration. Thus, it makes testing a bliss because we can easily mock function inputs or provide partial inputs.

However, if we start adding type check to all functions in all namespaces (in our case with malli metadata for instance), we introduce strict typing to our entire code base and therefore all the constraints that come with it.

Personally, I recommend adding validation for the entry point of the app only. For instance, if we develop a library, we will most likely have a top level namespace called `my-app.core` or `my-app.main` with the different functions our client can call. These functions are the ones we want to validate. All the internal logic, not supposed to be called by the clients, even though they can, do not need to be spec’ed as we want to maintain the flexibility I mentioned earlier.

A second example could be that we develop an app that has a `-main` function that will be called to start our system. A system can be whatever our app needs to perform. It can start servers, connect to databases, perform batch jobs etc. Note that in that case the entry point of our program is the `-main` function. What we want to validate is that the proper params are passed to the system that our `-main` function will start. Going back to our Fib app example, our system is very simple, it just returns the Fib sequence given the length. The length is what need to be validated in our case as it is provided externally via env variable. That is why we saw that the system function had malli metadata. However, our internal function have tests but no spec to keep that dynamic language flexibility that Clojure offers.

Finally, note the distinction between `instrumentation`, that is used for development (the metadata with the function schemas) and data validation for production (call to `cfg/validate-cfg`). For overhead reasons, we don't want to instrument our functions in production, it is a development tool. However, we do want to have our system throws an error when wrong params are provided to our system, hence the call to `cfg/validate-cfg`.

## Load/stress/integration tests

In functional programming, and especially in Clojure, it is important to avoid side effects (mutations, external factors, etc) as much as we can. Of course, we cannot avoid mutations as they are inevitable: start a server, connect to a database, IOs, update frontend web state and much more. What we can do is isolate these side effects so the rest of the code base remains pure and can enjoy the flexibility and thus predictable behavior.

### Mocking data

Some might argue that we should never mock data. From my humble personal experience, this is impossible for complex apps. An app I worked on consumes messages from different kafka topics, does write/read from a datomic database, makes http calls to multiple remote servers and produces messages to several kafka topics. So if I don’t mock anything, I need to have several remote http servers in a test cluster just for testing. I need to have a real datomic database with production-like data. I need all the other apps that will produce kafka messages that my consumers will process. In other words, it is not possible.

We can mock functions using [with-redefs](https://clojuredocs.org/clojure.core/with-redefs) which is very convenient for testing. Using the clojure.test [use-fixtures](https://clojuredocs.org/clojure.test/use-fixtures) is also great to start and tear down services after the tests are done.

### Integration tests

I mentioned above, an app using datomic and kafka for instance. In my integration tests, I want to be able to produce kafka messages and I want to interact with an actual datomic db to ensure proper behavior of my app. The common approach for this is to use `embedded` versions of these services. Our test fixtures can start/delete an embedded datomic database and start/stop kafka consumers/producers as well.

What about the http calls? We can `with-redefs` those to return some valid but randomly generated values. Integration tests aim at ensuring that all components of our app work together as expected and embedded versions of external services and redefinitions of vars can make the tests predictable and suitable for CI.

I have not touch on running tests in the CI, but integration tests should be run in the CI and if all services are embedded, there should be no difficulty in setting up a pipeline.

### Load/stress tests

To be sure an app performs well under heavy load, embedded services won’t work as they are limited in terms of performance, parallel processing etc. In our example above, If I want to start lots of kafka consumers and to use a big datomic transactor to cater lots of transactions, embedded datomic and embedded kafka won’t suffice. So I have to run a datomic transactor on my machine (maybe I want the DB to be pre-populated with millions or entities as well) and I will need to run kafka on my machine as well (maybe using confluent [cp-all-in-one](https://github.com/confluentinc/cp-all-in-one) container setup). Let’s get fancy, and also run prometheus/grafana to monitor the performance of the stress tests.

Your intuition is correct, it would be a nightmare for each developer of the project to setup all services. One solution is to containerized all these services. a datomic transactor can be run in docker, confluent provides a docker-compose to run kafka zookeeper, broker, control center etc, prometheus scrapper can be run in a container as well as grafana. So providing docker-compose files in our repo so each developer can just run `docker-compose up -d` to start all necessary services is the solution I recommend.

Note that I do not containerized my clojure app so I do not have to change anything in my workflow. I deal with load/stress tests the same way I deal with my unit tests. I just start the services in the containers and my Clojure REPL as per usual.

This setup is not the only solution to load/stress tests but it is the one I successfully implemented in my project and it really helps us being efficient.

## Conclusion

I highlighted some common testing tools and methods that the Clojure community use and I explained how I personally incorporated these tools and methods to my projects. Tools are common to everybody, but how we use them is considered opinionated and will differ depending on the projects and team decision.

If you are starting your journey as a Clojure developer, I hope you can appreciate the quality of open-source testing libraries we have access to. Also, please remember that keeping things pure is the key to easy testing and debugging; a luxury not so common in the programming world. Inevitably, you will need to deal with side effects but isolate them as much as you can to make your code robust and your tests straight forward.

Finally, there are some tools I didn’t mention to keep things short so feel free to explore what the Clojure community has to offer. The last advice I would give is to not try to use too many tools or only the shiny new ones you might find. Keep things simple and evaluate if a library is worth being added to your deps.

