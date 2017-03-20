(ns openpr.core
  (:require [clojure.edn :as edn]
            [clojure.string :refer [join]]
            [clj-http.client :as client]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [environ.core :refer [env]]
            [ring.middleware.json :as json]
            [ring.middleware.params :as params]))

(defn- github-fetch [& params]
  (-> "https://api.github.com/"
      (str (join "/" params))
      (client/get {:basic-auth [(:github-user env) (:github-pass env)] :accept :json :as :auto})
      :body))

(defn- get-team-id [org team]
  (->> (github-fetch "orgs" org "teams")
       (filter #(= (:name %) team))
       first
       :id))

(defn- get-member-logins [org team]
  (->> (github-fetch "teams" (get-team-id org team) "members")
       (map :login)))

(defn- get-org-prs [org]
  (->> (github-fetch "orgs" org "repos")
       (map #(github-fetch "repos" org (:name %) "pulls"))
       flatten))

(defn- get-team-prs [org team]
  (let [prs (get-org-prs org) member-logins (get-member-logins org team)]
    (filter #(some #{(:login (:user %))} member-logins) prs)))

(defroutes api-routes
  (GET "/:org/:team" [org team] {:body (get-team-prs org team)})
  (route/not-found {:body {:error "Page not found"}}))

(def api
  (-> api-routes
      params/wrap-params
      json/wrap-json-response))
