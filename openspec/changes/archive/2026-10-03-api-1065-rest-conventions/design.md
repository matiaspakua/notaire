> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #1065 (audit-2026-09) found mixed REST naming, create statuses, and an
unused `POST /pagos/params`. ADR-003 is versioning-only. Folio create already
returns 201; RegistrationDraft create still returns 200. No create emits
`Location` today. Open questions on language/search are resolved conservatively
at design time: prefer English resource nouns that match established `/api/v1`
paths; prefer plurals and `/search`; phase renames later.

## Goals / Non-Goals

**Goals:**
- Record ADR-023 naming conventions and phased-rename policy.
- Remove dead `POST /api/v1/pagos/params`.
- Shared `Location` helper; apply on sample creates (pagos, folio, minutas).
- RegistrationDraft generate → `201` + `Location`.

**Non-Goals:**
- Big-bang path renames across the API.
- Adding `Location` to every create endpoint in one PR.
- Introducing `/api/v2` in this slice.

## Decisions

1. **ADR-023 (new naming ADR)** — Accepted conventions:
   - **Language:** new collection paths use English resource nouns aligned with
     existing English paths (`/people`, `/items`, `/audit-log`, hexagonal
     adapters). Spanish paths already shipped stay until a phased rename.
   - **Plural nouns** for collections; singular legacy paths rename later.
   - **Search:** prefer `/search` over `/buscar` for new/renamed routes.
   - **Actions:** model as sub-resources or status transitions on the resource
     (`PUT …/presentar`), not free verbs on the collection root.
   - **Versioning:** renames follow ADR-003 (deprecate + dual-route or v2);
     this slice does not rename.
2. **Remove `/pagos/params`** — no UI/Bruno callers; delete handler + unit tests;
   keep `POST /pagos` JSON.
3. **Shared helper** — e.g. `CreatedResponses.created(body, collectionPath, id)`
   using `ServletUriComponentsBuilder` / `UriComponentsBuilder`, returning
   `ResponseEntity` with `201` + `Location`.
4. **Sample set** — Payment, Folio, RegistrationDraft only in Slice-1.

## Riesgos / Trade-offs

- **[Risk] Clients depending on minuta `200`** → Mitigate: CHANGELOG BREAKING;
  body unchanged; only status/`Location` added.
- **[Risk] Clients depending on `/pagos/params`** → Mitigate: unused by UI/Bruno;
  BREAKING in CHANGELOG; JSON create remains.
- **[Risk] Incomplete Location coverage** → Accepted: phased; ADR + sample set
  establish the pattern.
- **[Trade-off] Keep singular `/folio` path for Location** → Matches current
  mapping; rename later under ADR-023.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Registration draft create returns 201 and Location | unit | `RegistrationDraftControllerTest` |
| Sample payment create includes Location | unit | `PaymentControllerTest` |
| Sample folio create includes Location | unit/integration | `FolioControllerTest` (or new unit) |
| Payment params create is absent | unit | `PaymentControllerTest` |
| ADR-023 exists and states English resource nouns | docs review | file presence in Gate 3 |
| JSON payment create includes Location header | unit | `PaymentControllerTest` |
| Generar minuta con datos completos (201+Location) | unit | `RegistrationDraftControllerTest` |

- New unit tests (`src/test/java/.../unit/` / adapter tests): assert `Location`
  header; assert `/pagos/params` → 404/405; assert minuta `201`.
- New integration tests: none required beyond existing Folio/Payment coverage
  updates if present.
- Coverage impact (JaCoCo ratchet floor; 80% target): helper covered by controller
  tests; removed params dead code reduces surface.

TDD: write failing tests first, observe red, then implement.

## Regression Strategy

- Existing tests affected:
  - `PaymentControllerTest` — delete params cases; add Location + absence.
  - `RegistrationDraftControllerTest` — expect `isCreated()` + Location.
  - Folio create tests — add Location assertion if create is covered.
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: payments Bruno regression (`bash testing/scripts/test.sh`
  or payments-focused Bruno) — must stay green without `/pagos/params`.
- Legacy paths at risk: none for params removal (dead); naming renames out of
  scope.

## Playwright Strategy

- Specs to add/update under `frontend/tests/e2e/`: n/a — no UI surface
- Golden path covered: n/a
- Edge / error paths covered: n/a
- Viewports: n/a
- Command: n/a — no UI surface (backend REST contract + ADR only)

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: deploy backend only; no schema coupling
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): `POST /api/v1/pagos` → 201 + Location;
  `POST /api/v1/pagos/params` → not found/method not allowed;
  `POST /api/v1/minutas-inscripcion` → 201 + Location

## Rollback Strategy

- Revert safe: yes — restore params endpoint and prior status codes via revert PR
- Database rollback: none needed
- Data written under the new behavior after revert: none (response metadata only;
  payment/minuta rows unchanged)
- Blast radius if rollback is delayed: clients that adopted `Location` or `201`
  for minutas must tolerate `200` again if reverted

## Migration Plan

1. Land ADR-023 + docs.
2. Add shared Location helper.
3. Wire sample creates; fix RegistrationDraft status.
4. Remove `/pagos/params` + obsolete tests.
5. Follow-up issues for phased renames (not this PR).

## Open Questions

None — language/search/PO choices resolved conservatively as English resource
nouns matching existing `/api/v1` paths; plurals and `/search` for new work;
phased renames only.
