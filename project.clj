(defproject cljbuild "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.immutant/web "2.1.5"]
                 [ring "1.5.0"]
                 [environ "1.1.0"]]
  :main ^:skip-aot cljbuild.core
  :uberjar-name "cljbuild.jar"
  :profiles {:dev {:plugins [[lein-immutant "2.1.0"]]}
             :uberjar {:aot :all
                       :omit-source true}})
