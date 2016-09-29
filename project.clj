(defproject cljbuild "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.immutant/web "2.1.5"]
                 [ring "1.5.0"]
                 [environ "1.1.0"]
                 [org.clojars.saeidscorp/optimus "0.19.0-SNAPSHOT"]
                 [org.clojars.saeidscorp/optimus-img-transform "0.2.0-SNAPSHOT"]
                 [org.clojars.saeidscorp/optimus-sass "0.1.0-SNAPSHOT"]
                 [selmer "1.0.7"]]
  ;:bower-dependencies [[jquery "^3.1.0"]]
  ;:bower {:directory "dev-resources/libs/components"}
  :main ^:skip-aot cljbuild.core
  :uberjar-name "cljbuild.jar"

  :jvm-opts ^:replace ["-Xmx500m" "-XX:MaxHeapFreeRatio=40"]

  :profiles {:dev     {:plugins [[lein-immutant "2.1.0"]
                                 [lein-environ "1.1.0"]
                                 ;[lein-bower "0.5.1"]
                                 ]
                       :env     {:optimus-js-engine "nashorn"}}
             :uberjar {:aot         :all
                       :omit-source true}})
