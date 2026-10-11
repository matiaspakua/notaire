# homer-hub Specification

## Purpose
The Homer landing page is a complete, current index of the stack.

## Requirements

### Requirement: Every compose service is listed

Homer SHALL contain an entry for each service of the observability compose file and the application compose file's operational services.

#### Scenario: No service is missing

- **WHEN** compose services are compared with Homer entries
- **THEN** each service is named by an entry (by name or documented alias)

### Requirement: Every dashboard is linked

Homer SHALL link every provisioned Grafana dashboard and the SonarQube backend and frontend projects.

#### Scenario: No dashboard is missing

- **WHEN** provisioned dashboard UIDs are compared with Homer links
- **THEN** every UID appears in a link, and both Sonar project keys appear
