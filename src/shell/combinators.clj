(ns shell.combinators
  (:refer-clojure :exclude [print])
  (:require [clojure.core.async :as async]
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

;; TODO: this is broken
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
              (if v
                (do (println v)
                    (recur (inc cnt) cs))
                (recur cnt (filter #{c} cs)))))))
       (when (or (and out (not (neg? (.read out))))
                 (and err (not (neg? (.read err)))))
         (binding [*out* *err*]
           (println "\n-- Output truncated after" max-lines "lines --")))
       result
       (finally (streams/close! out err))))))
