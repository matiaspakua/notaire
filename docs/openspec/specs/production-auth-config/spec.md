# production-auth-config Specification

## Purpose

TBD - created by archiving change fix-2488-prod-mfa-secure-cookie. Update Purpose after archive.

## Requirements

### Requirement: Production deployments wire MFA and the secure session cookie

`docker-compose.prod.yml` and the Kustomize base SHALL pass `NOTAIRE_MFA_ENCRYPTION_KEY` (required, from the
environment or the `notaire-secrets` Secret), `NOTAIRE_MFA_POLICY` (default `OPTIONAL`) and `COOKIE_SECURE`
(default `true`) to the backend.

#### Scenario: Production compose refuses to start without the MFA key

- **WHEN** `docker compose -f docker-compose.prod.yml config` runs without `NOTAIRE_MFA_ENCRYPTION_KEY`
- **THEN** compose fails with a message that names the variable

#### Scenario: The session cookie is Secure in production

- **WHEN** the production compose or the Kustomize base renders the backend environment
- **THEN** `COOKIE_SECURE` is `true` unless the operator overrides it

#### Scenario: Kustomize reads the MFA key from the Secret

- **WHEN** the Kustomize base renders the backend Deployment
- **THEN** `NOTAIRE_MFA_ENCRYPTION_KEY` comes from `notaire-secrets` and `NOTAIRE_MFA_POLICY` is set
