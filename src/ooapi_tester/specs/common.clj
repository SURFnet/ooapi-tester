(ns ooapi-tester.specs.common
  "Common specs for use in the ooapi namespaces."
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [ooapi-tester.specs.common.LongLanguageTypedString :as-alias LongLanguageTypedString]
            [ooapi-tester.specs.enums :as enums]
            [ooapi-tester.specs.LanguageTypedString :as-alias LanguageTypedString]
            [ooapi-tester.specs.LanguageTypedStringEN :as-alias LanguageTypedStringEN]
            [ooapi-tester.specs.LanguageTypedStringNL :as-alias LanguageTypedStringNL]
            [ooapi-tester.specs.StudyLoadDescriptor :as-alias StudyLoadDescriptor]
            [ooapi-tester.specs.re-spec :refer [re-spec text-spec looks-like-html?]])
  (:import (java.time LocalDate)
           (java.time.format DateTimeFormatter DateTimeParseException)
           (java.util UUID)))


(def date-format (DateTimeFormatter/ofPattern "uuuu-MM-dd"))

(defn get-localized-value
  "Get the first value of a LanguageTypedString where the language code matches the locale. The provided locales are tried in order."
  [attr locales]
  (->> locales
       (keep (fn [locale] (some #(when (string/starts-with? (% :language) locale) (% :value))
                                attr)))
       first))

(defn extract-rio-consumer
  "Find the first consumer with a consumerKey equal to 'rio' or return nil."
  [consumers]
  (some->> consumers
           (filter #(= (:consumerKey %) "rio"))
           first))

(defn valid-date? [date]
  (and (string? date)
       (try (let [d (LocalDate/parse date date-format)]
              ;; XSD schema does not accept "Year zero".
              (not (zero? (.getYear d))))
            (catch DateTimeParseException _ false))))

(s/def ::date
  (s/and (re-spec #"\d\d\d\d-[01]\d-[0123]\d")
         valid-date?))

(s/def ::duration
  (re-spec #"^P(\d+Y)?(\d+M)?(\d+W)?(\d+D)?(T(\d+H)?(\d+M)?(\d+S)?)?$"))

(defn valid-uuid? [uuid]
  (try (UUID/fromString uuid)
       true
       (catch IllegalArgumentException _ false)))

(s/def ::uuid
  (s/and (re-spec #"^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
         valid-uuid?))

;; Common types

(s/def ::LanguageTypedString/language
  (re-spec #"^[a-z]{2,4}(-[A-Z][a-z]{3})?(-([A-Z]{2}|[0-9]{3}))?$"))

(s/def ::LanguageTypedString/value
  (text-spec 1 1000))

(s/def ::LanguageTypedString
  (s/keys :req-un [::LanguageTypedString/language
                   ::LanguageTypedString/value]))

(s/def ::LanguageTypedStringNL/language
  #{"nl-NL" "nl-BE"})

(s/def ::LanguageTypedStringEN/language
  #{"en-AU"
    "en-BZ"
    "en-CA"
    "en-CB"
    "en-GB"
    "en-IE"
    "en-JM"
    "en-NZ"
    "en-PH"
    "en-TT"
    "en-US"
    "en-ZA"
    "en-ZW"})

(s/def ::nlLanguageTypedString
  (s/keys :req-un [::LanguageTypedStringNL/language
                   ::LanguageTypedString/value]))

(s/def ::enLanguageTypedString
  (s/keys :req-un [::LanguageTypedStringEN/language
                   ::LanguageTypedString/value]))

;; A collection of language typed strings with any set of languages
(s/def ::LanguageTypedStrings
  (s/coll-of ::LanguageTypedString))

(s/def ::LongLanguageTypedString/language
  (re-spec #"^[a-z]{2,4}(-[A-Z][a-z]{3})?(-([A-Z]{2}|[0-9]{3}))?$"))

(s/def ::LongLanguageTypedString/value
  (text-spec 1 3000))

(s/def ::LongLanguageTypedString
  (s/keys :req-un [::LongLanguageTypedString/language
                   ::LongLanguageTypedString/value]))

;; A collection of language typed strings with any set of languages
(s/def ::LongLanguageTypedStrings
  (s/coll-of ::LongLanguageTypedString))

;; A collection of language typed strings with at least one dutch entry
(s/def ::nlLanguageTypedStrings
  (s/cat :head (s/* ::LanguageTypedString)
         :nl ::nlLanguageTypedString
         :tail (s/* ::LanguageTypedString)))

;; A collection of language typed strings with at least one english entry
(s/def ::enLanguageTypedStrings
  (s/cat :head (s/* ::LanguageTypedString)
         :en ::enLanguageTypedString
         :tail (s/* ::LanguageTypedString)))

(s/def ::codeType
  (s/or :predefined enums/codeTypes
        :custom (re-spec #"x-[\w.]+")))
(s/def ::code string?)
(s/def ::codeTuple
  (s/keys :req-un [::codeType ::code]))
(s/def ::otherCodes (s/coll-of ::codeTuple))

(s/def ::StudyLoadDescriptor/value number?)
(s/def ::StudyLoadDescriptor/studyLoadUnit enums/studyLoadUnits)
(s/def ::studyLoad (s/keys :req-un [::StudyLoadDescriptor/studyLoadUnit ::StudyLoadDescriptor/value]))

;; XSD says 0-999 for ISCED, so broad/narrow fields.  OOAPI spec def
;; is 4 digits, so it accepts detailed fields. See also
;; `nl.surf.eduhub-rio-mapper.rio/narrow-isced`
(s/def ::fieldsOfStudy (re-spec #"\d{1,4}"))
(s/def ::learningOutcomes (s/coll-of ::LanguageTypedStrings))
(s/def ::level enums/levels)
(s/def ::levelOfQualification #{"1" "2" "3" "4" "4+" "5" "6" "7" "8"})

(s/def ::modeOfDelivery
  (s/and (s/coll-of enums/modesOfDelivery)
         ;; for RIO at least one of the below is required
         #(some #{"online" "hybrid" "situated"} %)))

(s/def ::sector enums/sectors)

(defn level-sector-mapping
  "Map level and sector to RIO `niveau`.
  Returns nil on invalid level+sector mapping."
  [level sector]
  (case level
    "undefined" "ONBEPAALD"
    "nt2-1" "NT2-I"
    "nt2-2" "NT2-II"
    (case sector
      "secondary vocational education"
      (case level
        "secondary vocational education" "MBO"
        "secondary vocational education 1" "MBO-1"
        "secondary vocational education 2" "MBO-2"
        "secondary vocational education 3" "MBO-3"
        "secondary vocational education 4" "MBO-4"
        nil)

      "higher professional education"
      (case level
        "associate degree" "HBO-AD"
        "bachelor" "HBO-BA"
        "master" "HBO-MA"
        "doctoral" "HBO-PM"
        "undivided" "HBO-O"
        nil)

      "university education"
      (case level
        "bachelor" "WO-BA"
        "master" "WO-MA"
        "doctoral" "WO-PM"
        "undivided" "WO-O"
        nil)
      nil)))

(defn level-sector-map-to-rio?
  "True if we can map the given level and sector to RIO."
  [{:keys [level sector]}]
  (some? (level-sector-mapping level sector)))

;; Address
(s/def ::additional any?)
(s/def ::addressType #{"postal" "visit" "deliveries" "billing" "teaching"})
(s/def ::city (text-spec 1 40))
(s/def ::countryCode (re-spec #"[a-zA-Z]{2}"))
(s/def ::geolocation (s/keys :req-un [::latitude ::longitude]))
(s/def ::latitude number?)
(s/def ::longitude number?)
;; Dutch postcode format, since that's what RIO accepts. Note that in
;; rio, the postcalCode should not contain any whitespace.
(s/def ::postalCode (re-spec #"[1-9]\d{3}\s*[A-Z]{2}"))

;; note that the `street` address field is never used in RIO, so not
;; specced for the mapper

;; The streetNumber in OOAPI is a number + "huisletter"
;; of "nummertoevoeging". In RIO huisnummers can be 1 - 99999 and
;; toevoegingen are 6 chars, separated by dash or space.

(s/def ::streetNumber
  (s/or :num int?
        :str (s/and (re-spec #"[1-9]\d{0,3}((-| )\S{1,6})?")
                    #(not (looks-like-html? %)))))

(s/def ::address (s/keys :req-un [::addressType]
                         :opt-un [::additional
                                  ::city
                                  ::countryCode
                                  ::geolocation
                                  ::postalCode
                                  ::streetNumber]))
(s/def ::addresses (s/coll-of ::address))