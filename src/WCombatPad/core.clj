(ns WCombatPad.core
  (:use hiccup.core)
  (:use hiccup.page-helpers)
  (:use hiccup.form-helpers)
  (:use [ring.util.response :only (redirect)]))

(defn show-login []
  [(form-to [:post "/login"]
                   (label "password" "Password") [:br]
                   (password-field "password" "")
                   (submit-button "Enviar"))])

(defn- get-map-headers []
   [:head
   (include-css "/files/css/mat.css")
   (include-js 
            "/files/js/jquery-1.6.1.min.js" 
            "/files/js/jquery-ui-1.8.12.custom.min.js"
            "/files/js/mat.js")])

(defn template [a-fn & params]
  (html5 (get-map-headers)
         (vec (concat
           [:body
            ]
           (apply a-fn params))))) 


(defn filter-loged [{{loged :loged :as session} :session uri :uri } fn & params]
  (if loged (apply template fn params)
      (assoc (redirect "/login") :session (assoc session :redirection uri))))

