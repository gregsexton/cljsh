(ns shell.core
  (:gen-class)
  (:refer-clojure :exclude [eval print])
  (:require [clojure.string :as str]
            [shell.proc :refer :all]
            [shell.reader :refer :all]))

;;; TODO: setup a system with reloaded repl?

;;; TODO: how to make interactive programs like top work?

;;; TODO: what should this take and return?
(defn eval [form]
  ((clojure.core/eval form)))

;;; TODO: stderr
(defn print [result]
  (-> result :out slurp clojure.core/print))

(defn repl [in] (foo "bar") (-> in read eval print))

;;; TODO: should build up utils like this, but where?
(def lines #(str/split % #"\n"))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
