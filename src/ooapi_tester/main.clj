(ns ooapi-tester.main
  (:require
   [ooapi-tester.core :as tester]
   [babashka.cli :as cli]
   [clojure.string :as str])
  (:gen-class))

(set! *warn-on-reflection* true)

(def spec
  {:gateway   {:ref          "<gateway url>"
               :desc         "The SURFeduhub gateway url to use. Should be a valid url without a trailing slash."
               :coerce       :string
               :default-desc "https://gateway.test.surfeduhub.nl"
               :default      "https://gateway.test.surfeduhub.nl"}
   :schachome     {:ref          "<schachome>"
                   :desc         "The schachome of the OOAPI endpoint to test."
                   :coerce       :string
                   :require true}})

(def gateway-user (System/getenv "SURFEDUHUB_USER"))
(def gateway-password (System/getenv "SURFEDUHUB_PASSWORD"))

(defn -main
  [& args]
  (when (empty? args)
    (println "Missing required arguments. Try --help.")
    (System/exit 1))

  (when (= (first args) "--help")
    (println
     "Simple OOAPI Tester\n
      OOAPI tester relies on two required environment variables: SURFEDUHUB_USER and SURFEDUHUB_PASSWORD\n
      Usage:\n"
     (cli/format-opts {:spec spec :order [:schachome :gateway]}))
    (System/exit 0))

  (when (str/blank? gateway-user)
    (println "Missing required environment variable: SURFEDUHUB_USER")
    (System/exit 1))

  (when (str/blank? gateway-password)
    (println "Missing required environment variable: SURFEDUHUB_PASSWORD")
    (System/exit 1))

  (let [opts (cli/parse-opts args {:spec spec})]
    (tester/validate-endpoint (assoc opts :gateway-user gateway-user :gateway-password gateway-password))))

