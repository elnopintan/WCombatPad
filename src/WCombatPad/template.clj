(ns WCombatPad.template
  (:use hiccup.core)
  (:use hiccup.page)
  (:use hiccup.form)
)

(defn get-map-headers []
   [:head
   (include-css "/files/css/mat.css")
   (include-js 
            "/files/js/jquery-1.6.1.min.js" 
            "/files/js/jquery-ui-1.8.12.custom.min.js"
            "/files/js/mat.js")])


(defn template-body [root user]
  [:div.menu
   (if (not root) [:a {:href "/" } "Volver" ]) " "
   [:a {:href "/user/profile" } "Perfil"] " "
   [:a {:href "/exit" } "Salir" ]])

(defmacro template-for-root-with-user
  [user body]
   `(html5 (get-map-headers)
               [:body
                (template-body true ~user)
                (vec (concat [:div#main ]
                             ~body))]))  

(defmacro template-with-user
  [user body]
   `(html5 (get-map-headers)
               [:body
                (template-body false ~user)
                (vec (concat [:div#main ]
                             ~body))]))
(defmacro template [body]
  `(html5 (get-map-headers)
         [:body 
          [:div.menu ]
          (vec (concat
                [:div#main]
                ~body))])) 
