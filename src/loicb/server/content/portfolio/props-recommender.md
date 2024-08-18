#:post{:id "game-challenge-recommender"
       :page :portfolio
       :employer "Flybot Pte Ltd" 
       :date ["2023-08-08"]
       :title "Challenge Recommender"
       :tags ["Clojure" "Kafka" "Datomic" "Kubernetes" "AWS EKS"]
       :css-class "props-recommender"
       :image #:image{:src "https://www.flybot.sg/assets/flybot-logo.png"
                      :src-dark "https://www.flybot.sg/assets/flybot-logo.png"
                      :alt "Flybot Logo"}}
+++
I am currently working on a recommender system that suggests user-based challenges to [Golden Island](https://www.80166.com/)'s players. `Golden Island` is a card/board game platforms that offers a dozen of games including *Pǎo Dé Kuài* (跑得快), *Dou dizhu* (鬥地主), *Mahjong* etc.

Some game operators are already creating props (challenges) that are personalized in the sense that they apply only to a subset of users (game, levels, coin balance etc) in response to a subset of events (game, bet size etc).

The recommender is a **Clojure** application deployed in a POD in `AWS EKS` that consume events from `kafka` topics and produces personalized challenges to a dedicated kafka topic. It uses `Datomic` as storage solution within the EKS cluster. The app can query multiple recommender engines to perform A/B testing, meaning that we can run 2 recommenders at once and compare their performance.

The end goal is to ease the job of game operators by taking care of `when` to recommend challenges and to `whom`, so the game operators can focus on the `what` the challenges could be and have constant performance feedback.
+++
## Rational
I am currently working on a recommender system that suggests user-based challenges to [Golden Island](https://www.80166.com/)'s players. `Golden Island` is a card/board game platforms that offers a dozen of games including *Pǎo Dé Kuài* (跑得快), *Dou dizhu* (鬥地主), *Mahjong* etc.

Some game operators are already creating props (challenges) that are personalized in the sense that they apply only to a subset of users (game, levels, coin balance etc) in response to a subset of events (game, bet size etc).

The recommender is a **Clojure** application deployed in a POD in `AWS EKS` that consume events from `kafka` topics and produces personalized challenges to a dedicated kafka topic. It uses `Datomic` as storage solution within the EKS cluster. The app can query multiple recommender engines to perform A/B testing, meaning that we can run 2 recommenders at once and compare their performance.

The end goal is to ease the job of game operators by taking care of `when` to recommend challenges and to `whom`, so the game operators can focus on the `what` the challenges could be and have constant performance feedback.

The repositories are closed-source because private to Flybot Pte. Ltd.
