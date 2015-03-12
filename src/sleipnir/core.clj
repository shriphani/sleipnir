(ns sleipnir.core
  "Default routines and other boilerplate to
  launch a crawl"
  (:require [cheshire.core :refer :all]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [clojure.string :as string]
            [net.cgrand.enlive-html :as html]
            [org.bovinegenius.exploding-fish :as uri])
  (:import [java.io StringReader]
           [java.net URLDecoder]
           [org.apache.commons.lang3 StringEscapeUtils]))

(defn extract-anchors
  "Extract href attributes
  from all <a> tags on a web-page"
  [decoded-uri unescaped-html]
  (let [resource (-> unescaped-html (StringReader.) html/html-resource)
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
    (println links)
    links))

(defn write-clj-obj
  "Writes a clojure object as the payload"
  [decoded-uri unescaped-html wrtr]
  (pprint {:url     decoded-uri
           :payload unescaped-html}
          wrtr))

(def configs-defaults
  {:contact-url ["SLEIPNIR_CONTACT_URL" nil]
   :job-name    ["SLEIPNIR_JOB_NAME" nil]
   :description ["SLEIPNIR_JOB_DESCRIPTION" nil]
   :seeds-file  ["SLEIPNIR_SEEDS_FILE" "seeds.txt"]
   :extractor-address ["SLEIPNIR_EXTRACTOR_ADDRESS"
                       "http://localhost:3000/extract"]
   :writer-address ["SLEIPNIR_WRITER_ADDRESS"
                    "blank"]})

(defn generate-config-file
  [config]
  (let [default-config-file (slurp
                             (io/resource "crawler-beans.cxml"))]
    (assert (:seeds-file config)) ; seeds file cannot be non-existent
    (assert (:contact-url config)) ; contact url can't be nil
    (let [config-contents
          (reduce
           (fn [acc [item [key default-val]]]
             (string/replace acc
                             (re-pattern key)
                             (str
                              (or (get config item)
                                  default-val))))
           default-config-file
           configs-defaults)]
      config-contents)))
