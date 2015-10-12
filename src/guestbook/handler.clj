(ns guestbook.handler
  (:require [compojure.core :refer [defroutes routes wrap-routes]]
            [guestbook.layout :refer [error-page]]
            [guestbook.routes.home :refer [home-routes]]
            [guestbook.routes.ws :refer
             [websocket-routes start-router! stop-router!]]
            [guestbook.middleware :as middleware]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.3rd-party.rotor :as rotor]
            [selmer.parser :as parser]
            [environ.core :refer [env]]))

;START:init-destroy
(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []
  (timbre/merge-config!
    {:level     (if (env :dev) :trace :info)
     :appenders {:rotor (rotor/rotor-appender
                          {:path     "guestbook.log"
                           :max-size (* 512 1024)
                           :backlog  10})}})
  (if (env :dev) (parser/cache-off!))
  (start-router!)
  (timbre/info (str
                 "\n-=[guestbook started successfully"
                 (when (env :dev) " using the development profile")
                 "]=-")))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (timbre/info "guestbook is shutting down...")
  (stop-router!)
  (timbre/info "shutdown complete!"))
;END:init-destroy


(def app-routes
  (routes
    #'websocket-routes
    (-> #'home-routes
        (wrap-routes middleware/wrap-csrf)
        (wrap-routes middleware/wrap-formats))
    (route/not-found
      (:body
        (error-page {:code  404
                     :title "page not found"})))))

(def app
  (middleware/wrap-base #'app-routes))

