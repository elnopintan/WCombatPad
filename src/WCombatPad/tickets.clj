(ns WCombatPad.tickets
  (:use hiccup.core)
  (:use [ WCombatPad.core :only (template)])) 

(def sample-ticket { :name "Sample" :url "/user/new/fsdfsafafafaffaf" })

(defn show-ticket [{name :name url :url}]
  [:div#ticket
   (str name " ")
   [:a {:href url } "URL" ]])

(defn show-tickets []
    (template
     (concat [:div.tickets]
             (map show-ticket [sample-ticket]))))
           
      