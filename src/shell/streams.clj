(ns shell.streams
  (:refer-clojure :exclude [print])
  (:require [byte-streams :as streams]
            [clojure.java.io :as io]))

;;; TODO: needs to join 0-many streams. should optionally take a join strategy?
(defn join [stream-a stream-b]
  ;; TODO: need to stream obvs. how to do the buffering here? what do
  ;; shells normally do?
  ;; TODO: will need to do this on a separate thread. core async?
  stream-a
  #_(let [a (when stream-a (slurp stream-a))
          b (when stream-b (slurp stream-b))]
      (streams/convert (str a b) java.io.InputStream)))

;; TODO: cap printing? different combinator.
(defn print [stream] (io/copy stream *out*))
