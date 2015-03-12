# sleipnir

Sleipnir allows you to specify clojure routines to use with
battle-tested web-crawlers (heritrix in particular).

## Running

First, spin up a Heritrix instance (REQUIRED for a crawl to complete).

```
wget https://s3-us-west-2.amazonaws.com/sleipnir-heritrix/heritrix-3.3.0-SNAPSHOT-dist.zip
unzip heritrix-3.3.0-SNAPSHOT-dist.zip
cd heritrix-3.3.0-SNAPSHOT-dist
./bin/heritrix -a admin:admin
```

Now Heritrix is running at https://localhost:8443 and can be accessed
with the username/pass : admin/admin.


Next, let us set up a simple crawl using clojure routines. 

We start with the imports:

```clojure
(ns sleipnir.demo
  "Dude this is the demo"
  (:require [net.cgrand.enlive-html :as html]
            [sleipnir.handler :as handler]
            [org.bovinegenius.exploding-fish :as uri])
  (:import [java.io StringReader]))
```

Say, I want
to walk through reddit's pagination. We use
enlive selectors for our extractor code:

```clojure
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
```

Then, we want to store the submitted links in some location

```clojure
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
```

And then set up and execute the crawl. The config object has a ton of
options (I'll flesh the documentation out soon). Several of these
options tweak Heritrix's settings.

```clojure
(handler/crawl {:heritrix-addr "https://localhost:8443/engine"
                :job-dir       "/Users/shriphani/Documents/reddit-job"
                :username      "admin"
                :password      "admin"
                :seeds-file    "/Users/shriphani/Documents/reddit-job/seeds.txt"
                :contact-url   "http://shriphani.com/"
                :out-file      "/tmp/bodies.clj"
                :extractor     reddit-pagination-extractor
                :writer        reddit-submission-links-writer})
```

In the config above, we specify where heritrix is launched, the job
directory, the payload directory and the extraction and writer routines.

The result is a heritrix job that walks through the pagination and dumps
the submitted links to `/tmp/bodies.clj`.

Here's a screengrab of the job:

<img src="http://blog.shriphani.com/img/heritrix-sleipnir-demo.png" />


## License

Copyright © 2015 Shriphani Palakodety
