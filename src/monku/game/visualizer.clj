(ns monku.game.visualizer)

;; Chess piece symbols
(def pieces
  {:white {:monk "♔" :pawn "♙"}
   :black {:monk "♚" :pawn "♟"}})

(def colors
  {:temple-white "#F5F5DC"
   :temple-black "#87CEFA"
   :move-start   "#800000"
   :move-option  "#FFD700"
   :base-color   "#F0D9B5"})

(defn ->wrapper-attrs []
  {:style {:font-family "monospace"
           :font-size   "24px"
           :line-height "1"
           :border      "2px solid #8B4513"
           :display     "inline-block"}})

(defn ->row-attrs [row]
  {:key row :style {:display "flex"}})

(defn ->col-attrs [col & {:keys [bg-color]}]
  {:key   col
   :style {:width            "40px"
           :height           "40px"
           :background-color bg-color
           :display          "flex"
           :align-items      "center"
           :justify-content  "center"
           :border           "1px solid #999"}})

(defn get-piece-symbol [piece]
  (when piece
    (let [{:keys [player piece]} piece]
      (get-in pieces [player piece]))))

(defn render-board [{:keys [pieces config]}]
  (let [positions       (into {} pieces)
        temple-positions (into {} (get config :temples))]
    [:div (->wrapper-attrs)
     (for [row (reverse (range (get config :size 0)))]
       [:div (->row-attrs row)
        (for [col (range (get config :size 0))]
          (let [coords   {:x col :y row}
                piece    (get positions coords)
                temple   (get temple-positions coords)
                bg-color (cond
                           (some? temple) (get colors (if (= :white temple)
                                                        :temple-white
                                                        :temple-black))
                           :else (get colors :base-color))]
            [:div (->col-attrs col {:bg-color bg-color})
             (get-piece-symbol piece)]))])]))

(defn render-card [{:keys [moves config]}]
  (let [card-size (get config :size 0)
        positions (into {} moves)]
    [:div (->wrapper-attrs)
     (for [row (reverse (range card-size))]
       [:div (->row-attrs row)
        (for [col (range card-size)]
          (let [coords   {:x col :y row}
                position (get positions coords)
                bg-color (cond
                           (= :piece position) (get colors :move-start)
                           (= :move position)  (get colors :move-option)
                           :else               (get colors :base-color))]
            [:div (->col-attrs col {:bg-color bg-color})
             (when (get config :show-coords false)
               [:span {:style {:font-size "16px"}}
                col ";" row])]))])]))

(defn- tap-hiccup> [hiccup]
  (tap> (with-meta hiccup
          {:portal.viewer/default :portal.viewer/hiccup})))

(defn display-board [game-state]
  (tap-hiccup> [:div {:style {:display "flex"}}
                (render-board game-state)]))

(defn display-board-and-cards [game-state cards]
  (tap-hiccup> (into [:div
                      {:style {:display "flex"}}
                      (render-board game-state)]
                     (mapv (fn [card]
                            (render-card {:moves card :config (:config game-state)}))
                           cards))))

(defn display-card [card config]
  (tap-hiccup> [:div {:style {:display "flex"}}
                (render-card {:moves card :config config})]))

(comment
  ;; Cards
  (display-card [[{:x 2 :y 2} :piece]] {:size 5 :show-coords true})
  (display-card (nth monku.game.cards/game-cards 2) {:size 5})
  (doseq [c monku.game.cards/game-cards]
    (display-card c {:size 5}))

  ;; Board
  (let [config {:size   5
                :monk-x 2
                :temples [[{:x 2 :y 0} :white]
                          [{:x 2 :y 4} :black]]
                :players [[0 :white]
                          [4 :black]]}]
    #_(display-board {:pieces (monku.game.board/initialize-board-state config)
                    :config config})
    #_(display-board-and-cards {:pieces (monku.game.board/initialize-board-state config)
                              :config config}
                             (take 3 monku.game.cards/game-cards)))

  :done)
