#:post{:id "clojure-full-stack-webapp"
       :order 2
       :page :portfolio
       :employer "Flybot Pte Ltd" 
       :date "2022"
       :repos [["Flybot" "https://github.com/skydread1/flybot.sg"]]
       :articles [["How to deploy full stack Clojure website to AWS" "https://blog.loicblanchard.me/post/3"]
                  ["Lasagna-pull Pattern applied to flybot.sg backend" "https://blog.loicblanchard.me/post/7"]
                  ["Pull Pattern: Query in deep nested data structure" "https://blog.loicblanchard.me/post/2"]
                  ["Clojure Mono Repo example : server + 2 clients" "https://blog.loicblanchard.me/post/5"]]
       :title "Flybot Website"
       :tags ["Clojure" "ClojureScript" "Figwheel" "Re-Frame" "Malli" "Lasagna-pull" "Fun-map" "Datalevin" "Reitit"]
       :css-class "flybot-website"
       :image #:image{:src "https://www.flybot.sg/assets/flybot-logo.png"
                      :src-dark "https://www.flybot.sg/assets/flybot-logo.png"
                      :alt "Flybot Logo"}}
+++
[flybot.sg](https://www.flybot.sg/) is an open-source full-stack Clojure web-app that allows company’s employees to write posts to showcase their open-source libraries, their contributions and all technical knowledge that could interest the functional programming community. HRs can also post job offers. Admins can edit any piece of content in any pages as the whole content can be written in Markdown.

The purpose of this project was to demonstrate how the `lasagna stack` ([flybot-sg/lasagna-pull](https://github.com/flybot-sg/lasagna-pull) and [robertluo/fun-map](https://github.com/robertluo/fun-map)) could ease the web development experience for any Clojure developers.
+++
## Rational

[flybot.sg](https://www.flybot.sg/) is an open-source full-stack Clojure web-app that allows company’s employees to write posts to showcase their open-source libraries, their contributions and all technical knowledge that could interest the functional programming community. HRs can also post job offers. Admins can edit any piece of content in any pages as the whole content can be written in Markdown.

The purpose of this project was to demonstrate how the "lasagna stack" ([flybot-sg/lasagna-pull](https://github.com/flybot-sg/lasagna-pull) and [robertluo/fun-map](https://github.com/robertluo/fun-map)) could ease the web development experience for any Clojure developers.

The [skydread1/flybot.sg](https://github.com/skydread1/flybot.sg) repo was then created and is open-source so all Clojure developers can see the benefit of the `pull pattern` and `fun map` as well as a good example of the usage of other very good open-source libraries of the Clojure community.

## Stack

### Backend

- [reitit](https://github.com/metosin/reitit) for backend routing
- [muuntaja](https://github.com/metosin/muuntaja) for http api format negotiation, encoding and decoding
- [malli](https://github.com/metosin/malli) for data validation
- [aleph](https://github.com/clj-commons/aleph) as http server
- [reitit-oauth2](https://github.com/skydread1/reitit-oauth2) for oauth2
- [datalevin](https://github.com/juji-io/datalevin) as datalog database
- **[fun-map](https://github.com/robertluo/fun-map) for systems**
- **[lasagna-pull](https://github.com/flybot-sg/lasagna-pull) to precisely select from deep data structure**

### Frontend

- [figwheel-main](https://github.com/bhauman/figwheel-main) for live code clj/cljs reloading
- [hiccup](https://github.com/weavejester/hiccup) for DOM representation
- [reitit](https://github.com/metosin/reitit) for frontend routing
- [malli](https://github.com/metosin/malli) for data validation
- [markdown-to-hiccup](https://github.com/mpcarolin/markdown-to-hiccup) to write the content in markdown.
- [re-frame](https://github.com/day8/re-frame) a framework for building user interfaces, leveraging [reagent](https://github.com/reagent-project/reagent)
- [re-frame-http-fx](https://github.com/day8/re-frame-http-fx) a re-frame effects handler wrapping [cljs-ajax](https://github.com/JulianBirch/cljs-ajax)

## Repo

You can have a look at the code on my [GitHub repo](https://github.com/skydread1/flybot.sg)

We use a mono repo structure where the `server` (clj files), and `client` (cljs files) reside alongside each others.
A `common` (cljc files) top folder is also used for data validation that applies for both server and client.

We actually have 2 clients: web and mobile.
So the web app frontend resides in the same repo as the mobile frontend and the 2 share most of the re-frame events.

The mono-repo structure is as followed:

```clojure
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

You can read more about it in my article: [Clojure Mono Repo example : server + 2 clients](https://blog.loicblanchard.me/post/5).

## Features

### Markdown to write the content

Once logged in, a software engineer or HR can create/edit/delete a post using Markdown for the post content. Some configurations such as adding an illustrative image for light mode and dark mode or to display author name and date of the articles are also available.

A preview option is also available to see how the post would look like before being submitted.

### Oauth2

We use google oauth2 for authentication. Once an employee is logged in via google, the ring session is updated server side and a ring cookie is sent to the client to ensure proper authorization on post submission. There is also an Admin panel to add admin permissions to employees that require specific admin roles. Only admins can edit/delete posts of others.

## Design

The website is fully responsive and the design is simple but clean. The dark mode is handled using global css variables and by having a theme field in the re-frame DB. The persistence of the dark/light mode is done via local storage.

## Data

### Data Persistence

We used [datalevin](https://github.com/juji-io/datalevin) as DB, which is an open-source DB that supports datalog storage with a similar syntax to Datomic.

### Data validation

We use [malli](https://github.com/metosin/malli) for data validation for both backend and frontend.
Furthermore, [lasagna-pull](https://github.com/flybot-sg/lasagna-pull) can accept a malli schema as optional parameter to be sure the pull pattern provided respects the malli schema for that specific query. It is very convenient as if the query shape does not match the schema provided by the API, a detailed error is thrown and no query is performed.

## Lasagna Stack

- [fun-map](https://github.com/robertluo/fun-map) allows us to define a system and perform associative dependency injections.
- [lasagna-pull](https://github.com/flybot-sg/lasagna-pull) makes selecting data in nested structure more intuitive via a pattern that describes the data to be pulled following the shape of the data.

I wrote articles about how these 2 libraries benefit web development and design in my tech blog:
- [Lasagna-pull Pattern applied to flybot.sg backend](https://blog.loicblanchard.me/post/7)
- [Pull Pattern: Query in deep nested data structure](https://blog.loicblanchard.me/post/2)

## CI/CD

### CI

The GitHub actions run both backend (clj) and frontend (cljs) tests.

If all the CI tests pass, the GitHub action proceeds to create the js bundle and finally the app docker image is created and deployed to AWS ECR.

### CD

The website [flybot.sg](http://flybot.sg) is deployed in an EC2 instance in front of LBs.

The app image is generated via the `deps.edn` and [atomisthq/jibbit](https://github.com/atomisthq/jibbit) directly and started via docker in the EC2 instance.

We also have the possibility to create an uberjar using [clojure/tools.build](https://github.com/clojure/tools.build) for local testing.

When new GitHub PR is merged, the new container image is automatically generated and sent to AWS ECR via Github Actions.

You can read more about how I deployed the app to AWS in this article: [How to deploy full stack Clojure website to AWS](https://blog.loicblanchard.me/post/3).

## Hot reloading

[figwheel-main](https://github.com/bhauman/figwheel-main) allows us to do hot reloading when a file is saved and provide clj/cljs REPL to print at anytime the re-frame DB for instant feedback. It also allow us to generate an optimized js bundle from the cljs files. The configuration parameters are very well thought and the library makes the development experience a bliss. Figwheel also allows us to run our own ring server by providing a ring-handler in the config. This feature works very well with our fun-map system.

## Learn more

Feel free to visit [flybot.sg](https://www.flybot.sg/) and especially the [blog](https://www.flybot.sg/blog).
