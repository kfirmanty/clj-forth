(ns kfirmanty.clj-forth.core-test
  (:require [clojure.test :refer :all]
            [kfirmanty.clj-forth.core :refer :all]))

(deftest should-execute-simple-math-program
  (let [env (consume "1 1 +")]
    (is (= (:stack env) '(2.0)))))

(deftest should-define-fn
  (let [env (consume ": POW DUP *;")]
    (is (get-fn env "POW"))))

(deftest should-execute-defined-fn
  (let [env (consume "2 : POW DUP * ; POW")]
    (is (= (:stack env) '(4.0)))))
