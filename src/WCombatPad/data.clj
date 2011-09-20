(ns WCombatPad.data
  (:use somnium.congomongo))



(let [mongo-uri (System/getenv "MONGOLAB_URI")]
  (if (= (System/getenv "PADMODE") "local")
    (mongo! :db mongo-uri )
    (let [[ _ user password host str-port db] (re-matches  #"mongodb://([^:]*):([^@]*)@([^:]*):([^/]*)/([^ ]*)" mongo-uri)
          port (Integer. str-port)
          conn (make-connection db :host host :port port)]
       (do (set-connection! conn)
          (authenticate conn user password)))))
  


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
  (let [
        number (fetch-count :combat-status :where {:name combat-name}) ]
    (if (> number 0)
      (get-combat-data combat-name (dec number))
      {:name combat-name  :offset [0 0] :grid-size 10 :order -1}
      )))
  ([combat-name order]
  (fetch-one :combat-status :where {:name combat-name :order order })
  ))


(defn get-pad-list [] (fetch :pads))
  

(defn create-id [name] (.replaceAll name " " "_"))

(defn create-pad [name]
  (insert! :pads
   {:_id (create-id name) :name name}
   )
  )
(defn delete-pad [id] (destroy! :pads (fetch-one :pads :where {:_id id})))

(defn next-state [combat-name description type & changes]
  (let [last-state (get-combat-data combat-name)
        map-changes (apply assoc {} changes)
        chars (if (:character map-changes)
                (conj (:characters last-state) (:character map-changes))
                (:characters last-state))
        char-change (map-changes :character-change)
        changed-chars (if char-change
                      (map #(if (= (:name %) (:name char-change))
                              (merge % char-change)
                              %)
                           chars)
                      chars)      
        changes-with-chars (assoc (dissoc map-changes :character :character-change) :characters changed-chars)
        next-state (merge last-state changes-with-chars)]
  (if (= (:type last-state) type)
    (update!
     :combat-status  last-state (assoc next-state :description description))
    (insert!
     :combat-status
           (dissoc (assoc next-state :order (inc (:order next-state))
                          :description description :type type) :_id)))))
 
(defn set-image-uri [combat-name image-name]
  (next-state  combat-name (str "Nuevo mapa " image-name) "MapImage" :mat image-name ))



(defn change-grid [combat-name posx posy size]
  (next-state combat-name (str "Cambio de rejilla ["posx " " posy "] " size)
              "Grid" :offset [posx posy] :grid-size size))

(defn set-new-character [combat-name character-name avatar]
  (next-state combat-name (str "Nuevo Personaje "character-name) "NewCharacter" :character {:name character-name :avatar avatar :pos [0 0] :size 1 }
              ))

(defn move-character [combat-name character-name pos]
  (next-state combat-name
              (str character-name " movido")
              (str "Move"character-name)
              :character-change {:name character-name :pos pos}))
(defn resize-character [combat-name character-name size]
  (next-state combat-name
              (str "Tamaño de " character-name " modificado")
              (str "Resize"character-name)
              :character-change {:name character-name :size size}))
(defn kill-character [combat-name character-name dead]
  (next-state combat-name
              (str character-name (if (= dead "yes") " muere" " vive"))
              (str "Life" character-name)
              :character-change {:name character-name :dead dead}))

(defn get-state-list [combat-name]  (sort-by :order (fetch :combat-status :only [:order :description] :where {:name combat-name} )))

(defn undo-action [combat-name]
  (destroy! :combat-status (get-combat-data combat-name)))