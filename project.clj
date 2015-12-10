(defproject reagent-dnd "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [devcards "0.2.1"]
                 [re-frame "0.4.1"]
                 [reagent "0.5.1"]
                 [secretary "1.2.3"]
                 [cljsjs/react-dnd "2.0.2-0"]
                 [cljsjs/highlight "8.4-0"]
                 [cljsjs/react-dnd-html5-backend "2.0.0-0"]
                 [medley "0.7.0"]
                 [markdown-clj "0.9.82"]]

  :source-paths ["src/clj" "src/cljs"]

  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-figwheel "0.5.0-1"]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]
  :figwheel {:css-dirs ["resources/public/css"]}

  :cljsbuild {:builds [{:id           "examplesprod"
                        :source-paths ["src/cljs" "examples"]
                        :compiler     {:main          examples.core
                                       :devcards      true
                                       :asset-path    "js/compiled/examples"
                                       :output-to     "resources/public/js/compiled/examples.js"
                                       :optimizations :advanced}}
                       {:id           "examples"
                        :source-paths ["src/cljs" "examples"]
                        :figwheel     {:on-jsload "examples.core/mount-root"}
                        :compiler     {:main                 examples.core
                                       :devcards             true
                                       :asset-path           "js/compiled/examples"
                                       :output-to            "resources/public/js/compiled/examples.js"
                                       :output-dir           "resources/public/js/compiled/examples"
                                       :source-map-timestamp true}}
                       {:id           "tutorialprod"
                        :source-paths ["src/cljs"]
                        :compiler     {:main          "tutorial.core"
                                       :devcards      true
                                       :asset-path    "js/compiled/out"
                                       :output-to     "resources/public/js/compiled/tutorial.js"
                                       :optimizations :advanced}}]})
