(ns WCombatPad.routes
  (:use compojure.core)
  (:use [hiccup.middleware :only (wrap-base-url)])
  (:require [compojure.handler :as handler])
  (:require [compojure.route :as route])
  (:use ring.middleware.session.store)
  (:use ring.util.response)
 (:use ring.middleware.session.cookie)
 (:use [WCombatPad.core :only (filter-loged
                               show-login
                               show-combat
                               show-list
                               get-map)]))
 (def store (cookie-store))
(defroutes pad-routes
  (route/resources "/")
  
  (GET "/combat" args (filter-loged args show-list ))
  (GET "/login" {{redir :redirection :as session} :session} (show-login))
  (GET "/loged" {session :session} (if (session :loged) "HOLA" "ADIOS"))
  (POST "/login" {{redir :redirection :as session} :session {password :password} :params}
        (assoc (redirect redir) :session (assoc session :loged true)))
  (GET "/combat/:combat-name" {{combat-name :combat-name} :params session :session :as args}
       (filter-loged args show-combat combat-name))
  (GET "/combat/:combat-name/map/:map-name" [combat-name map-name] (get-map map-name nil nil))
   )

(def pad-web (wrap-base-url (handler/site pad-routes)))