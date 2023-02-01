# OOAPI Tester

This is a simple tool to test an OOAPI endpoint using the SURFeduhub gateway. Right now it only tests whether an endpoint is compatible with the [RIO](https://www.rio-onderwijs.nl/) functionality of [SURFeduhub](https://www.surf.nl/surfeduhub). It tests if the following paths work and provide the correct responses:

| Path                                                                                | Test                                                                                      |
| ----------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------- |
| `/`                                                                                 | Response validation                                                                       |
| `/educations-specifications`                                                        | Response validation, tests if at least one item can be found                              |
| `/education-specifications/{educationSpecificationId}?returnTimelineOverrides=true` | Response validation, uses an id from `/education-specifications`                          |
| `/education-specifications/{educationSpecificationId}/education-specifications`     | Response validation                                                                       |
| `/education-specifications/{educationSpecificationId}/programs`                     | Response validation                                                                       |
| `/education-specifications/{educationSpecificationId}/courses`                      | Response validation                                                                       |
| `/programs`                                                                         | Response validation, tests if at least one item can be found                              |
| `/programs/{programId}?returnTimelineOverrides=true`                                | Response validation, uses an id from `/programs`                                          |
| `/programs/{programId}/offerings`                                                   | Response validation, uses an id from `/programs`, tests if at least one item can be found |
| `/courses`                                                                          | Response validation, tests if at least one item can be found                              |
| `/courses/{courseId}?returnTimelineOverrides=true`                                  | Response validation, uses an id from `/courses`                                           |
| `/courses/{courseId}/offerings`                                                     | Response validation, uses an id from `/courses`, tests if at least one item can be found  |

Response validation is performed using the specs defined in the [RIO Mapper repository](https://github.com/jomco/eduhub-rio-mapper). The OOAPI tester assumes your endpoint is connected to SURFeduhub and that you have valid access credentials. You should also know your `schachome` (usually the domain name of your endpoint).

## Usage
OOAPI tester is provided in several formats:
- An Uberjar that you can run using Java.
- Native binaries for Windows, MacOS and Linux.

### Native binaries
1. Download the version you want to use from the [Github Releases page](releases).
2. Move the file somewhere convenient.
3. If you downloaded one of the native binaries, rename it to `ooapi-tester` or `ooapi-tester.exe` (if you are on Windows).
4. On MacOS or Linux: make it executable: `chmod +x ooapi-tester`.
5. Make sure you have the `SURFEDUHUB_USER` and `SURFEDUHUB_PASSWORD` environment variables set. On MacOS and Linux you can do the following: `export SURFEDUHUB_USER=<your username>` and `export SURFEDUHUB_PASSWORD=<your password>`. In a Windows Powershell session: `$Env:SURFEDUHUB_USER = "<your username>"` and `$Env:SURFEDUHUB_PASSWORD = "<your password>"`.
6. Run it: `./ooapi-tester --schachome <your schachome here>`.
7. OOAPI Tester will create an HTML report in the current working directory.

### Java Uberjar
1. Make sure you have Java installed and on your `PATH`.
2. Download the jar from the [Github Releases page](releases).
3. Move the jar somewhere convenient.
4. Make sure you have the `SURFEDUHUB_USER` and `SURFEDUHUB_PASSWORD` environment variables set. On MacOS and Linux you can do the following: `export SURFEDUHUB_USER=<your username>` and `export SURFEDUHUB_PASSWORD=<your password>`. In a Windows Powershell session: `$Env:SURFEDUHUB_USER = "<your username>"` and `$Env:SURFEDUHUB_PASSWORD = "<your password>"`.
5. Run it: `java -jar ooapi-tester.jar --schachome <your schachome here>`.
6. OOAPI Tester will create an HTML report in the current working directory.

## Building and packaging
You'll need Java and [Clojure](https://clojure.org/guides/getting_started) to develop and build the uberjar. If you want to compile to native binaries, you'll also need [GraalVM](https://www.graalvm.org/downloads/)

1. Run `git submodule init` to pull in the RIO Mapper repository.
2. Run `clj -T:build init-mapper` once to initialize git submodules.
3. Run `clj -T:build prep-mapper` to prepare the submodule (checks out proper version and generates `deps.edn` from `project.clj`)
4. Run `clj -T:build uberjar` to create an uberjar.
5. Run `clj -T:build native` to compile the uberjar as a native binary.

## License
Distributed under the  GNU GPLv3 License. See LICENSE for more information.
