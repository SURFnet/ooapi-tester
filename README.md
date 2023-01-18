# OOAPI Tester

This is a simple tool to test an OOAPI endpoint using the SURFeduhub gateway. Right now it tests the following cases:

| Path                                                                            | Test                                                                                      |
| ------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------- |
| `/`                                                                             | Response validation                                                                       |
| `/educations-specifications`                                                    | Response validation, tests if at least one item can be found                              |
| `/education-specifications/{educationSpecificationId}`                          | Response validation, uses an id from `/education-specifications`                          |
| `/education-specifications/{educationSpecificationId}/education-specifications` | Response validation                                                                       |
| `/education-specifications/{educationSpecificationId}/programs`                 | Response validation                                                                       |
| `/education-specifications/{educationSpecificationId}/courses`                  | Response validation                                                                       |
| `/programs`                                                                     | Response validation, tests if at least one item can be found                              |
| `/programs/{programId}`                                                         | Response validation, uses an id from `/programs`                                          |
| `/programs/{programId}/offerings`                                               | Response validation, uses an id from `/programs`, tests if at least one item can be found |
| `/courses`                                                                      | Response validation, tests if at least one item can be found                              |
| `/courses/{courseId}`                                                           | Response validation, uses an id from `/courses`                                           |
| `/courses/{courseId}/offerings`                                                 | Response validation, uses an id from `/courses`, tests if at least one item can be found  |

Response validation is performed by sending the `X-Validate: true` header to the gateway.

## Usage

1. Make sure you have java installed and on your `PATH`
2. Make sure you have the `SURFEDUHUB_USER` and `SURFEDUHUB_PASSWORD` environment variables set.
3. Run `ooapi-tester --schachome <schachome>`.

## Building and packaging

1. Run `clj -T:build uberjar` to create an uberjar
2. Run `clj -T:build package` to package the uberjar as an executable.

