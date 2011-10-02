(ns WCombatPad.template
  (:use hiccup.core)
  (:use hiccup.page-helpers)
  (:use hiccup.form-helpers)
)

(defn get-map-headers []
   [:head
   (include-css "/files/css/mat.css")
   (include-js 
            "/files/js/jquery-1.6.1.min.js" 
            "/files/js/jquery-ui-1.8.12.custom.min.js"
            "/files/js/mat.js")])

(defn template-body [user]
  [:div.menu
    [:a {:href "/user/profile" } "Perfil"]])
  
(defmacro template-with-user [ user body]
   `(html5 (get-map-headers)
           (vec
            (concat
               [:body
                (template-body ~user)
                ]
           ~body)))) 
(defmacro template [body]
  `(html5 (get-map-headers)
         (vec (concat
           [:body
            ]
           ~body)))) 