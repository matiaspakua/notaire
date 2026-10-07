# jwt-logout-revocation — delta

## Purpose

Logout revokes the presented JWT on the server.

## MODIFIED Requirements

### Requirement: Logout revocation

Logout SHALL revoke the JWT presented by the request (Bearer or session cookie) so that it can no longer authenticate any API request, SHALL leave other tokens of the same user valid, and SHALL keep a revocation only until the token expires.

#### Scenario: Token rejected after logout

- **WHEN** a client logs out with a valid token and then calls a protected endpoint with the same token (Bearer or cookie)
- **THEN** the request is rejected with 401

#### Scenario: Other session unaffected

- **WHEN** the same user holds a second token from another login
- **THEN** that token keeps working after the first one is logged out

#### Scenario: Logout without a valid token

- **WHEN** logout is called with no token or an invalid one
- **THEN** it answers 200 with `ok: true` and revokes nothing

#### Scenario: Expired revocations purged

- **WHEN** a revocation is recorded
- **THEN** revocations whose token already expired are deleted
