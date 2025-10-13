---
id: hibou-platform
page: blog
date: 
  - 2025-10-13
title: Building a Generic Analytics Platform with Hibou
home-page?: true
employer: Flybot Pte Ltd
css-class: hibou-platform
tags: 
  - Clojure
  - Rama
  - Analytics
  - Mono Repo
  - Full-Stack
rss-feeds:
  - all
  - clojure
image:
  src: https://www.flybot.sg/assets/flybot-logo.png
  src-dark: https://www.flybot.sg/assets/flybot-logo.png
  alt: Flybot Logo
---

## TLDR

Following our successful POC with Rama for analytics, we evolved the stack into a generic, reusable platform called **Hibou** (French for "owl", known for their excellent eyesight).

The goal was to transform our client-specific implementation into a **business-agnostic** analytics suite that can serve multiple clients with different data requirements.


## Context

This project builds upon the foundation established in our [Gaming Stats Aggregation with Rama](http://loicb.dev/blog/game-stats-rama) POC.

While the POC proved that Rama could handle our analytics needs, it had limitations:
- One monolithic analytics module
- Implementation tightly coupled to a specific client's needs
- Difficult to maintain and extend
- API and module configurations were not aligned

The next logical step was to create a **generic analytics platform** that could be configured for different use cases without rewriting core logic.

## Architecture

The `hibou` stack is organized as a **mono-repo** composed of multiple components:

- `hibou-analytics` : A generic Rama module builder for analytics
- `hibou-dashboards` : A pre-built Rama module for dashboard storage
- `hibou-api`: Server providing authentication and Rama interactions via pullable API
- `hibou-ui` : Replicant-based web app for the UI

Each component is designed to be business-agnostic, allowing different clients to leverage the platform with minimal customization.

## hibou-analytics: Module Builder

The core innovation is treating the analytics module as a **configuration-driven** system.

Instead of writing Rama code for each new client, we provide a `defclient-module` function that generates an entire Rama module from an EDN config file validated by Malli schema.

The module builder supports:
- **Multiple depot types**: Rama native depots and Kafka external depots
- **Flexible PState storage**: Nested maps for dimensions with arbitrary leaf metrics
- **Microbatch topologies**: Configurable partitioning strategies
- **Query topologies**: Predefined queries that always aggregate by granularity/bucket for efficiency
- **Cross-module joins**: Mirror queries to aggregate data across different modules

This approach makes the analytics layer truly generic. The same codebase works for any event-driven data source.

## Configuration as the Source of Truth

One of the key design decisions was to make **annotated EDN configs** the single source of truth for the entire stack.

A client repo only needs to provide EDN config files for its modules. These configs are then reused across:

1. **Module Generation**: The config defines the Rama module structure
2. **API Validation**: The API derives its Malli schemas directly from the configs
3. **UI Form Building**: The frontend generates query forms using the same configs

This eliminates the need to maintain separate schemas in three different places. When you update your module config, the API validation and UI forms automatically adapt.

To make this work, we use **annotations**, which are namespaced keywords that provide metadata for the API and UI layers without affecting Rama's interpretation of the config.

## Dashboards as Data

Initially, I considered using Datomic to store user dashboards. However, a colleague suggested: "why not store them with Rama since we're already using it for analytics?"

This made total sense. Commit fully to the platform.

The `hibou-dashboards` component is a simple Rama module that handles:
- Storing dashboards in a PState
- Processing commands via a depot (save, edit, delete)
- Querying user-specific and public dashboards

Having dashboards in Rama keeps everything in one place and leverages the same infrastructure.

## API: Encapsulation Layer

Like in our POC, we avoid using Rama's built-in REST API directly. Instead, `hibou-api` provides a **pullable** interface using [lasagna-pull](https://github.com/flybot-sg/lasagna-pull).

The API:
- Validates all queries using Malli schemas before hitting the Rama cluster
- Provides authentication/authorization via SSO tokens
- Encapsulates all Rama complexity from the frontend
- Represents the entire API as pure EDN data structures

This approach means the frontend never needs to know about Rama's internals. It just sends EDN queries and receives EDN responses.

## UI: Query Builder and Visualizations

The `hibou-ui` component uses [Replicant](https://replicant.fun/) for rendering and ECharts for visualizations.

The query editor is particularly interesting. It's a form builder that generates itself from the Malli schemas derived from the module configs. Users build queries visually with proper validation, but the underlying representation is always pure EDN.

Supported visualizations include:
- Overall aggregated summaries
- Tables with change indicators
- Bar charts and pie charts
- Time series line charts
- Record tables for detailed views

## Mono Repo Structure

Following our experience with [Clojure Mono Repos](http://loicb.dev/blog/clojure-mono-repo), we organized `hibou` as a mono-repo with clear component boundaries.

Each component has its own:
- Source directories
- Test suites
- README documentation
- CI/CD tasks

We use [Babashka tasks](https://book.babashka.org/#tasks) as our task runner across all components, making it easy to build, test, and deploy each part independently or together.

## Client Integration

A client repo integrates the hibou stack by:

1. Defining annotated EDN configs for their analytics modules
2. Using `hibou-analytics` to build and deploy Rama modules
3. Using `hibou-api` to create an authenticated web server
4. Embedding `hibou-ui` for the frontend

The client repo is minimal. It's mostly just configuration. All the heavy lifting is done by the reusable hibou components.

## Testing Strategy

We use multiple testing approaches:

- [rich-comment-tests](https://github.com/matthewdowney/rich-comment-tests) for quick REPL-driven tests
- [Kaocha](https://github.com/lambdaisland/kaocha) for module-level and integration tests
- [tools.build](https://github.com/clojure/tools.build) to build uberjars for testing
- Rama's In-Process Cluster (IPC) for unit testing modules

The configuration-driven approach makes testing easier. We can generate test modules with different configs and verify behavior systematically.

## Deployment

We use several tools for deployment:

- [Jibbit](https://github.com/atomisthq/jibbit) to build container images without Docker
- Custom Rama CI tools for deploying modules from deployment files
- EKS deployment configs for the API server pods

The separation between the Rama cluster (hosting analytics and dashboard modules) and the EKS cluster (hosting the API/UI) allows us to scale each independently.

## Results

The hibou platform successfully transformed our POC into a production-ready system.

We now have:
- A generic analytics platform that works with any event-driven data
- Reduced development time for new clients (just write configs)
- A single source of truth via annotated EDN files
- Full-stack Clojure from data ingestion to UI
- Clean separation of concerns across components

## Limitations and Future Work

While the platform is functional, there are areas for improvement:

- Query logic can be optimized further
- Only outer joins are fully supported (inner/left joins are WIP)
- Dashboard features are still basic (no admin operations, duplication, ownership transfer)
- Config format could be more user-friendly
- The Rama analytics PStates are only storing **rolled-up** data as for now

## Conclusion

Building hibou taught us valuable lessons about designing for reusability from the start.

By treating everything as data (from module definitions to API endpoints to UI forms), we created a flexible system that adapts to different requirements without code changes.

The combination of Rama for distributed stream processing, lasagna-pull for pure data APIs, and Replicant for reactive UIs created a coherent full-stack platform that leverages Clojure's strengths at every layer.

The key was resisting the temptation to optimize for the first client and instead investing time upfront to build generic, configurable components.


