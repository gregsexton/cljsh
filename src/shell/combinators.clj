(ns shell.combinators
  (:refer-clojure :exclude [print])
  (:require [clojure.core.async :as async]
            [shell.streams :as streams]))

;;; all combinators take a proc map and give back something allowing combining
;;; they should all take care of closing out streams appropriately

;; TODO: document signatures of combinators in docstrings

;;; TODO: this needs to be thought through. if we ask for stdout could
;;; get blocked on stderr not being consumed. need to be able to
;;; stream the lines for lazy processing but also close out the
;;; stream..

;;; TODO: need to think through how to handle stdout and stderr
;;; separately. need a higher order combinator to then provide a
;;; single combinator for the two approaches.

(defn lines
  ([result] (lines result :out))
  ([result channel-selector]
   ;; TODO: reify seq and close out stream
   (-> result channel-selector streams/stream->lines)))

;;; TODO: are printers really combinators? how do I envision this
;;; working with run-job?

(defn- println-to [channel & args]
  (binding [*out* channel]
    (apply println args)))

(defn- run-async-printer [lines output]
  (let [lines-ch (async/chan)
        control (async/onto-chan lines-ch lines)]
    (async/go-loop [line (async/<! lines-ch)]
      (when line
        (println-to output line)
        (recur (async/<! lines-ch))))
    control))

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
          (finally (streams/close! out err))))))

(defn truncated-print
  ([result] (truncated-print result :out :err))
  ([result stdout-selector stderr-selector]
   (let [out (stdout-selector result)
         err (stderr-selector result)
         out-ch (async/chan)
         err-ch (async/chan)
         max-lines 500]
     (try
       (->> out streams/stream->lines (async/onto-chan out-ch))
       (->> err streams/stream->lines (async/onto-chan err-ch))
       (async/<!!
        (async/go-loop [cnt 0 cs [out-ch err-ch]]
          (when (and (pos? (count cs)) (< cnt max-lines))
            (let [[v c] (async/alts! cs)]
              (if-not v
                (recur cnt (filterv #(not= c %) cs))
                (do (println-to (if (= c out-ch) *out* *err*) v)
                    (recur (inc cnt) cs)))))))
       (when (or (and out (not (neg? (.read out))))
                 (and err (not (neg? (.read err)))))
         (println-to *err* "\n-- Output truncated after" max-lines "lines --"))
       result
       (finally (streams/close! out err))))))
