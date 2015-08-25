(ns shell.reader
  (:refer-clojure :exclude [read])
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [instaparse.core :as insta]))

(def ^:private parser
  (insta/parser
   "Pipe = Cmd (<'|'> Cmd)*
    Cmd = Program Arg*
    Program = Token
    Arg = Token
    Token = <ws*> (tchars|SQuoted) <ws*>
    SQuoted = <'\\''> (escapedQuote|quoteChar)* <'\\''>
    ws = #'\\s+'
    escapedQuote = '\\\\\\''        (* backslash quote *)
    quoteChar = #'(?!\\\\\\'|\\').' (* anything except escaped quote or a quote *)
    tchars = #'[a-zA-Z0-9_/~+\"-]+'"))

(defn- simplify [tree] tree)

(defn- rewrite [tree]
  (insta/transform
   {:Pipe (fn [cmd & more]
            (if (empty? more)
              (list cmd)
              (list (list* '| cmd more))))
    :Cmd (fn [program & args]
           (list* 'cmd program args))
    :Program identity
    :Arg identity
    :SQuoted str
    :Token identity
    :escapedQuote (constantly "'")
    :quoteChar str
    :ws str
    :tchars symbol
    :any str}
   tree))

(defn- coerce [s]
  (-> s
      parser
      rewrite
      simplify))

(defn read [s]
  (try (let [form (->> s
                       java.io.StringReader.
                       java.io.PushbackReader.
                       clojure.core/read)]
         (if (list? form) form (coerce s)))
       (catch Exception _ (coerce s))))
