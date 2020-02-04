(ns twitter-api.database
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [buddy.hashers :as hashers]
            [twitter-api.auth :refer :all]
            [monger.operators :refer :all])
  (:import [com.mongodb MongoOptions ServerAddress]
             org.bson.types.ObjectId))

(def conn (atom (mg/connect)))
(def db (atom (mg/get-db @conn "twitter_api")))
(def status-ok 200)
(def status-created 201)
(def status-forbidden 401)
(def status-duplicated 409)
(def duplicated-message "Entity already exists")
(def forbidden-message "You are not authorized to accesse this resource")

(defn create-response [status body]
  {:status status
   :content-type "application/json; charset=UTF-8"
   :body body})

(defn unwrap-user-id [token]
  (def parsedToken (clojure.string/replace token #"Bearer " ""))
  (def userId (unwrap-token parsedToken))
  (:user userId))

;; Private database access methods
(defn- insert-like [tweetId userId]
  (mc/update @db "tweets" { :_id (ObjectId. tweetId) } {$push {:likes userId}} {:multi false})
  "Tweet liked")

(defn- insert-follow [userId followedUserId]
  (mc/update @db "users" { :_id (ObjectId. userId) } {$push {:follows followedUserId}} {:multi false})
  "User Followed")

(defn- insert-block [userId blockedUserId]
  (mc/update @db "users" { :_id (ObjectId. userId) } {$push {:blocks blockedUserId}} {:multi false})
  "User Blocked")

(defn- insert-user [userName userPassword]
  (def encriptedPassword (hashers/derive userPassword))
  (let [result (mc/insert-and-return @db "users" {:name userName :password encriptedPassword})]
    (create-response status-created (str (:_id result)))))

(defn- retrieve-tweets-by-user-id [userId]
  (def result (mc/find-maps @db "tweets" {:owner userId}))
  (map
   (fn [tweet] (assoc tweet :_id (str (:_id tweet))))
   result))

;; Only for preview purpose
(defn drop-all []
  (mc/remove @db "users")
  (mc/remove @db "tweets")
  (create-response status-ok "Database cleared"))

(defn insert-tweet [userId content]
  (let [result (mc/insert-and-return @db "tweets" {:owner userId :content content})]
    (create-response status-created (assoc result :_id (str (:_id result))))))

;; Tweets Operations
(defn create-tweet [token content]
    (def userId (unwrap-user-id token))
    (def result (mc/find-one-as-map @db "users"  { :_id (ObjectId. userId) }))
      (if (some? result)
          (insert-tweet userId content)
          (create-response status-forbidden forbidden-message)))

(defn retrieve-tweet [bearer objectId]
  (def result (mc/find-one-as-map @db "tweets" { :_id (ObjectId. objectId) }))
  (if (= (unwrap-user-id bearer) (:owner result))
    (create-response status-ok (assoc result :_id (str (:_id result))))
    (create-response status-forbidden forbidden-message)))

(defn like-tweet [bearerToken likedTweetId]
  (def userId (unwrap-user-id bearerToken))
  (def userResult (mc/find-one-as-map @db "users"  { :_id (ObjectId. userId) }))
  (if (some? userResult)
    (create-response status-ok (insert-like likedTweetId userId))
    (create-response status-forbidden forbidden-message)))

;; User Operations
(defn follow-user [bearerToken followedUserId]
  (def userId (unwrap-user-id bearerToken))
  (def userResult (mc/find-one-as-map @db "users"  { :_id (ObjectId. userId) }))
  (if (some? userResult)
    (create-response status-ok (insert-follow userId followedUserId))
    (create-response status-forbidden forbidden-message)))

(defn block-user [bearerToken blockedUserId]
  (def userId (unwrap-user-id bearerToken))
  (def userResult (mc/find-one-as-map @db "users"  { :_id (ObjectId. userId) }))
  (if (some? userResult)
    (create-response status-ok (insert-block userId blockedUserId))
    (create-response status-forbidden forbidden-message)))

(defn create-user [userName userPassword]
  (def user (mc/find-one-as-map @db "users" {:name userName}))
  (if (some? user)
    (create-response status-duplicated duplicated-message)
    (insert-user userName userPassword)))

(defn retrieve-tweets-for-user [token userId]
  (def userId (unwrap-user-id token))
  (def userResult (mc/find-one-as-map @db "users"  { :_id (ObjectId. userId) }))
  (if (= userId (str (:_id userResult)))
    (create-response status-ok (retrieve-tweets-by-user-id userId))
    (create-response status-forbidden forbidden-message)))

;; Login operations
(defn login [userName userPassword]
  (def encriptedPassword (hashers/derive userPassword))
  (def result (mc/find-one-as-map @db "users" {:name userName}))
  (if (hashers/check userPassword (:password result))
    (generate-signature (str (:_id result)))
    (create-response status-forbidden forbidden-message)))