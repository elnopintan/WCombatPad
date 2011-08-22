(ns WCombatPad.images
  (:import java.net.URL )
  (:import javax.imageio.ImageIO)
  (:import java.io.ByteArrayOutputStream)
  (:import java.io.ByteArrayInputStream)
  (:import java.awt.Color)
  (:import org.jets3t.service.security.AWSCredentials)
  (:import org.jets3t.service.impl.rest.httpclient.RestS3Service)
  (:import org.jets3t.service.model.S3Object)
  (:require (clojure.contrib [duck-streams :as ds]))
  (:use [WCombatPad.data :only (get-combat-data)]))

(defn s3-service []
  (let [accesskey (System/getenv "S3ACCESSKEY")
        secretkey (System/getenv "S3SECRETKEY")]
        (RestS3Service.
                 (AWSCredentials. accesskey secretkey))))

(defn save-image-file-remote [file-name dir file]
  (let [service (s3-service)
        object  (S3Object. file)]
    (do (.setKey object file-name)
    (.putObject service (str "WCombatPad/" dir) object))))

(defn save-image-file-local [file-name dir file]
  (ds/copy file (ds/file-str (str "resources/public/images/" dir "/" file-name))))

(defn load-image-file-local [dir file-name]
  (.openStream (URL. (str "http://localhost:3000/files/images/" dir "/" file-name))))

(defn load-image-file-remote [dir file-name]
  (let [service (s3-service)
        bucket (.getBucket service "WCombatPad")]
    (.getDataInputStream
     (.getObject service bucket (str dir "/" file-name)))))

(if (= "remote" (System/getenv "PADMODE"))
  (do
    (def save-image-file save-image-file-remote)
    (def load-image-file load-image-file-remote))
  (do
    (def save-image-file save-image-file-local)
    (def load-image-file load-image-file-local)))

(defn paint-grid [graphics image {[offset-x offset-y] :offset  grid-size :grid-size} ]
  (let [width (.getWidth image)
        height (.getHeight image)]
  (do   (.setColor graphics Color/BLACK)
        (doall (map #(.drawLine graphics 0 % width %)
             (take-while #(< % height) (iterate #(+ grid-size %) offset-y))))
        (doall (map #(.drawLine graphics % 0 % height)
                    (take-while #(< % width) (iterate #(+ grid-size %) offset-x)))))))

(defn paint-characters [graphics image {characters :characters grid-size :grid-size [offset-x offset-y] :offset }]
  (do (print characters) (doall (map
          (fn [{avatar :avatar [off-char-x off-char-y] :pos size :size}]
            (let [char-image (ImageIO/read (load-image-file "chars" avatar))
                  pos-x (+ (* off-char-x grid-size) offset-x 1)
                  pos-y (+ (* off-char-y grid-size) offset-y 1)]
              (do
                (print (str avatar ";" pos-x ";" pos-y ";" grid-size))
               (.drawImage
               graphics char-image
               pos-x pos-y
               (- (* size grid-size) 1) (- (* size grid-size) 1) nil)
              )))
          characters))))

(defn run-on-image [map-name order & funcs]
 (let [ {mat :mat :as mat-data} (get-combat-data map-name order)
        image (ImageIO/read (load-image-file "maps" mat))
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



