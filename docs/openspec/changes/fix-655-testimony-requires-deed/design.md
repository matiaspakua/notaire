# Design

## Context

Issue #655 lists endpoints that accept incomplete bodies; one is the bare `POST /testimonio`. Run 7: the Owner asked to require the deed unless removing the endpoint was clearly cleaner. The UI generates testimonies with `POST /testimonio/generar` (signed deed). The bare create is still used by the Playwright seed helpers (`createTestimonio`, TS-0032), Bruno and backend tests, which all send a deed.

## Goals / Non-Goals

Goal: no new testimony without its deed. Non-goals: testimonies already stored without a deed; `PUT` semantics; removing the endpoint.

## Decisions

Require the deed instead of removing the endpoint: removal would force the seeds to go through `/generar` (which needs a signed deed and generated content), and remove allowlist and contract entries, for no user-visible gain. A subclass of `DtoTestimony` keeps the request shape identical, so oasdiff reports only `deed` becoming required.

## Riesgos / Trade-offs

An external client creating bare testimonies without a deed now gets 400; none ships in this repository.

## Testing Strategy

`TestimonyCreateRequiresDeedIntegrationTest` (4 cases) and Bruno `testimonies/09` written first and observed failing.

## Regression Strategy

Full backend suite; full Bruno run against this backend on PostgreSQL 17 (it found the `testimony-movements` fixture creating a bare testimony, fixed in this PR); Playwright chromium TS-0012, TS-0031, TS-0032 (24 passed).

## Playwright Strategy

TS-0012, TS-0031, TS-0032: 24 passed.

## Deployment Strategy

Backend only.

## Rollback Strategy

Revert the merge commit.
