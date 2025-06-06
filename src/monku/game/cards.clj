(ns monku.game.cards)

(def card-1
  [[{:x 2 :y 2} :piece]
   [{:x 0 :y 2} :move]
   [{:x 2 :y 3} :move]
   [{:x 4 :y 2} :move]])

(def card-2
  [[{:x 2 :y 2} :piece]
   [{:x 2 :y 3} :move]
   [{:x 1 :y 2} :move]
   [{:x 2 :y 1} :move]])

(def card-3
  [[{:x 2 :y 2} :piece]
   [{:x 2 :y 4} :move]
   [{:x 2 :y 1} :move]])

(def card-4
  [[{:x 2 :y 2} :piece]
   [{:x 1 :y 3} :move]
   [{:x 3 :y 3} :move]
   [{:x 2 :y 1} :move]])

(def card-5
  [[{:x 2 :y 2} :piece]
   [{:x 3 :y 3} :move]
   [{:x 4 :y 2} :move]
   [{:x 1 :y 1} :move]])

(def game-cards
  [card-1
   card-2
   card-3
   card-4
   card-5])
