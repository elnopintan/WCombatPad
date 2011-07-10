(ns WCombatPad.core
  (:use hiccup.core)
  (:use hiccup.page-helpers)
  (:use hiccup.form-helpers))

(defn show-login []
  (html5 (form-to [:post "/login"]
                   (label "password" "Password") [:br]
                   (password-field "password" "")
                   (submit-button "Enviar"))))
