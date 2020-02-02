(ns twitter-api.database
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [buddy.hashers :as hashers])
  (:import [com.mongodb MongoOptions ServerAddress]
             org.bson.types.ObjectId))

(def conn (atom (mg/connect)))
(def db (atom (mg/get-db @conn "twitter_api")))

(defn mapToObject [result]
  (assoc result :_id (str (:_id result))))

(defn create-response [status body]
  {:status status
   :headers {}
   :content-type "application/json; charset=UTF-8"
   :body body})

(defn retrieve-tweets-for-user [userId]
  (def result (mc/find-maps @db "tweets" {:owner userId}))
  (apply hash-map result)
  (map
   (fn [tweet] (mapToObject tweet))
   result))

(defn create-tweet [userId content]
    (mc/insert-and-return @db "tweets" {:owner userId :content content}))

(defn delete-tweet [tweetId]
    (mc/remove-by-id @db "tweets" tweetId))

(defn retrieve-tweet [objectId]
  (def result (mc/find-one-as-map @db "tweets" { :_id (ObjectId. objectId) }))
  (mapToObject result))

(defn like-tweet [objectId])

;; user operations
(defn create-user [userName userPassword]
  (def encriptedPassword (hashers/derive userPassword))
  (mc/insert @db "users" {:name userName :password encriptedPassword})
  true)

;; login user
(defn login [userName userPassword]
  ) ;; (hashers/check "secretpassword" "bcrypt+sha512$4i9sd34m...")