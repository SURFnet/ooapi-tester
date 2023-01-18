(ns ooapi-tester.specs.offering
  (:require [clojure.spec.alpha :as s]
            [ooapi-tester.specs.common :as common]
            [ooapi-tester.specs.Offering :as-alias Offering]))

(s/def ::Offering/consumer (s/keys))
(s/def ::Offering/priceInformationItem (s/keys))

(s/def ::Offering/offeringId ::common/uuid)
(s/def ::Offering/endDate ::common/date)
(s/def ::Offering/startDate ::common/date)
(s/def ::Offering/modeOfDelivery ::common/modeOfDelivery)
(s/def ::Offering/enrollStartDate ::common/date)
(s/def ::Offering/enrollEndDate ::common/date)
(s/def ::Offering/maxNumberStudents number?)
(s/def ::Offering/priceInformation (s/coll-of ::Offering/priceInformationItem))
(s/def ::Offering/consumers (s/coll-of ::Offering/consumer))
(s/def ::Offering/flexibleEntryPeriodStart ::common/date)
(s/def ::Offering/flexibleEntryPeriodEnd ::common/date)

(s/def ::Offering
  (s/keys :req-un [::Offering/offeringId
                   ::Offering/endDate
                   ::Offering/startDate
                   ::Offering/modeOfDelivery]
          :opt-un [::Offering/enrollStartDate
                   ::Offering/enrollEndDate
                   ::Offering/maxNumberStudents
                   ::Offering/priceInformation
                   ::Offering/consumers
                   ::Offering/flexibleEntryPeriodStart
                   ::Offering/flexibleEntryPeriodEnd]))

(s/def ::Offering/items (s/coll-of ::Offering))

(s/def ::OfferingsRequest
  (s/keys :req-un [::Offering/items]))