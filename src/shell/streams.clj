(ns shell.streams
  (:refer-clojure :exclude [print])
  (:require [byte-streams :as streams]
            [clojure.java.io :as io])
  (:import java.io.InputStream))

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

(defn null-input-stream []
  (java.io.ByteArrayInputStream. (make-array Byte/TYPE 0)))

(defn string-input-stream [string]
  (java.io.ByteArrayInputStream. (.getBytes string)))

;; TODO: encoding
(defn stream->lines
  "Returns a lazy seq of lines produced by consuming from the given
  string. This does not close the given stream."
  [stream]
  (if-not stream
    []
    (let [reader (java.io.BufferedReader.
                  (java.io.InputStreamReader. stream))]
      ((fn this []
         (lazy-seq
          (when-let [line (.readLine reader)]
            (cons line (this)))))))))

;;; TODO: needs to join 0-many streams. should optionally take a join strategy?
(defn join [stream-a stream-b]
  ;; TODO: need to stream obvs. how to do the buffering here? what do
  ;; shells normally do?
  ;; TODO: will need to do this on a separate thread. core async?
  stream-a
  #_(let [a (when stream-a (slurp stream-a))
          b (when stream-b (slurp stream-b))]
      (streams/convert (str a b) java.io.InputStream)))

(defn print
  ([stream] (print stream *out*))
  ([stream out] (io/copy stream out)))
