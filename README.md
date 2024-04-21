<div align="center">
    <a href="https://www.loicblanchard.me/" target="_blank" rel="noopener noreferrer"><img src="./resources/public/assets/loic-logo.png" alt="Loic logo" width="25%"></a>
</div>

<div align="center">
    <a href="https://clojure.org/" target="_blank" rel="noopener noreferrer"><img src="https://img.shields.io/badge/clojure-v1.11.1-blue.svg" alt="Clojure Version"></a>
    <a href="https://github.com/skydread1/portfolio/actions/workflows/main.yml"><img src="https://github.com/skydread1/portfolio/actions/workflows/main.yml/badge.svg" alt="CI"></a>
    <a href="https://app.netlify.com/sites/loicblanchard/deploys"><img
      src="https://api.netlify.com/api/v1/badges/e12042df-3f60-4b26-8823-8a77e6dc2d2f/deploy-status"
      alt="Netlify Status" /></a>
    <a href="https://codecov.io/gh/skydread1/portfolio" ><img src="https://codecov.io/gh/skydread1/portfolio/branch/master/graph/badge.svg"/></a>
    <a href="https://github.com/skydread1/portfolio/issues" target="_blank" rel="noopener noreferrer"><img src="https://img.shields.io/badge/contributions-welcome-blue.svg" alt="Contributions welcome"></a>
</div>

<h1 align="center">🔷 Portfolio SPA in ClojureScript 🔷</h1>

## 🔷 Rational

This portfolio was implemented with **ClojureScript**. It is a Single Page Application that leverages [reagent](https://github.com/reagent-project/reagent), a minimalistic interface between ClojureScript and **React**.

For frontend management, [re-frame](https://github.com/day8/re-frame) is used. To compile the cljs code and perform hot reloading, I used [figwheel-main](https://figwheel.org/).

The routing is done with [reitit](https://github.com/metosin/reitit).

The markdown content is converted into [hiccup](https://github.com/weavejester/hiccup) (a clojure-friendly markup) and the post/vignette configurations are made in EDN which is validated at compile time with a [malli](https://github.com/metosin/malli) schema.

The app is deployed on **Netlify** every time a branch is merged to master.

## 🔷 Features

All the posts/vignettes are written in markdown files in [content](./src/loicb/server/content/) folder.

The UI supports light/dark mode and is responsive.

At the top of each markdown file, you can provide an EDN config map to add additional properties to the post such as an illustrative image, the order on the page, the title in the URL, github and article links etc...
When you create a pull request (PR) to master, **Netlify** provides a preview of what the new app version would look like.

For development, starting a clj/cljs REPL with `fighweel` allows you to have the local changes reflected on port 9500 on file save.

## 🔷 Content

### Organization

Each post has its own markdown file in the folder of the page it belongs to.

For instance:
```
.
├── about
│   └── aboutme.md
└── portfolio
    ├── blog-django.md
    ├── flybot-website.md
    └── props-recommender.md
├── blog
│   └── just-tick-lib.md
```

### Config Clojure map

A markdown file for a post is divided into three parts, separated by +++ demarcations:
```md
some config map
+++
post summary
+++
full post content
```

Following is an example of clojure map for a post config:

```clojure
#:post{:date ["2024-02-01" "2024-04-01"]
       :page :portfolio
       :title "My New Project"
       :css-class "my-project"
       :image-beside #:image{:src "/assets/some-illustrative-img.png"
                             :src-dark "/assets/some-illustrative-img-dark.png"
                             :alt "A logo"}}
```

This map goes at the top of a markdown file like so:

```md
post map here
+++
My project consists in...
+++
## Rational

The goal of my project is...

## Stack

To achieve this...
...
```

I refer to the post summary part as the `vignette` because it can conveniently be used on a home page for instance to just display a recap of the projects.

### Config validation

To know what params can be provided to the config map, you can have a look at the malli schema in [validation](./src/loicb/common/validation.cljc). You can take inspiration from my own posts in [content](./src/loicb/server/content/).

## 🔷 Compile

At CLJ compile time, the following steps happen:
1. Read all markdown files
2. Validate the post configs against a `Malli` schema
3. Assoc the post markdown content to the configs 
4. A macro stores a vector of the posts to be loaded in the re-frame DB

At CLJS compile time, the following steps happen:
1. A re-frame event initializes the re-frame DB, loading all the posts from the clojure macro and the theme from local storage.
2. The `reitit` route table is created
3. The post markdown are converted to hiccup via `markdown-to-hiccup`. 

## 🔷 Getting Started

### Clone the repo

Feel free to clone or fork the repository and modify both the code and post content to suit your need.

### Dev

You can perform ClojureScript jack-in to open the webpage in a browser on port `9500`, alongside an interactive REPL in your IDE.

You can then edit and save source files to trigger hot reloading in the browser.

#### Prerequisites

- Delete any `main.js` in the resources folder
- Check if `cljs-out/dev-main.js` is the script source in [index.html](./resources/public/index.html): that is where figwheel will recompile the cljs when you save a file.

#### VS Code

If you use VS Code, the jack-in is done in 2 steps to be able to start the REPL in VS Code instead of terminal:

1. Choose the aliases for the deps and press enter
2. Choose the ClojureScript REPL you want to launch and press enter

For 2. since our app is only a frontend app, we don't need to load some clj backend deps.

Jack-in `deps+figwheel`:

- Deps: no alias to add so just press enter
- REPL: tick `:web/dev` and press enter

This should start a cljs REPL in VS Code and open your browser localhost in port 9500.

#### Emacs

If you use Emacs (or Doom Emacs, or Spacemacs) with CIDER, the CIDER jack-in is done in 3 steps:

1. `C-u M-x cider-jack-in-clj&cljs` or `C-u M-x cider-jack-in-cljs`
2. By default, emacs use the `cider/nrepl` alias such as in `-M:cider/nrepl`. You need to keep this alias at the end such as `-M:web/dev:cider/nrepl`
3. Select ClojureScript REPL type: `figwheel-main`
4. Select figwheel-main build: `dev`

#### Hot reloading

Once the REPL started, saving a cljs file will automatically update the js in your localhost.

However, if you change the content of a markdown file, you will need to save the file [md.clj](./src/loicb/server/md.clj) which is where the macro that read all the markdown files resides. Refresh your browser and the new markdown content will appear.

### Prod

The GitHub action is triggered when code is pushed.

I use [clojure/tools.build](https://github.com/clojure/tools.build) to create tasks related to the build.

It runs the `js-bundle` task in the build.clj file as follows:

```
clojure -T:build js-bundle
```

This command compiles the cljs to the optimized js bundle that Netlify will use to generate the preview in the PR.

Note: be sure to not forget to use `main.js` as script source in [index.html](./resources/public/index.html): that is where figwheel will produce the optimize js from your cljs file when you push your update to your online repo.

## 🔷 Tests

### Clj tests

The clj macro that loads the markdown files is tested and the test can be run like so:

```clojure
clj -A:server/test  
```

This test ensure that all your markdown files respect the malli schema.

### Cljs tests

The cljs state management tests can be run like so:

```clojure
clj -A:web/test
```

These frontend cljs tests ensure that the state (in our re-frame DB) is as expected after user actions (navigation, theme, post interaction etc).

### Regression tests on save

The tests mentioned above are also run on every save and the results are displayed at http://localhost:9500/figwheel-extra-main/auto-testing

### Cljs tests (headless) - for CI

In the CI, there is no browser, so we need a specific alias to run the test in headless mode:

```clojure
clj -A::web/test-headless
```

## 🔷 Continuous Integration

Every time you push some code, the following happens:
- run the clj tests
- run the cljs tests in headless mode
- compile the cljs file into the js bundle `main.js` and commit it to the repo.

## 🔷 Continuous Deployment

**Opening a pull request (PR)** to merge your changes to master, makes `Netlify` create a preview for you to see how the new version of the website would look like.

**Merging to master** automatically publishes the last version of the website via Netlify.

Note: The markdown files are converted to hiccup via Clojure macros, so they are converted at compile time.

## 🔷 Hosting with Netlify

I use **Netlify** for hosting platform because it is easy to setup and the previews of the new website version on GitHub MR is convenient.

Connecting your repo to Netlify is straight forward and the only configuration we need to update is the `Publish directory`. It should be set to
`resources/public` because that is where our static files reside.

Note that the server will be hosted in the US by default, and you cannot change the location with the free version.

## 🔷 Contributing

Feel free to open new issues if you discover bugs or wish to add new features.

## 🔷 License

Copyright © 2023 Loic Blanchard

Distributed under the MIT License - see the [LICENSE](./LICENSE) file for details.
