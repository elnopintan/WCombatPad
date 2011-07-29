(ns WCombatPad.data
  (:use somnium.congomongo))

(mongo! :db "wcombatpad")


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
  [combat-name]                                     ;  ejemplo
  (let [
        number (fetch-count :combat-status :where {:name combat-name}) 
        last-combat (fetch-one :combat-status :where {:name combat-name :order (dec number)} )]
    (if last-combat
      last-combat
      {:name combat-name  :offset [0 0] :grid-size 10 :order -1}
      )))

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
        movement (map-changes :move)
        moved-chars (if movement
                      (map #(if (= (:name %) (:name movement))
                              (assoc % :pos (:pos movement))
                              %)
                           chars)
                      chars)      
        changes-with-chars (assoc (dissoc map-changes :character :move) :characters moved-chars)
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
  (next-state combat-name (str "Nuevo Personaje "character-name) "NewCharacter" :character {:name character-name :avatar (str "/files/images/chars/" avatar) :pos [0 0] }
              ))

(defn move-character [combat-name character-name pos]
  (next-state combat-name
              (str character-name " movido")
              (str "Move"character-name)
              :move {:name character-name :pos pos}))

(defn get-state-list [combat-name] (map #(:description %) (fetch :combat-status :only [:description] :where {:name combat-name} ))) 