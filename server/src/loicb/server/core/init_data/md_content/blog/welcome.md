# About this Website: full-stack Clojure(Script)

This website was entirely made with Clojure and ClojureScript.

It used a very similar stack to another website I developed for the company [Flybot Pte Ltd](https://www.flybot.sg)

## Backend Stack

- [reitit](https://github.com/metosin/reitit) for backend routing
- [muuntaja](https://github.com/metosin/muuntaja) for http api format negotiation, encoding and decoding
- [malli](https://github.com/metosin/malli) for data validation
- [aleph](https://github.com/clj-commons/aleph) for http server
- [reitit-oauth2](https://github.com/skydread1/reitit-oauth2) for oauth2
- [datalevin](https://github.com/juji-io/datalevin) for datalog db
- **[fun-map](https://github.com/robertluo/fun-map) for systems**
- **[lasagna-pull](https://github.com/flybot-sg/lasagna-pull) to precisely select from deep data structure**

## Frontend

- [figwheel-main](https://github.com/bhauman/figwheel-main) for live code reloading
- [hiccup](https://github.com/weavejester/hiccup) for DOM representation
- [reitit](https://github.com/metosin/reitit) for frontend routing
- [malli](https://github.com/metosin/malli) for data validation
- [markdown-to-hiccup](https://github.com/mpcarolin/markdown-to-hiccup) to write the content in markdown.
- [re-frame](https://github.com/day8/re-frame) a framework for building user interfaces, leveraging [reagent](https://github.com/reagent-project/reagent)
- [re-frame-http-fx](https://github.com/day8/re-frame-http-fx) a re-frame effects handler wrapping [cljs-ajax](https://github.com/JulianBirch/cljs-ajax)