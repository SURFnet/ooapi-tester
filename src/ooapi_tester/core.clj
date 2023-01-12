(ns ooapi-tester.core
  (:require [clj-http.client :as http]
            [clojure.pprint :as pprint]
            [clojure.string :as str]))

(set! *warn-on-reflection* true)

(def example-opts
  {:gateway "https://gateway.test.surfeduhub.nl"
   :gateway-user "xxx"
   :gateway-password "xxx"
   :scachome "xxx"})

(def data (atom {}))

(defn make-rand-id-fn
  [idkw]
  (fn [response] (get (rand-nth (get response :items)) idkw)))

(def requests
  [{:path "/"}
   {:path "/education-specifications"
    :query-params {"consumer" "rio"}
    :needs-items true}
   {:path "/education-specifications/{educationSpecificationId}"
    :id-param "educationSpecificationId"
    :depends-on "/education-specifications"
    :rand-id-fn (make-rand-id-fn :educationSpecificationId)}
   {:path "/education-specifications/{educationSpecificationId}/education-specifications"
    :query-params {"consumer" "rio"}
    :id-param "educationSpecificationId"
    :depends-on "/education-specifications"
    :rand-id-fn (make-rand-id-fn :educationSpecificationId)}
   {:path "/education-specifications/{educationSpecificationId}/programs"
    :query-params {"consumer" "rio"}
    :id-param "educationSpecificationId"
    :depends-on "/education-specifications"
    :rand-id-fn (make-rand-id-fn :educationSpecificationId)}
   {:path "/education-specifications/{educationSpecificationId}/courses"
    :query-params {"consumer" "rio"}
    :id-param "educationSpecificationId"
    :depends-on "/education-specifications"
    :rand-id-fn (make-rand-id-fn :educationSpecificationId)}
   {:path "/programs"
    :query-params {"consumer" "rio"}
    :needs-items true}
   {:path "/programs/{programId}"
    :id-param "programId"
    :depends-on "/programs"
    :rand-id-fn (make-rand-id-fn :programId)}
   {:path "/programs/{programId}/offerings"
    :query-params {"consumer" "rio"}
    :id-param "programId"
    :depends-on "/programs"
    :rand-id-fn (make-rand-id-fn :programId)
    :needs-items true}
   {:path "/courses"
    :query-params {"consumer" "rio"}
    :needs-items true}
   {:path "/courses/{courseId}"
    :id-param "courseId"
    :depends-on "/courses"
    :rand-id-fn (make-rand-id-fn :courseId)}
   {:path "/courses/{courseId}/offerings"
    :query-params {"consumer" "rio"}
    :id-param "courseId"
    :depends-on "/courses"
    :rand-id-fn (make-rand-id-fn :courseId)
    :needs-items true}])

(defn get-rand-id
  [source-path rand-id-fn]
  (rand-id-fn (get @data source-path)))

(defn judge
  [response-success? needs-items has-items?]
  (cond
    (not response-success?)
    [:failure "The request was not successfull."]

    (and response-success? needs-items has-items?)
    [:success "We could succesfully get a valid response with items."]

    (and response-success? needs-items (not has-items?))
    [:failure "We were expecting a response with at least one item, but got none."]

    (and response-success? (not needs-items))
    [:success "We could succesfully get a valid response"]))

(defn do-request
  [{:keys [path query-params id-param depends-on needs-items rand-id-fn]}
   {:keys [gateway gateway-user gateway-password schachome]}]
  (let [id (when (and id-param depends-on) (get-rand-id depends-on rand-id-fn))
        url (if id
              (str gateway (str/replace path (str "{" id-param "}") id))
              (str gateway path))
        req-opts (cond-> {:headers {"X-Route" (str "endpoint=" schachome)
                                    "X-Validate" "true"
                                    "Accept" "application/json; version=5"}
                          :as :json
                          :basic-auth [gateway-user gateway-password]}

                   query-params
                   (assoc :query-params query-params))]

    (println "---------------------------------------")
    (println (str "GET " url))
    (pprint/pprint (assoc req-opts :basic-auth [gateway-user "REDACTED"]))

    (let [{:keys [status body reason-phrase]} (http/get url req-opts)
          endpoint-status (when (= status 200)
                            (get-in body [:gateway :endpoints (keyword schachome) :responseCode]))
          response-success? (and (= status 200) (= endpoint-status 200))
          endpoint-response (when response-success?
                              (get-in body [:responses (keyword schachome)]))
          has-items? (boolean (not-empty (:items endpoint-response)))
          [success-or-failure message] (judge response-success? needs-items has-items?)]

      (println (str "Gateway response: " status " " reason-phrase))
      (println)

      (let [result (cond-> {:path path
                            :status success-or-failure
                            :message message
                            :code endpoint-status}

                     (= endpoint-status 200)
                     (assoc :url (get-in body [:gateway :endpoints (keyword schachome) :url]))

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
  [{:keys [schachome]}]
  (let [results (with-out-str (pprint/print-table [:path :status :url :message] (vals @data)))
        filename (str schachome "-results.txt")]
    (spit filename results)
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
  (validate-endpoint example-opts)
  
  
  )




