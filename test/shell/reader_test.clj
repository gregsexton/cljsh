(ns shell.reader-test
  (:refer-clojure :exclude [read])
  (:require [shell.reader :refer :all]
            [midje.sweet :refer :all]))

(facts "about coercing simple commands"
       (fact "should coerce a simple command"
             (read "ls") => '((cmd ls)))
       (fact "should coerce a command with an arg"
             (read "ls -lah") => '((cmd ls -lah)))
       (fact "should coerce a command with args"
             (read "ls -l -ah") => '((cmd ls -l -ah)))
       (fact "should handle args that can't be symbols"
             (read "ls /foo/bar") => (list (list 'cmd 'ls (symbol "/foo/bar")))))

(facts "about coercing single quoted args"
       (fact "should handle single-quoted args"
             (read "ls 'foo bar'") => '((cmd ls "foo bar")))
       (fact "should handle multiple quoted args"
             (read "ls 'foo' 'bar'") => '((cmd ls "foo" "bar")))
       (fact "should handle single-quoted args containing escaped quotes"
             (read "ls 'foo \\'bar'") => '((cmd ls "foo 'bar")))
       (fact "should handle single-quotes containing double quotes"
             (read "ls 'blah\"'") => '((cmd ls "blah\"")))
       (fact "should handle an ambiguous quote sequence (if there were no escapes)"
             (read "ls 'foo''bar'") => '((cmd ls "foo" "bar")))
       (fact "should error if a quote contains an unescaped quote char"
             (read "ls 'foo'bar'") => throws)
       (fact "should handle two escaped quotes next to each other"
             (read "ls 'foo\\'\\'bar'") => '((cmd ls "foo''bar")))
       (fact "should error on an invalid quote sequence"
             (read "ls 'foo'\\'bar'") => throws)
       (fact "should handle a pipe in a quote"
             (read "ls 'foo|bar'") => '((cmd ls "foo|bar")))
       (fact "should handle empty quote"
             (read "ls ''") => '((cmd ls ""))))

(facts "about coercing piped sequences of commands"
       (fact "should coerce a piped command"
             (read "ls|grep") => '((| (cmd ls) (cmd grep))))
       (fact "should coerce a piped command with args"
             (read "ls -lah|grep -v blah") => '((| (cmd ls -lah)
                                                   (cmd grep -v blah)))))

(facts "about double quotes"
       (fact "allow double quotes but they are not special"
             (read "ls \"foo\"") => (list (list 'cmd 'ls (symbol "\"foo\"")))))

(facts "about whitespace"
       (fact "should allow leading whitespace"
             (read "  foo") => '((cmd foo)))
       (fact "should allow trailing whitespace"
             (read "foo   ") => '((cmd foo)))
       (fact "should allow arbitrary whitespace between args"
             (read "foo    bar    baz") => '((cmd foo bar baz)))
       (fact "should handle tabs"
             (read "\t foo \t bar \t baz \t") => '((cmd foo bar baz))))

;;; TODO: error propagation

