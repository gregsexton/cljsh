(ns shell.core
  (:gen-class)
  (:require [shell.proc :refer :all]))

;;; TODO: what should this take and return?
;;; TODO: stderr
(defn eval [form]
  (-> (form) :out slurp))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
