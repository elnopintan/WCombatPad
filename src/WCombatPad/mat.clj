(ns WCombatPad.mat
  (:use hiccup.core)
  (:use hiccup.page-helpers)
  (:use hiccup.form-helpers)
  (:use [WCombatPad.data :only (get-combat-data set-image-uri get-state-list)])
  (:use [ring.util.response :only (redirect)])
  (:require (clojure.contrib [duck-streams :as ds])
  ))

(defn- get-map-headers [{grid-size :grid-size [offset-x offset-y] :offset }] [:head (include-css "/files/css/mat.css")
          [:script {:type "text/javascript"} (str "gridSize=" grid-size"; offsetX=" offset-x "; offsetY=" offset-y";"  )]
          (include-js 
            "/files/js/jquery-1.6.1.min.js" 
            "/files/js/jquery-ui-1.8.12.custom.min.js"
            "/files/js/mat.js")])


(defn- show-character-position [ grid-size [offset-x offset-y]  number {image :avatar  [x y] :pos}]
  [:img.token {:src image :style (str "z-index: 10; width:"
                                      grid-size "px; top:"
                                      (+ offset-y (* y grid-size)) "px; left:"
                                      (+ offset-x (- (* x grid-size) (* grid-size number))) "px;")} ])

(defn- show-mat [{combat-name :name mat :mat grid-size :grid-size offset :offset  characters :characters}]
  [:section#mat
   (map (partial show-character-position grid-size offset) (iterate inc 0) characters)
   [:div#position {:style (str "width:" (- grid-size 4) "px;"
                               "height:" (- grid-size 4) "px;"
                               ) } ""]
   [:img#map {:src (str "/combat/" combat-name "/map") :style (str "left:-" (* grid-size (count characters)) "px;") }]
   ])

(defn- show-character [{char-name :name image :avatar}]
  [:div.character [:img {:src image :width "30px" :height "30px" }] char-name])

(defn- show-characters [{characters :characters}]
  [:section#characters
   (unordered-list (map #(show-character %) characters))])
(defn- multipart-form [form]
  (assoc form 1 (assoc (get form 1) :enctype "multipart/form-data" )))
(defn- upload-form [pad-name]
  [:section#upload_form
   "Nueva imagen"
  (multipart-form (form-to [:post (str "/combat/" pad-name "/map")]
           (file-upload "image")
           (submit-button "Subir")))])                                      
(defn- show-actions [{pad-name :name}] [:section#actions
                                   (unordered-list
                                    [
                                     (upload-form pad-name) 
                                     ])])
(defn show-state-list [combat-name] [:section#states (unordered-list (get-state-list combat-name))])
(defn show-combat [combat-name]
  (let [combat-data (get-combat-data combat-name)]
    (html5 (get-map-headers combat-data)
           (show-mat combat-data) 
           [:nav
            (show-actions combat-data)
            (show-characters combat-data) 
            (show-state-list combat-name)])))

(defn save-image [combat-name {img-name :filename stream :tempfile}]
  (let [file-name (str combat-name img-name)]
  (do
    (ds/copy stream (ds/file-str (str "resources/public/images/maps/" combat-name img-name)))
    (set-image-uri combat-name file-name)
    (redirect (str "/combat/" combat-name))
      )))
