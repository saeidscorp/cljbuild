(ns cljbuild.core
  (:require [immutant.web :refer [run]]
            [environ.core :refer [env]]
            [ring.util.response :as response]
            [ring.middleware.content-type]
            [ring.middleware.not-modified]
            [selmer.parser :as selmer]

            [clojure.java.io :as jio]

            (optimus [prime :as optimus]
                     [assets :as assets]
                     [optimizations :as optimizations]
                     [strategies :as strategies]
                     [html :as html]))
  (:gen-class))

(selmer/add-tag! :assets
                 (fn [[bundle-name] context-map]
                   (cond (.endsWith ^String bundle-name ".css")
                         (html/link-to-css-bundles (:request context-map) [bundle-name])

                         (.endsWith ^String bundle-name ".js")
                         (html/link-to-js-bundles (:request context-map) [bundle-name]))))

(defn get-assets []                                         ;; 4
  (concat                                                   ;; 5
    (assets/load-bundle "libs/components"                   ;; 6
                        "vendor-styles.css"                 ;; 7
                        ["/semantic/dist/semantic.css"      ;; 8
                         "/font-awesome/css/font-awesome.css"]) ;; 9
    (assets/load-bundle "public"
                        "app-styles.css"
                        ["/styles/app.css"])
    (assets/load-bundles "libs/components"                  ;; 10
                         {"vendor-scripts.js" ["/jquery/dist/jquery.js"
                                               "/semantic/dist/semantic.js"]})
    (assets/load-assets "libs/components"                   ;; 12
                        ["/font-awesome/fonts/fontawesome-webfont.ttf"
                         "/semantic/dist/themes/default/assets/fonts/icons.ttf"
                         "/semantic/dist/themes/default/assets/images/flags.png"])))

(selmer/set-resource-path! (jio/resource "public"))

(defn make-handler [profile]
  (-> (fn [req] (response/content-type (response/response
                                         (selmer/render-file "main.html" {:request req})) "text/html"))
      (optimus/wrap get-assets
                    (if (= :dev profile)                    ;; 16
                      optimizations/none                    ;; 17
                      optimizations/all)                    ;; 18
                    (if (= :dev profile)                    ;; 19
                      strategies/serve-live-assets          ;; 20
                      strategies/serve-frozen-assets))      ;; 21
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
