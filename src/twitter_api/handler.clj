(ns twitter-api.handler
  (:require [compojure.core :refer :all]
     [compojure.handler :as handler]
     [compojure.route :as route]
     [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
     [ring.util.response :refer [response]]
     [twitter-api.database :refer :all]))

(defroutes app-routes
  (GET "/rest/v1/user/:id/tweet" [id]
       (retrieveTweetsForUser id))
  (GET "/rest/v1/tweet/:id" [id]
       {:status 200
        :headers {}
        :content-type "application/json; charset=UTF-8"
        :body (retrieveTweet id)})
  (POST "/rest/v1/tweet" req
        (let [owner (get-in req [:body "owner"])
              content (get-in req [:body "content"])]
          (createTweet owner content)))
  (DELETE "/rest/v1/tweet/:id" [id]
        (response (deleteTweet (Integer/parseInt id))))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (wrap-json-body)
      (wrap-json-response)))