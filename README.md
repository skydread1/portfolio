<div align="center">
    <a href="https://www.loicb.dev/" target="_blank" rel="noopener noreferrer"><img src="./resources/public/assets/loic-logo.png" alt="Loic logo" width="25%"></a>
</div>

<div align="center">
    <a href="https://clojure.org/" target="_blank" rel="noopener noreferrer"><img src="https://img.shields.io/badge/clojure-v1.12.3-blue.svg" alt="Clojure Version"></a>
    <a href="https://github.com/skydread1/portfolio/actions/workflows/main.yml"><img src="https://github.com/skydread1/portfolio/actions/workflows/main.yml/badge.svg" alt="CI"></a>
    <a href="https://app.netlify.com/sites/loicblanchard/deploys"><img
      src="https://api.netlify.com/api/v1/badges/e12042df-3f60-4b26-8823-8a77e6dc2d2f/deploy-status"
      alt="Netlify Status" /></a>
    <a href="https://codecov.io/gh/skydread1/portfolio" ><img src="https://codecov.io/gh/skydread1/portfolio/branch/master/graph/badge.svg"/></a>
    <a href="https://github.com/skydread1/portfolio/issues" target="_blank" rel="noopener noreferrer"><img src="https://img.shields.io/badge/contributions-welcome-blue.svg" alt="Contributions welcome"></a>
</div>

<h1 align="center">🔷 Portfolio SPA in ClojureScript 🔷</h1>

## 🔷 Rationale

This portfolio was implemented with **ClojureScript**. It is a Single Page Application that leverages [reagent](https://github.com/reagent-project/reagent), a minimalistic interface between ClojureScript and **React**.

For frontend management, [re-frame](https://github.com/day8/re-frame) is used. To compile the cljs code and perform hot reloading, I used [figwheel-main](https://figwheel.org/).

The routing is done with [reitit](https://github.com/metosin/reitit).

The markdown content is converted into [hiccup](https://github.com/weavejester/hiccup) (a clojure-friendly markup) and the post/vignette configurations are made in EDN which is validated at compile time with a [malli](https://github.com/metosin/malli) schema.

At compile time, a RSS feed is generated for the clojure blog posts using [clj-rss](https://github.com/yogthos/clj-rss).

The app is deployed on **Netlify** via GitHub Actions every time a branch is merged to master. Build artifacts (JS bundles, RSS feeds) are generated during CI/CD and are not committed to the repository.

## 🔷 Prerequisites

- [babashka](https://github.com/babashka/babashka) to run the different project tasks.

## 🔷 Features

All the posts/vignettes are written in markdown files in [content](./src/loicb/server/content/) folder.

The UI supports light/dark mode and is responsive.

At the top of each markdown file, you can provide an EDN config map to add additional properties to the post such as an illustrative image, the order on the page, the title in the URL, github and article links etc...
When you create a pull request (PR) to master, **Netlify** provides a preview of what the new app version would look like.

For development, starting a clj/cljs REPL with `figwheel` allows you to have the local changes reflected on port 9500 on file save.

## 🔷 Content

### Organization

Each post has its own markdown file in the folder of the page it belongs to.

For instance:
```
.
├── about
│   └── aboutme.md
├── blog
│   └── some-article.md
```

### Config Clojure map

A markdown file for a post is divided into three parts, separated by +++ demarcations:
```
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

```
post map here
+++
My article summary goes here
+++
My article content goes here
...
```

I refer to the post summary part as the `vignette` because it can conveniently be used on a home page for instance to just display a recap of the projects.

## 🔷 Getting Started

### Clone the repo

Feel free to clone or fork the repository and modify both the code and post content to suit your need.

### Dev

```sh
bb dev
```

This starts a REPL server with the necessary middleware for ClojureScript development.

Then connect to the REPL server with project type `deps.edn + Figwheel Main` and tick the `:dev` alias.

### Hot reloading

Once the REPL started, saving a cljs file will automatically update the js in your localhost.

However, if you change the content of a markdown file, you will need to save the file [md.clj](./src/loicb/server/md.clj) which is where the macro that reads all the markdown files resides. Refresh your browser and the new markdown content will appear.

## 🔷 Tests

### Local development tests

```sh
bb test  
```

This task runs both clj and cljs tests:
- clj tests ensure that all your markdown files respect the malli schema.
- cljs tests ensure that the state (in our re-frame DB) is as expected after user actions (navigation, theme, post interaction etc).

### Code formatting

Check code formatting:
```sh
bb fmt-check
```

Fix code formatting:
```sh
bb fmt-fix
```

### Regression cljs tests on save

The cljs tests are also run on every save and the results are displayed at http://localhost:9500/figwheel-extra-main/auto-testing

### CI tests (headless)

In the CI, there is no browser, so we use headless mode:

```sh
bb test-ci
```

## 🔷 Build Tasks

### Clean build artifacts

```sh
bb clean
```

Removes `target` and `dist` directories.

### JS bundle

```sh
bb build
```

Builds the optimized ClojureScript bundle and generates `index.html` in `target/public`.

### RSS feed

```sh
bb rss
```

Generates RSS feed for blog posts tagged with `clojure` in `resources/public/blog/rss/`.

The RSS **clojure** feed is accessible at [blog/rss/clojure-feed.xml](https://www.loicb.dev/blog/rss/clojure-feed.xml).

### Production release

```sh
bb dist
```

This task runs `bb build` and `bb rss`, then gathers all necessary static files into a `dist/` folder ready for deployment.

### Dependencies

Check for outdated dependencies:
```sh
bb outdated
```

## 🔷 CI/CD

The project uses GitHub Actions for continuous integration and deployment.

### For Pull Requests
1. Run tests with `bb test-ci`
2. Run code formatting checks with `bb fmt-check`  
3. Build static files with `bb dist`
4. Deploy preview to Netlify with unique URL (`pr-{number}`)

### For Main Branch (Production)
1. Run tests with `bb test-ci`
2. Run code formatting checks with `bb fmt-check`
3. Build static files with `bb dist` 
4. Deploy to production Netlify site

## 🔷 Hosting with Netlify

The website is hosted on Netlify (US servers by default).

To enable deployments, configure these secrets in your GitHub repository settings:

- `NETLIFY_AUTH_TOKEN`: Personal access token from Netlify
- `NETLIFY_SITE_ID`: Site ID from your Netlify site settings

## 🔷 Implementation Details

### Config validation

To know what params can be provided to the config map, you can have a look at the malli schema in [validation](./src/loicb/common/validation.cljc). You can take inspiration from my own posts in [content](./src/loicb/server/content/).

### Compile process

At CLJ compile time, the following steps happen:
1. Read all markdown files
2. Validate the post configs against a `Malli` schema
3. Assoc the post markdown content to the configs 
4. A macro stores a vector of the posts to be loaded in the re-frame DB

At CLJS compile time, the following steps happen:
1. A re-frame event initializes the re-frame DB, loading all the posts from the clojure macro and the theme from local storage.
2. The `reitit` route table is created
3. The post markdown are converted to hiccup via `markdown-to-hiccup`.

## 🔷 Contributing

Feel free to open new issues if you discover bugs or wish to add new features.

## 🔷 License

Copyright © 2023-2025 Loic Blanchard

Distributed under the MIT License - see the [LICENSE](./LICENSE) file for details.
