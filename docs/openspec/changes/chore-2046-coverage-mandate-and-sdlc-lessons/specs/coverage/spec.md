# coverage Delta

## MODIFIED Requirements

### Requirement: JaCoCo SHALL enforce minimum coverage thresholds via pom.xml

#### Scenario: Floor is at least 85% line / 70% branch

- **WHEN** running `mvn test -pl backend-api -Dtest=JacocoCoverageConfigConsistencyTest#shouldEnforceRaisedCoverageFloor`
- **THEN** the test reads the LINE and BRANCH `<minimum>` values from `backend-api/pom.xml`
  and passes only if LINE >= 0.85 and BRANCH >= 0.70
