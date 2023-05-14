# portfolio

Personal Website - [loicblanchard.me](https://www.loicblanchard.me/)

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

The website:
- contains a blog page to write articles in markdown
- supports dark mode.
- is fully responsive.

## Content

### Organization

Each post has its own markdown files in the folder of the page it belongs to.

```
├── content
│   ├── about
│   │   ├── bosch.md
│   │   ├── cpe.md
│   │   ├── electriduct.md
│   │   ├── flybot.md
│   │   ├── recap.md
│   │   └── socials.md
│   ├── blog
│   │   └── deploy_clojure_aws.md
│   ├── contact
│   │   └── contact_me.md
│   └── home
│       ├── flybot_card_games.md
│       ├── flybot_mobile_app.md
│       ├── flybot_website.md
│       ├── magic_nostrand.md
│       └── portfolio_website.md
```

### Config Clojure map

A markdown file of a post is divided into 2 parts:
- above the demarcation `+++` is a clojure map of configs (title, page, order etc.)
- below the demarcation `+++` is the post content as markdown.

Here is an example of clojure map for a post:

```clojure
#:post{:order 0
       :page :home
       :title "Portfolio Website"
       :css-class "portfolio"
       :image-beside #:image{:src "assets/loic-logo.png"
                             :src-dark "assets/loic-logo.png"
                             :alt "Logo referencing Aperture Science"}}
```

## Compile

At CLJ compile time, the following steps happen:
1. Read all markdown files
2. Validate the post configs against a `Malli` schema
3. Assoc the post markdown content to the configs 
4. A macro stored a vector of the posts to be loaded in the re-frame DB

At CLJS compile time, the following steps happen:
1. A re-frame event initializes the re-frame DB, loading all the posts from the clojure macro and the theme from local storage.
2. The `reitit` route table is created
3. The post markdown are converted to hiccup via `markdown-to-hiccup`. 

## Build

### Dev

I use clj/cljs REPL of `figwheel` for hot reloading on save

### Prod

The github action is triggered when the code is pushed.

I use [clojure/tools.build](https://github.com/clojure/tools.build) to create tasks related to the build.

It runs the build.clj task `deploy` like so:

```
clojure -T:build deploy
```

This command compiles the cljs to the optimized js bundle that Netlify will use to generate the preview in the PR.

## Continuous integration

Adding of modifying a markdown file and merging to master will recompile the cljs to the js bundle before automatically publishing the last version of the website via Netlify.

The markdown files are converted to hiccup via Clojure macros, so they are converted at compile time.

## Hosting

I use Netlify for hosting platform because it is easy to setup and the previews of the new website version on GitHub MR is convenient.
