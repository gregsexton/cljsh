(ns shell.proc
  (:refer-clojure :exclude [< >])
  (:require [clojure.java.io :as io]
            [me.raynes.conch.low-level :as conch]
            [shell.streams :as streams])
  (:import java.io.InputStream))

;;; TODO: move this and associated extends to streams
(defprotocol Pipeable
  (pipe [this out-stream]
    "Pipe this to the supplied out-stream. This may or may not close
    the out-stream depending on implementation."))

(extend-type InputStream
  Pipeable
  (pipe [this out-stream]
    (io/copy this out-stream)
    (.close out-stream)))

(extend-type String
  Pipeable
  (pipe [this out-stream]
    ;; TODO: encoding
    (io/copy (.getBytes this) out-stream)
    (.close out-stream)))

(extend-type nil
  Pipeable
  (pipe [_ out-stream]
    ;; TODO: should this close? how would you type and C-d?
    (.close out-stream)))

;;; TODO: dynamic var? a single def? probably should be in the system state
;;; TODO: would be nice though to temporarily bind the cwd for a
;;; sequence of commands kind of like push and pop. could macro this (with-cwd...
(def cwd (atom (System/getProperty "user.dir")))

(def ^:private builtins
  {"cd" (fn [dir]
          (fn []
            (reset! cwd dir)
            ;; TODO: need some helpers for creating process maps. need
            ;; a helper to create a 'null' one
            {:out nil :err nil}))})

(defn cmd* [program & args]
  (if-let [builtin (get builtins program)]
    (apply builtin args)
    (fn this
      ([] (this {:out nil :err nil}))
      ([input]
       (let [p (apply conch/proc program (concat args [:dir @cwd]))]
         (pipe (:out input) (:in p))
         (assoc p :err (streams/join (:err input) (:err p))))))))

;;; TODO: how to pass in variables? prefix with $ ?? :/ use cmd* ?
(defmacro cmd [program & args]
  (letfn [(normalize [arg]
            (cond (symbol? arg) (str arg)
                  (number? arg) (str arg)
                  :else arg))]
    `(cmd* (str '~program) ~@(map normalize args))))

(defn | [& cmds]
  (apply comp (reverse cmds)))

;;; TODO: these conflict... really do need to rename and remove exclusions
;;; TODO: implement these redirections
(defn < [])

(defn << [str] {:out str})

(defn > [])

(defn >> [])
