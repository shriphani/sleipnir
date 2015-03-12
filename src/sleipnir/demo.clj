(ns sleipnir.demo
  "Dude this is the demo"
  (:require [net.cgrand.enlive-html :as html]
            [sleipnir.handler :as handler]
            [org.bovinegenius.exploding-fish :as uri])
  (:import [java.io StringReader]))

(defn reddit-pagination-extractor
  "Pulls reddit pagination using enlive"
  [url body]
  (let [resource (-> body (StringReader.) html/html-resource)
        anchors  (html/select resource [:span.nextprev :a])]
    (filter
     identity
     (map
      (fn [an-anchor]
        (println an-anchor)
        (try (uri/resolve-uri url
                              (-> an-anchor
                                  :attrs
                                  :href))))
      anchors))))

(defn reddit-submission-links-writer
  "Gets links to reddit submissions"
  [url body wrtr]
  (let [resource (-> body (StringReader.) html/html-resource)
        submissions (html/select resource
                                 [:p.title :a.title])
        links (filter
               identity
               (map
                (fn [an-anchor]
                  (try (uri/resolve-uri url
                                        (-> an-anchor
                                            :attrs
                                            :href))))
                submissions))]
    (doseq [link links]
     (binding [*out* wrtr]
       (println link)))))

(handler/crawl {:heritrix-addr "https://localhost:8443/engine"
                :job-dir       "/Users/shriphani/Documents/reddit-job"
                :username      "admin"
                :password      "admin"
                :seeds-file    "/Users/shriphani/Documents/reddit-job/seeds.txt"
                :contact-url   "http://shriphani.com/"
                :out-file      "/tmp/bodies.clj"
                :extractor     reddit-pagination-extractor
                :writer        reddit-submission-links-writer})
