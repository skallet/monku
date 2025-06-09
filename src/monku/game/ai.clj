(ns monku.game.ai
  (:require
   [monku.game.moves :refer [get-only-player-pieces
                             get-player-cards
                             ->moves
                             do-move
                             update-status
                             game-finished?
                             calculate-board-score]]))

(defn generate-all-moves [board-state player]
  "Generate all possible moves for a player in the current board state"
  (let [pieces (get-only-player-pieces (:pieces board-state) player)
        cards (get-player-cards board-state player)]
    (for [piece pieces
          card cards
          move (->moves board-state piece card)]
      {:piece piece
       :card card
       :move move
       :player player})))

(defn apply-move [board-state move-data]
  "Apply a move to the board state and return the new state"
  (let [{:keys [piece card move]} move-data]
    (-> board-state
        (do-move piece card move)
        (update-status))))

(defn ->score [board-state]
  (calculate-board-score board-state))

(defn- minimax
  [board-state depth maximizing? alpha beta]
  (cond
    (or (>= 0 depth) (game-finished? board-state))
    (->score board-state)

    maximizing?
    (let [moves (generate-all-moves board-state (:player board-state))]
      (loop [moves    moves
             max-eval Double/NEGATIVE_INFINITY
             alpha    alpha]
        (if (empty? moves)
          max-eval
          (let [move         (first moves)
                new-state    (apply-move board-state move)
                eval-score   (minimax new-state (dec depth) false alpha beta)
                new-max-eval (max max-eval eval-score)
                new-alpha    (max alpha eval-score)]
            (if (>= new-alpha beta)
              new-max-eval ; Beta cutoff
              (recur (rest moves) new-max-eval new-alpha))))))

    :else
    (let [moves (generate-all-moves board-state (:player board-state))]
      (loop [moves    moves
             min-eval Double/POSITIVE_INFINITY
             beta     beta]
        (if (empty? moves)
          min-eval
          (let [move         (first moves)
                new-state    (apply-move board-state move)
                eval-score   (minimax new-state (dec depth) true alpha beta)
                new-min-eval (min min-eval eval-score)
                new-beta     (min beta eval-score)]
            (if (<= new-beta alpha)
              new-min-eval ; Alpha cutoff
              (recur (rest moves) new-min-eval new-beta))))))))

(defn minimax-best-move
  "Find the best move using minimax algorithm with alpha-beta pruning
   More efficient than BFS for deeper searches"
  [board-state player max-depth]
  (let [moves (generate-all-moves board-state player)]
    (when (seq moves)
      (let [should-maximaze (= player :white)
            move-scores (map (fn [move]
                               (let [new-state (apply-move board-state move)
                                     score (minimax new-state
                                                    (dec max-depth)
                                                    (not should-maximaze)
                                                    Double/NEGATIVE_INFINITY
                                                    Double/POSITIVE_INFINITY)]
                                 {:move move :score score}))
                             moves)
            search-fn (if should-maximaze max-key min-key)
            best-move (apply search-fn :score move-scores)]
        best-move))))

(defn get-ai-move
  "Get the best move for the AI using the specified algorithm and depth
   algorithm can be :bfs or :minimax"
  [board-state player & {:keys [max-depth algorithm]
                         :or {max-depth 3 algorithm :minimax}}]
  (case algorithm
    ;; :bfs (bfs-best-move board-state player max-depth)
    :minimax (minimax-best-move board-state player max-depth)
    (throw (ex-info "Unknown algorithm" {:algorithm algorithm}))))

(comment

  (require '[monku.game.core :refer [initialize-game]])
  (require '[monku.game.visualizer :refer [display-board
                                           display-board-and-cards]])
  (def test-game (initialize-game {}))

  (let [init-game (initialize-game {:starting-player (rand-nth [:white :black])})]
    (portal.api/clear)
    (display-board-and-cards init-game (get-in init-game [:cards :white]))
    (display-board-and-cards (assoc init-game :black-pov true) (get-in init-game [:cards :black]))
    (loop [g init-game
           s 1000]
      (if (and (< 0 s) (= :playing (get g :status)))
        (when-let [c (get-ai-move g (get g :player)
                                  :max-depth 4
                                  :algorithm :minimax)]
          (let [next-move  (get c :move)
                card       (get next-move :card)
                piece      (get next-move :piece)
                move       (get next-move :move)
                next-state (apply-move g next-move)]
            (when-not (= next-state g)
              (display-board-and-cards (assoc next-state :black-pov (= :black (get g :player)))
                                       [card
                                        [[(first piece) :piece]
                                         [move :move]]]))
            (recur next-state (dec s))))
        (tap> [:finish g]))))

  (when-let [c (get-ai-move test-game :white
                            :max-depth 3
                            :algorithm :minimax)]
    (let [next-move  (get c :move)
          card       (get next-move :card)
          piece      (get next-move :piece)
          move       (get next-move :move)
          next-state (apply-move test-game next-move)]
      (portal.api/clear)
      (tap> next-state)
      (display-board test-game)
      (display-board-and-cards next-state [card
                                           [[(first piece) :piece]
                                            [move :move]]])))

  (let [moves (generate-all-moves test-game :white)]
    (portal.api/clear)
    (doseq [{:keys [move piece card] :as move-data} moves]
      (let [next-state (apply-move test-game move-data)]
        (display-board-and-cards next-state [card
                                             [[(first piece) :piece]
                                              [move :move]]])))
    (tap> (count moves)))

  (do
    (portal.api/clear)
    (display-board-and-cards test-game (get-in test-game [:cards :white]))
    (display-board-and-cards (assoc test-game :black-pov true) (get-in test-game [:cards :black])))

  :done)
