> Governed by [CONSTITUTION.md](../../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

#1966 turned on shrink-only ratchets for Checkstyle (`-Dcheckstyle.maxAllowedViolations=3157`)
and SpotBugs (`enforce-quality-ratchet.py`, max 1183), and in the same PR added code that broke
them (Checkstyle 3173, SpotBugs 1187). It also edited `openapi.yaml` without regenerating the
frontend types. Evidence: CI run 40758857573 (jobs 123065615854, 123065615915) and Frontend CI
run 40758857667 (job 123065101239) on `b11667c91`.

## Goals / Non-Goals

**Goals:** fix every finding #1966 introduced; regenerate the types; keep behavior identical.

**Non-Goals:** raising or moving floors; touching workflows or `github-page/`.

## Decisions

- Fix in code rather than suppress or raise floors — the baseline file states floors may only shrink.
- `List.copyOf` keeps `null` as `null` so `@NotEmpty` still answers 400 for a missing list.
- Bean-method injection of `RoleModuleAuthorizationFilter` matches Spring Security idiom and
  keeps the filter order (`addFilterAfter(..., JwtAuthenticationFilter.class)`).

## Riesgos / Trade-offs

- [A record copy changes list identity] → no caller relies on identity; covered by `CatalogItemsRequestTest`.
- [Bean-method parameter wiring] → covered by the Spring context in integration tests and Playwright.

## Testing Strategy

| Scenario | Test level | Test class / file |
|----------|------------|-------------------|
| Catalog item ids are copied and unmodifiable; `null` kept | unit | `adapter/in/web/budget/CatalogItemsRequestTest` |
| Not-found mapping is locale-independent; null message → 400 | unit | `unit/support/IllegalArgumentResponsesTest` |
| Document type create defaults / update keeps stored flags | unit | `adapter/in/web/document/DocumentTypeControllerTest` |
| Config builds with the JWT filter only | unit | `unit/SecurityAndCorsConfigTest` |
| Checkstyle ≤ 3157, SpotBugs ≤ 1183 | CI gate | `ci.yml` Coverage Gate, Code Quality |
| Generated types match `openapi.yaml` | CI gate | `frontend-ci.yml` TypeScript Check |
| Runtime images upgrade Alpine packages before `USER` | unit | `workspace/tests/test_backend_image_os_patches.py` |
| Image has no fixable CRITICAL CVE | CI gate | `ci.yml` Build Docker Image (Trivy) |

- Coverage impact: neutral to positive.

## Regression Strategy

- Existing tests affected: `SecurityAndCorsConfigTest` (constructor arity only).
- Full suite command: `mvn verify -pl backend-api -am`
- HTTP/Bruno API suite: unchanged endpoints; covered by CI integration tests.
- Legacy paths at risk: none.

## Playwright Strategy

- n/a — no UI surface; generated types are compile-time only. Playwright CI still runs on the PR.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: none
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): `main` CI, Frontend CI and Deploy GitHub Page succeed.

## Rollback Strategy

- Revert safe: yes — formatting and local refactors only.
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: none
