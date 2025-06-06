(ns monku.game.board)

(defn create-figs
  [{:keys [size
           monk-x
           figs-y
           player-type]}]
  (->> (range size)
       (map (fn [x]
              [{:x x :y figs-y}
               {:piece  (if (= x monk-x) :monk :pawn)
                :player player-type}]))))

(defn initialize-board-state
  "Create starting board with all pawns and monks."
  [{:keys [size
           monk-x
           players]
    :or   {size            5
           monk-x          2
           players         [[0 :white]
                            [(dec size) :black]]}}]
  (->> players
       (mapcat (fn [[figs-y player-type]]
                 (create-figs {:size        size
                               :monk-x      monk-x
                               :figs-y      figs-y
                               :player-type player-type})))))
