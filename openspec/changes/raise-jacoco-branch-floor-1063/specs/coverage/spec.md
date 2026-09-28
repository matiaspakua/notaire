# Coverage Specification

## Purpose
Defines JaCoCo coverage thresholds and validation for the Notaire project.

## ADDED Requirements

### Requirement: JaCoCo SHALL enforce minimum coverage thresholds via pom.xml
Any Spring Boot module with JPA entities or service code SHALL declare
minimum coverage thresholds in `pom.xml` JaCoCo plugin configuration.

#### Scenario: Line coverage floor is enforced
- **WHEN** the JaCoCo plugin is configured with a `<minimum>` line value
  in `pom.xml` and `mvn jacoco:check` is run
- **THEN** the build fails if line coverage is below the threshold

#### Scenario: Branch coverage floor is enforced
- **WHEN** the JaCoCo plugin is configured with a `<minimum>` branch value
  in `pom.xml` and `mvn jacoco:check` is run
- **THEN** the build fails if branch coverage is below the threshold

#### Scenario: Floor is at least 80% line / 65% branch
- **WHEN** running `mvn test -pl backend-api -Dtest=JacocoCoverageConfigConsistencyTest#shouldEnforceRaisedCoverageFloor`
- **THEN** the test reads the LINE and BRANCH `<minimum>` values from `backend-api/pom.xml`
  and passes only if LINE >= 0.80 and BRANCH >= 0.65

#### Scenario: Threshold values are proven by successful build
- **WHEN** the build passes `mvn jacoco:check -pl backend-api`
- **THEN** the actual coverage meets or exceeds the configured thresholds
  (current: 84.9% line / 74.0% branch)

### Requirement: Documented floor SHALL match pom.xml
The JaCoCo thresholds in `pom.xml` SHALL be consistent with documented
quality floors in `.claude/rules/code-quality.md` and
`CONSTITUTION.md` to prevent drift and ensure documentation accuracy.

#### Scenario: Code-quality.md matches pom.xml
- **WHEN** running `mvn test -pl backend-api -Dtest=JacocoCoverageConfigConsistencyTest#shouldHaveConsistentCoverageFloorAcrossDocsAndPom`
- **THEN** the test passes, confirming `.claude/rules/code-quality.md` line
  and branch coverage values match `backend-api/pom.xml` values

#### Scenario: CONSTITUTION.md matches pom.xml
- **WHEN** running `mvn test -pl backend-api -Dtest=JacocoCoverageConfigConsistencyTest#shouldMatchConstitutionFloorToPom`
- **THEN** the test confirms `CONSTITUTION.md` floor values (line 496)
  match `backend-api/pom.xml` thresholds

