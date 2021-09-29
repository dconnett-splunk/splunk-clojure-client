(ns splunk-clojure-client.postman  
  (:require [clojure.string]
            [clj-http.client :as client]
            [clojure.pprint]
            [clj-http.conn-mgr]
            [clj-http.core]
            [clj-http.cookies]
            [clojure.data.json :as json]))

(def postman (json/read-str (slurp "/Users/dconnett/Documents/Training/Developing With the Splunk REST API 8.1/Optional Files/Splunk REST Labs 8.1 Solution.postman_collection.json")))
