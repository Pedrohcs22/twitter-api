(ns twitter-api.handler
  (:require [compojure.core :refer :all]
     [compojure.handler :as handler]
     [compojure.route :as route]
     [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
     [ring.util.response :refer [response]]
     [twitter-api.database :refer :all]))

(defroutes user-routes
  (POST "/rest/v1/user" req
        (let [username (get-in req [:body "username"])
              password (get-in req [:body "password"])]
          (create-user username password)))
  (GET "/rest/v1/user/:id/tweet" [id]
       {:status 200
        :headers {}
        :content-type "application/json; charset=UTF-8"
        :body (retrieve-tweets-for-user id)}))

(defroutes login-routes
  (POST "/rest/v1/login" req
        (let [username (get-in req [:body "username"])
              password (get-in req [:body "password"])]
          (login username password))))

(defroutes tweet-routes
  (GET "/rest/v1/tweet/:id" [id]
       {:status 200
        :headers {}
        :content-type "application/json; charset=UTF-8"
        :body (retrieve-tweet id)})
  (POST "/rest/v1/tweet" req
        (let [owner (get-in req [:body "owner"])
              content (get-in req [:body "content"])]
          (create-tweet owner content)))
  (PUT "/rest/v1/tweet/:id/like" [id]
       (like-tweet id))
  (DELETE "/rest/v1/tweet/:id" [id]
          (response (delete-tweet (Integer/parseInt id)))))

(defroutes app-routes
  user-routes
  tweet-routes
  login-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (wrap-json-body)
      (wrap-json-response)))