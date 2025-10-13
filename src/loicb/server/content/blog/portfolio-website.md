---
id: portfolio-clojurescript-spa
page: blog
date: 
  - 2023-04-07
  - 2024-04-21
title: Portfolio Website
home-page?: true
css-class: portfolio
tags: 
  - ClojureScript
  - Figwheel
  - Re-Frame
  - Reagent
  - Lasagna-pull
  - Reitit
  - Babashka
repos:
  - ["Portfolio", "https://github.com/skydread1/portfolio"]
image:
  src: /assets/loic-logo.png
  src-dark: /assets/loic-logo.png
  alt: Logo referencing Aperture Science
---

This portfolio was implemented with **ClojureScript**. It is a Single Page Application that leverages [reagent](https://github.com/reagent-project/reagent), a minimalistic interface between ClojureScript and **React**.

For frontend management, [re-frame](https://github.com/day8/re-frame) is used. To compile the cljs code and perform hot reloading, I used [figwheel-main](https://figwheel.org/).

The routing is done with [reitit](https://github.com/metosin/reitit).

The markdown content is converted into [hiccup](https://github.com/weavejester/hiccup) (a clojure-friendly markup) and the post/vignette configurations are made in EDN which is validated at compile time with a [malli](https://github.com/metosin/malli) schema.

Light/dark mode and code block syntax highlighted are supported.

The app is deployed on **Netlify** via GitHub Actions every time a branch is merged to master. Build artifacts are generated during CI/CD and are not committed to the repository.

+++

## Rational

This portfolio was implemented with **ClojureScript**. It is a Single Page Application that leverages [reagent](https://github.com/reagent-project/reagent), a minimalistic interface between ClojureScript and **React**.

For frontend management, [re-frame](https://github.com/day8/re-frame) is used. To compile the cljs code and perform hot reloading, I used [figwheel-main](https://figwheel.org/).

The routing is done with [reitit](https://github.com/metosin/reitit).

The markdown content is converted into [hiccup](https://github.com/weavejester/hiccup) (a clojure-friendly markup) and the post/vignette configurations are made in EDN which is validated at compile time with a [malli](https://github.com/metosin/malli) schema.

Light/dark mode and code block syntax highlighted are supported.

The app is deployed on **Netlify** via GitHub Actions every time a branch is merged to master. Build artifacts are generated during CI/CD and are not committed to the repository.

## Stack

I have the following stack:
- [babashka](https://github.com/babashka/babashka) for task automation and build scripts
- [figwheel-main](https://figwheel.org/) for live code reloading
- [reagent](https://github.com/reagent-project/reagent) for react components
- [hiccup](https://github.com/weavejester/hiccup) for DOM representation
- [reitit](https://github.com/metosin/reitit) for routing
- [malli](https://github.com/metosin/malli) to validate some configs at the top of markdown files
- [markdown-to-hiccup](https://github.com/mpcarolin/markdown-to-hiccup) to allow me to write the page content in markdown.
- [re-frame](https://github.com/day8/re-frame) a framework for building user interfaces, leveraging [reagent](https://github.com/reagent-project/reagent)
- [lasagna-pull](https://github.com/flybot-sg/lasagna-pull) to precisely select from deep data structure
- [clj-rss](https://github.com/yogthos/clj-rss) to generate RSS feeds for blog posts

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
└── blog
    ├── blog_django.md
    ├── flybot_card_games.md
    ├── flybot_mobile_app.md
    ├── flybot_website.md
    ├── magic_nostrand.md
    └── portfolio_website.md
```

### Vignette and Post

Some `blog` posts have a flag to display the post in the home page via what I called `vignette`. They contain a short description of the post instead of the full content.

When the user clicks on a vignette, he goes to a new route with the full post content.

### Config Clojure map

A markdown file of a post is divided into 3 parts:
- above the demarcation `+ + +` is a clojure map of configs (title, page, order etc.)
- below the first demarcation `+ + +` is the post short description as markdown.
- below the second demarcation `+ + +` is the post content as markdown.

Here is an example of clojure config map for a post:

```clojure
#:post{:id "clojure-full-stack-webapp"
       :page :blog
       :home-page? true ;; display vignette in home page
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

## Build Tasks with Babashka

I use [babashka](https://github.com/babashka/babashka) to manage all build tasks through a `bb.edn` configuration file.

### Development

```bash
bb dev
```

Starts a REPL server with the necessary middleware for ClojureScript development and figwheel hot reloading.

### Testing

```bash
bb test      # Local development tests
bb test-ci   # CI tests (headless mode)
bb fmt-check # Check code formatting
bb fmt-fix   # Fix code formatting
```

### Production Build

```bash
bb build  # Build optimized ClojureScript bundle
bb rss    # Generate RSS feeds for blog posts
bb dist   # Create complete distribution in dist/ folder
bb clean  # Clean build artifacts
```

The `bb dist` command creates a complete static site in the `dist/` directory ready for deployment.

## Continuous Integration & Deployment

The project uses GitHub Actions for CI/CD with separate workflows for pull requests and production deployments.

### Pull Request Workflow
1. Run tests with `bb test-ci`
2. Check code formatting with `bb fmt-check`
3. Build static files with `bb dist`
4. Deploy preview to Netlify with unique URL (`pr-{number}`)

### Production Deployment (main branch)
1. Run tests with `bb test-ci` 
2. Check code formatting with `bb fmt-check`
3. Build static files with `bb dist`
4. Deploy to production Netlify site

## Hosting with Netlify

The website is hosted on Netlify (US servers) and deployed via GitHub Actions using the Netlify CLI. This approach provides:

- **Predictable deployments**: Build happens in controlled CI environment
- **Preview deployments**: Each pull request gets its own preview URL
- **No committed artifacts**: Clean git history without build files
- **Environment separation**: Different deploy targets for previews vs production

## Learn More

Have a look at the repo [README](https://github.com/skydread1/portfolio/blob/master/README.md) for more information.
