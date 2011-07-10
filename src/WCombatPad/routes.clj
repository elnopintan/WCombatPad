(ns WCombatPad.routes
  (:use compojure.core)
  (:use [hiccup.middleware :only (wrap-base-url)])
  (:require [compojure.handler :as handler])
  (:require [compojure.route :as route])
  (:use ring.middleware.session.store)
  (:use ring.util.response)
 (:use ring.middleware.session.cookie)
  (:use [WCombatPad.core :only (show-login)]))
 (def store (cookie-store))
(defroutes pad-routes
  (GET "/login" [] (show-login))
  (GET "/loged" {session :session} (if (session :loged) "HOLA" "ADIOS"))
  (POST "/login" {session :session {password :password} :params}
         (assoc (redirect "/loged") :session (assoc session :loged true))))

(def pad-web (wrap-base-url (handler/site pad-routes)))