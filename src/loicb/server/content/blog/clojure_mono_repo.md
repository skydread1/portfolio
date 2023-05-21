#:post{:id "clojure-mono-repo"
       :order 5
       :page :blog
       :title "Host full-stack Clojure app in a mono-repo"
       :css-class "clojure-mono-repo"
       :creation-date "16/02/2023"
       :show-dates? true}
+++

# Clojure Mono Repo example : server + 2 clients

## Context

Our app [skydread1/flybot.sg](https://github.com/skydread1/flybot.sg) is a full-stack Clojure web and mobile app.

We opted for a mono-repo to host the `server` and the 2 clients `web` and `mobile`.

Using only one `deps.edn`, we can easily starts the different parts of the app.

## Repo structure

```
├── client
│   ├── common
│   │   ├── src
│   │   │   └── flybot.client.common
│   │   └── test
│   │       └── flybot.client.common
│   ├── mobile
│   │   ├── src
│   │   │   └── flybot.client.mobile
│   │   └── test
│   │       └── flybot.client.mobile
│   └── web
│       ├── src
│       │   └── flybot.client.web
│       └── test
│           └── flybot.client.web
├── common
│   ├── src
│   │   └── flybot.common
│   └── test
│       └── flybot.common
├── server
│   ├── src
│   │   └── flybot.server
│   └── test
│       └── flybot.server
```
The `server` contains `.clj` files, the `common` the `.cljc` files and the clients the `.cljs` files.

## Deps Management.

You can have a look at the [deps.edn](https://github.com/skydread1/flybot.sg/blob/master/deps.edn).

We can use namespaced aliases in `deps.edn` to make the process clearer.

I will go through the different aliases and explain their purposes and how to I used them to develop the app.

## Common libraries

### clj and cljc deps

First, the root configuration of the deps.edn, inherited by all aliases:

```clojure
:deps    {;; both frontend and backend
           org.clojure/clojure     {:mvn/version "1.11.1"}
           metosin/malli           {:mvn/version "0.10.2"}
           metosin/reitit          {:mvn/version "0.6.0"}
           metosin/muuntaja        {:mvn/version "0.6.8"}
           sg.flybot/lasagna-pull  {:mvn/version "0.4.150"}

           ;; backend
           ring/ring-defaults      {:mvn/version "0.3.4"}
           aleph/aleph             {:mvn/version "0.6.1"}
           robertluo/fun-map       {:mvn/version "0.5.110"}
           datalevin/datalevin     {:mvn/version "0.8.5"}
           skydread1/reitit-oauth2 {:git/url "https://github.com/skydread1/reitit-oauth2.git"
                                    :sha     "c06a3be2f00d5358a50c108816fe0cbfa9f67be1"}}
 :paths   ["server/src" "common/src" "resources"]
```

The `deps` above are used in both `server/src` and `common/src` (clj and cljc files).

### Config and systems

Some of the notable deps that play a parts in how I starts my REPL, run my tests or build are:
- `datalevin`: in a `datalevin` folder, we generate dbs on system starts depending on the environment (dev backend, dev frontend)
- `robertluo/fun-map`: we use systems which are associative deps injection and very convenient for customization or using sub-systems.

We have a config EDN file `config/system.edn that is loaded by the system when the app is started
```clojure
{:prod     {:http-port       8123
            :db-uri          "datalevin/prod/flybotdb"
            :oauth2-callback "https://flybot.sg/oauth/google/callback"}
 :dev      {:http-port       8123
            :db-uri          "datalevin/dev/db"
            :oauth2-callback "http://localhost:8123/oauth/google/callback"}
 :figwheel {:db-uri          "datalevin/figwheel/db"
            :oauth2-callback "http://localhost:9500/oauth/google/callback"
            ;; "flybot-app://" for mobile, default to "/" for web
            :client-root-path "/"}
 :test     {:http-port       8100
            :db-uri          "datalevin/test/db"}}
```

The systems for dev/test can be seen in [server/src/flybot/server/systems.clj](https://github.com/skydread1/flybot.sg/blob/master/server/src/flybot/server/systems.clj). A system has a life-cycle and uses the config above to start. 

### Sample data

In the [common/test/flybot/common/test_sample_data.cljc](https://github.com/skydread1/flybot.sg/blob/master/common/test/flybot/common/test_sample_data.cljc) namespace, we have sample data that can be loaded in both backend dev system of frontend dev systems.

### IDE integration

I use the `calva` extension in VSCode to jack-in deps and figwheel REPLs.

## Server aliases

```clojure
;; JVM options to make datalevin work with java version > java8
;; alway use it
:jvm-base {:jvm-opts ["--add-opens=java.base/java.nio=ALL-UNNAMED"
                      "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED"]}

;; CLJ paths for the backend systems and tests
:server/dev {:extra-paths ["config" "target" "common/test" "server/test"]}

;; Run clj tests
:server/test {:extra-paths ["common/test" "server/test"]
              :extra-deps  {lambdaisland/kaocha {:mvn/version "1.80.1274"}}
              :main-opts   ["-m" "kaocha.runner"]}
```

### Prerequisites

if we want to have a UI, we can generate the main.js bundle via `clj T:build js-bundle`

### Dev

Starting just the backend REPL can then be done like so:
```
clj -A:jvm-base:server/dev
```

### Test

Running the backend tests in the terminal can be done like so:
```
clj -A:jvm-base:server/test
```

## Client common aliases

```clojure
;;---------- CLIENT ----------
;; use for both web and mobile
:client  {:extra-deps {com.bhauman/figwheel-main             {:mvn/version "0.2.18"}
                       org.clojure/clojurescript             {:mvn/version "1.11.60"}
                       reagent/reagent                       {:mvn/version "1.2.0"}
                       cljsjs/react                          {:mvn/version "18.2.0-1"}
                       cljsjs/react-dom                      {:mvn/version "18.2.0-1"}
                       markdown-to-hiccup/markdown-to-hiccup {:mvn/version "0.6.2"}
                       cljs-ajax/cljs-ajax                   {:mvn/version "0.8.4"}
                       re-frame/re-frame                     {:mvn/version "1.3.0"}
                       day8.re-frame/http-fx                 {:mvn/version "0.2.4"}
                       day8.re-frame/test                    {:mvn/version "0.1.5"}}
          :extra-paths ["client/web/src" "client/common/src"
                        "client/web/test" "common/test"
                        "config" "target"]}
```

The `:client` alias contains the frontend libraries common to web and react native.

The extra-paths contains the `cljs` files.

We can note the `client/common/src` path that contain most of the `re-frame` logic because most subscriptions and events work on both web and react native right away.

We note also `fighweel-main` which integrates very well with react native on top of the more commonly use react. Also `figwheel` allows us to have a custom ring-handler so we can provide the `ring-handler` from our systems like so: 

```clojure
;; in figwheel-main.edn
{:ring-handler flybot.server.systems/figwheel-handler
 :auto-testing true}
```

## Mobile Client

```clojure
;;---------- MOBILE ----------
;; deps for react native - use with :client
:mobile/rn {:extra-deps {org.clojure/clojurescript           {:mvn/version "1.10.773"} ;; last version causes error
                         camel-snake-kebab/camel-snake-kebab {:mvn/version "0.4.3"}
                         io.vouch/reagent-react-native       {:git/url "https://github.com/vouch-opensource/reagent-react-native.git"
                                                              :sha     "0fe1c600c9b81180f76b94ef6004c2f85e7d4aa0"}}
           :extra-paths ["client/mobile/src"]}
  
;; cljs repl for figwheel hot reloading development/testing
;; Run `npm run ios` in another terminal to start the simulator
:mobile/ios {:main-opts ["--main"  "figwheel.main"
                         "--build" "ios"
                         "--repl"]}
```

The `:mobile/rn` contains the cljs deps only uses for react native development and the `:mobile/ios` starts the figwheel REPL.

### Prerequisites

- [prepare your environment](https://reactnative.dev/docs/next/environment-setup)
- if no `node_module`s, run `npm install` at the root
- only tested with Xcode simulator (so ios only)

### Features

- Server will be launched on port 9500
- Just save a file to trigger hot reloading on your Xcode simulator

### Jack-in deps+figwheel

In VSCode, upon jack-in `deps+figwheel`, we are first prompt to select the deps aliases before being asked for the REPL
Jack-in: deps+figwheel
DEPS: `:jvm-base`, `:client`, `:mobile/rn`
REPL: `:mobile/ios`
Simulator: run npm run ios in an external terminal - once done it will star the cljs repl in VSCode

## Web Client

```clojure
;;---------- WEB ----------
;; cljs repl for figwheel hot reloading development/testing
;; be sure to not have a main.js in resources/public
:web/dev {:main-opts ["--main"  "figwheel.main"
                      "--build" "dev"
                      "--repl"]}

;; build the optimised js bundle
:web/prod {:main-opts ["--main"       "figwheel.main"
                       "--build-once" "prod"]}

;; Run the cljs tests
:web/test {:main-opts ["-m" "figwheel.main"
                       "-m" "flybot.client.web.test-runner"]}

;; Run the cljs tests with chrome headless
:web/test-headless {:main-opts ["-m" "figwheel.main"
                                "-co" "tests.cljs.edn"
                                "-m" "flybot.client.web.test-runner"]}
```

- `:web/dev` starts the dev REPL
- `:web/prod` generates the optimized js bundle main.js
- `:web/test` runs the cljs tests
- `:web/test-headless` runs the headless tests (to run cljs tests in GitHub CI)

### Dev

If you use VSCode, the jack-in is done in 2 steps to be able to start the REPL in VSCode instead of terminal:
- first chose the aliases for the deps and enter
- then chose the cljs repl you want to launch then enter

Prerequisites:
- delete any `main.js` in the resources folder
- delete `node_modules` at the root because no need for the web
- Check if `cljs-out/dev-main.js` is the script source in `index.html`

Features:
- It will open the browser on port `9500` automatically
- Just save a file to trigger hot reloading in the browser

Jack-in `deps+figwheel`:
- DEPS: `:jvm-base`, `:client`
- REPL: `:web/dev`

### Test in terminal

```
clj -A:jvm-base:client:web/test
```

## CI/CD aliases

### build.clj

To build the main.js and also an uber jar for local testing, we use [clojure/tools.build](https://github.com/clojure/tools.build).

Note: We do not use the uberjar anymore as we use jibbit to push an image directly to AWS ECR.

```clojure
;;---------- BUILD ----------
;; build frontend js bundle : clj -T:build js-bundle
;; build backend uberjar: clj -T:build uber
;; build both js and jar : clj -T:build uber+js
:build {:deps       {io.github.clojure/tools.build {:git/tag "v0.9.4" :git/sha "76b78fe"}}
        :ns-default build}
```

### Jibbit

```clojure
;; build image and push to image repo
;; clj -T:jib build
:jib {:deps       {io.github.atomisthq/jibbit {:git/tag "v0.1.14" :git/sha "ca4f7d3"}}
      :ns-default jibbit.core
      :ns-aliases {jib jibbit.core}}
```

[atomisthq/jibbit](https://github.com/atomisthq/jibbit) uses the `deps.edn` and the config in [jib.edn](https://github.com/skydread1/flybot.sg/blob/master/jib.edn) to generate an image for the Clojure app.

The `:jib` alias triggers the image creation.

To create the image and push it to the ECR account
```
clj -T:jib build
```

### Start container with image

## Antq

```clojure
:outdated {;; Note that it is `:deps`, not `:extra-deps`
             :deps {com.github.liquidz/antq {:mvn/version "RELEASE"}}
             :main-opts ["-m" "antq.core"]}
```
[liquidz/antq](https://github.com/liquidz/antq) points out outdated dependencies.

The `:outdated` alias prints the outdated deps and their last version available:

```
clj -A:outdated
```

## Notes

We have not released the mobile app yet, that is why there is no aliases related to CD for react native yet.

## Conclusion

This is one solution to handle server and clients in the same repo.

It is important to have a clear directory structure to only load require namespaces and avoid errors

Using `:extra-paths` and `:extra-deps` in deps.edn is important because it prevent deploying unnecessary namespaces and libraries on the server and client.

Adding namespace to the aliases make the distinction between backend, common and client (web and mobile) clearer.

Using `deps` jack-in for server only work and `deps+figwheel` for frontend work is made easy using `calva` in VSCode (work in other editor as well).

How your app start will of course depend on your code logic. Using `roberluo/fun-map` for life-cycle-map allows us to define systems accept edn configs and perform associative deps injection and represent the whole components as a map.

