# notary-role-separation Specification

## Purpose
TBD - created by archiving change feat-1638-notary-role-separation. Update Purpose after archive.

## Requirements

### Requirement: Notaries and administrators have distinct authorities

User types `ADMIN` and `ADMINISTRADOR` MUST resolve to `ROLE_ADMIN`, the type `ESCRIBANO` MUST resolve to `ROLE_NOTARY` and every other type to `ROLE_USER` (case-insensitive, trimmed). A `ROLE_NOTARY` request MUST be rejected with 403 on the administrator-only paths and accepted on the non-administrative ones. The migration MUST promote the account named `admin` to `Administrador` and the seeded administrator MUST be created with that type, so a database always has an administrator.

#### Scenario: administrator types resolve to the admin authority

- **WHEN** a user of type Admin, administrador or ADMINISTRADOR is resolved
- **THEN** the authority is ROLE_ADMIN

#### Scenario: the notary type resolves to the notary authority

- **WHEN** a user of type Escribano is resolved
- **THEN** the authority is ROLE_NOTARY and not ROLE_ADMIN

#### Scenario: a notary is denied administrator paths

- **WHEN** a notary calls the users, roles or audit-log endpoints
- **THEN** each answers 403

#### Scenario: a notary keeps notarial endpoints

- **WHEN** a notary lists people
- **THEN** the answer is 200

#### Scenario: the seeded administrator is an administrator

- **WHEN** the initializer creates the first account
- **THEN** its type is Administrador

#### Scenario: the frontend does not treat a notary as an administrator

- **WHEN** the admin guard evaluates the Escribano type
- **THEN** it is not admin-capable

#### Scenario: an administrator survives the migration

- **WHEN** the database checks run after Flyway
- **THEN** an enabled administrator account exists and the account named admin is not a notary
