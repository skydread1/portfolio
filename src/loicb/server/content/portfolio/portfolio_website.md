#:post{:id "portfolio-clojurescript-spa"
       :order 5
       :page :portfolio
       :date "2023"
       :repos [["Portfolio" "https://github.com/skydread1/portfolio"]]
       :title "Portfolio Website"
       :css-class "portfolio"
       :tags ["ClojureScript" "Figwheel" "Re-Frame" "Reagent" "Lasagna-pull" "Reitit"]
       :image #:image{:src "/assets/loic-logo.png"
                      :src-dark "/assets/loic-logo.png"
                      :alt "Logo referencing Aperture Science"}}
+++
This portfolio website you are currently visiting is a Single Page Application written in `ClojureScript`.

The website contains a list of the projects I worked on as a Software Engineer and the stacks I used. It also contains a page with my resume.

The markdown content is converted into hiccup (a clojure-friendly markup) and the post/vignette configurations are made in EDN which is validated at compile time with a malli schema.

The website is fully responsive and support light/dark mode.
+++
## Stack

This website is a Single Page Application written in ClojureScript.

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

The website contains a list of the projects I worked on as a Software Engineer and the stack I used. It also contains a page with my resume.

The content is written in markdown and compiled to hiccup.

The website is fully responsive and support light/dark mode.

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

```
#:post{:id "clojure-full-stack-webapp"
       :order 2
       :page :portfolio
       :employer "Flybot Pte Ltd" 
       :date "2022"
       :repos [["Flybot" "https://github.com/skydread1/flybot.sg"]]
       :articles [["How to deploy full stack Clojure website to AWS" "https://blog.loicblanchard.me/post/3"]
                  ["Lasagna-pull Pattern applied to flybot.sg backend" "https://blog.loicblanchard.me/post/7"]
                  ["Clojure Mono Repo example : server + 2 clients" "https://blog.loicblanchard.me/post/5"]]
       :title "Flybot Website"
       :tags ["Clojure" "ClojureScript" "Figwheel" "Re-Frame" "Malli" "Lasagna-pull" "Fun-map" "Datalevin" "Reitit"]
       :css-class "flybot-website"
       :image #:image{:src "https://www.flybot.sg/assets/flybot-logo.png"
                      :src-dark "https://www.flybot.sg/assets/flybot-logo.png"
                      :alt "Flybot Logo"}}
```

## Compile

At CLJ compile time, the following steps happen:
1. Read all markdown files
2. Validate the post configs against a `Malli` schema
3. Assoc the post markdown content to the configs 
4. A macro stores a vector of the posts to be loaded in the re-frame DB

At CLJS compile time, the following steps happen:
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

The Ci does the following:
- run the clj tests
- run the cljs tests in headless mode
- compile the cljs file into the js bundle `main.js` and commit it to the repo.

## Continuous Deployment

**Opening a pull request (PR)** to merge your changes to master, makes `Netlify` create a preview for you to see how the new version of the website would look like.

**Merging to master** automatically publishes the last version of the website via Netlify.

## Hosted with Netlify

I use **Netlify** for hosting platform because it is easy to setup and the previews of the new website version on GitHub MR is convenient.

## Learn More

Have a look at the repo [README](https://github.com/skydread1/portfolio/blob/master/README.md) for more information.
