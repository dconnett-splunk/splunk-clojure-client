(ns splunk-clojure-client.main
  (:require [clojure.string]
            [clj-http.client :as client]
            [clojure.pprint]
            [clj-http.conn-mgr]
            [clj-http.core]
            [clj-http.cookies]))

(use 'clojure.set)

;; Merge maps of data recusrively, I use this to compose HTTP params.
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

;; Debugging helper, not sure how this works...
(def ^:dynamic *verbose* false)

;; Debugging helper, not sure how this works...
(defmacro printfv
  [fmt & args]
  `(when *verbose*
     (printf ~fmt ~@args)))


;; Debugging helper, I don't know how this works...
(defmacro dbg [x] `(let [x# ~x] (clojure.pprint/pprint '~x "=" x#) x#))


;; Debugging helper, add function to pretty print function call.
(defn cl-print [x] (doto x (clojure.pprint/pprint)))


;; Splunk base instance URI.
(def splunk-base-uri "http://restapi-hur-04.class.splunk.com:8089/services")

;; Splunks login endpoint.
(def splunk-auth-uri (str splunk-base-uri "/auth/login"))

;; Splunks search endpoint.
(def splunk-search-uri (str splunk-base-uri "/search/jobs"))

;; Splunks session delete endpoint.
(defn splunk-delete-auth-uri [token] (str splunk-base-uri "/authentication/httpauth-tokens/" token))

;; Creates params for authentication. Change these for your environment.
(def splunk-auth-map
  {:form-params
   {:username "restclient" :password "restC0der" :output_mode "json"}})
(
 ;; Base HTTP params that are common to all HTTP methods.
 def splunk-base-params
  {:accept :json
   :throw-entire-message? true
   :decode-body-headers true :as :auto
   :headers
   {:content-type "application/x-www-form-urlencoded"}})

;; Defines task1 HTTP Parameters.
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

;; Executes task1 post.
(defn task1-post []
  (client/post splunk-auth-uri (task1-params-authenticate)))

;; Gets task1's HTTP response body.
(defn task1-response-body []
  (:body (task1-post)))

;; Gets session key task1's body. Executes task1 to do this.
(defn get-session-key []
  (select-keys (task1-response-body) [:sessionKey]))

;; Takes a session token, and produces the Splunk formatted HTTP param.
(defn get-splunk-auth-header [token]
  {:Authorization (str "Splunk " token)})

;; Refreshes current session key.
(defn refresh-session-key []
  (def session-key (get-session-key)))

;; Formats the current session key for use in composing HTTP params.
(defn splunk-session-param []
  {:header (get-splunk-auth-header (:sessionKey session-key))})

;; Define task3 HTTP params.
(defn task3-params [token]
  {:accept :json
   :throw-entire-message? true
   :decode-body-headers true :as :auto
   :headers (merge
             {:content-type "application/x-www-form-urlencoded"}
             (get-splunk-auth-header (:sessionKey session-key)))})

;; Complete task3 task.
(defn task3-delete []
  (client/delete (splunk-delete-auth-uri (:sessionKey session-key)) (task3-params (:sessionKey session-key))))


(refresh-session-key)

(task3-delete)

;; Define task4 HTTP Parameters.
(defn task4-params []
  (deep-merge-with union
                   splunk-base-params
                   (splunk-session-param)))

;; Complete task 4 of the REST API Splunk Course.
(defn task4-post []
  (client/post splunk-auth-uri (task4-params)))

;; Another cookie test. Doesn't work...
;; (binding [clj-http.core/*cookie-store* (clj-http.cookies/cookie-store)]
;;   (refresh-session-key)
;;   (task4-post))

;; Create a cookie store to be used for subsequent calls.
(def my-cs (clj-http.cookies/cookie-store))

;; Simple test of setting cookies with binding. Currently doesn't seem to work.
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

;; Simple test of setting cookies with let Currently doesn't seem to work.
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