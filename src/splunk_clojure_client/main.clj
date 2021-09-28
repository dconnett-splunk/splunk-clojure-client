(ns splunk-clojure-client.main
  (:require [clojure.string]
            [clj-http.client :as client]
            [clojure.pprint]
            [clj-http.conn-mgr]
            [clj-http.core]
            [clj-http.cookies]))

(use 'clojure.set)

(defn deep-merge-with
  "Like merge-with, but merges maps recursively, applying the given fn
  only when there's a non-map at a particular level.
  (deep-merge-with + {:a {:b {:c 1 :d {:x 1 :y 2}} :e 3} :f 4}
                     {:a {:b {:c 2 :d {:z 9} :z 3} :e 100}})
  -> {:a {:b {:z 3, :c 3, :d {:z 9, :x 1, :y 2}}, :e 103}, :f 4}"
  [f & maps]
  (apply
   (fn m [& maps]
     (if (every? map? maps)
       (apply merge-with m maps)
       (apply f maps)))
   maps))

(def ^:dynamic *verbose* false)

(defmacro printfv
  [fmt & args]
  `(when *verbose*
     (printf ~fmt ~@args)))

(defmacro dbg [x] `(let [x# ~x] (clojure.pprint/pprint '~x "=" x#) x#))

(defn cl-print [x] (doto x (clojure.pprint/pprint)))

(def splunk-base-uri "http://restapi-hur-04.class.splunk.com:8089/services")
(def splunk-auth-uri (str splunk-base-uri "/auth/login"))
(defn splunk-delete-auth-uri [token] (str (splunk-base-uri) "/authentication/httpauth-tokens/" token))
(def splunk-auth-map
  {:form-params
   {:username "restclient" :password "restC0der" :output_mode "json"}})
(def splunk-base-params
  {:accept :json
   :throw-entire-message? true
   :decode-body-headers true :as :auto
   :headers
   {:content-type "application/x-www-form-urlencoded"}})

(defn task1-params-authenticate []
  (merge
   splunk-base-params
   splunk-auth-map)
  ;; => {:accept :json,
  ;;     :throw-entire-message? true,
  ;;     :decode-body-headers true,
  ;;     :as :auto,
  ;;     :headers {:content-type "application/x-www-form-urlencoded"},
  ;;     :form-params {:username "restclient", :password "restC0der", :output_mode "json"}}
  )

(defn task1-post []
  (client/post splunk-auth-uri (task1-params-authenticate)))

(defn task1-response-body []
  (:body (task1-post)))

(defn get-session-key []
  (select-keys (task1-response-body) [:sessionKey]))

(defn get-splunk-auth-header [token]
  {:Authorization (str "Splunk " token)})

(defn refresh-session-key []
  (def session-key (get-session-key)))

(defn splunk-session-param []
  {:header (get-splunk-auth-header (:sessionKey session-key))})


(defn task3-params [token]
  {:accept :json
   :throw-entire-message? true
   :decode-body-headers true :as :auto
   :headers (merge
             {:content-type "application/x-www-form-urlencoded"}
             (get-splunk-auth-header (:sessionKey session-key)))})

(defn task3-delete []
  (client/delete (splunk-delete-auth-uri (:sessionKey session-key)) (task3-params (:sessionKey session-key))))


(refresh-session-key)

(task3-delete)

(defn task4-params []
  (deep-merge-with union
                   splunk-base-params
                   (splunk-session-param)))
(defn task4-post []
  (client/post splunk-auth-uri (task4-params)))

(binding [clj-http.core/*cookie-store* (clj-http.cookies/cookie-store)]
  (refresh-session-key)
  (task4-post))

(def my-cs (clj-http.cookies/cookie-store))

(binding [clj-http.core/*cookie-store* (clj-http.cookies/cookie-store)]
  (client/post
   splunk-auth-uri
   {:accept :json
    :debug true
    :debug-body true
    :save-request? true
    :response-interceptor (fn [resp ctx] (println ctx))
    :throw-entire-message? true
    :decode-body-headers true
    :as :auto
    :headers
    {:content-type "application/x-www-form-urlencoded"}
    :cookie-store my-cs
    :form-params
    {:username "restclient"
     :password "restC0der"
     :output_mode "json"}}))


(clojure.pprint/pprint (let [my-cs (clj-http.cookies/cookie-store)]
                         (client/post
                          splunk-auth-uri
                          {:accept :json
                           :debug true
                           :debug-body true
                           :save-request? true
                           :throw-entire-message? true
                           :response-interceptor (fn [resp ctx] (println ctx))
                           :decode-body-headers true
                           :as :auto
                           :headers
                           {:content-type "application/x-www-form-urlencoded"}
                           :cookie-store my-cs
                           :form-params
                           {:username "restclient"
                            :password "restC0der"
                            :output_mode "json"}})))

(clojure.pprint/pprint (clj-http.cookies/get-cookies my-cs))