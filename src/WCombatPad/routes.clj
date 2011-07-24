(ns WCombatPad.routes
  (:use compojure.core)
  (:use [hiccup.middleware :only (wrap-base-url)])
  (:require [compojure.handler :as handler])
  (:require [compojure.route :as route])
  (:use ring.middleware.session.store)
  (:use ring.util.response)
 (:use ring.middleware.session.cookie)
 (:use [WCombatPad.core :only (filter-loged
                               show-login)])
 (:use [WCombatPad.mat :only (show-combat save-image)])
 (:use [WCombatPad.list :only (show-list new-combat delete-combat)])
 (:use [WCombatPad.images :only (get-map)]))



(def store (cookie-store))
(defroutes pad-routes
  (route/resources "/files")
  (GET "/" args (filter-loged args show-list ))
  (GET "/login" {{redir :redirection :as session} :session} (show-login))
  (GET "/loged" {session :session} (if (session :loged) "HOLA" "ADIOS"))
  (POST "/login" {{redir :redirection :as session} :session {password :password} :params}
        (assoc (redirect redir) :session (assoc session :loged true)))
  (POST"/combat" {{combat-name :matname} :params} (new-combat combat-name))
  (GET "/combat/:combat-name" {{combat-name :combat-name} :params session :session :as args}
       (filter-loged args show-combat combat-name))
  (DELETE "/combat/:combat-name" [combat-name] (delete-combat combat-name))
  (GET "/combat/:combat-name/map" [combat-name ] (get-map combat-name))
  (POST "/combat/:combat-name/map" {{combat-name :combat-name image :image } :params} (save-image combat-name image))
   )

(def pad-web (wrap-base-url (handler/site pad-routes)))