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
            [heritrix-clojure.core :as heritrix])
  (:import [java.io StringReader]
           [java.net URLDecoder]
           [org.apache.commons.lang3 StringEscapeUtils]))

(defn init []
  (println "sleipnir is starting"))

(defn destroy []
  (println "sleipnir is shutting down"))

(def app-config (atom {:extractor core/extract-anchors
                       :port      3000
                       :archive?  false
                       :writer    core/write-clj-obj}))

(defn process-request
  [request]
  [(-> request
       :params
       :url
       (URLDecoder/decode "UTF-8"))
   (-> request
       :params
       :body
       (StringEscapeUtils/unescapeHtml4))])

(defn extractor
  [extractor-routine request]
  (let [[decoded-uri unescaped-html] (process-request request)]
    (extractor-routine decoded-uri unescaped-html)))

(defn writer
  [writer-routine request out-file]
  (when out-file
    (let [wrtr (io/writer out-file :append true)
          [decoded-uri unescaped-html] (process-request request)]
      (writer-routine decoded-uri
                      unescaped-html
                      wrtr)
      (.close wrtr))))

(defroutes app-routes
  (POST "/extract" request (extractor (:extractor @app-config) request))
  (POST "/write" request (writer (:writer @app-config)
                                 request
                                 (:out-file @app-config))))

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
                                 last)
        new-config           (merge config
                                    {:extractor-address
                                     (str "http://localhost:"
                                          (or (:port config)
                                              (:port @app-config))
                                          "/extract")
                                     :writer-address (if (:out-file config)
                                                       (str "http://localhost:"
                                                            (or (:port config)
                                                                (:port @app-config))
                                                            "/write")
                                                       "blank")})]
    (do (swap! app-config merge new-config)  ; resolve the config

        ;; spin up webservice
        (server/run-server (handler/site #'app-routes)
                           {:port (:port @app-config)})

        ;; create the directory
        (when-not (.exists
                   (io/as-file job-dir))
          (.mkdir (io/as-file job-dir)))

        ;; generate-config-file
        (let [new-config-file (core/generate-config-file @app-config)]
          (spit (str job-dir
                     "/crawler-beans.cxml")
                new-config-file))

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
