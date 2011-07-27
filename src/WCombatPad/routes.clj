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
 (:use [WCombatPad.mat :only (show-combat save-image save-grid save-character)])
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
  (POST "/combat"
        {{combat-name :matname} :params :as args}
        (filter-loged args new-combat combat-name))
  (GET "/combat/:combat-name"
       {{combat-name :combat-name} :params session :session :as args}
       (filter-loged args show-combat combat-name))
  (DELETE "/combat/:combat-name"
          {{combat-name :combat-name} :params :as args}
          (filter-loged args delete-combat combat-name))
  (GET "/combat/:combat-name/map"
       {{combat-name :combat-name} :params :as args}
       (get-map combat-name))
  (POST "/combat/:combat-name/map"
        {{combat-name :combat-name image :image } :params :as args}
        (filter-loged args save-image combat-name image))
  (POST "/combat/:combat-name/character"
        {{combat-name :combat-name char-name :charname image :avatar } :params :as args}
        (filter-loged args save-character combat-name char-name image))
  (POST "/combat/:combat-name/grid"
        {{combat-name :combat-name str-posx :posx str-posy :posy str-grid-size :gridsize} :params :as args}
        (let [ posx (Integer. str-posx)
               posy (Integer. str-posy)
               grid-size (Integer. str-grid-size)
               ]
        (filter-loged args save-grid combat-name posx posy grid-size)))

   )

(def pad-web (wrap-base-url (handler/site pad-routes)))