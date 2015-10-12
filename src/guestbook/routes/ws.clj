;START:ns
(ns guestbook.routes.ws
  (:require [compojure.core :refer [GET defroutes]]
            [taoensso.timbre :as timbre]
            [immutant.web.async :as async]
            [cognitect.transit :as transit]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [guestbook.db.core :as db]))
;END:ns

;START:channels
(defonce channels (atom #{}))
;END:channels

;START:connect-disconnect
(defn connect! [channel]
  (timbre/info "channel open")
  (swap! channels conj channel))

(defn disconnect! [channel {:keys [code reason]}]
  (timbre/info "close code:" code "reason:" reason)
  (swap! channels #(remove #{channel} %)))
;END:connect-disconnect

;START:transit
(defn encode-transit [message]
  (let [out    (java.io.ByteArrayOutputStream. 4096)
        writer (transit/writer out :json)]
    (transit/write writer message)
    (.toString out)))

(defn decode-transit [message]
  (let [in (java.io.ByteArrayInputStream. (.getBytes message))
        reader (transit/reader in :json)]
    (transit/read reader)))
;END:transit

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
;END:save-message

;START:handle-message
(defn handle-message! [channel message]
  (let [response (-> message
                     decode-transit
                     (assoc :timestamp (java.util.Date.))
                     save-message!)]
    (if (:errors response)
      (async/send! channel (encode-transit response))
      (doseq [channel @channels]
        (async/send! channel (encode-transit response))))))
;END:handle-message

;START:ws-handler
(defn ws-handler [request]
  (async/as-channel
    request
    {:on-open    connect!
     :on-close   disconnect!
     :on-message handle-message!}))
;END:ws-handler

;START:defroutes
(defroutes websocket-routes
           (GET "/ws" [] ws-handler))
;END:defroutes