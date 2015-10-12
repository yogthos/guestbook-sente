(defproject guestbook "0.1.0-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [selmer "0.8.9"]
                 [com.taoensso/timbre "4.1.0"]
                 [com.taoensso/tower "3.0.2"]
                 [markdown-clj "0.9.68"]
                 [environ "1.0.0"]
                 [compojure "1.4.0"]
                 [ring-webjars "0.1.1"]
                 [ring/ring-defaults "0.1.5"]
                 [ring "1.4.0"
                  :exclusions [ring/ring-jetty-adapter]]
                 [metosin/ring-middleware-format "0.6.0"]
                 [metosin/ring-http-response "0.6.3"]
                 [bouncer "0.3.3"]
                 [prone "0.8.2"]
                 [org.clojure/tools.nrepl "0.2.10"]
                 [cljs-ajax "0.3.14"]
                 [reagent "0.5.0"]
                 [cljsjs/react "0.13.3-1"]
                 [migratus "0.8.2"]
                 [conman "0.1.6"]
                 [com.h2database/h2 "1.4.188"]
                 [org.immutant/web "2.1.0"]
                 [org.clojure/tools.reader "0.9.2"]
                 [org.clojure/clojurescript "1.7.107" :scope "provided"]
                 [com.taoensso/sente "1.7.0-RC1"]]

  :min-lein-version "2.0.0"
  :uberjar-name "guestbook.jar"
  :repl-options {:init-ns guestbook.handler}
  :jvm-opts ["-server"]

  :main guestbook.core

  ;START:cljsbuild
  :plugins [[lein-environ "1.0.0"]
            [migratus-lein "0.1.5"]
            [lein-cljsbuild "1.0.6"]]

  :cljsbuild
  {:builds {:app {:source-paths ["src-cljs"]
                  :compiler {:output-to     "resources/public/js/app.js"
                             :output-dir    "resources/public/js/out"
                             :optimizations :none
                             :source-map true
                             :pretty-print  true}}}}
  :clean-targets ^{:protect false} ["resources/public/js"]
  ;END:cljsbuild

  :profiles
  {:uberjar {:omit-source true
             :env {:production true}
             :aot :all}
   :dev           [:project/dev :profiles/dev]
   :test          [:project/test :profiles/test]
   ;START:dev-profile
   :project/dev {:dependencies [[ring/ring-mock "0.2.0"]
                                [ring/ring-devel "1.4.0"]
                                [pjstadig/humane-test-output "0.7.0"]]
                 :injections [(require 'pjstadig.humane-test-output)
                              (pjstadig.humane-test-output/activate!)]
                 :env {:dev true
                       :port       3000
                        :nrepl-port 7000}}
   ;END:dev-profile
   :project/test {:env {:test       true
                        :port       3001
                        :nrepl-port 7001}}
   ;END:dev-profile
   :profiles/dev {}
   :profiles/test {}})
