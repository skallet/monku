(ns monku.game.board)

(defn create-figs
  [{:keys [size
           monk-y
           figs-x
           player-type]}]
  (->> (range size)
       (map (fn [y]
              [{:y y :x figs-x}
               {:piece  (if (= y monk-y) :monk :pawn)
                :player player-type}]))))

(defn initialize-board-state
  "Create starting board with all pawns and monks."
  [{:keys [size
           monk-y
           players]
    :or   {size    5
           monk-y  3
           players [[0 :white]
                    [(dec size) :black]]}}]
  (->> players
       (mapcat (fn [[figs-x player-type]]
                 (create-figs {:size        size
                               :monk-y      monk-y
                               :figs-x      figs-x
                               :player-type player-type})))))
