(ns kfirmanty.clj-forth.core
  (:require [kfirmanty.clj-forth.std-lib :as std-lib])
  (:gen-class))

(defn get-fn [env f]
  (get-in env [:fns f]))

(declare execute-loop)

(defn call-fn [env f]
  (if (coll? f)
    (execute-loop env f)
    (f env)))

(defn number-str? [v]
  (re-matches #"\d+\.?\d?" v))

(defn add-fn [env command]
  (if (:compile-fn env)
    (update-in env [:fns (:compile-fn env)] conj command)
    (-> env
        (assoc :compile-fn command)
        (assoc-in [:fns command] []))))

(defn execute [env command]
  (cond
    (= command ";") (assoc env :compile-mode? false
                           :compile-fn nil)
    (:compile-mode? env) (add-fn env command)
    (= command ":") (assoc env :compile-mode? true)
    (number-str? command) (std-lib/push-stack env (Double/parseDouble command))
    (get-fn env command) (call-fn env (get-fn env command))
    :else (println "unknown command: " command "\nin env:\n" env)))

(defn execute-loop [env commands]
  (loop [env env command (first commands) commands (rest commands)]
    (if (nil? command)
      env
      (recur (execute env command) (first commands) (rest commands)))))

(defn consume
  ([input]
   (consume std-lib/env input))
  ([env input]
   (let [commands (clojure.string/split input #"\s+")]
     (execute-loop env commands))))

(defn -main [& args]
  (execute-loop args))
