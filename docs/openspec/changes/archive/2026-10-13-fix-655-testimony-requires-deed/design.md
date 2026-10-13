# Design

## Context

Issue #655 lists endpoints that accept incomplete bodies; one is the bare `POST /testimonies`. Run 7: the Owner asked to require the deed unless removing the endpoint was clearly cleaner. The UI generates testimonies with `POST /testimonies/generate` (signed deed). The Playwright seed helper is `createTestimony`. TS-0032, Bruno, and backend tests still send a deed on the bare create. The retired helper name `createTestimonio` stays out of the seed.

## Goals / Non-Goals

Goal: no new testimony without its deed. Non-goals: testimonies already stored without a deed; `PUT` semantics; removing the endpoint.

## Decisions

Require the deed instead of removing the endpoint: removal would force the seeds to go through `/generate` (which needs a signed deed and generated content), and remove allowlist and contract entries, for no user-visible gain. A subclass of `DtoTestimony` keeps the request shape identical, so oasdiff reports only `deed` becoming required.

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
