#:post{:id "game-stats-rama"
       :page :blog
       :home-page? true
       :employer "Flybot Pte Ltd"
       :date ["2024-08-12"]
       :title "Gaming Stats Aggregation with Rama"
       :css-class "stats-rama"
       :tags ["Clojure" "Rama" "AWS" "EKS" "Kafka"]
       :image #:image{:src "https://www.flybot.sg/assets/flybot-logo.png"
                      :src-dark "https://www.flybot.sg/assets/flybot-logo.png"
                      :alt "Flybot Logo"}}
+++
I am currently working on a project that aims at moving our gaming platform stat aggregation and visualization project to a new stack.

We have an event-driven system that produces lots of events in multiple kafka topics. Our current solution to aggregate stats and visualize them in dashboards is the setup [Apache Druid](https://druid.apache.org/) + [Imply](https://imply.io/).

As a **Clojure**-focused company, we decided to experiment with a new architecture: [Rama](https://redplanetlabs.com/learn-rama) + in-house full-stack Clojure Visualization tool.
+++
## Rationale

I am currently working on a project that aims at moving our gaming platform stat aggregation and visualization project to a new stack.

We have an event-driven system that produces lots of events in multiple kafka topics. Our current solution to aggregate stats and visualize them in dashboards is the setup [Apache Druid](https://druid.apache.org/) + [Imply](https://imply.io/).

As a **Clojure**-focused company, we decided to experiment with a new architecture: [Rama](https://redplanetlabs.com/learn-rama) + in-house full-stack Clojure Visualization tool.

In this article, I will show my reasoning step by step.

## Context

Rama being in Beta, it was quickly evolving as I was experimenting with it.

When I started experimenting with Rama, Rama version was *0.18.0*.

Nathan Marz guided me throughout the different releases and was always very prompt to help me understand Rama.

A very experienced colleague took care of the deployments in AWS as he is responsible for our servers and we worked together with Nathan to understand the AWS setup that could suit our needs the best.

Also, another colleague joined me on the app development after a while giving a fresh look to my design. So we were 3 engineers with different level of XP working on that project towards the end.

## Locally test a Rama module with External Kafka support

The first goal I set up was to just be able to run locally a simple Rama module that aggregates data from one external kafka topic in a PState and provide a few query topology to fetch data from that PState.

I used a `microbatch` topology because it has simpler fault-tolerance semantics, higher throughput, and is more expressive due to availability of batch blocks.

Rama provides an In-Process Cluster (IPC) to perform unit testing. So to ensure that the microbatch topology was working as expected and that the query topologies fetched data properly, I wrote some unit tests using the IPC.

Red Planet Labs provides a repo [rama-kafka](https://github.com/redplanetlabs/rama-kafka) to connect Rama depots to external kafka topics. So I was able to connect an external kafka cluster to the Rama module via the rama-kafka `KafkaExternalDepot`.

## Locally test a Rama module on a single-node cluster

The IPC allows you to run everything in one JVM process.

Worker processes are simulated as threads.

So you get parallel processing to test the behavior of your app but you do not get distributed parallel processing as you would do in prod that have multiple processes and multiple machines.

Running a [single node cluster](https://redplanetlabs.com/docs/~/operating-rama.html#_running_single_node_cluster) locally can allow you to test your modules on multiple processes.

While the cluster runs on a single machine, it does start multiple processes which enables the actual distribution:

- **Realistic Behavior**: The single-node cluster mimics a distributed system more accurately by involving actual network communication.
- **Serialization Testing**: Messages and states are serialized and deserialized as they pass between processes, uncovering potential serialization issues that might not appear in IPC.
- **Partition and Worker Distribution**: You can observe how partitions are distributed among worker processes and test real parallelism.
- **Failure Handling**: You can simulate scenarios like worker crashes or Zookeeper downtime.

Therefore, I uberjar the rama module and deployed it on a single-node cluster on my machine to ensure it was working as expected. I was able to query data from my REPL using the `foreign-query` function.

## test a Rama module on a AWS multi-node Cluster

My colleague set up a multi-node Rama cluster on our AWS. We then deployed the telemetry module provided my Rama (to get the cluster UI metrics) amd our own module with success.

## Designing an API

Rama has a built-in [REST API](https://redplanetlabs.com/docs/~/rest.html#gsc.tab=0) for doing depot appends, PState queries, and query topology invokes with HTTP requests. This allow Rama modules to be read and written to from any programming language, as well as allowing ad-hoc queries to be done with tools like curl.

The REST API uses JSON to represent arguments and responses. All requests are done using POST requests so that JSON can be provided in the body.

However, in our case, we use Clojure and we of course prefer to use EDN for our queries. Furthermore, we want to avoid hitting the Rama cluster with invalid requests.

So I decided to have a dedicated app that encapsulates the Rama logic. By the nature of our analytics use case, we never need to fetch data from a specific partition only (via `foreign-select`). The dashboards always fetch multiple games, multiple users, etc. So we always fetch Pstate data across multiple partitions therefore we always interact with the Cluster using query topologies (via `foreign-query`) .

We do not want the dashboard app (client) to require any knowledge about Rama design. Hence the custom rama-api app that provides malli schema validation of any input given. The rama-api app perform the correct foreign queries if the http request contains a valid pure EDN query such as:

```clojure
{:time-window {:kind :absolute
               :from "2024-08-20T09:00:00+08:00"
               :to "2024-08-20T10:00:00+08:00"}
 :filters {:seed-ids "fuc|tax-up"
           :reward-types #{:reward.type/task}}
 :group-by-keys [:reward-type :__time]}
```

Actually, we even use the [lasagna-pull](https://github.com/flybot-sg/lasagna-pull) to represent our API as pure clojure data (you can read more about it in [here](http://localhost:9500/blog/lasagna-pull-applied-to-flybot))

So to sum up, we use our own API instead of built-in Rama REST API. Our API only uses pull request and represents the API endpoints as one single pullable (via a pattern) EDN data structure. The client only hits the rama-api server so the rama logic is never exposed. The server also takes care of authorization.

## Local Client

As I was working on our rama-api app, a colleague started working on a local client first.

I suggested to use a Clerk notebook first just to display statically the data in a good UI locally for a quick POC. My colleague went a step further and made the Clerk notebook dynamic leveraging reagent and [clerk-sync](https://book.clerk.vision/#clerk-sync):

> Clerk Sync is a way to support lightweight interactivity between Clerk's render display running in the browser and the JVM. By flagging a form defining an atom with ::clerk/sync metadata, Clerk will sync this atom to Clerk's render environment. It will also watch recompute the notebook whenever the value inside the atom changes.

So the clerk notebook pretty much behaved like a classic reagent app.

Thus, the client hits the rama-api via http post requests on the JVM and render the results in graphs on the browser via the clerk sync atom. The user interact with the browser to modify the queries (all represented as EDN) and the dashboard is refresh every 5min.

## Supporting more topics

We have many kafka topics and we have lots of different kind of metrics we want to aggregate and monitor on our gaming platform. So we then focused on improving the implementation of the backend (the rama module).

As Red Planet Labs release new RAMA versions and with the feedback from Nathan Marz, we are able to improve the implementation of our rama app and also improve our AWS setup to have an acceptable performance. It is still work in progress but we are getting there.

## Full-stack web app for the client

Instead of having the client being just a local clerk notebook, I am switching to a full-stack Clojure app so game operators can save their dashboards and also share dashboards with others etc (similar to what they could do with Imply). I already designed full-stack web-app before so it should not take us too long to have something ready. It is work in progress as well.

