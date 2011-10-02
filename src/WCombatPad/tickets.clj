(ns WCombatPad.tickets
  (:use hiccup.core)
  (:use [ WCombatPad.template])
  (:use [WCombatPad.data :only (save-ticket get-tickets)])
  (:import java.util.UUID)
  ) 

(def sample-ticket { :name "Sample" :url "/user/new/fsdfsafafafaffaf" })

(defn show-ticket [{name :name url :url used :used }]
  [:div.ticket
   (str name " ")
   [:a {:href url } "URL " ]
   (if used "usado")])

(defn show-tickets [{admin :admin}]
  (if admin
    (template
     (concat [[:div#tickets]]
             (map show-ticket (get-tickets))))))

(defn generate-ticket [name]
  (let [ uuid (str (UUID/randomUUID))]
    { :name name :uuid uuid :url (str "/user/new/" uuid)}))

(defn produce-tickets [ & names]
  (doall
   (map #(save-ticket (generate-ticket %)) names))) 
  

