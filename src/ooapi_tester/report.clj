(ns ooapi-tester.report
  (:require 
   [hiccup.page :as page]
   [clojure.data.json :as json]
   [clojure.java.io :as io]
   [clojure.string :as str]))

(set! *warn-on-reflection* true)

(defn path->id
  [path]
  (if (= path "/")
    "root"
    (-> path
        (subs 1)
        (str/replace "{" "")
        (str/replace "}" "")
        (str/replace "/" "-"))))

(defn enrich-status
  [status-kw]
  (case status-kw
    :success "success ✅"
    :failure "failure ❌"
    :skipped "skipped ↪️"))

(defn simple-status
  [status-kw]
  (case status-kw
    :success "✅"
    :failure "❌"
    :skipped "↪️"))

(defn path-report
  [{:keys [path url status code message response spec-message]} {:keys [doc]} opts]
  [:section {:id (path->id path)}
   [:h3 path]
   [:i doc]
   [:table
    [:tr [:td "URL"] [:td [:code url]]]
    [:tr [:td "Status"] [:td (enrich-status status)]]
    [:tr [:td "Code"] [:td [:code code]]]
    [:tr [:td "Message"] [:td message]]]
   (when response
     [:details
      [:summary "Response"]
      [:pre [:code {} (with-out-str (json/pprint response))]]])
   
   (when spec-message
     [:details
      [:summary "Validation message"]
      [:pre [:code {} spec-message]]])])

(defn summary
  [data requests]
  [:table
   [:tr
    [:th "Path"]
    [:th "Status"]]
   (for [request requests]
     (let [row (get data (:path request))
           path (:path row)]
       [:tr
        [:td [:a {:href (str "#" (path->id path))} path]]
        [:td (simple-status (:status row))]]))])

(defn report
  [data requests opts]
  (page/html5 [:head
               [:meta {:charset "UTF-8"}]
               [:meta {:name "viewport", :content "width=device-width, initial-scale=1.0"}]
               [:meta {:http-equiv "X-UA-Compatible", :content "ie=edge"}]
               [:title (str "OOAPI Report - " (:schachome opts))]
               [:style (slurp (io/resource "simple.min.css"))]]
              [:body
               [:header
                [:h1 "OOAPI Report"]
                [:code (:schachome opts)]]
               [:main
                [:section
                 [:h2 "Summary"]
                 (summary data requests)]
                (for [request requests]
                  (path-report (get data (:path request)) request opts))]]))
