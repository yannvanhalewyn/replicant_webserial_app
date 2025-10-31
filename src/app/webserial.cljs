(ns app.webserial
  (:require [promesa.core :as p]))

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
                (p/then (fn [result]
                          (when-not (.-done result)
                            (let [value (.-value result)
                                  text (.decode text-decoder value)]
                              (println "Received:" text))
                            (read-loop))))))]
      (read-loop))))

(defn connect! []
  (when (webserial-supported?)
    (let [port (js/navigator.serial.requestPort)]
      (-> (.open port #js {:baudRate 9600})
        (p/then
          #(let [reader (.getReader (.-readable port))
                 writer (.getWriter (.-writable port)) ]
             (start-reader-loop! reader)
             {::port port
              ::reader reader
              ::writer writer
              ::encoder (js/TextEncoder.)}))))))

(defn disconnect! [connection]
  (when connection
    (-> (.cancel (::reader connection))
      (p/then #(.releaseLock (::writer connection)))
      (p/then #(.close (::port connection))))))

(defn send-data! [connection data]
  (when-let [writer (::writer connection)]
    (let [encoded (.encode (::encoder connection) data)]
      (-> (.write writer encoded)
        (p/then #(println "Sent:" data))
        (p/catch #(println "Error sending data:" %))))))
