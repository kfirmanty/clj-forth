(ns kfirmanty.clj-forth.core-test
  (:require [clojure.test :refer :all]
            [kfirmanty.clj-forth.core :refer :all]))

(defn eq-stack? [env stack]
  (= (:stack env) stack))

(deftest should-execute-simple-math-program
  (let [env (consume "1 1 +")]
    (is (eq-stack? env '(2)))))

(deftest should-define-fn
  (let [env (consume ": POW DUP * ;")]
    (is (get-fn env "POW"))))

(deftest should-execute-defined-fn
  (let [env (consume "2 : POW DUP * ; POW")]
    (is (eq-stack? env '(4)))))

(deftest should-swap-elements-on-stack
  (let [env (consume "1 2 SWAP")]
    (is (eq-stack? env '(1 2)))))

(defn mult2 [v]
  (* v 2))

(defn extract [v]
  (:a v))

(deftest should-execute-clojure-fn
  (let [env (consume "10 kfirmanty.clj-forth.core-test/mult2")]
    (is (eq-stack? env '(20)))))

(deftest should-parse-clojure-map
  (let [env (consume "{:a 100} kfirmanty.clj-forth.core-test/extract")]
    (is (eq-stack? env '(100)))))

(deftest should-execute-if-condition
  (let [env (consume "0 0 2 IF DUP ELSE SWAP THEN")]
    (is (eq-stack? env '(0 0 0)) "should execute truthy branch"))
  (let [env (consume "0 2 0 IF DUP ELSE SWAP THEN")]
    (is (eq-stack? env '(0 2)) "should execute falsey branch"))
  (let [env (consume "0 2 IF DUP THEN")]
    (is (eq-stack? env '(0 0)) "should execute IF without ELSE")))
