(ns ooapi-tester.core
  (:require
   [clj-http.lite.client :as http]
   [clojure.data.json :as json]
   [clojure.pprint :as pprint]
   [clojure.spec.alpha :as spec]
   [clojure.string :as str]
   [expound.alpha :as expound]
   [ooapi-tester.core :as tester]
   [ooapi-tester.report :as report]
   [ooapi-tester.specs.course]
   [ooapi-tester.specs.education-specification]
   [ooapi-tester.specs.offering]
   [ooapi-tester.specs.program]))

(set! *warn-on-reflection* true)

(def example-opts
  {:gateway "https://gateway.test.surfeduhub.nl"
   :gateway-user "xxx"
   :gateway-password "xxx"
   :schachome "xxx"})

(def data (atom {}))

(defn make-rand-id-fn
  [idkw]
  (fn [response] (get (rand-nth (get response :items)) idkw)))

(def requests
  [{:path "/"
    :doc "The service path is mandatory for all OOAPI endpoints."}
   {:path "/education-specifications"
    :query-params {"consumer" "rio"}
    :needs-items true
    :doc "EducationSpecifications map to OpleidingsEenheden in RIO. Having a path to query all EducationSpecifications meant for RIO is a prerequisite for the migration to RIO."}
   {:path "/education-specifications/{educationSpecificationId}"
    :id-param "educationSpecificationId"
    :depends-on "/education-specifications"
    :rand-id-fn (make-rand-id-fn :educationSpecificationId)
    :spec :ooapi-tester.specs.education-specification/EducationSpecificationTopLevel
    :doc "An EducationSpecification maps to an OpleidingsEenheid in RIO. Having a path to request a single EducationSpecification is a prerequisite for the RIO mapper to work."}
   {:path "/education-specifications/{educationSpecificationId}/education-specifications"
    :query-params {"consumer" "rio"}
    :id-param "educationSpecificationId"
    :depends-on "/education-specifications"
    :rand-id-fn (make-rand-id-fn :educationSpecificationId)
    :doc "This path makes it possible to request nested EducationSpecifications. This allows the RIO mapper to create relations between OpleidingsEenheden in RIO."}
   {:path "/education-specifications/{educationSpecificationId}/programs"
    :query-params {"consumer" "rio"}
    :id-param "educationSpecificationId"
    :depends-on "/education-specifications"
    :rand-id-fn (make-rand-id-fn :educationSpecificationId)
    :doc "Programs map to AangebodenOpleidingen in RIO. This path isn't strictly required, but good practice to implement."}
   {:path "/education-specifications/{educationSpecificationId}/courses"
    :query-params {"consumer" "rio"}
    :id-param "educationSpecificationId"
    :depends-on "/education-specifications"
    :rand-id-fn (make-rand-id-fn :educationSpecificationId)
    :doc "Courses map to AangebodenOpleidingen in RIO. This path isn't strictly required, but good practice to implement."}
   {:path "/programs"
    :query-params {"consumer" "rio"}
    :needs-items true
    :doc "Programs map to AangebodenOpleiding in RIO. Having a path to query all Programs meant for RIO is a prerequisite for the migration to RIO."}
   {:path "/programs/{programId}"
    :id-param "programId"
    :depends-on "/programs"
    :rand-id-fn (make-rand-id-fn :programId)
    :spec :ooapi-tester.specs.program/Program
    :doc "A Program maps to an AangebodenOpleiding in RIO. Having a path to request a single Program is a prerequisite for the RIO mapper to work."}
   {:path "/programs/{programId}/offerings"
    :query-params {"consumer" "rio"}
    :id-param "programId"
    :depends-on "/programs"
    :rand-id-fn (make-rand-id-fn :programId)
    :needs-items true
    :spec :ooapi-tester.specs.offering/OfferingsRequest
    :doc "Offerings map to AangebodenOpleidingCohorten. Having a path to request the Offerings belonging to a Program is a prerequisite for the RIO mapper to work."}
   {:path "/courses"
    :query-params {"consumer" "rio"}
    :needs-items true
    :doc "Courses map to AangebodenOpleiding in RIO. Having a path to query all Courses meant for RIO is  is only necessary if you want to upload course information to RIO."}
   {:path "/courses/{courseId}"
    :id-param "courseId"
    :depends-on "/courses"
    :rand-id-fn (make-rand-id-fn :courseId)
    :spec :ooapi-tester.specs.course/Course
    :doc "A Course maps to an AangebodenOpleiding in RIO. Having a path to request a single Program is only necessary if you want to upload course information to RIO."}
   {:path "/courses/{courseId}/offerings"
    :query-params {"consumer" "rio"}
    :id-param "courseId"
    :depends-on "/courses"
    :rand-id-fn (make-rand-id-fn :courseId)
    :needs-items true
    :spec :ooapi-tester.specs.offering/OfferingsRequest
    :doc "Offerings map to AangebodenOpleidingCohorten. Having a path to request the Offerings belonging to a Course is only necessary if you want to upload course information to RIO."}])

(defn get-rand-id
  [source-path rand-id-fn]
  (rand-id-fn (get-in @data [source-path :response])))

(defn judge
  [response-success? needs-items has-items? spec spec-result]
  (cond
    (not response-success?)
    [:failure "The request was not successfull."]

    (and response-success? needs-items has-items? spec (true? spec-result))
    [:success "We could succesfully get a valid response with items."]
    
    (and response-success? needs-items has-items? spec (false? spec-result))
    [:failure "Response is not valid for RIO"]

    (and response-success? needs-items has-items? (not spec))
    [:success "We could succesfully get a valid response with items."]

    (and response-success? needs-items (not has-items?))
    [:failure "We were expecting a response with at least one item, but got none."]

    (and response-success? (not needs-items) (not spec))
    [:success "We could succesfully get a response"]

    (and response-success? (not needs-items) spec (true? spec-result))
    [:success "We could succesfully get a valid response"]

    (and response-success? (not needs-items) spec (false? spec-result))
    [:failure "Response is not valid for RIO"]))

(defn do-request
  [{:keys [path query-params id-param depends-on spec needs-items rand-id-fn]}
   {:keys [gateway gateway-user gateway-password schachome]}]
  (let [id (when (and id-param depends-on) (get-rand-id depends-on rand-id-fn))
        url (if id
              (str gateway (str/replace path (str "{" id-param "}") id))
              (str gateway path))
        req-opts (cond-> {:headers {"X-Route" (str "endpoint=" schachome)
                                    "X-Validate" "true"
                                    "Accept" "application/json; version=5"} 
                          :basic-auth [gateway-user gateway-password]}

                   query-params
                   (assoc :query-params query-params))]

    (println "---------------------------------------")
    (println (str "GET " url))
    (pprint/pprint (assoc req-opts :basic-auth [gateway-user "REDACTED"]))

    (let [{:keys [status body reason-phrase]} (http/get url req-opts)
          body (json/read-str body :key-fn keyword)
          endpoint-status (when (= status 200)
                            (get-in body [:gateway :endpoints (keyword schachome) :responseCode]))
          response-success? (and (= status 200) (= endpoint-status 200))
          endpoint-response (when response-success?
                              (get-in body [:responses (keyword schachome)]))
          spec-result (when (and endpoint-response spec) (spec/valid? spec endpoint-response))
          has-items? (boolean (not-empty (:items endpoint-response)))
          [success-or-failure message] (judge response-success? needs-items has-items? spec spec-result)]

      (println (str "Gateway response: " status " " reason-phrase))
      (println)

      (let [result (cond-> {:path path
                            :status success-or-failure
                            :message message
                            :code endpoint-status
                            :url (get-in body [:gateway :endpoints (keyword schachome) :url])}

                     spec
                     (assoc
                      :spec-result spec-result
                      :spec-message (expound/expound-str spec endpoint-response)) 
                     
                     endpoint-response
                     (assoc :response endpoint-response))]
        (swap! data assoc path result)
        result))))

(defn dependency-needed?
  [{:keys [depends-on]}]
  (boolean depends-on))

(defn dependency-requested?
  [{:keys [depends-on]}]
  (if depends-on
    (get @data depends-on)
    true))

(defn dependency-successfull?
  [{:keys [depends-on]}]
  (if depends-on
    (let [result (get @data depends-on)]
      (= :success (:status result)))
    true))

(defn into-queue
  [coll]
  (into (clojure.lang.PersistentQueue/EMPTY) coll))

(defn store-results
  [{:keys [schachome] :as opts}]
  (let [filename (str schachome ".html")]
    (spit filename (report/report @data requests opts))
    (println)
    (println (str "Test results were written to " filename))))

(defn validate-endpoint
  [opts]
  (reset! data {})
  (loop [queue (into-queue requests)]
    (if (empty? queue)
      @data
      (let [request (first queue)]
        (cond

          ;; dependency not yet done, move to back
          (and (dependency-needed? request)
               (not (dependency-requested? request)))
          (recur (conj (pop queue) request))

          ;; dependency done, but unsuccessfull
          (and (dependency-needed? request)
               (dependency-requested? request)
               (not (dependency-successfull? request)))
          (do (swap! data assoc (:path request) {:path (:path request)
                                                 :status :skipped
                                                 :message "Request was skipped"})
              (recur (pop queue)))

          ;; dependency done, and successfull
          (and (dependency-needed? request)
               (dependency-requested? request)
               (dependency-successfull? request))
          (do (do-request request opts)
              (recur (pop queue)))

          ;; dependency not needed
          (not (dependency-needed? request))
          (do (do-request request opts)
              (recur (pop queue)))))))
  (store-results opts))

(comment 
  (def opts {:gateway "https://gateway.test.surfeduhub.nl"
             :gateway-user (System/getenv "SURFEDUHUB_USER")
             :gateway-password (System/getenv "SURFEDUHUB_PASSWORD")
             :schachome "dataaccess.test.saxion.nl"})
  
  (validate-endpoint opts)
  (store-results opts)
  )






