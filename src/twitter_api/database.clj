(ns twitter-api.database
  (:require [monger.core :as mg]
            [monger.collection :as mc])
  (:import [com.mongodb MongoOptions ServerAddress]
             org.bson.types.ObjectId))

(def conn (atom (mg/connect)))
(def db (atom (mg/get-db @conn "twitter_api")))

(defn mapToObject [result]
  (assoc result :_id (str (:_id result))))

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