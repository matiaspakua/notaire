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
| Branch | `refactor/973_rename_leaf_reference_tables` (Slice 1); subsequent slices get their own branch (see `tasks.md`) | created |
| Tasks | `tasks.md` | Slice 1 groups 2-9 complete; 10-12 pending PR/deploy |
| Commits | — | pending |
| Pull Request | — | pending (one PR per slice, per epic's vertical-slice convention) |
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
| Schema matches JPA `@Table`/`@Column` mapping after each slice | `FlywaySchemaValidationIntegrationTest` (`mvn test -Ppg-integration`) | pending — Docker/PostgreSQL unavailable in this environment; not yet run for Slice 1, must run before merge |
| No regression in existing backend/integration/E2E suite per slice | `mvn verify -pl backend-api`; `bash testing/scripts/test.sh`; Playwright suite | Slice 1: `mvn test -pl backend-api` passing (1051/1051, 0 failures/errors), Checkstyle clean; Bruno/Playwright not run (no UI surface, no live env in this session) |

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
