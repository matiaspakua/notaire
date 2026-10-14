<!-- Governed by CONSTITUTION.md. CU76, issue #574. -->

## ADDED Requirements

### Requirement: Budget template API behaviour is preserved

The budget-templates endpoints MUST keep their status codes and success bodies.

#### Scenario: create then list

- **WHEN** a template is created for an existing procedure type and concept
- **THEN** the answer is 201 with both ids and the template appears in the list and in the list by procedure type

#### Scenario: invalid and duplicate create

- **WHEN** ids are missing, unknown, or the template already exists
- **THEN** the answers are 400, 400 and 409 respectively

#### Scenario: update and delete

- **WHEN** notes are updated or a template is deleted, for an existing and an unknown template
- **THEN** the answers are 200 or 204 for the existing one and 404 for the unknown one

### Requirement: No production code uses JpaControllerProvider

Production classes MUST NOT reference JpaControllerProvider and the class MUST NOT exist.

#### Scenario: architecture guard

- **WHEN** the production classpath is scanned
- **THEN** no class named JpaControllerProvider exists and none references it
