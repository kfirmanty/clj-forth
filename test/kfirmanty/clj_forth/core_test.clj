(ns kfirmanty.clj-forth.core-test
  (:require [clojure.test :refer :all]
            [kfirmanty.clj-forth.core :refer :all]))

(deftest should-execute-simple-math-program
  (let [env (consume "1 1 +")]
    (is (= (:stack env) '(2)))))

(deftest should-define-fn
  (let [env (consume ": POW DUP * ;")]
    (is (get-fn env "POW"))))

(deftest should-execute-defined-fn
  (let [env (consume "2 : POW DUP * ; POW")]
    (is (= (:stack env) '(4)))))

(deftest should-swap-elements-on-stack
  (let [env (consume "1 2 SWAP")]
    (is (= (:stack env) '(1 2)))))

(defn mult2 [v]
  (* v 2))

(defn extract [v]
  (:a v))

(deftest should-execute-clojure-fn
  (let [env (consume "10 kfirmanty.clj-forth.core-test/mult2")]
    (is (= (:stack env) '(20)))))

(deftest should-parse-clojure-map
  (let [env (consume "{:a 100} kfirmanty.clj-forth.core-test/extract")]
    (is (= (:stack env) '(100)))))
