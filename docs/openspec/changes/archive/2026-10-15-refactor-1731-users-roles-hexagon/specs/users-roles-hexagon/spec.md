## ADDED Requirements

### Requirement: Users and roles are managed through domain-typed ports

The user and role HTTP adapters MUST call inbound ports only. Use cases MUST own persistence transactions. Login password verification (BCrypt and legacy MD5 upgrade) MUST live in the application layer behind a password hasher port. Role module lists MAY be stored and returned as today but MUST NOT gain new enforcement in this change (fenced for #1683).

#### Scenario: User CRUD and login keep the wire contract

- **WHEN** a client lists, gets, creates, updates, deletes users, or logs in / out
- **THEN** status codes and JSON fields match the pre-refactor contract (including login valid/token/cookie and MD5→BCrypt upgrade)

#### Scenario: Role CRUD and assign keep the wire contract

- **WHEN** a client manages roles or assigns/unassigns a role to a user
- **THEN** status codes and JSON fields (including wire `modulos`) match the pre-refactor contract

#### Scenario: controllers have no persistence imports

- **WHEN** ArchUnit R1 runs after the slice
- **THEN** UserController and RoleController have no baseline edges to repository or business entities

#### Scenario: module grants stay storage-only

- **WHEN** role modules are written or read
- **THEN** no new server-side path-family enforcement is introduced (deferred to #1683)
