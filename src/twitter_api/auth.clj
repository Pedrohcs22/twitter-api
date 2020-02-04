(ns twitter-api.auth
  (:require [buddy.sign.jwt :as jwt]))

(defonce secret "29F7D967B163B7087BA7C4CF0C5B9F9BE5156E1C33C140E2F17551093E8085C8")

(defn generate-signature [username]
  (jwt/sign {:user username} secret))

(defn unwrap-token [token]
  (jwt/unsign token secret))