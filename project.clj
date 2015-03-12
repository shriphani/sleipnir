(defproject sleipnir "0.2.1"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [cheshire "5.4.0"]
                 [compojure "1.1.6"]
                 [enlive "1.1.5"]
                 [hiccup "1.0.5"]
                 [heritrix-clojure "0.1.0"]
                 [http-kit "2.1.16"]
                 [org.apache.commons/commons-lang3 "3.3.2"]
                 [org.bovinegenius/exploding-fish "0.3.4"]
                 [ring/ring-json "0.3.1"]
                 [ring-server "0.3.1"]]
  :plugins [[lein-ring "0.8.12"]]
  :ring {:handler sleipnir.handler/app
         :init sleipnir.handler/init
         :destroy sleipnir.handler/destroy}
  :profiles
  {:uberjar {:aot :all}
   :production
   {:ring
    {:open-browser? false, :stacktraces? false, :auto-reload? false}}
   :dev
   {:dependencies [[ring-mock "0.1.5"] [ring/ring-devel "1.3.1"]]}})
