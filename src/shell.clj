(ns shell
  (:gen-class)
  (:refer-clojure :exclude [print read eval > <])
  (:require [byte-streams :as byte-streams]
            [clojure.main :as main]
            [shell.proc :refer :all]
            [shell.reader :refer :all]
            [shell.combinators :refer :all]))

;;; TODO: setup a system with reloaded repl?

;;; TODO: how to make interactive programs like top work?

(defn- result-map? [res]
  (and (map? res) (contains? res :out)))    ;TODO: use prismatic schema

(defn job [cmd]
  (print (cmd)))

(defn eval [form]
  (clojure.core/eval form))

(defn repl-read [request-prompt request-exit]
  (let [input (.readLine *in*)]
    (if (= input ":quit")
      request-exit
      (read input))))

(defn repl-print [result]
  (when-not (result-map? result)
    (println result)))

(defn prompt []
  (printf "%s (%s)\n$ " @cwd (ns-name *ns*)))

(defn repl* [input] ((comp repl-print eval read) input))

(defn repl []
  (main/repl :read repl-read
             :eval eval
             :print repl-print
             :prompt prompt
             ;; :caught caught      ;TODO:
             ))

(defn -main [& args] (repl))
