(ns sleipnir.handler
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [compojure.core :refer :all]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.middleware.json :as middleware]
            [hiccup.middleware :refer [wrap-base-url]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [sleipnir.core :as core]
            [org.httpkit.server :as server]
            [heritrix-clojure.core :as heritrix]))

(defn init []
  (println "sleipnir is starting"))

(defn destroy []
  (println "sleipnir is shutting down"))

(def app-config (atom {:extractor core/extract-anchors
                       :port 3000}))

(defroutes app-routes
  (POST "/extract" request ((:extractor @app-config) request)))

(defn crawl
  "Sets up a crawl. Expects heritrix to be running!!"
  [config]
  (let [heritrix-engine-addr (:heritrix-addr config)
        job-dir              (:job-dir config)
        heritrix-uname       (:username config)
        password             (:password config)
        seeds                (:seeds-file config)
        job-name             (-> job-dir
                                 (string/split #"/")
                                 last)]
    (do (swap! app-config merge config)  ; resolve the config

        ;; spin up webservice
        (server/run-server (handler/site #'app-routes)
                           {:port (:port @app-config)})

        ;; create the directory
        (when-not (.exists
                   (io/as-file job-dir))
          (.mkdir (io/as-file job-dir)))

        ;; generate-config-file
        (let [new-config (core/generate-config-file @app-config)]
          (spit (str job-dir
                     "/crawler-beans.cxml")
                new-config))

        ;; add-job to heritrix
        (heritrix/add heritrix-engine-addr
                      job-dir
                      heritrix-uname
                      password)

        (let [job-addr (str heritrix-engine-addr "/job/" job-name)]

          ;; build
          (heritrix/build job-addr
                          heritrix-uname
                          password)

          ;; launch
          (heritrix/launch job-addr
                           heritrix-uname
                           password)

          (str "Job is launched at: " job-addr)))))
