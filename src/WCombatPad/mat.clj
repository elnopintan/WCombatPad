(ns WCombatPad.mat
  (:use hiccup.core)
  (:use hiccup.page-helpers)
  (:use hiccup.form-helpers)
  (:use [WCombatPad.data :only (get-combat-data
                                set-image-uri
                                get-state-list
                                change-grid
                                set-new-character
                                move-character
                                )])
  (:use [WCombatPad.images :only (save-image-file)])
  (:use [ring.util.response :only (redirect)])
  (:require (clojure.contrib [duck-streams :as ds])
  ))

(defn- get-map-headers [{ mat-name :name grid-size :grid-size [offset-x offset-y] :offset } script]
  (vec (concat
        [:head
   (include-css "/files/css/mat.css")]
   (if script
     [[:script {:type "text/javascript"} (str "gridSize=" grid-size"; offsetX=" offset-x "; offsetY=" offset-y"; combatName='" mat-name "';" )]
          (include-js 
            "/files/js/jquery-1.6.1.min.js" 
            "/files/js/jquery-ui-1.8.12.custom.min.js"
            "/files/js/mat.js")]))))


(defn- show-character-position [ grid-size [offset-x offset-y]  number {image :avatar  [x y] :pos char-name :name}]
  [:img.token {:id char-name :src image :style (str "z-index: 10; width:"
                                      grid-size "px; height:" grid-size "px; top:"
                                      (+ offset-y (* y grid-size)) "px; left:"
                                      (+ offset-x (- (* x grid-size) (* grid-size number))) "px;")} ])

(defn- show-mat [{combat-name :name mat :mat grid-size :grid-size offset :offset  characters :characters order :order}]
  [:section#mat
   (map (partial show-character-position grid-size offset) (iterate inc 0) characters)
   [:div#position {:style (str "width:" (- grid-size 4) "px;"
                               "height:" (- grid-size 4) "px;"
                               ) } ""]
   [:img#map {:src (str "/combat/" combat-name "/map/" order) :style (str "left:-" (* grid-size (count characters)) "px;") }]
   ])

(defn- show-character [{char-name :name image :avatar}]
  [:div.character [:img {:src image :width "30px" :height "30px" }] char-name])

(defn- show-characters [{characters :characters}]
  [:section#characters
   (unordered-list (map #(show-character %) characters))])
(defn- multipart-form [form]
  (assoc form 1 (assoc (get form 1) :enctype "multipart/form-data" )))
(defn- upload-form [{pad-name :name}]
  [:section#upload_form
   "Nueva imagen"
  (multipart-form (form-to [:post (str "/combat/" pad-name "/map")]
           (file-upload "image")
           (submit-button "Subir")))])

(defn- change-grid-form [{pad-name :name
                          [posx posy] :offset
                          grid-size :grid-size}]
  [:section#change_grid
   "Modificar rejilla"
   (form-to [:post (str "/combat/" pad-name "/grid")]
            (label "posx" "Offset X")
            (text-field "posx" posx)[:br]
            (label "posy" "Offset Y")
            (text-field "posy" posy)[:br]
            (label "gridsize" "Anchura")
            (text-field "gridsize" grid-size)[:br]
            (submit-button "Modificar"))])

(defn- create-character [{pad-name :name}]
  [:section#create_character
   "Crear personaje"
   (multipart-form (form-to
                    [:post (str "/combat/" pad-name "/character")]
                    (label "charname" "Nombre")
                    (text-field "charname")[:br]
                    (label "avatar" "Avatar")
                    (file-upload "avatar")[:br]
                    (submit-button "Crear")
                    ))])
            
(defn- show-actions [combat-data] [:section#actions
                                   (unordered-list
                                    (map #(% combat-data)
                                     [ upload-form
                                      change-grid-form
                                      create-character] ))])
(defn show-state [combat-name {order :order description :description}]
  [:div
   [:a { :href (str "/combat/" combat-name "/state/" order)} description]
   " "
   [:a { :href (str "/combat/" combat-name "/state/" order ".png")} " IMG"]])

(defn show-state-list [combat-name]
  [:section#states (unordered-list
                    (map #(show-state combat-name %)
                         (get-state-list combat-name)))])

(defn show-body [{ combat-name :name :as combat-data }]
  [:body (show-mat combat-data) 
           [:nav
            (show-actions combat-data)
            (show-characters combat-data) 
            (show-state-list combat-name)]])

(defn
  show-combat
  ([combat-name]
     (let [combat-data (get-combat-data combat-name)]
     (html5 (get-map-headers combat-data true)
           (show-body combat-data)
           )))
  ([combat-name order]
     (let [combat-data (get-combat-data combat-name order)]
       (html5
        (get-map-headers combat-data false)
        (show-mat combat-data)
        [:nav
         (show-state-list combat-name)
         [:a {:href (str "/combat/" combat-name) } "Volver"]]))))
  

(defn save-file [file-name dir stream]
  (save-image-file file-name dir stream)
 ; (ds/copy stream (ds/file-str (str "resources/public/images/" dir "/" file-name)))
  )

(defn save-image [combat-name {img-name :filename stream :tempfile}]
  (let [file-name (str combat-name img-name)]
  (do
    (save-file file-name "maps" stream)  
    (set-image-uri combat-name file-name)
    (redirect (str "/combat/" combat-name))
    )))

(defn save-character [combat-name character-name {img-name :filename stream :tempfile}]
  (let [file-name (str combat-name character-name img-name)]
    (do
      (save-file file-name "chars" stream)
      (set-new-character combat-name character-name file-name)
      (redirect (str "/combat/" combat-name)))))
      


(defn save-grid [combat-name posx posy grid-size]
  (do (change-grid combat-name posx posy grid-size)
      (redirect (str "/combat/" combat-name))))

(defn save-move [combat-name char-name posx posy]
  (do (move-character combat-name char-name [posx posy])
      (html (show-body (get-combat-data combat-name)))))

