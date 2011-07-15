(ns WCombatPad.core
  (:use hiccup.core)
  (:use hiccup.page-helpers)
  (:use hiccup.form-helpers)
  (:use [ring.util.response :only (redirect)]))

(defn show-login []
  (html5 (form-to [:post "/login"]
                   (label "password" "Password") [:br]
                   (password-field "password" "")
                   (submit-button "Enviar"))))
(defn filter-loged [{{loged :loged :as session} :session uri :uri } fn & params]
  (if loged (apply fn params)
      (assoc (redirect "/login") :session (assoc session :redirection uri))))
(defn get-map-headers [] [:head (include-css "/css/mat.css") 
          (include-js 
            "/js/jquery-1.6.1.min.js" 
            "/js/jquery-ui-1.8.12.custom.min.js")])
(defn get-combat-data [combat-name]
  {:mat "http://i669.photobucket.com/albums/vv53/elnopintan/AOW/almacen_hundido_bajo_parcial.jpg"
   :characters [{:name "cleric"
                 :avatar "https://secure.gravatar.com/avatar/9c3be996ad1960ae9eec9b82e0231880?s=140&d=https://gs1.wac.edgecastcdn.net/80460E/assets%2Fimages%2Fgravatars%2Fgravatar-140.png"
                 :pos [10 10]}
                {:name "warriow"
                 :avatar "img2"
                 :pos [50 50]}]})
(defn show-character-position [{image :avatar  [x y] :pos}]
  [:img.token {:src image :width 20 :top y :left x} ])

(defn show-mat [{mat :mat characters :characters}] [:section#mat
                              [:img#map {:src mat :width "500px"}]
                              (map show-character-position characters)
                              ])
(defn show-character [{char-name :name image :avatar}]
  [:div.character [:img {:src image :width "30px" :height "30px" }] char-name])
(defn show-characters [{characters :characters}]
  [:section#characters
   (unordered-list (map #(show-character %) characters))])  
                                      
(defn show-actions [combat-data] [:section#actions
                                  (unordered-list ["move" "move" "move"])])

(defn show-combat [combat-name]
  (let [combat-data (get-combat-data combat-name)]
    (html5 (get-map-headers)
           (show-mat combat-data) 
           [:nav (map #(% combat-data) [show-characters show-actions])])))
(defn link-to-combat [combat-name] [:a {:href (str "/combat/" combat-name)} combat-name])

(defn show-list [] 
  (html5 (unordered-list (map link-to-combat ["uno" "dos" "tres"]))))
