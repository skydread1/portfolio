#:post{:id "portfolio-clojurescript-spa"
       :page :blog
       :home-page? true
       :date ["2023-04-07" "2024-04-21"]
       :repos [["Portfolio" "https://github.com/skydread1/portfolio"]]
       :title "Portfolio Website"
       :css-class "portfolio"
       :tags ["ClojureScript" "Figwheel" "Re-Frame" "Reagent" "Lasagna-pull" "Reitit"]
       :image #:image{:src "/assets/loic-logo.png"
                      :src-dark "/assets/loic-logo.png"
                      :alt "Logo referencing Aperture Science"}}
+++
This portfolio was implemented with **ClojureScript**. It is a Single Page Application that leverages [reagent](https://github.com/reagent-project/reagent), a minimalistic interface between ClojureScript and **React**.

For frontend management, [re-frame](https://github.com/day8/re-frame) is used. To compile the cljs code and perform hot reloading, I used [figwheel-main](https://figwheel.org/).

The routing is done with [reitit](https://github.com/metosin/reitit).

The markdown content is converted into [hiccup](https://github.com/weavejester/hiccup) (a clojure-friendly markup) and the post/vignette configurations are made in EDN which is validated at compile time with a [malli](https://github.com/metosin/malli) schema.

Light/dark mode and code block syntax highlighted are supported.

The app is deployed on **Netlify** every time a branch is merged to master.
+++
## Rational

This portfolio was implemented with **ClojureScript**. It is a Single Page Application that leverages [reagent](https://github.com/reagent-project/reagent), a minimalistic interface between ClojureScript and **React**.

For frontend management, [re-frame](https://github.com/day8/re-frame) is used. To compile the cljs code and perform hot reloading, I used [figwheel-main](https://figwheel.org/).

The routing is done with [reitit](https://github.com/metosin/reitit).

The markdown content is converted into [hiccup](https://github.com/weavejester/hiccup) (a clojure-friendly markup) and the post/vignette configurations are made in EDN which is validated at compile time with a [malli](https://github.com/metosin/malli) schema.

Light/dark mode and code block syntax highlighted are supported.

The app is deployed on **Netlify** every time a branch is merged to master.

## Stack

I have the following stack:
- [figwheel-main](https://figwheel.org/) for live code reloading
- [reagent](https://github.com/reagent-project/reagent) for react components
- [hiccup](https://github.com/weavejester/hiccup) for DOM representation
- [reitit](https://github.com/metosin/reitit) for routing
- [malli](https://github.com/metosin/malli) to validate some configs at the top of markdown files
- [markdown-to-hiccup](https://github.com/mpcarolin/markdown-to-hiccup) to allow me to write the page content in markdown.
- [re-frame](https://github.com/day8/re-frame) a framework for building user interfaces, leveraging [reagent](https://github.com/reagent-project/reagent)
- [lasagna-pull](https://github.com/flybot-sg/lasagna-pull) to precisely select from deep data structure

## Features

The website contains
- a description of the personal/professional projects I worked on as a Software Engineer - my resume
- technical articles related to Clojure for the most part

## Content

### Organization

Each post has its own markdown files in the folder of the page it belongs to.

```
.
├── about
│   └── aboutme.md
└── portfolio
    ├── blog_django.md
    ├── flybot_card_games.md
    ├── flybot_mobile_app.md
    ├── flybot_website.md
    ├── magic_nostrand.md
    └── portfolio_website.md
```

### Vignette and Post

In the `/portfolio` route, I showcase all the projects via what I called `vignette`. They contain a short description of the post instead of the full content.

When the user clicks on a vignette, he goes to a new route with the full post content.

### Config Clojure map

A markdown file of a post is divided into 3 parts:
- above the demarcation `+ + +` is a clojure map of configs (title, page, order etc.)
- below the first demarcation `+ + +` is the post short description as markdown.
- below the second demarcation `+ + +` is the post content as markdown.

Here is an example of clojure config map for a post:

```clojure
#:post{:id "clojure-full-stack-webapp"
       :page :portfolio
       :employer "Flybot Pte Ltd"
       :date "2022"
       :repos [["Flybot" "https://github.com/skydread1/flybot.sg"]]
       :articles [["How to deploy full stack Clojure website to AWS" "../blog/deploy-clj-app-to-aws"]
                  ["Lasagna-pull Pattern applied to flybot.sg backend" "../blog/lasagna-pull-applied-to-flybot"]
                  ["Clojure Mono Repo example : server + 2 clients" "../blog/clojure-mono-repo"]]
       :title "Flybot Website"
       :tags ["Clojure" "ClojureScript" "Figwheel" "Re-Frame" "Malli" "Lasagna-pull" "Fun-map" "Datalevin" "Reitit"]
       :css-class "flybot-website"
       :image #:image{:src "https://www.flybot.sg/assets/flybot-logo.png"
                      :src-dark "https://www.flybot.sg/assets/flybot-logo.png"
                      :alt "Flybot Logo"}}
```

## Compile

At **CLJ** compile time, the following steps happen:
1. Read all markdown files
2. Validate the post configs against a `Malli` schema
3. Assoc the post markdown content to the configs 
4. A macro stores a vector of the posts to be loaded in the re-frame DB

At **CLJS** compile time, the following steps happen:
1. A re-frame event initializes the re-frame DB, loading all the posts from the clojure macro and the theme from local storage.
2. The `reitit` router is created
3. The post markdowns are converted to hiccup via `markdown-to-hiccup`. 

## Build

### Dev

I use clj/cljs REPL of `figwheel` for hot reloading on file save.

### Prod

The github action is triggered when the code is pushed.

I use [clojure/tools.build](https://github.com/clojure/tools.build) to create tasks related to the build.

It runs the build.clj task to generate the main.js bundle:

```
clojure -T:build js-bundle
```

This command compiles the cljs to the optimized js bundle that Netlify will use to generate the preview in the PR.

## Continuous Integration

The CI does the following:
- Run the clj tests
- Run the cljs tests in headless mode
- Compile the cljs file into the js bundle `main.js` and commit it to the repo.

## Continuous Deployment

**Opening a pull request (PR)** to merge changes to master makes `Netlify` create a preview of how the new version of the website would look like once deployed.

**Merging to master** automatically publishes the last version of the website on Netlify.

## Hosted with Netlify

I use **Netlify** for hosting platform because it is easy to setup and the previews of the new website version on GitHub PR are convenient.

## Learn More

Have a look at the repo [README](https://github.com/skydread1/portfolio/blob/master/README.md) for more information.
