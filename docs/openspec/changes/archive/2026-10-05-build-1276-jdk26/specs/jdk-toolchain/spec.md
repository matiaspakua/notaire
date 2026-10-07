## Purpose

One JDK version for build, CI and runtime, with pinned container bases. Source: #1276, #1045; owner CU76.

## ADDED Requirements

### Requirement: The toolchain is JDK 26

The system MUST compile with `java.version` 26, run every CI Java setup on 26, and build and run the backend image on JDK 26 bases pinned to a minor or digest.

#### Scenario: Build and CI use JDK 26

- **WHEN** the root `pom.xml` and every workflow are read
- **THEN** `java.version` and every `java-version` or `JAVA_VERSION` is 26

#### Scenario: Docker bases are pinned JDK 26

- **WHEN** `backend-api/Dockerfile` and `Dockerfile.slim` are read
- **THEN** every `maven` and `eclipse-temurin` base is a JDK 26 tag with a minor or digest pin

#### Scenario: The backend builds and runs on JDK 26

- **WHEN** `mvn verify` runs on JDK 26 and the image is built and started
- **THEN** all tests and the coverage gate pass and the health endpoint is UP
