(ns WCombatPad.core
  (:use hiccup.core)
  (:use hiccup.page-helpers)
  (:use hiccup.form-helpers)
  (:use [ring.util.response :only (redirect)]))

(defn show-login []
  (html5 (form-to [:post "/login"]
                   (label "password" "Password") [:br]
                   (password-field "password" "")
                   (submit-button "Enviar"))))
(defn filter-loged [{{loged :loged :as session} :session uri :uri } fn & params]
  (if loged (apply fn params)
      (assoc (redirect "/login") :session (assoc session :redirection uri))))
(defn show-combat [combat-name] combat-name)
(defn link-to-combat [combat-name] [:a {:href (str "/combat/" combat-name)} combat-name])
(defn show-list [] 
  (html5 (unordered-list [(link-to-combat "uno") "dos" "tres"])))
