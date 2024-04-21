#:post{:id "mcts"
       :page :portfolio
       :date ["2021-07-19" "2021-08-13"]
       :articles [["MCTS applied to card games" "../blog/article-mcts"]]
       :title "MCTS applied to card games"
       :css-class "mcts"
       :tags ["MCTS" "Monte Carlo Tree Search" "Clojure" "Card Game"]
       :image #:image{:src "https://www.flybot.sg/assets/flybot-logo.png"
                      :src-dark "https://www.flybot.sg/assets/flybot-logo.png"
                      :alt "Flybot Logo"}}
+++
At Flybot, I had the opportunity to work on a Monte Carlo Tree Search (MCTS) **Clojure** implementation for our card games.

I combined `MCTS` with `domain knowledge` of our games to balance quality and performance of the plays.

A few of our games implement the same `Game` protocol so the MCTS implementation is independent of the underlying game.
+++
## Rational

At Flybot Pte Ltd, one of my projects was to develop a robot-player capable of playing multiple rounds of card games, such as the popular Chinese card game `big-two`.

A few of our games implement the same protocol so the implementation can be independent of the underlying game.

The primary objective was to create an AI that could replace AFK players and provide varying levels of difficulty for offline gameplay.

To achieve this, I explored two key approaches: 
- **Monte Carlo Tree Search** (MCTS)
- **Domain knowledge**

## MCTS Theory

Monte Carlo Tree Search (MCTS) is a powerful algorithm known for its role in the success of AI applications like AlphaGo. At its core, MCTS leverages Monte Carlo simulations to guide the search for highly rewarding paths in a game tree. This approach is essential for games with deterministic rules and perfect information, where players have complete knowledge of the game state and no chance events occur.

However, card games like `big-two` introduce imperfect information, as players do not have access to their opponents' cards information. To apply MCTS to such games, we need to do one of the following:
- Pre-select moves by filtering the dumb moves
- Access hidden information (the other player’s hand). This method is called *Determinization* or also *Perfect Information Monte Carlo Sampling*.

## MCTS Implementation

Our MCTS implementation involves representing the game tree as a collection of nodes, where each node corresponds to a specific game state. These nodes store relevant statistics, including visit counts and scores.

The MCTS process can be broken down into four key steps:

- `Selection`: Determining which child node to explore next.
- `Expansion`: Adding newly selected child nodes to the tree.
- `Simulation`: Running multiple game scenarios with random moves and evaluating the AI's total score.
- `Update`: Back-propagating rewards from simulations to update branch nodes in the tree.

## MCTS Iteration

A complete MCTS iteration comprises the four steps mentioned above: `expand`, `select`, `simulate`, and `update`.

The more iterations we run, the more accurate our tree becomes, but this comes at the cost of increased computation time.

## MCTS for Games with More Than 2 Players

When dealing with games involving more than two players, such as `big-two` with has four players, we must consider the scores of all participants. Each robot (player) aims at maximizing their score, and `UCT` (Upper Confidence Bound  applied to trees) values are computed based on the concerned robot's score.

## Caching

Caching plays a critical role in optimizing performance. By caching possible children states and sampled states, we reduce redundant computations and speed up the AI's decision-making process.

## Performance Issues

Performance challenges can arise, especially at the beginning of a game with numerous possible moves. To address this, I introduced conditions to trigger MCTS only when the number of remaining cards in players' hands falls below a certain threshold. This significantly improved computational efficiency.

## Domain Knowledge

One limitation of MCTS is its tendency to explore non-promising branches. To overcome this, I incorporated domain knowledge for the initial game moves. This domain knowledge includes a game plan that guides the AI's decision-making process. However, the specific details of this game plan remain confidential.

## Conclusion

In conclusion, the fusion of MCTS and domain knowledge has enabled us to create a functional AI for big-two that can replace human players and offer different levels of difficulty. As of the time of writing this article in 2023, the implementation is currently undergoing testing as part of a larger system and is not yet in production. This hybrid approach represents a promising solution for developing robust AI players in complex card games.
