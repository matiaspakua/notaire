# coverage Delta

## MODIFIED Requirements

### Requirement: JaCoCo SHALL enforce minimum coverage thresholds via pom.xml

Any Spring Boot module with JPA entities or service code SHALL declare
minimum coverage thresholds in `pom.xml` JaCoCo plugin configuration. The
enforced BUNDLE floor SHALL be at least **85% line** and **70% branch**
(Owner target >90% is documented separately; floors ratchet raise-only).

#### Scenario: Line coverage floor is enforced

- **WHEN** the JaCoCo plugin is configured with a `<minimum>` line value
  in `pom.xml` and `mvn jacoco:check` is run
- **THEN** the build fails if line coverage is below the threshold

#### Scenario: Branch coverage floor is enforced

- **WHEN** the JaCoCo plugin is configured with a `<minimum>` branch value
  in `pom.xml` and `mvn jacoco:check` is run
- **THEN** the build fails if branch coverage is below the threshold

#### Scenario: Floor is at least 85% line / 70% branch

- **WHEN** running `mvn test -pl backend-api -Dtest=JacocoCoverageConfigConsistencyTest#shouldEnforceRaisedCoverageFloor`
- **THEN** the test reads the LINE and BRANCH `<minimum>` values from `backend-api/pom.xml`
  and passes only if LINE >= 0.85 and BRANCH >= 0.70

#### Scenario: Threshold values are proven by successful build

- **WHEN** the build passes `mvn jacoco:check -pl backend-api`
- **THEN** the actual coverage meets or exceeds the configured thresholds
  (Owner target >90% per #2046; floors ratchet separately)
