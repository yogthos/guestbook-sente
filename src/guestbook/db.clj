(ns guestbook.db)

(def messages (atom {}))

(defn save-message! [path value]
  {:path path
   :value (get-in (swap! messages assoc-in path value) path)})

(defn get-messages []
  @messages)
