(defproject splunk-clojure-client "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main splunk-clojure-client.main
  :aot [splunk-clojure-client.main]
  :uberjar-name "splunk-clojure-client-standalone.jar"
  ;; :plugins [[lein-swank "1.4.4"]]
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/tools.cli "1.0.206"]
                 [compojure "1.6.2"]
                 [ring/ring-core "1.9.4"]
                 [clj-http "3.12.3"]
                 [cheshire "RELEASE"] ;; for :as :json
                 [crouton "RELEASE"] ;; for :decode-body-headers
                 [org.clojure/tools.reader "RELEASE"] ;; for :as :clojure
                 [ring/ring-codec "RELEASE"] ;; for :as :x-www-form-urlencoded
                 [org.clojure/data.json "2.4.0"]
                 [luposlip/json-schema "0.3.2"]


                 [http-kit "2.5.3"]

                 [http-kit/dbcp "0.1.0"] ;; database access

                 [mysql/mysql-connector-java "8.0.26"] ;; mysql jdbc driver

                 ;; [org.fressian/fressian "0.6.3"]

                 ;; for serialization clojure object to bytes
                 ;; [com.taoensso/nippy "1.1.0"]

                 ;; Redis client & message queue
                 ;; [com.taoensso/carmine "1.5.0"]

                 ;; logging,  another option [com.taoensso/timbre "1.5.2"]
                 [org.clojure/tools.logging "1.1.0"]
                 [ch.qos.logback/logback-classic "1.2.6"]
                 ;; template
                 [me.shenfeng/mustache "1.1"]]
                   
                 
        :profiles {:uberjar {:aot :all}
                              :reveal {:dependencies [[vlaaad/reveal "1.3.219"]]
                                       :repl-options {:nrepl-middleware [vlaaad.reveal.nrepl/middleware]}
                                       :jvm-opts ["-Dfile.encoding=UTF8" "-Dvlaaad.reveal.prefs={:font-size 14}"]}})
