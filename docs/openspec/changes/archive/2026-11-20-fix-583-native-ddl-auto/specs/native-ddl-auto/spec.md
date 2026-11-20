<!-- Governed by CONSTITUTION.md. CU76, issue #583. The issue stays open. -->

## ADDED Requirements

### Requirement: Native helper uses ddl-auto none

The native stack helper SHALL start the backend with Hibernate `ddl-auto=none` and SHALL NOT pass `ddl-auto=update`.

#### Scenario: the helper does not update the schema

- **WHEN** `workspace/stack/native.sh` is read
- **THEN** it contains `ddl-auto=none` and does not contain `ddl-auto=update`
