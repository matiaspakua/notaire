# native-ddl-auto Specification

## Purpose
The native stack helper starts the backend with ddl-auto=none. Issue #583 stays open for profiles and the default schema check.

## Requirements

### Requirement: Native helper uses ddl-auto none

The native stack helper SHALL start the backend with Hibernate `ddl-auto=none` and SHALL NOT pass `ddl-auto=update`.

#### Scenario: the helper does not update the schema

- **WHEN** `workspace/stack/native.sh` is read
- **THEN** it contains `ddl-auto=none` and does not contain `ddl-auto=update`
