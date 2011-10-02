(ns WCombatPad.users
  (:import  [org.jasypt.util.password StrongPasswordEncryptor])
  (:use [WCombatPad.data :only (new-user find-user update-user) ])
  (:use [WCombatPad.core :only (template)])
  (:use hiccup.core)
  (:use hiccup.form-helpers)
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

(defn show-create-user [ error ticket]
  (template
   [:div.user-creation
    (if error
      [:div.error error])
    (form-to [:post "/user/new"]
             (label "user" "Usuario") [:br]
             (text-field "user" "") [:br]
             (label "password" "Password")[:br]
             (password-field "password" "")[:br]
             (label "repeat" "Repite el Password")
             (password-field "password" "repeat")
             (hidden-field "ticket" ticket))]))

   (defn redirect-with-error [redirection error]
     (assoc (redirect redirection) :session {:error error}))
   
   (defn do-create-user [ticket user password repeat]
     (cond
      (find-user) (assoc
                      (redirect-with-error 
                       (str "/user/new/" ticket)
                       "Existe un usario con ese nombre"))
      (not (create-user)) (assoc
                              (redirect-with-error
                                (str "/user/new/" ticket)
                                "Las passwords no coinciden"))
      :else (redirect "/login")))
   
                               
       