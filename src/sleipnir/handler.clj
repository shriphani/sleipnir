(ns sleipnir.handler
  (:require [compojure.core :refer :all]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.middleware.json :as middleware]
            [hiccup.middleware :refer [wrap-base-url]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [sleipnir.core :as core]))

(defn init []
  (println "sleipnir is starting"))

(defn destroy []
  (println "sleipnir is shutting down"))

(def extractor (atom core/extract-anchors))

(defroutes app-routes
  (POST "/extract" request (@extractor request)))

(def app
  (-> (routes app-routes)
      (handler/site)
      (wrap-base-url)))
