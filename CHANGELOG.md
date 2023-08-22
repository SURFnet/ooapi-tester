# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added 

### Fixed

## [0.1.27] - 2023-08-22

### Added
- Updated the specs the mapper uses to validate

## [0.1.26] - 2023-06-28

### Added
- Updated the specs the mapper uses to validate
- Added a summary about "RIO Readiness" to the report

## [0.1.21] - 2023-02-01

### Fixed
- Release the JAR in CI/CD pipeline.

## [0.1.18] - 2023-02-01

### Added
- A Changelog.

### Changed

- The `education-specifications/{educationSpecificationId}` and `/courses/{courseId}` paths are now tested using the `returnTimelineOverrides=true` query parameter as they should be.

## [0.1.17] - 2023-01-32

First public release

### Added

- Native binaries for MacOS, Windows and Linux.
- CI/CD pipeline.
- Basic functionality for testing an OOAPI endpoint.
- Uses the same specs as the RIO mapper.
- Generates a nice HTML report
