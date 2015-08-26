(ns shell.combinators
  (:refer-clojure :exclude [print])
  (:require [clojure
             [string :as str]]
            [shell.streams :as streams]))

(defn lines
  ([result] (lines result :out))
  ([result channel-selector]
   ;; TODO: lazy streaming seq. slurp gobbles everything
   (-> result channel-selector slurp (str/split #"\n"))))

(defn print [result]
  (-> (streams/join (:out result) (:err result))
      streams/print))
