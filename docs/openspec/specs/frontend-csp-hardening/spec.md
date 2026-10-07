# frontend-csp-hardening Specification

## Purpose

Harden the frontend Content-Security-Policy in production so scripts are
nonce-constrained and `unsafe-eval` is not allowed, reducing XSS impact.
Source: #1051; owner CU78 – Security and Compliance.

## Requirements

### Requirement: Production CSP forbids unsafe-eval

In production, the `Content-Security-Policy` response header MUST NOT include
`'unsafe-eval'` in `script-src` (or equivalent script directives).

#### Scenario: Production CSP has no unsafe-eval

- **WHEN** the Next.js application serves responses with production security
  headers
- **THEN** the CSP `script-src` directive does not contain `'unsafe-eval'`

### Requirement: Production script-src is nonce-based

In production, CSP MUST authorize scripts via a per-request nonce (and/or
`strict-dynamic` with nonce) rather than a blanket `'unsafe-inline'` allow for
scripts.

#### Scenario: Production CSP script-src is nonce-based

- **WHEN** a production response includes Content-Security-Policy
- **THEN** `script-src` includes a `nonce-…` source (and does not rely on
  unrestricted `'unsafe-inline'` for scripts)

#### Scenario: App remains functional under nonce CSP

- **WHEN** a user loads login and completes login/logout under production CSP
  settings used by E2E/production build
- **THEN** required application scripts execute and the login/logout flows succeed
