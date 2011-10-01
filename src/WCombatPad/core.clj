(ns WCombatPad.core
  (:use hiccup.core)
  (:use hiccup.page-helpers)
  (:use hiccup.form-helpers)
  (:use [WCombatPad.users :only (authenticate)]) 
  (:use [ring.util.response :only (redirect)]))

(defn get-map-headers []
   [:head
   (include-css "/files/css/mat.css")
   (include-js 
            "/files/js/jquery-1.6.1.min.js" 
            "/files/js/jquery-ui-1.8.12.custom.min.js"
            "/files/js/mat.js")])

(defmacro template [body]
  `(html5 (get-map-headers)
         (vec (concat
           [:body
            ]
           ~body)))) 

(defn show-login []
  (template [(form-to
              [:post "/login"]
              (label "user" "Usuario") [:br]
              (text-field "user" "") [:br]
              (label "password" "Password") [:br]
              (password-field "password" "")
              (submit-button "Enviar"))]))






(defn filter-loged [{{user :user :as session} :session uri :uri } fn & params]
  (if user (apply fn params)
      (assoc (redirect "/login") :session (assoc session :redirection uri))))

