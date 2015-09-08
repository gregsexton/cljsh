(ns shell.combinators
  (:refer-clojure :exclude [print])
  (:require [clojure
             [string :as str]]
            [shell.streams :as streams]))

;;; TODO: combinators should allow enabling/disabling :out :err

;;; all combinators take a proc map and give back something allowing combining

;; TODO: document signatures of combinators in docstrings

(defn lines
  ([result] (lines result :out))
  ([result channel-selector]
   ;; TODO: lazy streaming seq. slurp gobbles everything
   (-> result channel-selector slurp (str/split #"\n"))))

;;; TODO: break out the join? -- need to be able to interleave in different ways
(defn print
  ([result] (print result :out :err))
  ([result & channel-selectors]
   (-> (apply streams/join
              (map (fn [selector] (selector result))
                   channel-selectors))
       streams/print)
   result))
