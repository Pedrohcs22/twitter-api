(ns twitter-api.database
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [buddy.hashers :as hashers]
            [twitter-api.auth :refer :all])
  (:import [com.mongodb MongoOptions ServerAddress]
             org.bson.types.ObjectId))

(def conn (atom (mg/connect)))
(def db (atom (mg/get-db @conn "twitter_api")))
(def status-ok 200)
(def status-created 201)
(def status-forbidden 401)
(def forbidden-message "You are not authorized to accesse this resource")

(defn create-response [status body]
  {:status status
   :content-type "application/json; charset=UTF-8"
   :body body})

(defn retrieve-tweets-by-user-id [userId]
  (def result (mc/find-maps @db "tweets" {:owner userId}))
  (map
   (fn [tweet] (assoc tweet :_id (str (:_id tweet))))
   result))

(defn unwrap-user-id [token]
  (def parsedToken (clojure.string/replace token #"Bearer " ""))
  (def userId (unwrap-token parsedToken))
  (:user userId))

(defn retrieve-tweets-for-user [token userId]
  (def userId (unwrap-user-id token))
  (def userResult (mc/find-one-as-map @db "users"  { :_id (ObjectId. userId) }))
  (if (= userId (str (:_id userResult)))
    (create-response status-ok (retrieve-tweets-by-user-id userId))
    (create-response status-forbidden forbidden-message)))

(defn drop-all []
  (mc/remove @db "users")
  (mc/remove @db "tweets")
  (create-response status-created "Created"))

(defn insert-tweet [userId content]
  (let [result (mc/insert-and-return @db "tweets" {:owner userId :content content})]
    (create-response status-created (assoc result :_id (str (:_id result))))))

(defn create-tweet [token content]
    (def userId (unwrap-user-id token))
    (def result (mc/find-one-as-map @db "users"  { :_id (ObjectId. userId) }))
      (if (= userId (str (:_id result)))
          (insert-tweet userId content)
          (create-response status-forbidden forbidden-message)))

(defn delete-tweet [tweetId]
    (mc/remove-by-id @db "tweets" tweetId))

(defn retrieve-tweet [bearer objectId]
  (def result (mc/find-one-as-map @db "tweets" { :_id (ObjectId. objectId) }))
  (if (= (unwrap-user-id bearer) (:owner result))
    (create-response status-ok (assoc result :_id (str (:_id result))))
    (create-response status-forbidden forbidden-message)))

(defn like-tweet [bearerToken objectId]
  ;;(mc/update-by-id db "tweets"  (ObjectId. objectId) {$push {:likes "überachievement"}} {:multi false})
  )

;; user operations
(defn create-user [userName userPassword]
  (def encriptedPassword (hashers/derive userPassword))
  (let [result (mc/insert-and-return @db "users" {:name userName :password encriptedPassword})]
    (create-response status-created (str (:_id result)))))

;; login user
(defn login [userName userPassword]
  (def encriptedPassword (hashers/derive userPassword))
  (def result (mc/find-one-as-map @db "users" {:name userName}))
  (if (hashers/check userPassword (:password result))
    (generate-signature (str (:_id result)))
    (create-response status-forbidden forbidden-message)))