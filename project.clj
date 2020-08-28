(defproject WCombatPad "1.0.2"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.10.1"]
                [midje "1.7.0"]
                [org.jasypt/jasypt "1.7"]
		[compojure "1.6.1"]
		[hiccup "1.0.5"]
                [javax.servlet/servlet-api "2.5"]
                 [ring "1.6.3"]
                [net.java.dev.jets3t/jets3t "0.7.1"]
                [congomongo "0.4.6"]
                 [clache "0.7.0"]
                 [org.clojure/tools.logging "0.4.1"]

                                        ;                [org.clojure/tools.nrepl "0.0.5"]
                 [org.postgresql/postgresql "42.2.16"]
                 [org.clojure/java.jdbc "0.7.11"]
                 
                ]
  :plugins [[lein-eclipse "1.0.0"]
                     [lein-ring "0.9.6"]]
  :min-lein-version "2.0.0"
  :ring {:handler WCombatPad.routes/pad-web})
