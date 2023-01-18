(ns ooapi-tester.specs.enums)

(def codeTypes
  "Enum used in EducationSpecification for primaryCode and otherCodes."
  #{"brin" "crohoCreboCode" "programCode" "componentCode" "offeringCode" "organizationId" "buildingId" "bagId" "roomCode" "systemId" "productId" "nationalIdentityNumber" "studentNumber" "studielinkNumber" "esi" "userName" "accountId" "emailAdress" "groupCode" "isbn" "issn" "orcId" "uuid" "schacHome" "identifier"})

(def modesOfDelivery
  "Enum used in Offerings for modeOfDelivery."
  #{"distance-learning" "on campus" "online" "hybrid" "situated"})

(def modeOfStudy
  "Enum used in Programs for modeOfStudy."
  #{"full-time" "part-time" "dual training"})

(def programType
  "Enum used in Programs for programType."
  #{"program" "minor" "honours" "specialization" "track"})

(def educationSpecificationTypes
  "Enum used in EducationSpecification for educationSpecificationType."
  #{"program" "privateProgram" "cluster" "course"})

(def formalDocumentTypes
  "Enum used in EducationSpecification for formalDocument."
  #{"diploma" "certificate" "no official document" "testimonial" "school advice"})

(def levels
  "Enum used in EducationSpecification for level."
  #{"secondary vocational education" "secondary vocational education 1" "secondary vocational education 2" "secondary vocational education 3" "secondary vocational education 4" "associate degree" "bachelor" "master" "doctoral" "undefined" "undivided" "nt2-1" "nt2-2"})

(def sectors
  "Enum used in EducationSpecification for sector."
  #{"secondary vocational education" "higher professional education" "university education"})

(def studyLoadUnits
  "Enum used in EducationSpecification for studyLoad."
  #{"contacttime" "ects" "sbu" "sp" "hour"})