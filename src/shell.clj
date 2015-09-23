(ns shell
  (:refer-clojure :exclude [print read eval > <])
  (:require [byte-streams :as byte-streams]
            [clojure.main :as main]
            [shell.proc :refer :all]
            [shell.reader :refer :all]
            [shell.combinators :refer :all]))

;;; TODO: setup a system with reloaded repl? shell config should be in the system
;;; TODO: how to make interactive programs like top work?

(defn- result-map? [res]
  (and (map? res) (contains? res :out)))    ;TODO: use prismatic schema

(defn run-job
  ;; TODO: make default combinator (in this case truncated-print) configurable
  ([cmd] (run-job truncated-print cmd))
  ([printer cmd]
   (-> (cmd)
       printer
       (assoc :run-as-job true))))

(defn eval [form]
  (clojure.core/eval form))

(defn repl-read [request-prompt request-exit]
  (let [input (.readLine *in*)]
    (if (= input ":quit")               ;TODO: should allow trailing whitespace
      request-exit
      (read input))))

(defn repl-print [result]
  ;; do not want to print out the process map of regular jobs
  (when-not (and (result-map? result) (:run-as-job result))
    (println result)))

(defn prompt []
  (printf "%s (%s)\n$ " @cwd (ns-name *ns*)))

(defn repl* [input] ((comp eval read) input))

(defn repl []
  (main/repl :read repl-read
             :eval eval
             :print repl-print
             :prompt prompt
             :init (fn [] (ns shell))
             ;; :caught caught      ;TODO:
             ))

(defn -main [& args] (repl))
