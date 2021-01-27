(ns WCombatPad.data
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.data.json :as json])
  (:use [ WCombatPad.cache :only (invalidate)]))

(def pg-uri
  (let [pg-uri-str (System/getenv "JDBC_DATABASE_URL")
        pg-uri-str (if (nil? pg-uri-str)  "jdbc:postgresql://localhost:5432/wcombatpad" pg-uri-str)]
    (Class/forName "org.postgresql.Driver")
  {:connection-uri pg-uri-str }))

(def ejemplo
  {:name "ejemplo"
   :mat "almacen_hundido_bajo_parcial"
   :grid-size 21
   :order 1
   :offset [8 18]
   :characters [{:name "cleric"
                 :avatar "https://secure.gravatar.com/avatar/9c3be996ad1960ae9eec9b82e0231880?s=140&d=https://gs1.wac.edgecastcdn.net/80460E/assets%2Fimages%2Fgravatars%2Fgravatar-140.png"
                 :pos [1 1]}
                {:name "warrior"
                 :avatar "https://secure.gravatar.com/avatar/9c3be996ad1960ae9eec9b82e0231880?s=140&d=https://gs1.wac.edgecastcdn.net/80460E/assets%2Fimages%2Fgravatars%2Fgravatar-140.png"
                 :pos [8 7]}]})

(defn combat-status-to-db [status] {
                                    :_id (str (:name status) "_" (:order status))
                                    :name (:name status)
                                    :ord_nu (:order status)
                                    :values (json/write-str status)})

(defn combat-status-from-db [row] (json/read-str (:values row) :key-fn keyword))

(defn get-combat-data
  ([combat-name]                                     ;  ejemplo
  (let [number (-> (jdbc/query pg-uri ["select count(*) as c from combat_status where name = ?" combat-name])
                   first
                   :c)]
    (if (> number 0)
      (get-combat-data combat-name (dec number))
      {:name combat-name  :offset [0 0] :grid-size 10 :order -1}
      )))
  ([combat-name order]
   (combat-status-from-db
    (->
     (jdbc/query pg-uri ["select * from combat_status where name = ? and ord_nu = ?" combat-name order])
     first))))

(defn get-pad-list [] (jdbc/query pg-uri ["select * from pads order by ord_nu desc"]))
(defn exists-pad? [pad-name] (> (:c (first (jdbc/query pg-uri ["select count(*) as c from pads where _id = ?" pad-name])))0))  

(defn create-id [name] (.replaceAll name " " "_"))

(defn create-pad [name]
  (jdbc/insert! pg-uri :pads
   {:_id (create-id name) :name name}
   )
  )
(defn delete-pad [id]
  (jdbc/delete! pg-uri :pads ["_id = ?" id]))

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
  (let [last-state (get-combat-data combat-name)
        new-state (assoc (get-next-state state last-state) :user user)
        type (get-type state)
        description (get-desc state)]
    (map combat-status-from-db (jdbc/insert! pg-uri :combat_status
                  (combat-status-to-db (assoc new-state :order (inc (:order new-state))
                    :description description :type type))))))
 
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

(defn get-state-list [combat-name]
  (map combat-status-from-db (jdbc/query pg-uri ["select * from combat_status where name = ? order by ord_nu" combat-name])))


(defn undo-action [combat-name]
  (jdbc/delete! pg-uri :combat_status ["name = ? and ord_nu = (select max(ord_nu) from combat_status  where name = ?)" combat-name combat-name]))

(defn user-to-db [user]
  (-> (assoc user :user_name (user :user) :admin (and (contains? user :admin) (:admin user)))
      (dissoc :user)))

(defn user-from-db [user]
  (if (nil? user)
    nil
    (-> (assoc user :user (user :user_name))
        (dissoc :user_name))))

(defn new-user [user]
  (print user)
  (jdbc/insert! pg-uri :users (user-to-db user)))

(defn find-user [username]
  (->
   (jdbc/query pg-uri ["select * from users where user_name = ?" username])
   first
   user-from-db))

(defn update-user [old new_user]
  (let [new-user-db (user-to-db new_user)]
    (jdbc/update! pg-uri :users new-user-db ["user_name = ?" (old :user)])))

(defn save-ticket [ticket]
  (jdbc/insert! pg-uri :tickets ticket))

(defn get-tickets []
  (map #(assoc % :user (:user_name %)) (jdbc/query pg-uri ["select * from tickets"])))

(defn valid-ticket? [uuid]
  (not (nil? (first (jdbc/query pg-uri ["select * from tickets where uuid = ? and used is null" uuid])))))

(defn use-ticket [uuid user]
  (let [ticket (first (jdbc/query pg-uri ["select * from tickets where uuid = ? and name = ?" uuid user]))]
           (print ticket)
           (jdbc/update! pg-uri :tickets {:used "true" :user_name user} ["uuid = ? and name = ?" uuid user])))

(defn create-ticket-table []
  (let [ddl (jdbc/create-table-ddl :tickets [[:name "varchar(100)" ]
                                             [:uuid "varchar(100)" :primary :key]
                                   [:url "varchar(200)"]
                                             [:used "varchar(10)"]
                                             [:user_name "varchar(100)"]])]
     (jdbc/db-do-commands pg-uri [ddl])))

(defn create-users-table []
  (let [ddl (jdbc/create-table-ddl :users [[:user_name "varchar(100)" :primary :key]
                                           [:password "varchar(100)"]
                                           [:admin "boolean"]])]
        (jdbc/db-do-commands pg-uri [ddl])))

(defn create-pads-table []
  (let [ddl (jdbc/create-table-ddl :pads [[:_id "varchar(200)" :primary :key]
                                          [:ord_nu "serial"]
                                          [:name "varchar(200)"]])]
    (jdbc/db-do-commands pg-uri [ddl])))

(defn create-combat-status-table []
  (let [ddl (jdbc/create-table-ddl :combat_status [[:_id "varchar(200)" :primary :key]
                                                   [:name "varchar(200)"]
                                          [:ord_nu "bigint"]
                                          [:values "text"]])]
    (jdbc/db-do-commands pg-uri [ddl])))
