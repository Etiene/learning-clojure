(ns openpr.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.edn :as edn]
            [ring.middleware.json :as json]
            [ring.middleware.params :as params]
            [clj-http.client :as client]
            [environ.core :refer [env]]))

(defn- get-team-id [org team]
  (let [teams
    ((client/get (str "https://api.github.com/orgs/" org "/teams")
        {:basic-auth [(env :github-user) (env :github-pass)] :accept :json :as :auto}) :body)]
    ((first(filter #(= (% :name) team) teams)) :id)))

(defn- get-members [org team]
  ((client/get (str "https://api.github.com/teams/" (get-team-id org team) "/members")
      {:basic-auth [(env :github-user) (env :github-pass)] :accept :json :as :auto}) :body))

(defn- get-repo-prs [org repo]
  ((client/get (str "https://api.github.com/repos/" org "/" repo "/pulls")
      {:basic-auth [(env :github-user) (env :github-pass)] :accept :json :as :auto}) :body))

(defn- get-org-prs [org team]
  (let [repos ((client/get (str "https://api.github.com/orgs/" org "/repos")
      {:basic-auth [(env :github-user) (env :github-pass)] :accept :json :as :auto}) :body)]
    (concat (map #( get-repo-prs org (% :name) ) repos))))

(defroutes api-routes
  (GET "/:org/:team" [org team] {:body (get-org-prs org team)})
  (route/not-found {:body {:error "Page not found"}}))

(def api
  (-> api-routes
    params/wrap-params
    json/wrap-json-response))
