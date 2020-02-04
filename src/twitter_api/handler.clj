(ns twitter-api.handler
  (:require [compojure.core :refer :all]
     [compojure.handler :as handler]
     [compojure.route :as route]
     [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
     [ring.util.response :refer [response]]
     [twitter-api.database :refer :all]))

;; database-routes created only for development purposes - not suitable for production
(defroutes database-routes
  (POST "/rest/v1/database/clear" req
        (drop-all)))

(defroutes user-routes
  (POST "/rest/v1/user" req
        (let [username (get-in req [:body "username"])
              password (get-in req [:body "password"])]
          (create-user username password)))

  (GET "/rest/v1/user/:id/tweet" req
       (let [bearer (get-in req [:headers "authorization"])
             id (get-in req [:route-params :id])]
         (retrieve-tweets-for-user bearer id)))

  (PUT "/rest/v1/user/:followedUserId/follow" req
       (let [bearer (get-in req [:headers "authorization"])
             followedUserId (get-in req [:route-params :followedUserId])]
         (follow-user bearer followedUserId)))

  (PUT "/rest/v1/user/:bloquedUserId/block" req
       (let [bearerToken (get-in req [:headers "authorization"])
             bloquedUserId (get-in req [:route-params :bloquedUserId])]
         (block-user bearerToken bloquedUserId))))

(defroutes login-routes
  (POST "/rest/v1/login" req
        (let [username (get-in req [:body "username"])
              password (get-in req [:body "password"])]
          (login username password))))

(defroutes tweet-routes
  (GET "/rest/v1/tweet/:id" req
       (let [bearer (get-in req [:headers "authorization"])
             id (get-in req [:route-params :id])]
         (retrieve-tweet bearer id)))

  (POST "/rest/v1/tweet" req
        (let [content (get-in req [:body "content"])
              bearer (get-in req [:headers "authorization"])]
          (create-tweet bearer content)))

  (PUT "/rest/v1/tweet/:id/like" req
       (let [bearer (get-in req [:headers "authorization"])
             id (get-in req [:route-params :id])]
         (like-tweet bearer id))))

(defroutes app-routes
  user-routes
  tweet-routes
  login-routes
  database-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (wrap-json-body)
      (wrap-json-response)))