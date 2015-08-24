(ns shell.proc
  (:require [clojure.java.io :as io]
            [me.raynes.conch.low-level :as conch])
  (:import [java.io InputStream]))

(defprotocol Pipeable
  (pipe [this out-stream]))

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

(defn cmd* [program & args]
  (fn this
    ([] (this {:out nil}))
    ([input]
     (let [p (apply conch/proc program args)]
       (pipe (:out input) (:in p))
       p))))

(defmacro cmd [program & args]
  (letfn [(normalize [arg]
            (cond (symbol? arg) (str arg)
                  (number? arg) (str arg)
                  :else arg))]
    `(cmd* (str '~program) ~@(map normalize args))))

;;; TODO: need to join together the error streams, should do this in cmd
(defn | [& cmds]
  (apply comp (reverse cmds)))

;;; TODO: these conflict...
;;; TODO: implement these redirections
(defn < [])

(defn << [str] {:out str})

(defn > [])

(defn >> [])
