(ns sleipnir.core
  "Default routines and other boilerplate to
  launch a crawl"
  (:require [net.cgrand.enlive-html :as html]
            [org.bovinegenius.exploding-fish :as uri])
  (:import [java.io StringReader]))

(defn extract-anchors
  "Extract href attributes
  from all <a> tags on a web-page"
  [url body]
  (let [resource (-> body (StringReader.) html/html-resource)
        anchors  (html/select resource
                              [:a])
        links    (filter
                  identity
                  (map
                   (fn [an-anchor]
                     (-> an-anchor :attrs :href))
                   anchors))]
    (map
     (fn [l]
       (uri/resolve-uri url
                        l))
     links)))

(defn crawl
  "Crawl config."
  [config]
  (let [extractor (:extractor config)]
    '*))
