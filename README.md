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

The markdown content is converted into [hiccup](https://github.com/weavejester/hiccup) (a clojure-friendly markup) and the post/vignette configurations use standard YAML frontmatter which is validated at compile time with a [malli](https://github.com/metosin/malli) schema.

At compile time, RSS feeds are generated for blog posts using [clj-rss](https://github.com/yogthos/clj-rss). Multiple feeds can be configured (e.g., "clojure" for Clojure-specific posts, "all" for all blog posts).

The app is deployed on **Netlify** via GitHub Actions every time a branch is merged to master. Build artifacts (JS bundles, RSS feeds) are generated during CI/CD and are not committed to the repository.

## 🔷 Prerequisites

- [babashka](https://github.com/babashka/babashka) to run the different project tasks.

## 🔷 Features

All the posts/vignettes are written in standard markdown files in [content](./src/loicb/build/content/) folder.

The UI supports light/dark mode and is responsive.

At the top of each markdown file, you can provide YAML frontmatter to configure post metadata such as title, date, tags, image, github repos, article links, and RSS feed inclusion. A `## TLDR` section can be added to provide a summary that appears in post vignettes.

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

### Markdown Format

Each markdown file uses standard **YAML frontmatter** for metadata, followed by an optional **`## TLDR`** section for the summary, and then the full article content:

```markdown
---
title: My New Project
date: 2024-02-01
page: blog
id: my-new-project
tags:
  - Clojure
  - Web
rss-feeds:
  - clojure
css-class: my-project
image:
  src: /assets/some-illustrative-img.png
  src-dark: /assets/some-illustrative-img-dark.png
  alt: A logo
repos:
  - ["Project Name", "https://github.com/user/repo"]
---

## TLDR

A brief summary of the project. This will appear in the vignette/preview.

## Introduction

The full article content starts here...
```

**Key points:**
- **YAML frontmatter** (between `---` markers) configures post metadata
- **`## TLDR` section** (optional) provides a summary shown in vignettes
- **Standard markdown** for the rest of the content
- The TLDR section is automatically extracted and removed from the full article view to avoid duplication

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

However, if you change the content of a markdown file, you will need to save the file [md.clj](./src/loicb/build/md.clj) which is where the macro that reads all the markdown files resides. Refresh your browser and the new markdown content will appear.

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

### RSS feeds

```sh
bb rss
```

Generates all configured RSS feeds in `resources/public/blog/rss/`.

Currently available feeds:
- **clojure**: Posts tagged with Clojure content
- **all**: All blog posts

To include a post in specific RSS feeds, add to your YAML frontmatter:
```yaml
rss-feeds:
  - clojure  # Include in Clojure feed
  - all      # Include in all posts feed
```

Available RSS feeds:
- [blog/rss/clojure-feed.xml](https://www.loicb.dev/blog/rss/clojure-feed.xml) - Clojure-related articles
- [blog/rss/all-feed.xml](https://www.loicb.dev/blog/rss/all-feed.xml) - All blog articles

New feeds can be added by updating the `feed-configs` in `src/loicb/build/rss.clj`.

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

To know what params can be provided to the config map, you can have a look at the malli schema in [validation](./src/loicb/common/validation.cljc). You can take inspiration from my own posts in [content](./src/loicb/build/content/).

### Compile process

At CLJ compile time, the following steps happen:
1. Read all markdown files with YAML frontmatter
2. Parse YAML metadata using `markdown-clj`
3. Extract `## TLDR` section as post summary
4. Normalize metadata (convert YAML types to Clojure types)
5. Validate the post configs against a `Malli` schema
6. A macro stores a vector of the posts to be loaded in the re-frame DB

At CLJS compile time, the following steps happen:
1. A re-frame event initializes the re-frame DB, loading all the posts from the clojure macro and the theme from local storage.
2. The `reitit` route table is created
3. The post markdown content is converted to hiccup via `markdown-to-hiccup`.

## 🔷 Contributing

Feel free to open new issues if you discover bugs or wish to add new features.

## 🔷 License

Copyright © 2023-2025 Loic Blanchard

Distributed under the MIT License - see the [LICENSE](./LICENSE) file for details.
