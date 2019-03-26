(ns kfirmanty.clj-forth.parser
  (:require [instaparse.core :as insta]
            [kfirmanty.clj-forth.std-lib :as std-lib]))

(def parser
  (insta/parser "<S> = (((COMPILE-END | COMPILE-START | CALL | DATA-STRUCTURE | CLOJURE-FN | QUOTED-CLOJURE-FN | NUM) / FN-NAME) <ws>)+
COMPILE-END = ';'
COMPILE-START = ':'
CALL = 'CALL'
DATA-STRUCTURE = #'\\{.*\\}' | #'\\[.*\\]'
FN-NAME = #'[A-Za-z+-/*]+'
QUOTED-CLOJURE-FN = '\\'' CLOJURE-FN
CLOJURE-FN = (#'[A-Za-z0-9-]+' '.'?)+ '/' #'[A-Za-z0-9-]+'
NUM = #'\\d+\\.?\\d*'
ws = #'\\s*'"))

(defn with-tag [tag read-fn]
  (fn [& i]
    [tag (apply read-fn i)]))

(defn resolve-fn [input]
  (-> input read-string resolve))

(defn transform-clojure-fn [& i]
  (let [fname (apply str i)]
    (-> fname
        resolve-fn
        (#(if (nil? %)
            (throw (ex-info (str "Unresolved fn: " fname) {}))
            %))
        std-lib/wrap-external)))

(defn parse [input]
  (->> input parser (insta/transform {:DATA-STRUCTURE (with-tag :DATA-STRUCTURE read-string)
                                      :NUM (with-tag :NUM read-string)
                                      :CLOJURE-FN (with-tag :CLOJURE-FN transform-clojure-fn)
                                      :QUOTED-CLOJURE-FN (with-tag :QUOTED-CLOJURE-FN (fn [_ [_ f]]
                                                                                        f))})))
