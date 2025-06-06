(ns monku.game.core
  (:require
   [monku.game.cards :refer [game-cards]]
   [monku.game.board :refer [initialize-board-state]]))

(defn initialize-game
  [{:keys [starting-player
           starting-monk-x
           temples
           board-size
           card-deck
           player-rows]
    :or   {starting-player :white
           starting-monk-x 2
           board-size      5
           card-deck       game-cards
           temples         [[{:x 2 :y 0} :white]
                            [{:x 2 :y 4} :black]]
           player-rows [[0 :white]
                        [4 :black]]}}]
  (let [config {:size    board-size
                :monk-x  starting-monk-x
                :temples temples
                :players player-rows}
        cards (shuffle card-deck)
        n-cards-white (if (= starting-player :white) 3 2)
        n-cards-black (if (= starting-player :black) 3 2)
        cards-white (take n-cards-white cards)
        cards-black (->> cards
                         (drop n-cards-white)
                         (take n-cards-black))]
    {:config config
     :pieces (initialize-board-state config)
     :cards {:white cards-white
             :black cards-black}
     :player starting-player
     :status :playing}))

(comment
  (let [state (initialize-game {})]
    (portal.api/clear)
    (monku.game.visualizer/display-board-and-cards state
                                                   (get-in state [:cards :white]))
    (monku.game.visualizer/display-board-and-cards (assoc state :black-pov true)
                                                   (get-in state [:cards :black]))))
