(ns cljbuild.core
  (:require [immutant.web :refer [run]]
            [environ.core :refer [env]]
            [ring.util.response :as response])
  (:gen-class))

(defn make-handler []
  (-> (fn [req] (response/response "Hello World From Wildfly!"))))

(defn -main [& args]
  (println "Starting server...")
  (run (make-handler) {:port (env :openshift-diy-port 8082) :host (env :openshift-diy-ip "127.0.0.1")})
  (println "Server started."))
