(ns build
  (:require [clojure.tools.build.api :as b]
            [org.corfield.build :as bb]
            [clojure.pprint :refer [pprint]]
            [clojure.string :as str]
            [lein2deps.api :as l2d]))

(def mapper-dir "eduhub-rio-mapper")
(def lib 'nl.surf/ooapi-tester)
;; if you want a version of MAJOR.MINOR.COMMITS:
(def version (format "0.1.%s" (b/git-count-revs nil)))

(defn init-mapper
  [opts]
  (b/git-process {:git-args ["submodule" "init"]}))

(defn prep-mapper
  [opts]
  (let [sha (str/trim (slurp "mapper-version"))]
    (b/git-process {:git-args ["checkout" sha "--force"]
                    :dir mapper-dir})
    (let [{:keys [deps]} (l2d/lein2deps {:project-clj (str mapper-dir "/project.clj")})
          updated-deps (-> deps
                           (update :paths conj "resources")
                           (dissoc :aliases :deps/prep-lib))]
      (spit (str mapper-dir "/deps.edn") (with-out-str (pprint updated-deps))))))

(defn uberjar "Run the CI pipeline of tests (and build the JAR)."
  [opts]
  (-> opts
      (assoc :lib lib :version version)
      (assoc :main 'ooapi-tester.main)
      (bb/clean)
      (bb/uber)))

(defn native
  [opts]
  (let [graal-vm-home (System/getenv "GRAALVM_HOME")]

    (if graal-vm-home
      (println (str "GRAALVM_HOME: " graal-vm-home))
      (do
        (println "Please set GRAALVM_HOME environment variable")
        (System/exit 1)))

    (b/process {:command-args [(str graal-vm-home "/bin/native-image")
                               "-jar"
                               (bb/default-jar-file lib version)
                               "-H:Name=ooapi-tester"
                               "-H:+ReportExceptionStackTraces"
                               "-H:+PrintClassInitialization"
                               "-H:ResourceConfigurationFiles=./resource-config.json"
                               "--enable-url-protocols=https"
                               "--verbose"
                               "--no-fallback"
                               "--no-server"
                               "-J-Xmx3g"]})))



