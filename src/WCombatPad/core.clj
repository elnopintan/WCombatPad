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
(defn get-combat-data [combat-name] "")
(defn show-mat [combat-data] [:section#mat ""])
(defn show-characters [combat-data] [:nav#characters ""])
(defn show-actions [combat-data] [:nav#actions ""])

(defn show-combat [combat-name] (let [combat-data (get-combat-data combat-name)](html5 (map #(% combat-data) [show-mat show-characters show-actions]))))
(defn link-to-combat [combat-name] [:a {:href (str "/combat/" combat-name)} combat-name])

(defn show-list [] 
  (html5 (unordered-list (map link-to-combat ["uno" "dos" "tres"]))))
