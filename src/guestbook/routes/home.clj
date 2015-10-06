;START:ns
(ns guestbook.routes.home
  (:require [guestbook.layout :as layout]
            [guestbook.db.core :as db]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [compojure.core :refer [defroutes GET POST]]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [ring.util.response :refer [response status]]))
;END:ns

;START:home-page
(defn home-page []
  (layout/render "home.html"))
;END:home-page

;START:validate-params
(defn validate-message [params]
  (first
   (b/validate
    params
    :name v/required
    :message [v/required [v/min-count 10]])))
;END:validate-params

;START:save-message
(defn save-message! [{:keys [params]}]
  (if-let [errors (validate-message params)]
    (-> {:errors errors} response (status 400))
    (do
      (db/save-message!
       (assoc params :timestamp (java.util.Date.)))
      (response {:status :ok}))))
;END:save-message

(defn about-page []
  (layout/render "about.html"))

;START:home-routes
(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/messages" [] (response (db/get-messages)))
  (POST "/add-message" req (save-message! req))
  (GET "/about" [] (about-page)))
;END:home-routes
