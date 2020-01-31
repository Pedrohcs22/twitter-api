(ns twitter-api.database
  (:require [monger.core :as mg]
            [monger.collection :as mc])
  (:import [com.mongodb MongoOptions ServerAddress]
             org.bson.types.ObjectId))

(let [conn (mg/connect)
      db   (mg/get-db conn "twitter_api")])

(defn retrieveAllTweets [] ["Hello mundo", "hello city"])

(defn createTweet [title description] ())

(defn deleteTweet [id] ())

(defn retrieveTweet [id] ())