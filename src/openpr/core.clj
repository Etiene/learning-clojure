(ns openpr.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.edn :as edn]
            [ring.middleware.json :as json]
            [ring.middleware.params :as params]
            [clj-http.client :as client]
            [environ.core :refer [env]]))

(defn get-team-id [org team]
  (let [res
    ((client/get (str "https://api.github.com/orgs/" org "/teams")
        {:basic-auth [(env :github-user) (env :github-pass)] :accept :json :as :auto})
          :body)]
    ((first(filter #(= (% :name) team) res)) :id)))

(defroutes api-routes
  (GET "/:org/:team" [org team] {:body {:id (get-team-id org team)}})
  (route/not-found {:body {:error "Page not found"}}))

(def api
  (-> api-routes
    params/wrap-params
    json/wrap-json-response))
