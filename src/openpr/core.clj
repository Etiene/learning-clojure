(ns openpr.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.edn :as edn]
            [ring.middleware.json :as json]
            [ring.middleware.params :as params]
            [clj-http.client :as client]
            [environ.core :refer [env]]))

(defn- github-fetch [& params]
  ((client/get (str "https://api.github.com/" (apply str params))
    {:basic-auth [(env :github-user) (env :github-pass)] :accept :json :as :auto}) :body))

(defn- get-team-id [org team]
  (let [teams (github-fetch "orgs/" org "/teams")]
    ((first(filter #(= (% :name) team) teams)) :id)))

(defn- get-members [org team]
  (github-fetch "teams/" (get-team-id org team) "/members"))

(defn- get-org-prs [org team]
  (let [repos (github-fetch "orgs/" org "/repos")]
    (concat (map #(github-fetch "repos/" org "/" (% :name) "/pulls") repos))))

(defroutes api-routes
  (GET "/:org/:team" [org team] {:body (get-org-prs org team)})
  (route/not-found {:body {:error "Page not found"}}))

(def api
  (-> api-routes
    params/wrap-params
    json/wrap-json-response))
