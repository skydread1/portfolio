#:post{:id "card-games-api"
       :page :blog
       :home-page? true
       :employer "Flybot Pte Ltd"
       :date ["2020-01-06" "2021-10-29"]
       :repos [["Magic" "https://github.com/nasser/magic"]]
       :articles [["Port your Clojure lib to the CLR with MAGIC" "../blog/port-clj-lib-to-clr"]]
       :title "Clojure Card Games Backend APIs"
       :css-class "card-games"
       :tags ["Clojure" "Clojure Spec" "Magic Compiler" "Interop CLR"]
       :image #:image{:src "https://www.flybot.sg/assets/flybot-logo.png"
                      :src-dark "https://www.flybot.sg/assets/flybot-logo.png"
                      :alt "Flybot Logo"}}
+++
At Flybot, I had the opportunity to create popular Asian Card Games APIs in **Clojure**.

I developed the backend of games such as *Pǎo Dé Kuài* (跑得快) and *Big two* (锄大地) which are climbing card games.

I also worked on a Library we called **MetaGame** that allows us to compose several `Pǎo Dé Kuài` or `Big two` games (or a mix of both). By composing games, I mean to play a few rounds of them up to a certain score for instance. This composition is generic enough to even allow us to make tournaments out of the underlying games.
+++
## Rational

At Flybot, I had the opportunity to create popular Asian Card Games APIs in **Clojure**.

I developed the backend of games such as *Pǎo Dé Kuài* (跑得快) and *Big two* (锄大地) which are climbing card games.

I also worked on a Library we called **MetaGame** that allows us to compose several `Pǎo Dé Kuài` or `Big two` games (or a mix of both) in tournaments for instance.

The repositories are closed-source because private to Flybot Pte. Ltd.

## Immutable data

Since we use Clojure, the game state can be represented as pure edn data leveraging `records`, `protocols` and `datafy`.

There is no need for any atoms, agent or vars as the new state is just another Clojure pure data structure.

This allows us to represent the game setup and rules as pure Clojure data as well so the game is easy to customize.

For non-clojure developers, you can imagine that you could represent your entire game state using a simple JSON file. In clojure, we use the [Extensible Data Notation](https://github.com/edn-format/edn)(EDN) format (which has a similar syntax to JSON).

## Data validation and Generation

The Clojure libraries I used are:
- [clojure/spec.alpha](https://github.com/clojure/spec.alpha) for the data registry that is used for data validation and generation.
- [clojure/test.check](https://github.com/clojure/test.check) to create custom generators to overcome the interdependence between the API functions inputs.

Using the libraries above, I design an integration test suite that can run hundreds for semi-random generated games (can be run in the CI as well) which ensure proper behavior of the API.

## CLR interoperability

It is more common to see interop with JavaScript for ClojureScript. However, in our case, we want our Clojure codebase to be run in the game engin Unity, so a dotnet environment.

Is is now possible to compile a Clojure project to dotnet assemblies and make it work in Unity using the [nasser/magic](https://github.com/nasser/magic) compiler. [clojure-clr](https://github.com/clojure/clojure-clr), which is the default clojure compiler to dotnet, cannot work in Unity because it relies on the DLR.

We use the reader conditionals in `.cljc` files to handle JVM/CLR interop in our project so we can run and test our Clojure project in both environments.

## Composing games

The first objective was to have a way to compose several games (such as `big-two` or `PDK`).

We wanted to be able to play several games up to a certain `score` target or up to a certain number of `rounds`.

The second objective was to allow the user to set up and run `tournaments`. A tournament is a sequence of stages in which we play `meta-games`.

Once again, we could leverage the Clojure data immutability to "describe" our `meta-game` in EDN format.

You can view a `meta-game` as a wrapper around a given sub-game such as `big-two` for instance.

We can have a `meta-game` of `meta-game` to create a `tournament`.

This is made possible via making the different projects implements a specific Game protocol.

Therefore, it is possible to describe the rules of the `meta-game` using pure Clojure data and setup games such as:
- playing 3 games of `big-two` and add up the score of each players, winner is the one with highest score
- playing several round of `big-two` until one player wins 2 rounds
- playing one round of `big-two` then on round of `pdk`.
- playing a tournament in which the semi-finals are a 3 round `big-two` game and the finals a single `pdk` game

All our games work with any number of players (relevant to the rules of course), so for the tournaments stages, we can have 3 group of 4 players playing `big-two` in the semi, and the 3 winners playing `pdk` in the finals without any issues.

Note: `meta-game` was also ported to the CLR successfully.

Once again, for non-clojure developers, you can imagine that your are describing a whole tournament setup using just a JSON file which is very powerful (but instead of JSON, we use EDN, the clojure equivalent).

## Learn more

### Clojure projects

All the Clojure libraries are private
Flybot Ptd Ltd All Right Reserved

### Magic compiler

The magic compiler is open source and you can read more about my contribution in the dedicated section.
