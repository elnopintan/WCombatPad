(ns WCombatPad.cache
  (:use fogus.clache)
  (:import fogus.clache.LRUCache))

(def cache (atom (.seed (LRUCache. nil nil nil 50) {})))

(defn invalidate [key]
  (swap! cache #(.miss % key nil)))

(defn from-cache [cache-key miss-fn]
  (let [cache-data (lookup @cache cache-key)]
    (if cache-data
      (do
        (swap! cache #(.hit % cache-key))
        cache-data)
      (let [new-cache-data (miss-fn)]
        (do
          (swap! cache #(.miss % cache-key new-cache-data))
          new-cache-data)))))