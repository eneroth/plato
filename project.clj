(defproject plato "0.1"
  :description "Persists state to local storage."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2138"]]
  :plugins [[lein-cljsbuild "1.0.1"]]
  :cljsbuild {
              :builds [{:id "dev"
                        :source-paths ["src-cljs"]
                        :compiler {:output-to "resources/public/js/plato.js"
                                   :optimizations :none
                                   :output-dir "resources/public/js/output"
                                   :pretty-print true
                                   :source-map "resources/public/js/output/plato.js.map"}}
                       {:id "prod"
                        :source-paths ["src-cljs"]
                        :compiler {:output-to "resources/public/js/plato.js"
                                   :optimizations :advanced
                                   :pretty-print false}}]})
