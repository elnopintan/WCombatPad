(ns WCombatPad.list
  (:use hiccup.core)
  (:use hiccup.page-helpers)
  (:use hiccup.form-helpers)
  (:use [ring.util.response :only (redirect)])
  (:use [ WCombatPad.data :only (get-pad-list create-pad delete-pad)]))


(defn new-combat [combat-name] (do (create-pad combat-name)(redirect "/")))
(defn- link-to-combat [{id :_id, combat-name :name}]
  [:div.combat_link
   [:div]
   [:a {:href (str "/combat/" id)} combat-name]
   (form-to [:delete (str "/combat/" id)] (submit-button "Borrar"))
   [:div]
   ])
(defn delete-combat [combat-name] (do (delete-pad combat-name)(redirect "/")))                   
(defn show-list "Shows the list of the combats" [] 
  (html5
   [:head (include-css "/files/css/mat.css")]
   [:div.combat_list
    (unordered-list
     (conj
     (map link-to-combat (get-pad-list))
     (form-to [:post "/combat"]
              (label "matname" "nuevo tablero")
              (text-field "matname" "")
              (submit-button "Crear"))))]))
   
