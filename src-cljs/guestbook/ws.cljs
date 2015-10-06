;START:ns
(ns guestbook.ws
  (:require [cognitect.transit :as t]))
;END:ns

;START:channel
(defonce ws-chan (atom nil))
(def json-reader (t/reader :json))
(def json-writer (t/writer :json))
;END:channel

;START:receive-message
(defn receive-message! [handler]
  (fn [msg]
    (->> msg .-data (t/read json-reader) handler)))
;END:receive-message

;START:send-message
(defn send-message! [msg]
  (if @ws-chan
    (->> msg (t/write json-writer) (.send @ws-chan))
    (throw (js/Error. "Websocket is not available!"))))
;END:send-message

(defn connect! [url receive-handler]
  (if-let [chan (js/WebSocket. url)]
    (do
      (set! (.-onmessage chan) (receive-message! receive-handler))
      (reset! ws-chan chan))
    (throw (js/Error. "Websocket connection failed!"))))
