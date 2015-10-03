(ns WCombatPad.core
  (:use hiccup.core)
  (:use hiccup.page)
  (:use hiccup.form)
  (:use [WCombatPad.users :only (authenticate)])
  (:use [WCombatPad.template :only (template)])
  (:use [ring.util.response :only (redirect)]))




(defn show-login []
  (template [(form-to
              [:post "/login"]
              (label "user" "Usuario") [:br]
              (text-field "user" "") [:br]
              (label "password" "Password") [:br]
              (password-field "password" "")
              (submit-button "Enviar"))]))






(defn filter-loged [{{user :user :as session} :session uri :uri } fn & params]
  (if user (apply fn user params)
      (assoc (redirect "/login") :session (assoc session :redirection uri))))

