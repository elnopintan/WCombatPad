(ns WCombatPad.mat
  (:use hiccup.core)
  (:use hiccup.page-helpers)
  (:use hiccup.form-helpers)
  (:use [WCombatPad.data :only (get-combat-data)])
  (:use [ring.util.response :only (redirect)]))

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
   [:img#map {:src (str "/combat/" combat-name "/map/" mat) :style (str "left:-" (* grid-size (count characters)) "px;") }]
   ])

(defn- show-character [{char-name :name image :avatar}]
  [:div.character [:img {:src image :width "30px" :height "30px" }] char-name])

(defn- show-characters [{characters :characters}]
  [:section#characters
   (unordered-list (map #(show-character %) characters))])  
                                      
(defn- show-actions [combat-data] [:section#actions
                                  (unordered-list ["move" "move" "move"])])
(defn show-combat [combat-name]
  (let [combat-data (get-combat-data combat-name)]
    (html5 (get-map-headers combat-data)
           (show-mat combat-data) 
           [:nav (map #(% combat-data) [show-characters show-actions])])))
