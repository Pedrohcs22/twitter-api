(ns twitter-api.handler
  (:require [compojure.core :refer :all]
     [compojure.handler :as handler]
     [compojure.route :as route]
     [ring.middleware.json :as json]
     [ring.util.response :refer [response]]
     [twitter-api.database :refer :all]))

(defroutes app-routes
  (GET "/rest/v1/tweet" []
       (response (retrieveAllTweets)))
  (GET "/rest/v1/tweet/:id" [id]
       (response (retrieveTweet (Integer/parseInt id))))
  (POST "/rest/v1/tweet" {:keys [params]}
    (let [{:keys [title description]} params]
      (response (createTweet title description))))
  (DELETE "/rest/v1/tweet/:id" [id]
        (response (deleteTweet (Integer/parseInt id))))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (json/wrap-json-params)
      (json/wrap-json-response)))