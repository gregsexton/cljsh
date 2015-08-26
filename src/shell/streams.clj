(ns shell.streams
  (:refer-clojure :exclude [print])
  (:require [byte-streams :as streams]))

;;; TODO: needs to join 0-many streams. should optionally take a join strategy?
(defn join [stream-a stream-b]
  ;; TODO: need to stream obvs. how to do the buffering here? what do
  ;; shells normally do?
  (let [a (when stream-a (slurp stream-a))
        b (when stream-b (slurp stream-b))]
    (streams/convert (str a b) java.io.InputStream)))

;;; TODO: slurping everything in to memory is not going to cut it. but
;;; do I actually want to stream everything? particularly if running
;;; in emacs..
;;; TODO: need output to stream! otherwise have to wait to see output
(defn print [stream]
  (clojure.core/print (slurp stream)))
