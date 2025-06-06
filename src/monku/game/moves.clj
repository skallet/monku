(ns monku.game.moves
  (:require
   [monku.game.cards :refer [card->start-coords
                             card->move-coords]]
   [monku.game.visualizer :as sut]))

(defn valid-move? [board-state piece move]
  (let [player (-> piece second :player)
        positions (into {} (get board-state :pieces))
        collision (get positions move)
        board-size (get-in board-state [:config :size] 0)]
    (and (<= 0 (:x move) (dec board-size))
         (<= 0 (:y move) (dec board-size))
         (or (not collision)
             (not= player (:player collision))))))

(defn card-move->position [move card piece]
  (let [player (-> piece second :player)
        piece-coord   (first piece)
        card-start    (card->start-coords card)
        with-rotation (fn [n]
                        (if (= player :white) n (- 0 n)))
        {:keys [x y]} move
        card-offset-x (with-rotation (- (:x card-start) x))
        card-offset-y (with-rotation (- (:y card-start) y))
        new-x         (- (:x piece-coord) card-offset-x)
        new-y         (- (:y piece-coord) card-offset-y)]
    {:x new-x :y new-y}))

(defn ->moves [board-state piece card]
  (->> card
       (card->move-coords)
       (mapv #(card-move->position % card piece))
       (filterv #(valid-move? board-state piece %))))

(comment
  (let [config      {:size    5
                     :monk-x  2
                     :temples [[{:x 2 :y 0} :white]
                               [{:x 2 :y 4} :black]]
                     :players [[0 :white]
                               [4 :black]]}
        _           (portal.api/clear)
        board       (monku.game.board/initialize-board-state config)
        cards       monku.game.cards/game-cards
        card        (nth cards 0)
        piece-white (nth board 2)
        piece-black (nth board 7)
        state       {:pieces board
                     :config config}
        pieces      (map (fn [[c _]]
                           [c :piece])
                         [piece-white piece-black])
        moves       (->> [piece-white piece-black]
                         (mapcat #(->moves state % card))
                         (map (fn [c] [c :move])))]
    (tap> [:valid-moves (count moves)])
    (monku.game.visualizer/display-card card config)
    (monku.game.visualizer/display-board state)
    (monku.game.visualizer/display-card (concat pieces moves) config)))
