(ns datahike-node
  (:require [datahike.store :refer [empty-store delete-store connect-store default-config config-spec release-store store-identity]]
            [environ.core :refer [env]]
            [konserve.node-filestore :as fs]
            [zufall.core :refer [rand-german-mammal]]
            [clojure.spec.alpha :as s]))

(def path (js/require "path"))
(def os (js/require "os"))

(defmethod store-identity :file [config]
  [:file (:scope config) (:path config)])

(defmethod empty-store :file [{:keys [path config]}]
  (fs/connect-fs-store path :opts {:sync? true} :config config))

(defmethod delete-store :file [{:keys [path]}]
  (fs/delete-store path))

(defmethod connect-store :file [{:keys [path config]}]
  (fs/connect-fs-store path :opts {:sync? true} :config config))

(defn get-host-address []
  (some (fn [[_ addrs]]
          (some #(when (= "IPv4" (.-family %)) (.-address %)) addrs))
        (js/Object.entries (.networkInterfaces os))))

(defmethod default-config :file [config]
  (merge
    {:path (:datahike-store-path env (str (.cwd js/process) "/datahike-db-" (rand-german-mammal)))
     :scope (get-host-address)}
    config))

(s/def :datahike.store.file/path string?)
(s/def :datahike.store.file/backend #{:file})
(s/def :datahike.store.file/scope string?)
(s/def ::file (s/keys :req-un [:datahike.store.file/backend
                               :datahike.store.file/path
                               :datahike.store.file/scope]))

(defmethod config-spec :file [_] ::file)