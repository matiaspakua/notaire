<!-- Governed by CONSTITUTION.md. Issue #2351. The issue stays open. -->

## ADDED Requirements

### Requirement: Revoked-token entity is not in the shared business package

The logout revocation entity SHALL live with the other security persistence types and SHALL keep the table name `revoked_tokens`.

#### Scenario: the shared business package does not contain the entity

- **WHEN** the revoked-token type is loaded
- **THEN** it is `adapter.out.persistence.security.RevokedToken`, it is a JPA entity, its table is `revoked_tokens`, and `business.RevokedToken` does not exist
