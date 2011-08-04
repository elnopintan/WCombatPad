(ns WCombatPad.images
  (:import java.net.URL )
  (:import javax.imageio.ImageIO)
  (:import java.io.ByteArrayOutputStream)
  (:import java.io.ByteArrayInputStream)
  (:import java.awt.Color)
  (:use [WCombatPad.data :only (get-combat-data)]))

(defn paint-grid [graphics image {[offset-x offset-y] :offset  grid-size :grid-size} ]
  (let [width (.getWidth image)
        height (.getWidth image)]
  (do   (.setColor graphics Color/BLACK)
        (doall (map #(.drawLine graphics 0 % width %)
             (take-while #(< % height) (iterate #(+ grid-size %) offset-y))))
        (doall (map #(.drawLine graphics % 0 % height)
                    (take-while #(< % width) (iterate #(+ grid-size %) offset-x)))))))

(defn paint-characters [graphics image {characters :characters grid-size :grid-size [offset-x offset-y] :offset }]
  (do (print characters) (doall (map
          (fn [{avatar :avatar [off-char-x off-char-y] :pos}]
            (let [char-image (ImageIO/read (URL. (str "http://localhost:3000" avatar )))
                  pos-x (+ (* off-char-x grid-size) offset-x 1)
                  pos-y (+ (* off-char-y grid-size) offset-y 1)]
              (do
                (print (str avatar ";" pos-x ";" pos-y ";" grid-size))
               (.drawImage
               graphics char-image
               pos-x pos-y
               (- grid-size 1) (- grid-size 1) nil)
              )))
          characters))))

(defn run-on-image [map-name order & funcs]
 (let [ {mat :mat :as mat-data} (get-combat-data map-name order)
        image (ImageIO/read (URL. (str "http://localhost:3000/files/images/maps/" mat)))
        output-stream (ByteArrayOutputStream.)
        graphics (.createGraphics image)]
    (do
        (doall (map #(% graphics image mat-data) funcs))
        (ImageIO/write image "png" output-stream)
        (ByteArrayInputStream. (.toByteArray output-stream)))))

(defn get-map [map-name order]
  (run-on-image map-name order paint-grid))

(defn get-image-state [map-name order]
  (run-on-image map-name order paint-grid paint-characters))