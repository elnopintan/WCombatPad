(ns WCombatPad.data
  (:require [clojure.java.jdbc :as jdbc])
  (:use [ WCombatPad.cache :only (invalidate)]))



(comment let [mongo-uri (System/getenv "MONGOLAB_URI")]
  (if (= (System/getenv "PADMODE") "local")
    (mongo! :db mongo-uri )
    (let [[ _ user password host str-port db] (re-matches  #"mongodb://([^:]*):([^@]*)@([^:]*):([^/]*)/([^ ]*)" mongo-uri)
          port (Integer. str-port)
          conn (make-connection db :host host :port port)]
       (do (set-connection! conn)
          (println (str user " " password))
          (authenticate conn user password)))))



(def pg-uri
  (let [pg-uri-str (System/getenv "POSTGRES_URI")
        pg-uri-str (if (nil? pg-uri-str)  "jdbc:postgresql://localhost:5432/wcombatpad" pg-uri-str)]
    (Class/forName "org.postgresql.Driver")
  {:connection-uri pg-uri-str }))

(def ejemplo
  {:name "ejemplo"
   :mat "almacen_hundido_bajo_parcial"
   :grid-size 21
   :offset [8 18]
   :characters [{:name "cleric"
                 :avatar "https://secure.gravatar.com/avatar/9c3be996ad1960ae9eec9b82e0231880?s=140&d=https://gs1.wac.edgecastcdn.net/80460E/assets%2Fimages%2Fgravatars%2Fgravatar-140.png"
                 :pos [1 1]}
                {:name "warrior"
                 :avatar "https://secure.gravatar.com/avatar/9c3be996ad1960ae9eec9b82e0231880?s=140&d=https://gs1.wac.edgecastcdn.net/80460E/assets%2Fimages%2Fgravatars%2Fgravatar-140.png"
                 :pos [8 7]}]})

(defn get-combat-data
  ([combat-name]                                     ;  ejemplo
  (comment let [
        number (fetch-count :combat-status :where {:name combat-name}) ]
    (if (> number 0)
      (get-combat-data combat-name (dec number))
      {:name combat-name  :offset [0 0] :grid-size 10 :order -1}
      )))
  ([combat-name order]
  (comment fetch-one :combat-status :where {:name combat-name :order order })
  ))


(defn get-pad-list [] (comment reverse (fetch :pads)))
(defn exists-pad? [pad-name] (comment fetch-one :pads :where {:_id pad-name}))  

(defn create-id [name] (comment .r
                                eplaceAll name " " "_"))

(defn create-pad [name]
  (comment insert! :pads
   {:_id (create-id name) :name name}
   )
  )
(defn delete-pad [id] (comment destroy! :pads (fetch-one :pads :where {:_id id})))

(defprotocol MatState
  (get-next-state [this prev-state] "Generates next state for a mat")
  (get-type [this] )
  (get-desc [this] )
  )

(deftype ImageState [uri]
  MatState
  (get-next-state [this prev-state]
    (assoc prev-state :mat (.uri this)))
  (get-type [this] "MapImage")
  (get-desc [this] (str "Nuevo mapa " (.uri this))))

(deftype GridState [ offset size]
  MatState
  (get-next-state [this prev-state]
    (assoc prev-state :offset (.offset this) :grid-size (.size this)))
  (get-type [this] "Grid")
  (get-desc [this] (str "Cambio de rejilla " (.offset this) " " (.size this)))
  )

(deftype NewCharState [ charname avatar]
  MatState
  (get-next-state [this prev-state]
    (let [{chars :characters } prev-state
          ]
      (assoc prev-state :characters (conj chars {:avatar (.avatar this) :name (.charname this) :pos [ 0 0] :size 1 }))))  
  (get-type [this] "NewCharacter")
  (get-desc [this] (str "Nuevo Personaje " (.charname this))))

(defn change-character [ prev-state  charname field value ]
  (let [{chars :characters } prev-state 
        {[char] true  other-chars false} (group-by #(= charname (:name %)) chars) ]
    (assoc prev-state :characters (conj other-chars (assoc char field value)))))

(deftype MoveCharState [ charname pos]
  MatState
  (get-next-state [this prev-state]
      (change-character prev-state (.charname this) :pos pos))
  (get-type [this] (str "Move" (.charname this)))
  (get-desc [this] (str (.charname this) " Movido")))

(deftype ResizeCharState [ charname size]
  MatState
  (get-next-state [this prev-state]
    (change-character prev-state (.charname this) :size size))
  (get-type [this] (str "Resize" (.charname this)))
  (get-desc [this] (str "Tamaño de " (.charname this) " Modificado")))

(deftype KillCharState [ charname dead]
  MatState
  (get-next-state [this prev-state]
    (change-character prev-state (.charname this) :dead dead))
  (get-type [this] (str "Life" (.charname this)))
  (get-desc [this] (str (.charname this) " está " (if (= "yes" dead) "muerto" "vivo"))))

  
(defn next-state [user combat-name ^WCombatPad.data.MatState state]
  (comment let [last-state (get-combat-data combat-name)
        new-state (assoc (get-next-state state last-state) :user user)
        type (get-type state)
        description (get-desc state)]
    (insert!
     :combat-status
     (dissoc (assoc new-state :order (inc (:order new-state))
                    :description description :type type) :_id))))
 
(defn set-image-uri [ user combat-name image-name]
  (invalidate image-name)
  (next-state user combat-name (ImageState. image-name)))


(defn change-grid [ user combat-name posx posy size]
  (invalidate (:mat (get-combat-data combat-name)))
  (next-state user combat-name (GridState. [posx posy] size)))
             

(defn set-new-character [ user combat-name character-name avatar]
  (next-state user combat-name (NewCharState. character-name avatar )))


(defn move-character [user combat-name character-name pos]
  (next-state user combat-name (MoveCharState. character-name pos)))

(defn resize-character [user combat-name character-name size]
  (next-state user combat-name (ResizeCharState. character-name size)))
             
(defn kill-character [user combat-name character-name dead]
  (next-state user combat-name (KillCharState. character-name dead)))

(defn get-state-list [combat-name]  (comment sort-by :order (fetch :combat-status :only [:order :description] :where {:name combat-name} )))

(defn undo-action [combat-name]
  (comment destroy! :combat-status (get-combat-data combat-name)))

(defn new-user [user]
  (jdbc/insert! pg-uri :users user))

(defn find-user [username]
  (first (jdbc/query pg-uri ["select * from users where user = ?" username]))

(defn update-user [old new]
  (comment update! :users old new))

(defn save-ticket [ticket]
  (jdbc/insert! pg-uri :tickets ticket))

(defn get-tickets []
  (jdbc/query pg-uri ["select * from tickets"]))

(defn valid-ticket? [uuid]
  (not (nil? (first (jdbc/query pg-uri ["select * from tickets where uuid = ? and used is null" uuid])))))

(defn use-ticket [uuid user]
  (let [ticket (first (jdbc/query pg-uri ["select * from tickets where uuid = ? and name = ?" uuid user]))]
           (print ticket)
           (jdbc/update! pg-uri :tickets {:used "true"} ["uuid = ? and name = ?" uuid user])))

(defn create-ticket-table []
  (let [ddl (jdbc/create-table-ddl :tickets [[:name "varchar(100)" :primary :key]
                                             [:uuid "varchar(100)"]
                                   [:url "varchar(200)"]
                                   [:used "varchar(10)"]])]
     (jdbc/db-do-commands pg-uri [ddl])))

(defn create-users-table []
  (let [ddl (jdbc/create-table-ddl :users [[:user "varchar(100)" :primary :key]
                                          [:password "varchar(100)"]])]
        (jdbc/db-do-commands pg-uri [ddl])))
