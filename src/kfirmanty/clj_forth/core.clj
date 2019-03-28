(ns kfirmanty.clj-forth.core
  (:require [kfirmanty.clj-forth.std-lib :as std-lib]
            [kfirmanty.clj-forth.parser :as parser])
  (:gen-class))

;;TODO: add dumping env to file, so you can distribute programs as VM images

(defn get-fn [env f]
  (get-in env [:fns f]))

(declare execute-loop)

(defn call-fn [env f]
  (if (coll? f)
    (execute-loop env f)
    (f env)))

(defn add-fn [env [command value :as tagged]]
  (if (:compile-fn env)
    (update-in env [:fns (:compile-fn env)] conj tagged)
    (-> env
        (assoc :compile-fn value)
        (assoc-in [:fns value] []))))

(defn call-command [env]
  (let [[f nenv] (std-lib/pop-stack env)]
    (f nenv)))

(defn execute-if [env [if-true if-false]]
  (let [[v env] (std-lib/pop-stack env)]
    (cond (std-lib/truthy? v) (execute-loop env [if-true])
          if-false (execute-loop env [if-false])
          :else env)))

(defn execute [env [command value :as input]]
  (cond
    (= command :COMPILE-END) (assoc env :compile-mode? false
                                    :compile-fn nil)
    (:compile-mode? env) (add-fn env [command value])
    (= command :COMPILE-START) (assoc env :compile-mode? true)
    (= command :CALL) (call-command env)
    (#{:DATA-STRUCTURE :NUM :QUOTED-CLOJURE-FN :STRING} command) (std-lib/push-stack env value)
    (= command :CLOJURE-FN) (call-fn env value)
    (= command :IF) (execute-if env (rest input))
    (get-fn env value) (call-fn env (get-fn env value))
    :else (println "unknown command: " command " with value: " value "\nin env:\n" env)))

(defn execute-loop [env commands]
  (loop [env env command (first commands) commands (rest commands)]
    (if (nil? command)
      env
      (recur (execute env command) (first commands) (rest commands)))))

(defn consume
  ([input]
   (consume std-lib/env input))
  ([env input]
   (let [commands (parser/parse input)]
     (execute-loop env commands))))

(defn -main [& args]
  (execute-loop args))
