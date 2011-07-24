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

(defn next-state [description old-state]
  (insert! :combat-status
           (dissoc (assoc old-state :order (inc (:order old-state))
                          :description description) :_id)))
 
(defn set-image-uri [combat-name image-name]
  (next-state (str "Nueva imagen " image-name) (assoc (get-combat-data combat-name) :mat image-name )))

(defn get-state-list [combat-name] (map #(:description %) (fetch :combat-status :only [:description] :where {:name combat-name} ))) 