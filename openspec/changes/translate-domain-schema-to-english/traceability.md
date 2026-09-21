# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #973 | open (epic; description/acceptance criteria to be narrowed to schema-only scope as part of this change — see `explore.md`) |
| Use Case | none | not applicable — purely technical/naming change, no user-facing behavior; epic #973 itself documents this exception |
| Specification | `openspec/changes/translate-domain-schema-to-english/` | in progress |
| Branch | `refactor/973_rename_leaf_reference_tables` (Slice 1, base `main`); `refactor/973_rename_property_concept_item_payment_tables` (Slice 2, stacked on Slice 1 — shares `data.sql`/`cleanup-test-data.sql`/data-dictionary edits, retarget to `main` once Slice 1 merges) | both created |
| Tasks | `tasks.md` | Slice 1 groups 2-9 complete, 10-12 pending deploy/close; Slice 2 groups 2-9 complete, same pending |
| Commits | Slice 1: `3e9172f`, `cd53632`, `51bcb82`, `1b0be93`, `d32df9f`; Slice 2: `facf643`, `f864118` | both slices committed and pushed |
| Pull Request | #1011 (Slice 1, base `main`); #1012 (Slice 2, base `refactor/973_rename_leaf_reference_tables`, stacked) | both open, CLEAN/MERGEABLE |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

No delta spec exists for this change (`skip_specs: true` — pure schema
rename, no capability's observable behavior changes). Per-slice
verification instead uses:

| Verification | Test | Status |
|---------------------------------|------|--------|
| Schema matches JPA `@Table`/`@Column` mapping after each slice | `FlywaySchemaValidationIntegrationTest` (`mvn test -Ppg-integration`) | Slice 1: passing — ran via the `pre-push` hook's preflight (Docker available there) |
| No regression in existing backend/integration/E2E suite per slice | `mvn verify -pl backend-api`; `bash testing/scripts/test.sh`; Playwright suite | Slice 1: `mvn test -pl backend-api` passing (1051/1051), Checkstyle clean, coverage ratchet held; Bruno suite not run locally (preflight non-`--full` skips it) — CI's `playwright-e2e.yml` must confirm before merge; Playwright n/a (no UI surface) |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/205-data-model/Diccionario de Datos.md` | yes — table/column names and TOC anchors updated for the 9 Slice 1 tables | pending (not yet committed) |
| `docs/200-architecture/205-data-model/ERD/*` (puml/svg/csv) | no — diagram regeneration deferred; declared gap, not silently skipped | — |
| `CHANGELOG.md` | n/a — not user-visible | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | proposal.md written; issue #973 narrowing still to be applied |
| 2 | Failing tests written, test cases designed | pending | per-slice: schema validation test must fail against old names before migration lands |
| 3 | Suite green, coverage held, docs updated | pending | |
| 4 | CI green, review approved, no conflicts | pending | |
| 5 | Deployed, smoke test passed, Issue closed | pending | issue #973 closes only once all slices in `tasks.md` are merged |

## Exceptions

None taken.
