(ns app.webserial)

(defonce port (atom nil))
(defonce reader (atom nil))
(defonce writer (atom nil))

(defn- check-webserial-support []
  (if (.-serial js/navigator)
    true
    (do
      (js/alert "WebSerial API is not supported in this browser. Please use Chrome, Edge, or Opera.")
      false)))

(js/console.log "HEllo? test")
(defn connect! [state-atom]
  (js/console.log "Connect! 2")
  (when (check-webserial-support)
    (->
     (js/navigator.serial.requestPort)
     (.then (fn [selected-port]
              (reset! port selected-port)
              (.open selected-port #js {:baudRate 9600})))
     (.then (fn []
              (swap! state-atom assoc
                     :serial-connected true
                     :serial-status "Connected")
              (println "Serial port connected!")

              ;; Set up reader
              (let [text-decoder (js/TextDecoder.)
                    readable (.-readable @port)
                    rdr (.getReader readable)]
                (reset! reader rdr)

                ;; Start reading loop
                (letfn [(read-loop []
                          (-> (.read rdr)
                              (.then (fn [result]
                                       (when-not (.-done result)
                                         (let [value (.-value result)
                                               text (.decode text-decoder value)]
                                           (println "Received:" text))
                                         (read-loop))))))]
                  (read-loop)))

              ;; Set up writer
              (let [text-encoder (js/TextEncoder.)
                    writable (.-writable @port)
                    wtr (.getWriter writable)]
                (reset! writer wtr))))
     (.catch (fn [err]
               (swap! state-atom assoc
                      :serial-connected false
                      :serial-status (str "Error: " (.-message err)))
               (println "Error connecting:" err))))))

(defn disconnect! [state-atom]
  (when @port
    (->
     (js/Promise.resolve
      (when @reader
        (.cancel @reader)
        (reset! reader nil))
      (when @writer
        (.releaseLock @writer)
        (reset! writer nil)))
     (.then (fn []
              (.close @port)))
     (.then (fn []
              (reset! port nil)
              (swap! state-atom assoc
                     :serial-connected false
                     :serial-status "Disconnected")
              (println "Serial port disconnected")))
     (.catch (fn [err]
               (println "Error disconnecting:" err))))))

(defn send-data! [data]
  (when @writer
    (let [encoder (js/TextEncoder.)
          encoded (.encode encoder data)]
      (-> (.write @writer encoded)
          (.then (fn []
                   (println "Sent:" data)))
          (.catch (fn [err]
                    (println "Error sending data:" err)))))))
