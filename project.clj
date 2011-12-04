(defproject WCombatPad "1.0.1"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.3.0"]
                [midje "1.3-alpha4"]
                [org.jasypt/jasypt "1.7"]
		[compojure "0.6.5"]
		[hiccup "0.3.7"]
                [ring/ring-jetty-adapter "1.0.0-RC1"]
                [net.java.dev.jets3t/jets3t "0.7.1"]
                [congomongo "0.1.7"]]
  :dev-dependencies [[lein-eclipse "1.0.0"]
                     [lein-ring "0.4.6"]]
  :ring {:handler WCombatPad.routes/pad-web})