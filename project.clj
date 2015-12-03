(defproject reagent-dnd "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.145"]
                 [devcards "0.2.1"]
                 [reagent "0.5.1"]
                 [cljsjs/react-dnd "2.0.2-0"]
                 [cljsjs/react-dnd-html5-backend "2.0.0-0"]]

  :source-paths ["src/clj" "src/cljs"]

  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-figwheel "0.5.0-1"]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds [{:id           "tutorialdev"
                        :source-paths ["src/cljs"]
                        :figwheel     {:devcards true}
                        :compiler     {:main                 "tutorial.core"
                                       :devcards             true
                                       :asset-path           "js/compiled/tutorial"
                                       :output-to            "resources/public/js/compiled/tutorial.js"
                                       :output-dir           "resources/public/js/compiled/tutorial"
                                       :source-map-timestamp true}}
                       {:id           "tutorialprod"
                        :source-paths ["src/cljs"]
                        :compiler     {:main          "tutorial.core"
                                       :devcards      true
                                       :asset-path    "js/compiled/out"
                                       :output-to     "resources/public/js/compiled/tutorial.js"
                                       :optimizations :advanced}}]})
