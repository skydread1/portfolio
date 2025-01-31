#:post{:id "game-challenge-recommender"
       :page :blog
       :home-page? true
       :employer "Flybot Pte Ltd" 
       :date ["2023-08-08"]
       :title "Challenge Recommender"
       :tags ["Clojure" "Kafka" "Datomic" "Kubernetes" "AWS EKS"]
       :css-class "props-recommender"
       :image #:image{:src "https://www.flybot.sg/assets/flybot-logo.png"
                      :src-dark "https://www.flybot.sg/assets/flybot-logo.png"
                      :alt "Flybot Logo"}}
+++
I worked on a recommender system stack that suggests user-based challenges to [Golden Island](https://www.80166.com/)'s players. `Golden Island` is a card/board game platforms that offers a dozen of games including *Pǎo Dé Kuài* (跑得快), *Dou dizhu* (鬥地主), *Mahjong* etc.

Some game operators are already creating props (challenges) that are personalized in the sense that they apply only to a subset of users (game, levels, coin balance etc) in response to a subset of events (game, bet size etc).

The end goal was to ease the job of game operators by taking care of `when` to recommend challenges and to `whom`, so the game operators can focus on the `what` the challenges could be and have constant performance feedback.

The recommender is made of **Clojure** applications deployed in PODs in `AWS EKS` that consume events from `kafka` topics and produces props (personalized challenges) to a dedicated kafka topic. It uses `Datomic` as storage solution. The app can query multiple recommender engines to perform A/B testing, meaning that we can run 2 recommenders at once and compare their performance.
+++
## Rational

I worked on a recommender system stack that suggests user-based challenges to [Golden Island](https://www.80166.com/)'s players. `Golden Island` is a card/board game platforms that offers a dozen of games including *Pǎo Dé Kuài* (跑得快), *Dou dizhu* (鬥地主), *Mahjong* etc.

Some game operators are already creating props (challenges) that are personalized in the sense that they apply only to a subset of users (game, levels, coin balance etc) in response to a subset of events (game, bet size etc).

The end goal was to ease the job of game operators by taking care of `when` to recommend challenges and to `whom`, so the game operators can focus on the `what` the challenges could be and have constant performance feedback.

The recommender is made of **Clojure** applications deployed in PODs in `AWS EKS` that consume events from `kafka` topics and produces props (personalized challenges) to a dedicated kafka topic. It uses `Datomic` as storage solution. The app can query multiple recommender engines to perform A/B testing, meaning that we can run 2 recommenders at once and compare their performance.

I will highlight the different decisions taken over time to show my process of thinking and how I got to the final design.

The repositories are closed-source because private to Flybot Pte. Ltd.

## Exploring existing solution

### Google recommendation AI

**Google Recommendations AI** is a machine learning service provided by Google that enables businesses to build and scale recommendation systems. We wanted to try to plug our data to it for the recommendation of our props (personalized challenges given to the users). Leveraging existing solution is often the first step to take to avoid overengineering things unnecessarily.

The Google Recommendation AI API endpoints expect data as JSON and it is straight forward to convert EDN to JSON so that was not an issue for us. However, I realized that the API was designed towards traditional e-commerce platform with events such as `detail-page-view`, `add-to-card` or `purchase-complete`. The main issue is that it requires `detail-page-view` events and there is nothing matching this in our gaming platform. Our platform suggest challenges to our users in a `prop-bar` and the user can `use`, `discard` or ignore the prop which will `expire` after a few. The prop are `generated` server-side. 

Moreover, The google api as a `click-direct-event` event which highlights when a user clicks a product but in our case the props are dropped to the player's prop-bar directly. So we cannot use `client-direct-event` because it only provides the positive results (when a user actually pressed “yes” to accept the prop) so it would mean with always have a CTR of 100% which is wrong.

Therefore, I deemed the Google Recommendations AI not suitable in our case because our gaming platform event flow does not follow e-commerce website and coercing our data to what the API expects would yield wrong predictions. So we needed to come up with our own solution.

## Data Analysis

### Python interop

Still with in mind to try to not reinvent the wheal, we had a look at libraries of the most popular language for ML: python. Using sample datasets, I wrote some simple Jupyter Notebooks trying some algo such as Random Forest Classification using `pandas` and `scikit-learn`. 

Of course, as Clojure developers, we quickly wanted to get instant feedback in our REPL so I experimented with [libpython-clj](https://github.com/clj-python/libpython-clj) which provides a deep Clojure/Python Integration so we are able to load/use python modules almost as if they were Clojure namespaces. And the Jupyter Notebooks became [clerk](https://github.com/nextjournal/clerk) notebooks and pandas was put aside to leave way to [tablecloth](https://github.com/scicloj/tablecloth).

The above work helped us understand the data we would need to deal with but it was just providing some notebook with data analysis for the game operators. It was time to work on a POC: an actual simple recommender that we can deploy and put to use.

## Deploying a POC

### Simple random recommender

The gaming platform we have follows an **event-driven** architecture using `Kafka` and multiple single-responsibility apps that are deployed in an AWS EKS cluster. So the recommender will be a Clojure app and deployed in the EKS cluster as well.

The first scope of the app was to just provide a **random** recommender. The app consumes from the different relevant event topics, stores data in Datomic and produces recommendation to a recommendation kafka topic.

Regarding our Clojure stack I use almost exclusively open-source libraries. We can note a few excellent libraries from the CEO himself [robertluo](https://github.com/robertluo):
- [fun-map](https://github.com/robertluo/fun-map): for systems (associative dependency injection), you can read more about it in this article [Fun-Map: a solution to deps injection in Clojure](https://www.loicblanchard.me/blog/fun-map).
- [waterfall](https://github.com/robertluo/waterfall):  minimalist kafka interaction library with additional optional batteries included.

Regarding CI/CD, I just needed to generate a container image in our Gitlab container registry for the devops to pick up for deployment in the EKS cluster. I use [jibbit](https://github.com/atomisthq/jibbit), a docker-less clojure image builder for the deps.edn (project dependencies).

So, I had a simple implementation of an app that does the following:
- consumes user game events and produces random recommendations

## Client feedback

### Operational from game operators

At this point, we actually had a new junior developer ready to work with me on the recommender. Once I got feedback from the game operators for the POC, we started addressing the problems right away. The main issue was that the recommender was spamming players because it was recommending on every user `game-finish` events. We needed to add some API calls to ensure the user was not already having an on-going challenge to avoid accidentally spamming his `prop-bar`.

Also we introduced Datomic for internal storage and added another kafka consumer to accept challenge addition/deletion in real time so the game operators could add new challenges to the recommender without requiring to restart the instance.

### Performance feedback from developers

Even if we were only recommending random props to the users, having a way to evaluate the use-rate of our challenges and other key points were necessary to have a reference to compare future versions with. We reused the `clerk` + `tablecloth` libs for that so we could highlight results programmatically with Clojure.

So at his stage, I had a working random recommender and a way to evaluate its performance and show that results to the game operators. It was time to actually work on improving the data aggregation for the model and come up with a better recommendation algo.

## Separation of concerns

As I was working on how to get relevant data for the model and implements the kafka/system logic to store it in Datomic, my colleague was working on a simple `popularity-based` recommender. The system was having a new component with a batch job to train the model on a daily basis using the data stored in Datomic. The predicted challenges were stored in user "queues" ready to be recommended on their next event.

The `popularity-based` was quickly replaced by a `collaborative filtering` algo that started to take more resources and time to compute. I also heard from my CEO that we could potentially use third party recommender engine web APIs to get recommendations. Therefore, I took the decision to extract the recommender engine in another repo. So we had 2 repos:
- one "kafka app": that consumes events, call a recommender engine over http and produce the recommendations. It also consumes from additional topics to aggregate data for the model.
- one "recommender engine app" that provides an http endpoint accepting a user event and responding a list of recommendations.

While I assigned my colleague to work on the recommender algo, I improved the kafka app to be able to accept multiple recommender engines so we could have a subset of users using some new recommender engines etc (like for A/B testing).

## Test and monitoring

Of course, throughout the development we had a good test suite with integration tests. You can have a look at my article regarding [Testing in Clojure](https://www.loicblanchard.me/blog/testing-in-clojure).

For the local load/stress tests, I opted for the following strategy: the external services are containerized but the Clojure REPL runs outside of Docker so I use it the same way I would use it for embedded services and regular unit tests.

Therefore, the development workflow for stress tests was simple: just run *docker-compose* for the services and then run the tests in the REPL/terminal as per usual. The containerized services were the following:
- datomic transactor with pre-populated DB with millions of entries
- confluent [cp-all-in-one](https://github.com/confluentinc/cp-all-in-one) containers for kafka
- prometheus and grafana (useful to prepare dashboards that could also be used in test and prod clusters)

## Current results

As for now, the recommender engin is performing a bit better than randomly recommending props so it still needs improvement. However, the kafka app is able to accept different recommenders to use and how to split the users in multiple groups for A/B testing. Game operators can add/remove challenges on the fly and they can even provide some user and event predicate to enforce some model constraint based on how they design challenged campaign.

## Conclusion

We could recap the steps I took like this:
1. Data analysis
       - try to coerce our data to use existing recommender engine API endpoints.
       - use python because it has a rich range of libraries for ML for initial dataset analysis
       - port the Jupyter Notebook python logic to Clojure using Clerk Notebook because we use Clojure for all our projects
2. POC
       - create a simple POC that can be deployed in existing AWS EKS cluster and use random recommendation
       - Tests and CI/CD are steady
3. Automation
       - fix wrong behavior with game operator's feedback
       - Make it easy to use
4. Scale
       - Extra the model training and queue computations from the kafka logic
5. Performance
       - Once the app running fine, start improving recommender algo
       - Make the kafka app suitable to use multiple recommender engines and improve data ingestion
