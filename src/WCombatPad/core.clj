(ns WCombatPad.core
  (:import java.net.URL )
  (:import javax.imageio.ImageIO)
  (:import java.io.ByteArrayOutputStream)
  (:import java.io.ByteArrayInputStream)
  (:import java.awt.Color)
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
(defn get-map-headers [{grid-size :grid-size}] [:head (include-css "/css/mat.css")
          [:script {:type "text/javascript"} (str "gridSize=" grid-size)]
          (include-js 
            "/js/jquery-1.6.1.min.js" 
            "/js/jquery-ui-1.8.12.custom.min.js"
            "/js/mat.js")])
(defn get-combat-data [combat-name]
  {:name combat-name
   :mat "almacen_hundido_bajo_parcial"
   :grid-size 21
   :offset [8 18]
   :characters [{:name "cleric"
                 :avatar "https://secure.gravatar.com/avatar/9c3be996ad1960ae9eec9b82e0231880?s=140&d=https://gs1.wac.edgecastcdn.net/80460E/assets%2Fimages%2Fgravatars%2Fgravatar-140.png"
                 :pos [1 1]}
                {:name "warriow"
                 :avatar "https://secure.gravatar.com/avatar/9c3be996ad1960ae9eec9b82e0231880?s=140&d=https://gs1.wac.edgecastcdn.net/80460E/assets%2Fimages%2Fgravatars%2Fgravatar-140.png"
                 :pos [8 7]}]})
(defn show-character-position [ grid-size [offset-x offset-y]  number {image :avatar  [x y] :pos}]
  [:img.token {:src image :style (str "z-index: 10; width:"
                                      grid-size "px; top:"
                                      (+ offset-y (* y grid-size)) "px; left:"
                                      (+ offset-x (- (* x grid-size) (* grid-size number))) "px;")} ])

(defn show-mat [{combat-name :name mat :mat grid-size :grid-size offset :offset  characters :characters}]
  [:section#mat
   (map (partial show-character-position grid-size offset) (iterate inc 0) characters)
   [:div#position {:style (str "width:" (- grid-size 4) "px;"
                               "height:" (- grid-size 4) "px;"
                               ) } ""]
   [:img#map {:src (str "/combat/" combat-name "/map/" mat) :style (str "left:-" (* grid-size (+ 1(count characters))) "px;") }]
   ])
(defn show-character [{char-name :name image :avatar}]
  [:div.character [:img {:src image :width "30px" :height "30px" }] char-name])
(defn show-characters [{characters :characters}]
  [:section#characters
   (unordered-list (map #(show-character %) characters))])  
                                      
(defn show-actions [combat-data] [:section#actions
                                  (unordered-list ["move" "move" "move"])])
(defn get-map [map-name ]
  (let [{grid-size :grid-size [offset-x offset-y] :offset} (get-combat-data map-name)
        image (ImageIO/read (URL. (str "http://localhost:3000/images/maps/" map-name ".jpg")))
        output-stream (ByteArrayOutputStream.)
        graphics (.createGraphics image)]
    (do (.setColor graphics Color/BLACK)
        (doall (map #(.drawLine graphics 0 % (.getWidth image) %)
             (take-while #(< % (.getHeight image)) (iterate #(+ grid-size %) offset-y))))
        (doall (map #(.drawLine graphics % 0 % (.getHeight image))
             (take-while #(< % (.getWidth image)) (iterate #(+ grid-size %) offset-x))))
        (ImageIO/write image "png" output-stream)
        (ByteArrayInputStream. (.toByteArray output-stream)))))

(defn show-combat [combat-name]
  (let [combat-data (get-combat-data combat-name)]
    (html5 (get-map-headers combat-data)
           (show-mat combat-data) 
           [:nav (map #(% combat-data) [show-characters show-actions])])))
(defn link-to-combat [combat-name] [:a {:href (str "/combat/" combat-name)} combat-name])

(defn show-list [] 
  (html5 (unordered-list (map link-to-combat ["uno" "dos" "tres"]))))
