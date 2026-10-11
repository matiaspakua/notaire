> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`IdentificationTypeController` already exposes list/create/update/delete. Slice 1 added `useIdentificationTypes` (GET only) and the person select. Admin catalogs such as `management-statuses` show the UI pattern: DataTable + Dialog with FormContainer/FormSection/FormField/FormActions, ConfirmDialog delete, `useDeleteError` for 409. Route English path matches peers (`/dashboard/administration/identification-types`), not the Spanish path in the issue text.

## Goals / Non-Goals

**Goals:** ADMIN CRUD page; admin index card; mutations invalidate person-form catalog cache; remove three bucket-C allowlist entries; Vitest + Playwright prove AC.

**Non-Goals:** Backend schema or OpenAPI shape changes; `/in-use` preflight endpoint; person-form `characters` maxLength; editing seeded DNI in E2E.

## Decisions

- **Route:** `/dashboard/administration/identification-types` under the existing ADMIN layout guard — same English segment style as `management-statuses`, not `/administracion/tipos-identificacion`.
- **Hook:** extend `useIdentificationTypes.ts` with `useCreateIdentificationType` / `useUpdateIdentificationType` / `useDeleteIdentificationType` rather than a second file; keep `identificationTypeKeys.all` as the shared query key so person form refreshes after catalog edits.
- **Delete:** call DELETE; on 409 show `useDeleteError` message. Do not invent a client-side in-use check (no endpoint). Optional Backend handoff: add `GET .../in-use` later for parity with statuses.
- **Fields:** `name` (required) and `characters` (optional metadata string, same as API). Send `version` on PUT only if the generated client / existing entity pattern already requires it; controller today applies name/characters only.
- **Nav:** admin index module card is the discoverability path (sidebar only links to the administration hub). Non-admin never sees the card and is redirected from the deep URL by the layout (#1052 / TS-0094 pattern).
- **Serialize:** Gate 1 artifacts may land in a docs PR without CHANGELOG. Product implementation + Playwright wait until #1518 merges (or foreman clears heavy-CI / CHANGELOG lock).

## Riesgos / Trade-offs

- [No `/in-use` pre-check] → User confirms delete then sees 409 toast; acceptable and matches #1345 for endpoints without preflight.
- [Deleting a type used by people] → FK 409; never delete seeded DNI/CUIT in E2E — create a unique type name for the scenario.
- [Heavy-CI serialize] → Hold product PR until #1518 clears; Gate 1 docs-only PR must not touch CHANGELOG or Playwright paths if opened early.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Admin lists types | Vitest + E2E | hook/page unit; TS-0127 |
| Admin creates a type | Vitest + E2E | mutation body; TS-0127 |
| Admin edits a type | Vitest + E2E | PUT body; TS-0127 |
| Admin deletes an unused type | E2E | TS-0127 |
| Delete in use shows reason | Vitest (mock 409) | page/hook unit |
| Non-admin redirected | E2E | TS-0127 (reuse TS-0094 helper pattern) |
| Allowlist entries removed | contract | `contracts/tests/test_api_reachability.py` |

- New unit tests: `frontend/src/tests/unit/identification-type-catalog.test.tsx` (mutations + 409 toast path).
- New integration tests: none required (API already covered by Bruno / JUnit).
- Coverage impact: frontend Vitest only; JaCoCo unchanged.

## Regression Strategy

- Existing tests affected: `people-identification-type.test.tsx` (still uses GET list — must keep passing after key/mutation additions); `dashboard-nav` / admin smoke if index keys change.
- Full suite: `bash frontend/verify.sh`; `python3 -m pytest contracts/tests/test_api_reachability.py` (or project wrapper); Bruno optional smoke on identification-types.
- Legacy paths at risk: none.

## Playwright Strategy

- Specs to add: `testing/e2e/tests/TS-0127-identification-type-catalog.spec.ts`
- Golden path: authenticateAsAdmin → open catalog → create unique type → edit name → delete → gone from table; person form select still lists types.
- Edge: EMPLEADO → `/dashboard/administration/identification-types` → `/dashboard?forbidden=1` + `access-denied-message`; no `btn-new-identification-type`.
- Prefer `data-testid`: `btn-new-identification-type`, `input-name-identification-type`, `input-characters-identification-type`, `btn-edit-identification-type`, `btn-delete-identification-type`, `search-identification-types`.
- Rules: no `.or()` under one `toBeVisible`; Escape dismiss without double-click Cancel; never edit seeded admin user; HttpOnly auth hydrate only with status cookie.
- Viewports: 1024px primary; spot-check 390px.
- Command: `cd testing/e2e && npx playwright test TS-0127-identification-type-catalog`

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: frontend-only deploy after merge
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): admin opens catalog, creates then deletes a throwaway type

## Rollback Strategy

- Revert safe: yes (UI + allowlist only)
- Database rollback: none needed
- Data written under the new behavior after revert: any types created via UI remain in DB (harmless)
- Blast radius if rollback is delayed: low — catalog CRUD only

## Migration Plan

1. Land Gate 1 OpenSpec (this change) — may be docs-only PR.
2. After #1518 clears: red Vitest + red TS-0127 on pre-change build.
3. Implement page/hooks/i18n/allowlist; green suite; CHANGELOG; product PR `Closes #1361`.

## Open Questions

None — Owner decisions for slice 1 stand; catalog is ADMIN-only by existing administration layout.
