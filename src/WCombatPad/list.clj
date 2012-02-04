(ns WCombatPad.list
  (:use hiccup.core)
  (:use hiccup.page-helpers)
  (:use hiccup.form-helpers)
  (:use [ WCombatPad.template ])
  (:use [ring.util.response :only (redirect)])
  (:use [ WCombatPad.data :only (get-pad-list create-pad delete-pad)]))


(defn new-combat [{admin :admin} combat-name]
  (do
    (if (and (not (= combat-name "")) admin) (create-pad combat-name))
    (redirect "/")))
  
(defn- link-to-combat [ admin {id :_id, combat-name :name}]
  (if (not (= id ""))
    [:div.combat_link
     [:div]
     [:a {:href (str "/combat/" id)} combat-name]
     (if admin
       (form-to [:delete (str "/combat/" id)] (submit-button "Borrar")))
     [:div]
     ]))

(defn delete-combat [{admin :adimn} combat-name]
  (do (if admin (delete-pad combat-name))
          (redirect "/")))                   
(defn show-list "Shows the list of the combats" [ { user :user admin :admin } ] 
  (template-for-root-with-user user
   [[:div.combat_list
     [:h3 "Combates"]
    (unordered-list
     (filter (complement nil?) (conj
     (map (partial link-to-combat admin) (get-pad-list))
     (if admin (form-to [:post "/combat"]
              (label "matname" "nuevo tablero")
              (text-field "matname" "")
              (submit-button "Crear"))))))]]))
   
