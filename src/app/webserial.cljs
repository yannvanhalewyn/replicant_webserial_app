(ns app.webserial)

(defn- webserial-supported? []
  (if (.-serial js/navigator)
    true
    (do
      (js/alert "WebSerial API is not supported in this browser. Please use Chrome, Edge, or Opera.")
      false)))

(defn- start-reader-loop! [rdr]
  (let [text-decoder (js/TextDecoder.)]
    (letfn [(read-loop []
              (-> (.read rdr)
                (.then (fn [result]
                         (when-not (.-done result)
                           (let [value (.-value result)
                                 text (.decode text-decoder value)]
                             (println "Received:" text))
                           (read-loop))))))]
      (read-loop))))

(defn connect! [state]
  (when (webserial-supported?)
    (let [port (js/navigator.serial.requestPort)]
      (-> (.open port #js {:baudRate 9600})
        (.then
          (fn []
            (let [reader (.getReader (.-readable port))
                  writer (.getWriter (.-writable port)) ]
              (start-reader-loop! reader)
              (swap! state assoc
                ::connection
                {::port port
                 ::reader reader
                 ::writer writer
                 ::encoder (js/TextEncoder.)}))))
        (.catch
          (fn [err]
           ;; Catches and displays errors in the UI
            (swap! state assoc
              :serial-connected false
              :serial-status (str "Error: " (.-message err)))
            (println "Error connecting:" err)))))))

(defn disconnect! [state]
  (when-let [connection (::connection state)]
    (-> (.cancel (::reader connection))
      (.then (fn []
               (.releaseLock (::writer connection))))
      (.then (fn []
               (.close (::port connection))))
      (.then (fn []
               (swap! state dissoc ::connection)))
      (.catch (fn [err]
                (println "Error disconeccting:" err))))))

(defn send-data! [connection data]
  (when-let [writer (::writer connection)]
    (let [encoded (.encode (::encoder connection) data)]
      (-> (.write writer encoded)
        (.then (fn []
                 (println "Sent:" data)))
        (.catch (fn [err]
                  (println "Error sending data:" err)))))))
