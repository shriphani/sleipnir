(ns sleipnir.handler
  (:require [compojure.core :refer [defroutes routes]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [hiccup.middleware :refer [wrap-base-url]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [sleipnir.routes.home :refer [home-routes]]))

(defn init []
  (println "sleipnir is starting"))

(defn destroy []
  (println "sleipnir is shutting down"))

(defroutes app-routes
  (route/resources "/extractor")
  (route/resources "/writer")
  (route/not-found "Not Found"))

(def app
  (-> (routes home-routes app-routes)
      (handler/site)
      (wrap-base-url)))
