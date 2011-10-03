(ns WCombatPad.routes
  (:use compojure.core)
  (:use [hiccup.middleware :only (wrap-base-url)])
  (:require [compojure.handler :as handler])
  (:require [compojure.route :as route])
  (:use ring.middleware.session.store)
  (:use ring.util.response)
  (:use ring.adapter.jetty)
 (:use ring.middleware.session.cookie)
 (:use [WCombatPad.core :only (filter-loged
                               show-login )])
 (:use [WCombatPad.template :only (template)])
 
 (:use [WCombatPad.mat :only (show-combat save-image save-grid save-character
                                          save-move save-resize save-kill)])
 (:use [WCombatPad.list :only (show-list new-combat delete-combat)])
 (:use [WCombatPad.users :only (authenticate do-create-user show-create-user
                                             modify-user do-modify-user )])
 (:use [WCombatPad.tickets :only (show-tickets)])
 (:use [WCombatPad.images :only (get-map get-image-state load-image-file)]))

(defn desanitize [a-str] (.replaceAll a-str "\\%3" "?"))

(def store (cookie-store))
(defroutes pad-routes
  (route/resources "/files")
  (GET "/" args (filter-loged args show-list ))
  (GET "/exit" _ (assoc (redirect "/") :session {}))
  (GET "/login" {{redir :redirection :as session} :session} (show-login ))
  (GET "/loged" {session :session} (if (session :loged) "HOLA" "ADIOS"))
  (POST "/login" {{redir :redirection :as session} :session { user :user password :password} :params}
        (let [loged-user (authenticate user password)] (if loged-user (assoc (redirect redir) :session (assoc session :user loged-user)) (redirect "/login"))))
  (GET "/tickets" args (filter-loged args show-tickets))
  (GET "/user/new/:ticket" {{ticket :ticket} :params
                            {error :error} :session }
       (show-create-user error ticket))
  (POST "/user/new" {{ticket :ticket
                     user :user
                     password :pass
                      repeat :repeat } :params}
        
        (do-create-user ticket user password repeat))
  (GET "/user/profile" { {error :error} :session  :as args}
       (filter-loged args modify-user  error))
  (POST "/user/profile" {{ old :old new :new repeat :repeat} :params
                       session :session
                       :as args}
        (filter-loged args do-modify-user old new repeat session))
  (POST "/combat"
        {{combat-name :matname} :params :as args}
        (filter-loged args new-combat combat-name))
  (GET "/combat/:combat-name"
       {{combat-name :combat-name} :params session :session :as args}
       (filter-loged args show-combat combat-name))
  (GET "/combat/:combat-name/state/:order"
       {{combat-name :combat-name str-order :order} :params :as args}
       (let [order (Integer. str-order)]
         (filter-loged args show-combat combat-name order)))
  (DELETE "/combat/:combat-name"
          {{combat-name :combat-name} :params :as args}
          (filter-loged args delete-combat combat-name))
  (GET "/combat/:combat-name/map/:order"
       {{combat-name :combat-name str-order :order} :params :as args}
       (let [order (Integer. str-order)]
       (get-map combat-name order)))
  (GET "/combat/:combat-name/state/:order.png"
       {{combat-name :combat-name str-order :order} :params :as args}
       (let [order (Integer. str-order)]
         (get-image-state combat-name order)))
  (GET "/remote/images/:dir/:file-name.:extension" [dir file-name extension]
       (load-image-file dir (str (desanitize file-name) "." extension)))
  (POST "/combat/:combat-name/map"
        {{combat-name :combat-name image :image } :params :as args}
        (filter-loged args save-image combat-name image))
  (POST "/combat/:combat-name/character"
        {{combat-name :combat-name char-name :charname image :avatar copy :copy } :params :as args}
        (filter-loged args save-character combat-name char-name image copy))
  
  (POST "/combat/:combat-name/move"
        {{combat-name :combat-name char-name :name str-posx :posx str-posy :posy } :params :as args}
        (let [ posx (Integer. str-posx)
               posy (Integer. str-posy)]
        (filter-loged args save-move combat-name char-name posx posy)))
  (POST "/combat/:combat-name/resize"
        {{combat-name :combat-name char-name :name str-size :size } :params :as args}
        (let [ size (Integer. str-size)]
        (filter-loged args save-resize combat-name char-name size)))
  (POST "/combat/:combat-name/kill"
        {{combat-name :combat-name char-name :name dead :dead } :params :as args}
        (filter-loged args save-kill combat-name char-name dead))

  (POST "/combat/:combat-name/grid"
        {{combat-name :combat-name str-posx :posx str-posy :posy str-grid-size :gridsize} :params :as args}
        (let [ posx (Integer. str-posx)
               posy (Integer. str-posy)
               grid-size (Integer. str-grid-size)
               ]
        (filter-loged args save-grid combat-name posx posy grid-size)))

   )


(def pad-web (wrap-base-url (handler/site pad-routes)))

(defn -main []
  (let [port (Integer/parseInt (System/getenv "PORT"))]
    (run-jetty pad-web {:port port})))