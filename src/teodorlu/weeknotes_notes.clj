(ns teodorlu.weeknotes-notes
  (:require
   [clj-reload.core :as clj-reload]
   [clj-simple-router.core :as clj-simple-router]
   [nextjournal.garden-email :as garden-email]
   [nextjournal.garden-id :as garden-id]
   [org.httpkit.server :as server]
   [ring.middleware.params :as ring.params]
   [ring.middleware.session :as session]
   [ring.middleware.session.cookie :refer [cookie-store]]
   [teodorlu.weeknotes-notes.path :as path]
   [teodorlu.weeknotes-notes.ui :as ui]))

;; Design
;;
;; - a text field for putting weeknotes
;; - a way to extract this week's weeknotes in org-mode or markdown

(defn garden-storage []
  (or (System/getenv "GARDEN_STORAGE") ".local/garden-storage"))

(def app
  (clj-simple-router/router
   {(str "HEAD " path/root)
    {:status 200
     :headers {"content-type" "text/html"}
     :body "Ready!"}

    (str "GET " path/root)
    ui/page-index}))

(def wrapped-app
  (-> app
      ;; garden-email
      (ring.params/wrap-params)
      (garden-email/wrap-with-email #_{:on-receive (fn [email] (println "Got mail"))})
      ;; garden-id
      (garden-id/wrap-auth #_{:github [{:team "nextjournal"}]})
      (session/wrap-session {:store (cookie-store)})))

(defn start! [opts]
  (let [server (server/run-server #'wrapped-app
                                  (merge {:legacy-return-value? false
                                          :host "0.0.0.0"
                                          :port 7777}
                                         opts))]
    (println (format "server started: http://localhost:%s"
                     (server/server-port server)))))

(defonce dev-server (atom nil))

(comment
  (clj-reload/reload)

  (do (when-let [s @dev-server]
        (s))
      (reset! dev-server (start! {:port 7196})))

  :rcf)

#_(start! {:port 7108})