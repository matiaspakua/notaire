<!-- Governed by CONSTITUTION.md. CU76, issue #583. The issue stays open. -->

## ADDED Requirements

### Requirement: Packaged ddl-auto is none

The packaged application SHALL set Hibernate `ddl-auto` to `none` and SHALL NOT read `SPRING_JPA_HIBERNATE_DDL_AUTO`.

#### Scenario: the property file has no environment placeholder

- **WHEN** `application.properties` is read
- **THEN** it contains `spring.jpa.hibernate.ddl-auto=none` and does not contain `SPRING_JPA_HIBERNATE_DDL_AUTO`
