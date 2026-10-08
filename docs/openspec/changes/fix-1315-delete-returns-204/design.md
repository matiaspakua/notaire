# Design

## Context

Issue #1315 asks for a coherent, valid OpenAPI contract. Run 6 found that DELETE /pagos/{id} and DELETE /historial/{id} returned 200 while documenting 204; the Owner decided the implementation must follow the spec and asked for an audit of every DELETE endpoint.

## Goals / Non-Goals

Goal: every DELETE handler and its contract agree, with a guard that keeps them in sync. Non-goals: the other #1315 slices (error status codes, validators); changing `DELETE /plantilla-presupuestos/tipo-tramite/{id}/concepto/{id}`, which documents and returns 200 consistently.

## Decisions

Change the implementation, not the spec, per the Owner (the spec already said 204 and 204 is the REST norm for an empty delete). For roles/usuarios the implementation already returned 204, so only the documentation changes; oasdiff flags the removed 200 as breaking, accepted because no client ever received a 200. The guard parses source like the other controller guards instead of booting the context, so it covers every controller in milliseconds.

## Riesgos / Trade-offs

An external client that compared the status to 200 exactly would now see 204; the shipped frontend (`apiDelete`), Playwright helpers (`response.ok()`) and Bruno (updated) are unaffected.

## Testing Strategy

`DeleteStatusMatchesContractTest` plus 19 MockMvc assertions and Bruno checks changed first and observed failing (17 handlers documented 204 but returned 200; RoleController documented 200 but returned 204).

## Regression Strategy

Full backend suite (`backend-api/verify.sh`); full Bruno run (324 requests, 549 tests) against this backend on PostgreSQL 17; frontend `api-client` unit test already covers DELETE 204.

## Playwright Strategy

Not run: no UI change; E2E helpers check `response.ok()` only.

## Deployment Strategy

Backend only.

## Rollback Strategy

Revert the merge commit.
