(ns WCombatPad.images
  (:import java.net.URL )
  (:import javax.imageio.ImageIO)
  (:import java.io.ByteArrayOutputStream)
  (:import java.io.ByteArrayInputStream)
  (:import java.awt.Color)
  (:import org.jets3t.service.security.AWSCredentials)
  (:import org.jets3t.service.impl.rest.httpclient.RestS3Service)
  (:import org.jets3t.service.model.S3Object)
  (:use [WCombatPad.cache :only (from-cache)]) 
  (:require (clojure.java [io :as ds]))
  (:use [WCombatPad.data :only (get-combat-data)]))




(defprotocol FileHandler
  (save-a-file [this file-name dir file] "Saves a image")
  (load-a-file [this dir file-name] "Load a image"))


(defn s3-service []
  (let [accesskey (System/getenv "S3ACCESSKEY")
        secretkey (System/getenv "S3SECRETKEY")]
        (RestS3Service.
                 (AWSCredentials. accesskey secretkey))))

(deftype S3Handler []
  FileHandler
  (save-a-file [ _ file-name dir file]
    (let [service (s3-service)
        object  (S3Object. file)]
      (do (.setKey object file-name)
          (.putObject service (str "WCombatPad/" dir) object))))
  (load-a-file [ _ dir file-name]
    (let [service (s3-service)
        bucket (.getBucket service "WCombatPad")]
      (.getDataInputStream
       (.getObject service bucket (str dir "/" file-name)))))
  )


(deftype LocalHandler []
  FileHandler
  (save-a-file [ _ file-name dir a-file]
    (ds/copy a-file (ds/file (str "resources/public/images/" dir "/" file-name)))
    )
  (load-a-file [ _ dir file-name]
    (.openStream (URL. (str "http://localhost:3000/files/images/" dir "/" file-name)))))



(if (= "remote" (System/getenv "PADMODE"))
  (def img-handler (S3Handler.))
  (def img-handler (LocalHandler.)))

(def save-image-file (partial save-a-file img-handler))
(def load-image-file (partial load-a-file img-handler))

(defn paint-grid [graphics image {[offset-x offset-y] :offset  grid-size :grid-size} ]
  (let [width (.getWidth image)
        height (.getHeight image)]
  (do   (.setColor graphics Color/BLACK)
        (doall (map #(.drawLine graphics 0 % width %)
             (take-while #(< % height) (iterate #(+ grid-size %) offset-y))))
        (doall (map #(.drawLine graphics % 0 % height)
                    (take-while #(< % width) (iterate #(+ grid-size %) offset-x)))))))

(defn paint-characters [graphics image {characters :characters grid-size :grid-size [offset-x offset-y] :offset }]
  (let [alive-characters (filter #(not (= "yes" (% :dead))) characters) ]
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
          alive-characters)))))

(defn run-on-image [{mat :mat :as mat-data} & funcs]
 (let [ image (ImageIO/read (load-image-file "maps" mat))
       graphics (.createGraphics image)
       ostream (ByteArrayOutputStream.)]
    (do
      (doall (map #(% graphics image mat-data) funcs))
      (ImageIO/write image "png" ostream)
      (.toByteArray ostream))))

(defn create-stream [image]
      (ByteArrayInputStream. image))
      



(defn get-map [map-name order]
  (let [ {mat :mat :as mat-data} (get-combat-data map-name order)]
    (create-stream (from-cache mat #(run-on-image mat-data paint-grid)))))

(defn get-image-state [map-name order]
  (create-stream (run-on-image (get-combat-data map-name order) paint-grid paint-characters)))



