(ns monku.server.core
  (:require [org.httpkit.server :as hk-server]))

(defprotocol Server
  (start [s] "Start server with given options.")
  (stop [s] "Stop server with given options."))

(defn default-handler [_req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "Hello from server!"})

(defn- run-server
  [{:keys [port
           handler]}]
  (hk-server/run-server handler
                        {:port port}))

(defn- stop-server
  [server-shutdown-fn & {:keys [timeout-ms]}]
  (server-shutdown-fn :timeout timeout-ms))

(defn create-server
  "Create `Server` instance."
  [& {:keys [port
             handler
             stop-timeout-ms]
      :or   {port            8080
             handler         #'default-handler
             stop-timeout-ms 100}}]
  (let [inst         (atom nil)
        stop-handler (fn [server-shutdown-fn]
                       (when (fn? server-shutdown-fn)
                         (stop-server server-shutdown-fn
                                      {:timeout-ms stop-timeout-ms})))]
    (reify Server
      (start [_]
        (try
          (swap! inst (fn [current]
                        (stop-handler current)
                        (run-server {:port    port
                                     :handler handler})))
          (catch Exception e
            (ex-info "Failed to start server"
                     {:port port}
                     e))))
      (stop [_]
        (swap! inst (fn [current]
                      (stop-handler current)
                      nil))))))
