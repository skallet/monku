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

(defn card-move->position [piece card move]
  (let [player (-> piece second :player)
        piece-coord   (first piece)
        card-start    (card->start-coords card)
        with-rotation (fn [n]
                        (if (= player :white) n (- n)))
        {:keys [x y]} move
        card-offset-x (with-rotation (- (:x card-start) x))
        card-offset-y (with-rotation (- (:y card-start) y))
        new-x         (- (:x piece-coord) card-offset-x)
        new-y         (- (:y piece-coord) card-offset-y)]
    {:x new-x :y new-y}))

(defn ->moves [board-state piece card]
  (->> card
       (card->move-coords)
       (mapv #(card-move->position piece card %))
       (filterv #(valid-move? board-state piece %))))

(defn valid-card-move? [board-state piece card move]
  (let [moves (into #{} (->moves board-state piece card))]
    (contains? moves move)))

(defn get-player-cards [board-state player]
  (get-in board-state [:cards player] []))

(defn player-has-given-card? [board-state player card]
  (let [player-cards (get-player-cards board-state player)]
    (some (fn [player-card]
            (= player-card card))
          player-cards)))

(defn do-move [board-state piece card move]
  (let [player  (-> piece second :player)
        playing (get board-state :player)
        next-playing (if (= :white playing) :black :white)]
    (if-not (and (valid-card-move? board-state piece card move)
                 (player-has-given-card? board-state player card)
                 (= player playing))
      board-state
      (-> board-state
          (assoc :player next-playing)
          (update :moves conj [piece card move])
          (update-in [:cards playing] #(filter (fn [player-card]
                                                 (not= player-card card))
                                               %))
          (update-in [:cards next-playing] conj card)
          (update :pieces (fn [pieces]
                            (->> pieces
                                 (filter (fn [p]
                                           (not= (first p) move)))
                                 (map (fn [p]
                                        (if (= p piece)
                                          [move (second p)]
                                          p))))))))))

(defn get-only-types [pieces type]
  (->> pieces
       (filter (fn [[_ {:keys [piece]}]] (= type piece)))
       (map (fn [[coords {:keys [player]}]] [coords player]))))

(defn get-only-player-pieces [pieces given-player]
  (filter (fn [[_ {:keys [player]}]] (= given-player player))
          pieces))

(defn get-monks [pieces]
  "Extract all monks from pieces with their positions and players"
  (get-only-types pieces :monk))

(defn get-temples [board-state]
  "Extract temples from board config"
  (get-in board-state [:config :temples] []))

(defn monk-at-temple? [monks temples player temple-color]
  "Check if a player's monk is at a temple of given color"
  (let [player-monks   (into #{} (map first (filter #(= player (second %)) monks)))
        target-temples (into #{} (map first (filter #(= temple-color (second %)) temples)))]
    (seq (clojure.set/intersection player-monks target-temples))))

(defn player-has-monk? [monks player]
  "Check if player still has their monk on the board"
  (some #(= player (second %)) monks))

(defn player-won? [board-state player]
  "Check if given player has won the game"
  (let [pieces                (get board-state :pieces)
        monks                 (get-monks pieces)
        temples               (get-temples board-state)
        opponent              (if (= player :white) :black :white)
        opponent-temple-color opponent]
    (or
     ;; Win condition 1: Player's monk reached opponent's temple
     (monk-at-temple? monks temples player opponent-temple-color)

     ;; Win condition 2: Opponent's monk was captured
     (not (player-has-monk? monks opponent)))))

(defn get-winner [board-state]
  "Determine the winner of the game, returns :white, :black, or nil"
  (cond
    (player-won? board-state :white) :white
    (player-won? board-state :black) :black
    :else nil))

(defn game-finished? [board-state]
  (some? (get-winner board-state)))

(defn update-status [board-state]
  (let [winner (get-winner board-state)]
    (if winner
      (-> board-state
          (assoc :status :finished)
          (assoc :winner winner))
      board-state)))

(defn calculate-board-score [board-state]
  (let [pieces (get board-state :pieces)
        white-pieces (get-only-player-pieces pieces :white)
        black-pieces (get-only-player-pieces pieces :black)]
    (cond
      (player-won? board-state :white) 1000
      (player-won? board-state :black) -1000
      :else (- (count white-pieces) (count black-pieces)))))

(comment
  (let [config      {:size        5
                     :show-coords false
                     :monk-x      2
                     :temples     [[{:x 2 :y 0} :white]
                                   [{:x 2 :y 4} :black]]
                     :players     [[0 :white]
                                   [4 :black]]}
        _           (portal.api/clear)
        board       (monku.game.board/initialize-board-state config)
        cards       monku.game.cards/game-cards
        card        (nth cards 0)
        ;; card        [[{:x 2 :y 1} :piece] [{:x 2 :y 4} :move]]
        piece-white (nth board 2)
        piece-black (nth board 7)
        pieces      (map (fn [[c _]]
                           [c :piece])
                         [piece-white piece-black])
        state       {:pieces board
                     :player :white
                     :status :playing
                     :cards  {:white [card
                                      (nth cards 1)
                                      (nth cards 2)]
                              :black [(nth cards 3)
                                      (nth cards 4)]}
                     :moves  []
                     :config config}
        moves-white (->moves state piece-white card)
        moves-black (->moves state piece-black card)
        moves       (->> (concat moves-white moves-black)
                         (map (fn [c] [c :move])))
        move        (when (seq moves-white)
                      (rand-nth moves-white))
        ;; move        {:x 1 :y 2}
        next-state  (-> state
                        (do-move piece-white card move)
                        (update-status))]
    (monku.game.visualizer/display-board-and-cards state [card
                                                          (concat pieces moves)
                                                          [[(first piece-white) :piece]
                                                           [move :move]]])
    (monku.game.visualizer/display-board-and-cards next-state
                                                   (get-in next-state [:cards :black]))
    (tap> next-state)))
