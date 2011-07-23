(ns WCombatPad.list
  (:use hiccup.core)
  (:use hiccup.page-helpers)
  (:use hiccup.form-helpers)
  (:use [ WCombatPad.data :only (get-pad-list)]))


(defn link-to-combat [combat-name] [:a {:href (str "/combat/" combat-name)} combat-name])
(defn show-list [] 
  (html5 (unordered-list
          (conj (map link-to-combat (get-pad-list))
          "nuevo tablero" )
          )))
