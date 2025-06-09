(ns monku.game.ai-test
  (:require
   [clojure.test :refer :all]
   [monku.game.ai :as sut]
   [monku.game.board :as gb]))

(deftest minimax-best-move-test
  (testing "One move end game for white"
      (let [config    {:size        4
                       :show-coords false
                       :monk-x      1
                       :temples     [[{:x 1 :y 1} :white]
                                     [{:x 1 :y 3} :black]]
                       :players     [[1 :white]
                                     [3 :black]]}
            pieces    (gb/initialize-board-state config)
            card      [[{:x 2 :y 1} :piece]
                       [{:x 2 :y 3} :move]
                       [{:x 2 :y 0} :move]]
            state     {:config config
                       :pieces pieces
                       :player :white
                       :status :playing
                       :cards  {:white [card]
                                :black []}}
            best-move (sut/minimax-best-move state :white 1)]
        #_(monku.game.visualizer/display-board-and-cards state [card (:move best-move)])
        (is (= 1000 (:score best-move)))
        (is (= {:x 1 :y 1} (-> best-move :move :piece first)))
        (is (= {:x 1 :y 3} (-> best-move :move :move)))
        (is (= :monk (-> best-move :move :piece second :piece)))
        (is (= :white (-> best-move :move :piece second :player)))))
  (testing "One move end game for black"
      (let [config    {:size        4
                       :show-coords false
                       :monk-x      1
                       :temples     [[{:x 1 :y 1} :white]
                                     [{:x 1 :y 3} :black]]
                       :players     [[1 :white]
                                     [3 :black]]}
            pieces    (gb/initialize-board-state config)
            card      [[{:x 2 :y 1} :piece]
                       [{:x 2 :y 3} :move]
                       [{:x 2 :y 0} :move]]
            state     {:config config
                       :pieces pieces
                       :player :black
                       :status :playing
                       :cards  {:white []
                                :black [card]}}
            best-move (sut/minimax-best-move state :black 1)]
        #_(monku.game.visualizer/display-board-and-cards state [card (:move best-move)])
        (is (= -1000 (:score best-move)))
        (is (= {:x 1 :y 3} (-> best-move :move :piece first)))
        (is (= {:x 1 :y 1} (-> best-move :move :move)))
        (is (= :monk (-> best-move :move :piece second :piece)))
        (is (= :black (-> best-move :move :piece second :player)))))
  (testing "Two move end game for white"
    (let [config    {:size        4
                     :show-coords false
                     :monk-x      1
                     :temples     [[{:x 1 :y 0} :white]
                                   [{:x 1 :y 3} :black]]
                     :players     [[0 :white]
                                   [3 :black]]}
          pieces    (gb/initialize-board-state config)
          card-1      [[{:x 2 :y 1} :piece]
                       [{:x 2 :y 2} :move]
                       [{:x 2 :y 0} :move]]
          card-2      [[{:x 2 :y 1} :piece]
                       [{:x 2 :y 3} :move]
                       [{:x 2 :y 2} :move]
                       [{:x 2 :y 0} :move]]
          state     {:config config
                     :pieces pieces
                     :player :white
                     :status :playing
                     :cards  {:white [card-1
                                      card-2]
                              :black []}}
          best-move (sut/minimax-best-move state :white 3)]
      (is (= 1000 (:score best-move)))
      (is (= {:x 1 :y 0} (-> best-move :move :piece first)))
      (is (= {:x 1 :y 1} (-> best-move :move :move)))
      (is (= :monk (-> best-move :move :piece second :piece)))
      (is (= :white (-> best-move :move :piece second :player)))))
  (testing "Two move end game for black"
    (let [config    {:size        4
                     :show-coords false
                     :monk-x      1
                     :temples     [[{:x 1 :y 0} :white]
                                   [{:x 1 :y 3} :black]]
                     :players     [[0 :white]
                                   [3 :black]]}
          pieces    (gb/initialize-board-state config)
          card-1      [[{:x 2 :y 1} :piece]
                       [{:x 2 :y 2} :move]
                       [{:x 2 :y 0} :move]]
          card-2      [[{:x 2 :y 1} :piece]
                       [{:x 2 :y 3} :move]
                       [{:x 2 :y 2} :move]
                       [{:x 2 :y 0} :move]]
          state     {:config config
                     :pieces pieces
                     :player :black
                     :status :playing
                     :cards  {:white []
                              :black [card-1
                                      card-2]}}
          best-move (sut/minimax-best-move state :black 3)]
      (is (= -1000 (:score best-move)))
      (is (= {:x 1 :y 3} (-> best-move :move :piece first)))
      (is (= {:x 1 :y 2} (-> best-move :move :move)))
      (is (= :monk (-> best-move :move :piece second :piece)))
      (is (= :black (-> best-move :move :piece second :player))))))
