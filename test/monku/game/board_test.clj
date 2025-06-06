(ns monku.game.board-test
  (:require
   [monku.game.board :as sut]
   [clojure.test :refer :all]))

(deftest create-figs-test
  (testing "invalid size"
    (is (empty? (sut/create-figs {:size 0})))
    (is (empty? (sut/create-figs {:size -1}))))
  (testing "one pawn"
    (is (= [[{:x 0 :y 0} {:piece :pawn :player :type}]]
           (sut/create-figs {:size        1
                             :player-type :type
                             :figs-y      0
                             :monk-x      5})))
    (is (= [[{:x 0 :y 0} {:piece :monk :player :type}]]
           (sut/create-figs {:size        1
                             :player-type :type
                             :figs-y      0
                             :monk-x      0}))))
  (testing "multiple pawns"
    (let [with-size      10
          with-monk-size (dec (int (/ with-size 2)))
          with-fig-y     5
          with-player-type :player-a
          pawns          (sut/create-figs {:size        with-size
                                           :player-type with-player-type
                                           :figs-y      with-fig-y
                                           :monk-x      with-monk-size})]
      (is (= 10 (count pawns)))
      (is (= #{:pawn :monk} (into #{} (map (comp :piece second) pawns))))
      (is (= (range 10) (map (comp :x first) pawns)))
      (is (= (take with-size (repeatedly (constantly with-fig-y)))
             (map (comp :y first) pawns)))
      (is (= (take with-size (repeatedly (constantly with-player-type)))
             (map (comp :player second) pawns)))
      (is (= :monk (-> pawns (nth with-monk-size) second :piece)))
      (is (every? #(= :pawn (-> % second :piece))
                  (filter (fn [[{:keys [x]}]]
                            (not= x with-monk-size))
                          pawns))))))

(deftest initialize-board-state-test
  (testing "empty board"
    (is (empty? (sut/initialize-board-state {:size 0})))
    (is (empty? (sut/initialize-board-state {:size -1}))))
  (testing "minimal board"
    (let [board (sut/initialize-board-state {:size 1 :monk-x 1})]
      (is (= #{:black :white} (into #{} (map (comp :player second) board))))
      (is (= 2 (count board)))))
  (testing "real size board"
    (let [board (sut/initialize-board-state {:size 5 :monk-x 2})]
      (is (= #{:black :white} (into #{} (map (comp :player second) board))))
      (is (= 10 (count board))))))
