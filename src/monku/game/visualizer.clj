(ns monku.game.visualizer
  (:require
   [monku.game.board :refer [initialize-board-state]]))

;; Chess piece symbols
(def pieces
  {:white {:monk "♔" :pawn "♙"}
   :black {:monk "♚" :pawn "♟"}})

(defn get-piece-symbol [piece]
  (when piece
    (let [{:keys [player piece]} piece]
      (get-in pieces [player piece]))))

(defn square-color [row col]
  (if (even? (+ row col)) :light :dark))

(defn render-board [{:keys [pieces config]}]
  (let [positions       (into {} pieces)
        tempe-positions (into {} (get config :temples))]
    [:div {:style {:font-family "monospace"
                   :font-size   "24px"
                   :line-height "1"
                   :border      "2px solid #8B4513"
                   :display     "inline-block"}}
     (for [row (reverse (range (get config :size 0)))]
       [:div {:key row :style {:display "flex"}}
        (for [col (range (get config :size 0))]
          (let [coords   {:x row :y col}
                piece    (get positions coords)
                temple   (get tempe-positions coords)
                color    (square-color row col)
                bg-color (cond
                           (some? temple)   "#FF00FF"
                           (= color :light) "#F0D9B5"
                           :else            "#B58863")]
            [:div {:key   col
                   :style {:width            "40px"
                           :height           "40px"
                           :background-color bg-color
                           :display          "flex"
                           :align-items      "center"
                           :justify-content  "center"
                           :border           "1px solid #999"}}
             (get-piece-symbol piece)]))])]))

(defn display-board [game-state]
  (tap> (with-meta (render-board game-state)
          {:portal.viewer/default :portal.viewer/hiccup})))

(comment
  (let [config {:size   5
                :monk-x 2
                :temples [[{:x 0 :y 2} :white]
                          [{:x 4 :y 2} :black]]
                :players [[0 :white]
                          [4 :black]]}]
    (display-board {:pieces (initialize-board-state config)
                    :config config})))
