(ns shell.glob
  (:require [clojure.string :as str]
            [me.raynes.fs :as fs]))

;;; TODO: need to research this more. I wonder if there's something already out there?

(defn expand [cwd arg]
  [(str/replace arg #"^~" (str (fs/home)))])
