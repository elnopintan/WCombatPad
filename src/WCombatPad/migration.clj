(ns WCombatPad.migration
  (:require [somnium.congomongo :as m]
            [WCombatPad.data :as d]
            [clojure.java.jdbc :as jdbc])
  )

(defn set_default_sizes []
  (comment do
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

(defn migrate-tickets [mongo-conn]
  (m/with-mongo mongo-conn
    (jdbc/insert-multi!
     d/pg-uri
     :tickets
     (map (fn [ticket]
            (-> ticket
            (assoc :user_name (:user ticket) :used (if (:used ticket) "true" "false"))
            (dissoc :user)(dissoc :_id)))
          (m/fetch :tickets)))))

(defn migrate-combat-status [mongo-conn]
  (m/with-mongo mongo-conn
    (jdbc/insert-multi!
     d/pg-uri
     :combat_status
     (map (fn [status]
            (->(dissoc status :_id )
               d/combat-status-to-db))
          (->>
           (m/fetch :combat-status)
           (map (fn [cs] [ (str (:name cs) "_" (:order cs)) cs]))
           (into {})
           (map val))))))

(defn migrate-users [mongo-conn]
  (m/with-mongo mongo-conn
    (jdbc/insert-multi!
     d/pg-uri
     :users
     (map (fn [user]
            (-> (dissoc user :_id)
                d/user-to-db))
          (m/fetch :users)))))

(defn migrate-pads [mongo-conn]
  (m/with-mongo mongo-conn
               (jdbc/insert-multi!
                d/pg-uri
                :pads
                (m/fetch :pads))))
