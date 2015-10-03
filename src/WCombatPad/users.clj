(ns WCombatPad.users
  (:import  [org.jasypt.util.password StrongPasswordEncryptor])
  (:use [WCombatPad.data :only (new-user find-user update-user valid-ticket? use-ticket) ])
  (:use [WCombatPad.template :only (template)])
  (:use hiccup.core)
  (:use hiccup.form)
  (:use ring.util.response)
  )


(defn validate-password [ { hashed-pass :password :as user} password]
  (.checkPassword (StrongPasswordEncryptor.) password hashed-pass))

(defn create-user [user password repeated]
  (let [encryptor (StrongPasswordEncryptor.)
        created-user {:user user
                     :password (.encryptPassword
                                encryptor password)}]
    (if (validate-password created-user repeated)
      (new-user created-user))))
  

(defn authenticate [username password]
  (let [user (find-user username)]
    (if (validate-password user password)
      user)))

(defn change-password [username old-pass new-pass repeat-pass]
  (let [user (find-user username)
        encryptor (StrongPasswordEncryptor.)
        new-user (assoc user :password (.encryptPassword encryptor new-pass))]
        (if (and (validate-password user old-pass)
                 (validate-password new-user repeat-pass))
          (do (update-user user new-user)
              new-user))))
      
(defn set-admin [username]
  (let [user (find-user username)]
    (update-user user (assoc user :admin true))))


(defn modify-user [ {user :user } error]
  (template
   [[:div.user-change
     (if error
       [:div.error error])
     (form-to [:post "/user/profile"]
              (label "old" "Antiguo password") [:br]
              (password-field "old" "") [:br]
              (label "new" "Nuevo password") [:br]
              (password-field "new" "") [:br]
              (label "repeat" "Repite el password") [:br]
              (password-field "repeat" "") [:br]
              (submit-button "Modificar"))]]))
  
(defn show-create-user [ error ticket]
  (if (valid-ticket? ticket)
    (template
   [[:div.user-creation
     [:h3 "Registra a tu usuario"]
    (if error
      [:div.error error])
    (form-to [:post "/user/new"]
             (label "user" "Usuario") [:br]
             (text-field "user" "") [:br]
             (label "pass" "Password")[:br]
             (password-field "pass" "")[:br]
             (label "repeat" "Repite el Password")[:br]
             (password-field "repeat" "")
             (hidden-field "ticket" ticket)[:br]
             (submit-button "Registrar"))]])
  ticket))

(defn redirect-correct
  ( [redirection]
      (redirect-correct redirection {}))
  ([redirection session]
     (assoc (redirect redirection) :session (dissoc session :error))))
(defn redirect-with-error
  ([redirection error]
     (redirect-with-error redirection error {}))
  ([redirection error session]
     (assoc (redirect redirection) :session (assoc session :error error))))

(defn do-modify-user [ {user :user} old password repeat session]
  (cond
   (not (change-password user old password repeat)) (redirect-with-error
                                                      (str "/user/" user)
                                                      "Password erroneo o no coinciden los passwords"
                                                      session)
   :else (redirect-correct "/" session)))

  
(defn do-create-user [ticket user password repeat]
  (cond
   (not (valid-ticket? ticket)) (redirect "/")
   (find-user user) (redirect-with-error 
                       (str "/user/new/" ticket)
                       "Existe un usuario con ese nombre")
   (not (create-user user password repeat ))(redirect-with-error
                                (str "/user/new/" ticket)
                                "Los passwords no coinciden")
   :else (do
           (use-ticket ticket user)
           (redirect-correct "/"))))
   
                               
       
