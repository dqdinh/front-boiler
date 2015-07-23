(defproject front-boiler "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :jvm-opts ^:replace ["-Xmx1g" "-server"]

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "0.0-3308"]
                 ;; Facilities for async programming and
                 ;; communication in Clojure
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 ;; Lisp/Hiccup style templating for Facebook's
                 ;; React in ClojureScript
                 [sablono "0.3.4"]
                 ;; Clojure JSON and JSON SMILE (binary json format)
                 ;; encoding/decoding
                 [cheshire "5.5.0"]
                 [com.andrewmcveigh/cljs-time "0.3.10"]
                 ;; A data inspection component for Om
                 [ankha "0.1.4"]
                 ;; A simple Ajax library for ClojureScript
                 [cljs-ajax "0.3.13"]
                 ;; A client-side router for ClojureScript
                 [secretary "1.2.3"]
                 ;; A date and time library for ClojureScript
                 [com.cemerick/clojurescript.test "0.3.3"]
                 [com.cemerick/url "0.1.1"]
                 [hiccups "0.3.0"]
                 ;; ClojureScript interface to Facebook's React
                 [org.omcljs/om "0.8.8"]]

  :plugins [[lein-cljsbuild "1.0.5"]
            ;; Leiningen plugin that pushes ClojureScript
            ;; code changes to the client
            [lein-figwheel "0.3.7"]
            [cider/cider-nrepl "0.9.1"]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {
    :builds [{:id "dev"
              :source-paths ["src"]

              :figwheel { :on-jsload "front-boiler.core/on-js-reload" }

              :compiler {:main front-boiler.core
                         :asset-path "js/compiled/out"
                         :output-to "resources/public/js/compiled/front-boiler.js"
                         :output-dir "resources/public/js/compiled/out"
                         :source-map-timestamp true }}
             {:id "min"
              :source-paths ["src"]
              :compiler {:output-to "resources/public/js/compiled/front-boiler.js"
                         :main front-boiler.core
                         :optimizations :advanced
                         :pretty-print false}}]}

  :figwheel {
             ;; :http-server-root "public" ;; default and assumes "resources"
             :server-port 3030 ;; default
             ;; :server-ip "127.0.0.1"

             :css-dirs ["resources/public/css"] ;; watch and update CSS

             ;; Start an nREPL server into the running figwheel process
             ;; :nrepl-port 7888

             ;; Server Ring Handler (optional)
             ;; if you want to embed a ring handler into the figwheel http-kit
             ;; server, this is for simple ring servers, if this
             ;; doesn't work for you just run your own server :)
             ;; :ring-handler front-boiler.core/handler

             ;; To be able to open files in your editor from the heads up display
             ;; you will need to put a script on your path.
             ;; that script will have to take a file path and a line number
             ;; ie. in  ~/bin/myfile-opener
             ;; #! /bin/sh
             ;; emacsclient -n +$2 $1
             ;;
             ;; :open-file-command "myfile-opener"

             ;; if you want to disable the REPL
             ;; :repl false

             ;; to configure a different figwheel logfile path
             ;; :server-logfile "tmp/logs/figwheel-logfile.log"
             })
