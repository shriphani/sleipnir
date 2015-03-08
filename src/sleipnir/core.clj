(ns sleipnir.core
  "Default routines and other boilerplate to
  launch a crawl"
  (:require [cheshire.core :refer :all]
            [net.cgrand.enlive-html :as html]
            [org.bovinegenius.exploding-fish :as uri]
            [clojure.pprint :refer [pprint]])
  (:import [java.io StringReader]
           [java.net URLDecoder]
           [org.apache.commons.lang3 StringEscapeUtils]))

(defn extract-anchors
  "Extract href attributes
  from all <a> tags on a web-page"
  [request]
  (println request)
  (let [url (-> request :params :url)
        body (-> request :params :body)
        decoded-uri (URLDecoder/decode url "UTF-8")
        unescapd-html (StringEscapeUtils/unescapeHtml4 body)
        resource (-> unescapd-html (StringReader.) html/html-resource)
        anchors  (html/select resource [:a])
        links    (filter
                  identity
                  (map
                   (fn [an-anchor]
                     (try (uri/resolve-uri decoded-uri
                                           (-> an-anchor
                                               :attrs
                                               :href))
                          (catch NullPointerException e nil)))
                   anchors))]
    (println decoded-uri)
    (println body)
    (println links)
    (generate-string links)))

(defn crawl
  "Crawl config."
  [config]
  (let [extractor (:extractor config)]
    '*))
