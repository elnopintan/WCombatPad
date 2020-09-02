(ns WCombatPad.migration
  (:use [somnium.congomongo])
  (:require [WCombatPad.data :as d])
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
  (d/create-ticket-table)
  (d/create-users-table)
  (d/create-pads-table)
  (d/create-combat-status-table))
