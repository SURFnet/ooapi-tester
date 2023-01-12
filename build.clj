(ns build
  (:require [clojure.tools.build.api :as b]
            [org.corfield.build :as bb]
            [clj-bin.bin :as bin]))

(def lib 'nl.surf/ooapi-tester)
;; if you want a version of MAJOR.MINOR.COMMITS:
(def version (format "0.1.%s" (b/git-count-revs nil)))

(defn uberjar "Run the CI pipeline of tests (and build the JAR)."
  [opts]
  (-> opts
      (assoc :lib lib :version version)
      (assoc :main 'ooapi-tester.main)
      (bb/clean)
      (bb/uber)))

(defn package
  [opts]
  (bin/bin
   (bb/default-jar-file lib version)
   (str (bb/default-target) "/ooapi-tester")
   (assoc opts :custom-preamble "#!/bin/sh\nexec java -jar $0 \"$@\"\n")))

