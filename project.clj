(defproject openpr "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :license {:name "MIT License"}
  :url "http://example.com/FIXME"
  :dependencies [
      [org.clojure/clojure "1.8.0"]
      [compojure "1.5.2"]
      [ring/ring-core "1.3.2"]
      [ring/ring-jetty-adapter "1.3.2"]
      [ring/ring-json "0.3.1"]
      [clj-http "2.3.0"]
      [environ "1.1.0"]
      [org.clojure/data.json "0.2.6"]]
  :plugins [
    [lein-ring "0.9.1"]
    [lein-environ "1.1.0"]]
  :ring {:handler openpr.core/api}
  :profiles {:dev {}}
)
