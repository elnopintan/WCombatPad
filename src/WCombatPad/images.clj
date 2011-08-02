(ns WCombatPad.images
  (:import java.net.URL )
  (:import javax.imageio.ImageIO)
  (:import java.io.ByteArrayOutputStream)
  (:import java.io.ByteArrayInputStream)
  (:import java.awt.Color)
  (:use [WCombatPad.data :only (get-combat-data)]))

(defn paint-grid [graphics width height offset-x offset-y grid-size ]
  (do   (.setColor graphics Color/BLACK)
        (doall (map #(.drawLine graphics 0 % width %)
             (take-while #(< % height) (iterate #(+ grid-size %) offset-y))))
        (doall (map #(.drawLine graphics % 0 % height)
                    (take-while #(< % width) (iterate #(+ grid-size %) offset-x))))))

(defn get-map [map-name order]
  (let [{grid-size :grid-size [offset-x offset-y] :offset mat :mat} (get-combat-data map-name order)
        image (ImageIO/read (URL. (str "http://localhost:3000/files/images/maps/" mat)))
        output-stream (ByteArrayOutputStream.)
        graphics (.createGraphics image)]
    (do
        (paint-grid graphics (.getWidth image) (.getHeight image) offset-x offset-y grid-size)
        (ImageIO/write image "png" output-stream)
        (ByteArrayInputStream. (.toByteArray output-stream)))))
