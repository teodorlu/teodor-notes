(ns weeknotes-notes.main
  (:require
   [clojure.string :as str]
   [integrant.core :as ig]
   [weeknotes-notes.core :as core]))

;; Production main entrypoint
;; `deps.edn` refers here

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn main
  "Application entrypoint for use with clojure -X"
  [opts]
  (require 'weeknotes-notes.system)
  (let [config
        (cond-> (core/default-config)
          (not (str/blank? (System/getenv "GARDEN_STORAGE")))
          (assoc-in [:weeknotes-notes/store :root]
                    (str (System/getenv "GARDEN_STORAGE")
                         "/edn-store"))
          (:port opts)
          (assoc-in [:weeknotes-notes/http-server :port] (:port opts)))]
    (ig/init config)))
