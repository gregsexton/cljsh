(defproject shell "0.1"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[byte-streams "0.2.0"]
                 [instaparse "1.4.1"]
                 [me.raynes/conch "0.8.0"]
                 [org.clojure/clojure "1.7.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [instaparse "1.4.1"]]
  :main ^:skip-aot shell
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[midje "1.7.0"]]}})
