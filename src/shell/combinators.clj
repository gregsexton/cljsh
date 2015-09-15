(ns shell.combinators
  (:refer-clojure :exclude [print])
  (:require [clojure.core.async :as async]
            [clojure.java.io :as io]
            [clojure
             [string :as str]]
            [shell.streams :as streams]))

;;; all combinators take a proc map and give back something allowing combining
;;; they should all take care of closing out streams appropriately

;; TODO: document signatures of combinators in docstrings

(defn lines
  ([result] (lines result :out))
  ([result channel-selector]
   ;; TODO: reify seq and close out stream
   (-> result channel-selector streams/stream->lines)))

(defn- run-async-printer [lines output]
  (let [lines-ch (async/chan)
        control (async/onto-chan lines-ch lines)]
    (async/go-loop [line (async/<! lines-ch)]
      (when line
        (binding [*out* output] (println line))
        (recur (async/<! lines-ch))))
    control))

(defn- close! [stream]
  (when stream (.close stream)))

(defn print
  ([result] (print result :out :err))
  ([result stdout-selector stderr-selector]
   (let [out (stdout-selector result)
         err (stderr-selector result)]
     (try (async/<!!
           (async/merge
            [(-> out streams/stream->lines (run-async-printer *out*))
             (-> err streams/stream->lines (run-async-printer *err*))]))
          result
          (finally (close! out) (close! err))))))

(defn truncated-print
  ([result] (truncated-print result :out :err))
  ([result stdout-selector stderr-selector]
   (let [out (stdout-selector result)
         err (stderr-selector result)
         max-lines 500]
     (try (async/<!!
           (async/merge
            [(-> out streams/stream->lines (->> (take max-lines)) (run-async-printer *out*))
             (-> err streams/stream->lines (->> (take max-lines)) (run-async-printer *err*))]))
          (when (and out (pos? (.read out)))
            (println "\n-- Stdout truncated after" max-lines "lines --"))
          (when (and err (pos? (.read err)))
            (binding [*out* *err*]
              (println "\n-- Stderr truncated after" max-lines "lines --")))
          result
          (finally (close! out) (close! err))))))
