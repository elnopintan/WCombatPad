(ns WCombatPad.migration
  (:use [somnium.congomongo])
  (:use [WCombatPad.data :only (get-combat-data
                                )])
  )

(defn set_default_sizes []
  (do
    (map
     (fn [data]
       (update! :combat-status data
                (assoc data :characters
                       (map #(assoc % :size 1) (data :characters)))))
     (fetch :combat-status))))

(defn create-db []
  )
