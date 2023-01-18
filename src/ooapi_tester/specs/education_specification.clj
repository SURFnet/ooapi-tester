(ns ooapi-tester.specs.education-specification
  (:require [clojure.spec.alpha :as s]
            [ooapi-tester.specs.common :as common]
            [ooapi-tester.specs.EducationSpecification :as-alias EducationSpecification]
            [ooapi-tester.specs.enums :as enums]
            [ooapi-tester.specs.re-spec :refer [text-spec]]))

;; Top level response keys
(s/def ::EducationSpecification/abbreviation (text-spec 1 255))
(s/def ::EducationSpecification/children (s/coll-of ::common/uuid))
(s/def ::EducationSpecification/description ::common/LongLanguageTypedStrings)
(s/def ::EducationSpecification/educationSpecificationId ::common/uuid)
(s/def ::EducationSpecification/educationSpecificationSubType #{"variant"})
(s/def ::EducationSpecification/educationSpecificationType
  enums/educationSpecificationTypes)

(s/def ::EducationSpecification/formalDocument enums/formalDocumentTypes)
(s/def ::EducationSpecification/name ::common/nlLanguageTypedStrings)
(s/def ::EducationSpecification/link (text-spec 1 2048))
(s/def ::EducationSpecification/parent ::common/uuid)
(s/def ::EducationSpecification/primaryCode ::common/codeTuple)
(s/def ::EducationSpecification/validFrom ::common/date)
(s/def ::EducationSpecification/validTo ::common/date)

(defn valid-type-and-subtype?
  "EducationSpecification should only have subType if type is 'program'."
  [{:keys [educationSpecificationType consumers]}]
  (let [{:keys [educationSpecificationSubType] :as rio-consumer} (common/extract-rio-consumer consumers)]
    (or (and (= educationSpecificationType "program")
             (= educationSpecificationSubType "variant"))
        (not (contains? rio-consumer :educationSpecificationSubType)))))

(s/def ::EducationSpecification/category (s/coll-of string?))
(s/def ::EducationSpecification/rio-consumer
  (s/keys :opt-un [::EducationSpecification/educationSpecificationSubType
                   ::EducationSpecification/category]))

(s/def ::EducationSpecification/consumerKey (s/and string? #(not= % "rio")))
(s/def ::EducationSpecification/other-consumer (s/keys :req-un [::EducationSpecification/consumerKey]))
(s/def ::EducationSpecification/consumer (s/or :other ::EducationSpecification/other-consumer :rio ::EducationSpecification/rio-consumer))
(s/def ::EducationSpecification/consumers (s/coll-of ::EducationSpecification/consumer))

(s/def ::EducationSpecification
  (s/and (s/keys :req-un
                 [::EducationSpecification/educationSpecificationType
                  ::EducationSpecification/name
                  ::EducationSpecification/educationSpecificationId
                  ::EducationSpecification/primaryCode]
                 :opt-un
                 [::EducationSpecification/abbreviation
                  ::EducationSpecification/children
                  ::EducationSpecification/consumers
                  ::EducationSpecification/description
                  ::EducationSpecification/educationSpecificationSubType
                  ::EducationSpecification/formalDocument
                  ::EducationSpecification/link
                  ::EducationSpecification/parent
                  ::common/fieldsOfStudy
                  ::common/learningOutcomes
                  ::common/level
                  ::common/levelOfQualification
                  ::common/otherCodes
                  ::common/sector
                  ::common/studyLoad])
         valid-type-and-subtype?
         common/level-sector-map-to-rio?))

(s/def ::EducationSpecificationTopLevel
  (s/merge ::EducationSpecification
           (s/keys :req-un [::EducationSpecification/validFrom]
                   :opt-un [::EducationSpecification/validTo])))
