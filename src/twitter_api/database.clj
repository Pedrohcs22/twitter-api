(ns twitter-api.database
  (:require [monger.core :as mg]
            [monger.collection :as mc])
  (:import [com.mongodb MongoOptions ServerAddress]
             org.bson.types.ObjectId))

(def conn (atom (mg/connect)))
(def db (atom (mg/get-db @conn "twitter_api")))

(defn mapToObject [result]
  {:_id (str (get result :_id)),
   :owner (str (get result :owner)),
   :content (str (get result :content))})

(defn retrieveTweetsForUser [userId]
    (mc/find-maps @db "tweets" {:owner userId}))

(defn createTweet [userId content]
    (mc/insert-and-return @db "tweets" {:owner userId :content content}))

(defn deleteTweet [tweetId]
    (mc/remove-by-id @db "tweets" tweetId))

(defn retrieveTweet [objectId]
  (def result (mc/find-one-as-map @db "tweets" { :_id (ObjectId. objectId) }))
  (mapToObject result))