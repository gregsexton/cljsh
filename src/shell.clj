(ns shell
  (:gen-class)
  (:refer-clojure :exclude [read eval print > <])
  (:require [byte-streams :as streams]
            [clojure.java.io :as io]
            [clojure
             [main :as main]
             [string :as str]]
            [shell.proc :refer :all]
            [shell.reader :refer :all]))

;;; TODO: setup a system with reloaded repl?

;;; TODO: how to make interactive programs like top work?

(defn- result-map? [res]
  (and (map? res) (contains? res :out)))    ;TODO: use prismatic schema

(defn eval [form]
  (let [res (clojure.core/eval form)]
    (if (result-map? res) res
        {:out (streams/convert (str res) java.io.InputStream)})))

;;; TODO: slurping everything in to memory is not going to cut it. but
;;; do I actually want to stream everything? particularly if running
;;; in emacs..
;;; TODO: need output to stream! otherwise have to wait to see output
;;; TODO: stderr
(defn print [result]
  (-> result :out slurp clojure.core/prn))

(defn repl-read [request-prompt request-exit]
  (let [input (.readLine *in*)]
    (if (= input ":quit")
      request-exit
      (read input))))

(defn prompt []
  (printf "%s (%s)\n$ " (System/getProperty "user.dir") (ns-name *ns*)))

(defn repl []
  (main/repl :read repl-read
             :eval eval
             :print print
             :prompt prompt
             ;; :caught caught      ;TODO:
             ))

;;; TODO: should build up utils like this, but where?
(def lines #(str/split % #"\n"))

(defn -main [& args] (repl))
