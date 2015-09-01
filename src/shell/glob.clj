(ns shell.glob
  (:require [clojure.string :as str]))

;;; TODO: need to research this more. I wonder if there's something already out there?

(defn expand [arg]
  [(str/replace arg "~" "/Users/gsexton")]) ;TODO: work out user's home dir
