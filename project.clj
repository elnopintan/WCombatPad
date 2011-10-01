(defproject WCombatPad "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [ring/ring-jetty-adapter "0.3.9"]
                 [midje "1.1"]
                 [org.jasypt/jasypt "1.7"]
		[compojure "0.6.2"]
		[hiccup "0.3.4"]
                [net.java.dev.jets3t/jets3t "0.7.1"]
    [congomongo "0.1.5-SNAPSHOT"]]
  :dev-dependencies [[lein-eclipse "1.0.0"]
                     [lein-ring "0.4.0"]]
  :ring {:handler WCombatPad.routes/pad-web})