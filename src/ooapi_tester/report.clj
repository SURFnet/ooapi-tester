(ns ooapi-tester.report
  (:require 
   [hiccup.page :as page]
   [cheshire.core :as json]))

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
  [{:keys [path url status code message response]} {:keys [doc]} opts]
  [:section
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
      [:pre [:code {} (json/generate-string response {:pretty true})]]])])

(defn summary
  [data requests]
  [:table
   [:tr
    [:th "Path"]
    [:th "Status"]]
   (for [request requests]
     (let [row (get data (:path request))]
       [:tr
        [:td (:path row)]
        [:td (simple-status (:status row))]]))])

(defn report
  [data requests opts]
  (page/html5 [:head
               [:meta {:charset "UTF-8"}]
               [:meta {:name "viewport", :content "width=device-width, initial-scale=1.0"}]
               [:meta {:http-equiv "X-UA-Compatible", :content "ie=edge"}]
               [:title (str "OOAPI Report - " (:schachome opts))]
               [:style (slurp "resources/simple.min.css")]]
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