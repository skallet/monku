(ns monku.server.core
  (:require [org.httpkit.server :as hk-server]))

(defprotocol Server
  (start [s opts] "Start server with given options.")
  (stop [s opts] "Stop server with given options."))

(defn default-handler [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "Hello from server!"})

(defn- run-server
  [{:keys [port
           handler]
    :or   {port    8080
           handler #'default-handler}}]
  (hk-server/run-server handler
                        {:port port}))

(defn- stop-server
  [server-shutdown-fn & {:keys [timeout-ms]
                         :or   {timeout-ms 100}}]
  (server-shutdown-fn :timeout timeout-ms))

(defn create-server
  "Create `Server` instance."
  []
  (let [inst (atom nil)]
    (reify Server
      (start [this opts]
        (stop this {})
        (reset! inst (apply run-server [opts])))
      (stop [_this opts]
        (when-let [shutdown-fn @inst]
          (apply stop-server [shutdown-fn opts])
          (reset! inst nil))))))
