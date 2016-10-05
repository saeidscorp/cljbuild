(ns cljbuild.core
  (:require [immutant.web :refer [run]]
            [environ.core :refer [env]]
            [ring.util.response :as response]
            [ring.middleware.content-type]
            [ring.middleware.not-modified]
            [selmer.parser :as selmer]
            [bundle-reader.core :as br]

            [clojure.java.io :as jio]

            (optimus [prime :as optimus]
                     [assets :as assets]
                     [optimizations :as optimizations]
                     [strategies :as strategies]
                     [html :as html])
            [optimus-sass.core])
  (:gen-class))

(selmer/remove-tag! :component)

(selmer/add-tag! :component
                 (fn [[type component-name] context-map]
                   (case type
                     "css" (html/link-to-css-bundles (:request context-map) [(br/component-handle component-name :css)])
                     "js" (html/link-to-js-bundles (:request context-map) [(br/component-handle component-name :js)]))))

(selmer/add-tag! :assets
                 (fn [[bundle-name] context-map]
                   (cond (.endsWith ^String bundle-name ".css")
                         (html/link-to-css-bundles (:request context-map) [bundle-name])

                         (.endsWith ^String bundle-name ".js")
                         (html/link-to-js-bundles (:request context-map) [bundle-name]))))

(defn get-assets []                                         ;; 4
  (let [[bundles resources] (br/get-assets-from-resources "public/"
                                                          ["app-styles.css"]
                                                          [])
        [vendor-bundles vendor-resources] (br/get-assets-from-resources "libs/"
                                                                        []
                                                                        ["main"])]
    (concat (apply assets/load-bundles bundles)
            (apply assets/load-assets resources) ;; 5
            (apply assets/load-bundles vendor-bundles)
            (apply assets/load-assets vendor-resources)
            #_(assets/load-bundle "libs/components"         ;; 6
                                  "vendor-styles.css"       ;; 7
                                  ["/semantic/dist/semantic.min.css" ;; 8
                                   "/font-awesome/css/font-awesome.min.css"
                                   "/lato-font/css/lato-font.min.css"]) ;; 9
            #_(assets/load-bundles "libs/components"        ;; 10
                                   {"vendor-scripts.js" ["/jquery/dist/jquery.min.js"
                                                         "/semantic/dist/semantic.min.js"]}))))

(selmer/set-resource-path! (jio/resource "public"))

(defn make-handler [profile]
  (-> (fn [req] (response/content-type (response/response
                                         (selmer/render-file "main.html" {:request      req
                                                                          :scripts      (optimus.html/link-to-js-bundles req (br/component-handle "main" :js))
                                                                          :stylesheets  (optimus.html/link-to-css-bundles req (br/component-handle "main" :css))
                                                                          :avatar-image (optimus.link/file-path req "/images/steve.jpg")})) "text/html"))
      (optimus/wrap get-assets
                    (if (= :dev profile)                    ;; 16
                      optimizations/none                    ;; 17
                      optimizations/all)                    ;; 18
                    (if (= :dev profile)                    ;; 19
                      strategies/serve-live-assets          ;; 20
                      strategies/serve-frozen-assets)
                    {:verbose true})      ;; 21
      (ring.middleware.content-type/wrap-content-type)      ;; 22
      (ring.middleware.not-modified/wrap-not-modified)))

(defonce server (atom nil))

(defn start [& {:keys [profile]
                :or   {profile :dev}}]
  (when-not @server
    (reset! server (run (make-handler profile)
                        {:port (env :openshift-diy-port 8082),
                         :host (env :openshift-diy-ip "127.0.0.1")}))))

(defn stop []
  (when @server
    (immutant.web/stop @server)
    (reset! server nil)))

(defn go []
  (stop)
  (start)
  :done)

(defn -main [& args]
  (println "Starting server...")
  (start :profile :prod)
  (println "Server started."))
