<!-- Governed by CONSTITUTION.md. CU78, issue #1682. -->

## ADDED Requirements

### Requirement: A new installation has the standard role template

After all migrations on an empty database, the roles Administrator, Notary and Office Staff MUST exist with their module grants.

#### Scenario: fresh database

- **WHEN** all Flyway migrations run on an empty database
- **THEN** the three template roles exist and Administrator holds every module

#### Scenario: seeded admin is an Administrator

- **WHEN** the seeded admin user has no role before V50
- **THEN** it is linked to the Administrator role afterwards

### Requirement: The seed never overwrites tenant changes

V50 MUST insert a template role only when no role has that name and MUST NOT touch the grants of an existing role.

#### Scenario: existing role keeps its grants

- **WHEN** a role named Notary with a single custom grant exists before V50
- **THEN** after V50 it still has exactly that grant and its description is unchanged

#### Scenario: other template roles are still seeded

- **WHEN** a role named Notary already exists before V50
- **THEN** Administrator and Office Staff are still created
