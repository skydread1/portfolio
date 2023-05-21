#:post{:id "card-games-api"
       :order 0
       :page :home
       :title "2020-2021 | Card Games Backends"
       :css-class "card-games"
       :image-beside #:image{:src "assets/cards.jpg"
                             :src-dark "assets/cards.jpg"
                             :alt "Card Deck"}}
+++
# Flybot: Clojure Card Games Backend APIs (2020 - 2021)

## Rational

At Flybot, I had the opportunity to create popular Asian Card Games API.

I worked on game such as Pǎo Dé Kuài (跑得快) and Big two (锄大地) which are climbing card games.

I also worked on a Library we called MetaGame that allows us to compose several `pdk` or `big-two` (or a mix of both) in tournaments for instance.

The repositories are closed-source because private to Flybot Pte. Ltd.

## Immutable data

Since we use Clojure, the game state can be represented as pure edn data levering `records`, `protocols` and `datafy`.

There is no need for any atoms, agent or vars as the new state is just another Clojure pure data structure.

This allows us to represent the game setup and rules as clojure pure data as well so the game is easy to custom.

## Data validation and Generation

We used
- [clojure/spec.alpha](https://github.com/clojure/spec.alpha) for the data registry that is used for data validation and generation.
- [clojure/test.check](https://github.com/clojure/test.check) to create custom generators to overcome the interdependence between the API functions inputs.

## CLR interoperability

It is more common to see interop with JavaScript for ClojureScript. However, in our case, we want our Clojure codebase to be run in the game engin Unity, so a dotnet environment.

Is is now possible to compile a Clojure project to dotnet assemblies and make it work in Unity using the [nasser/magic](https://github.com/nasser/magic) compiler.
([clojure-clr](https://github.com/clojure/clojure-clr), which is the default clojure compiler to dotnet cannot work in Unity because it relies on the DLR.)

We use the reader conditionals in `.cljc` files to handle JVM/CLR interop in our project so we can run and test our Clojure project in both environments.

## Composing games

The first objective was to have a way to compose several games (such as `big-two` or `PDK`).

We wanted to be able to play several games up to a certain `score` target or up to a certain number of `rounds`.

The second objective was to allow the user to set up and run `tournaments`. A tournament is a sequence of stages in which we play `meta-games`.

Once again, we could leverage the Clojure data immutability to "describe" our `meta-game` in pure EDN format.

You can view a `meta-game` as a wrapper around a given sub-game such as `big-two` for instance.

We can have a `meta-game` of `meta-game` to create a `tournaments`.

This is made possible via making the different projects implements a specific Game protocol.

Therefore, it is possible to describe the rules of the `meta-game` using pure Clojure data and setup games such as:
- playing 3 games of `big-two` and add up the score of each players, winner is the one with highest score
- playing several round of `big-two` until one player wins 2 rounds
- playing one round of `big-two` then on round of `pdk`.
- playing a tournament in which the semi-finals are a 3 round `big-two` game and the finals a single `pdk` game

All our games work with any number of players (relevant to the rules of course), so for the tournaments stages, we can have 3 group of 4 players playing `big-two` in the semi, and the 3 winners playing `pdk` in the finals without any issues.

Note: the `meta-game` was also ported to the CLR successfully.

## Learn more

### Clojure projects

All the Clojure libraries are private
Flybot Ptd Ltd All Right Reserved

### Magic compiler

The magic compiler is open source and you can read more about my contribution in the dedicated section.