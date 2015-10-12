;START:ns
(ns guestbook.routes.ws
  (:require [compojure.core :refer [GET POST defroutes]]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [guestbook.db.core :as db]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.immutant
             :refer [sente-web-server-adapter]]))
;END:ns

;START:socket
(let [connection (sente/make-channel-socket!
                   sente-web-server-adapter
                   {:user-id-fn (fn [ring-req] (get-in ring-req [:params :client-id]))})]
  (def ring-ajax-post (:ajax-post-fn connection))
  (def ring-ajax-get-or-ws-handshake (:ajax-get-or-ws-handshake-fn connection))
  (def ch-chsk (:ch-recv connection))                       ; ChannelSocket's receive channel
  (def chsk-send! (:send-fn connection))                    ; ChannelSocket's send API fn
  (def connected-uids (:connected-uids connection))         ; Watchable, read-only atom
  )
;END:socket

;START:save-message
(defn validate-message [params]
  (first
    (b/validate
      params
      :name v/required
      :message [v/required [v/min-count 10]])))

(defn save-message! [message]
  (if-let [errors (validate-message message)]
    {:errors errors}
    (do
      (db/save-message! message)
      message)))

(defn handle-message! [{:keys [id client-id ?data]}]
  (when (= id :guestbook/add-message)
    (let [response (-> ?data
                       (assoc :timestamp (java.util.Date.))
                       save-message!)]
      (if (:errors response)
        (chsk-send! client-id [:guestbook/error response])
        (doseq [uid (:any @connected-uids)]
          (chsk-send! uid [:guestbook/add-message response]))))))
;END:save-message

;START:router
(defonce router (atom nil))

(defn stop-router! []
  (when-let [stop-f @router] (stop-f)))

(defn start-router! []
  (stop-router!)
  (reset! router (sente/start-chsk-router! ch-chsk handle-message!)))
;END:router

;START:defroutes
(defroutes websocket-routes
           (GET "/ws" req (ring-ajax-get-or-ws-handshake req))
           (POST "/ws" req (ring-ajax-post req)))
;END:defroutes
