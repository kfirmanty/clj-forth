(ns kfirmanty.clj-forth.std-lib)

(defn push-stack [env val]
  (update env :stack conj val))

(defn pop-stack
  ([env]
   (let [[[v] env] (pop-stack env 1)]
     [v env]))
  ([env n]
   [(take n (:stack env)) (update env :stack #(drop n %))]))

(defn drop-optional [argslist]
  (take-while #(not= % '&) argslist))

(defn number-of-args [f]
  (let [arglist (-> f meta :arglists)
        shortest (->> arglist
                      (map drop-optional)
                      (sort #(compare (count %1) (count %2)))
                      first)]
    (count shortest)))

(defn wrap-external [f]
  (let [nargs (number-of-args f)]
    (fn [env]
      (let [[args nenv] (pop-stack env nargs)]
        (push-stack nenv (apply f args))))))

(defn add [v1 v2]
  (+ v1 v2))

(defn sub [v1 v2]
  (- v1 v2))

(defn mult [v1 v2]
  (* v1 v2))

(defn div [v1 v2]
  (/ v1 v2))

(defn eq [v1 v2]
  (if (= v1 v2)
    -1
    0))

(defn less [v1 v2]
  (if (< v2 v1)
    -1
    0))

(defn greater [v1 v2]
  (if (> v2 v1)
    -1
    0))

(defn truthy? [v]
  (not= v 0))

(defn falsey? [v]
  (= v 0))

(defn dup [env]
  (let [v (first (:stack env))]
    (push-stack env v)))

(defn swap [env]
  (let [[[f s] nenv] (pop-stack env 2)]
    (-> nenv
        (push-stack f)
        (push-stack s))))

(defn stack-print [env]
  (-> env :stack first print)
  env)

(def env {:stack '()
          :fns {"+" (wrap-external #'add)
                "-" (wrap-external #'sub)
                "/" (wrap-external #'div)
                "*" (wrap-external #'mult)
                "<" (wrap-external #'less)
                ">" (wrap-external #'greater)
                "=" (wrap-external #'eq)
                "DUP" dup
                "SWAP" swap
                "PRINT" stack-print}
          :compile-mode? false
          :compile-fn nil})
