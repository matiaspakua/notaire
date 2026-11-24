> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

`application.properties` reads `NOTAIRE_MFA_ENCRYPTION_KEY`, `NOTAIRE_MFA_POLICY` and `COOKIE_SECURE` (default false). Production compose requires its other secrets with `${VAR:?...}`; Kustomize reads them from `notaire-secrets` with placeholders committed.

## Goals / Non-Goals

**Goals:** Make MFA usable in production and send the session cookie Secure behind TLS.
**Non-Goals:** Enforcing MFA by default.

## Decisions

D1 The MFA key is required (`:?`) like the other production secrets: a deployment without it cannot enrol MFA, so fail at start.
D2 `COOKIE_SECURE` defaults to `true`: production terminates TLS in front of the proxy (HTTPS-TLS-RUNBOOK); an HTTP-only lab can set it to false.
D3 Policy defaults to `OPTIONAL`, as in application.properties (ADR-032).

## Riesgos / Trade-offs

An existing production `.env` without `NOTAIRE_MFA_ENCRYPTION_KEY` stops `docker compose up` until the key is added (`infra/scripts/ensure-mfa-encryption-key.sh`). A deployment served over plain HTTP loses the session cookie unless it sets `COOKIE_SECURE=false`.

## Testing Strategy

`infra/tests/test_prod_auth_config.py` loads the prod compose and the Kustomize base: required-key syntax, policy and cookie defaults, secretKeyRef, placeholder in secrets.yaml.

## Regression Strategy

`test_prod_compose.py` and `test_staging_kustomize.py` stay green.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release; operators add NOTAIRE_MFA_ENCRYPTION_KEY to the production .env or Secret.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
