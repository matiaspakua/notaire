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
| Issue | #973 | open; closes once Slice 11 (PR pending) merges — see `tasks.md` §13 |
| Use Case | none | not applicable — purely technical/naming change, no user-facing behavior; epic #973 itself documents this exception |
| Specification | `openspec/changes/translate-domain-schema-to-english/` | complete |
| Branch | Slices 1-10 all merged to `main`. Slice 11 (`refactor/973_rename_users_table`, scope-gap fix for `usuarios`, discovered after all 10 planned slices merged) branched fresh from post-merge `main` | Slice 11 PR pending |
| Tasks | `tasks.md` | Slices 1-10 all groups complete and merged; Slice 11 groups 2-9 complete, PR pending |
| Commits | Slices 1-10: see individual PRs #1011-#1020; Slice 11: `b4a1edc` | all committed and pushed |
| Pull Request | #1011 (Slice 1, MERGED); #1012 (Slice 2, MERGED); #1013 (Slice 3, MERGED into Slice 2 branch); #1014 (Slice 4, MERGED); #1015 (Slice 5, MERGED); #1016 (Slice 6, MERGED); #1017 (Slice 7, MERGED); #1018 (Slice 8, MERGED); #1019 (Slice 9, MERGED); #1020 (Slice 10, MERGED); Slice 11 PR pending creation | **10/11 slices merged to `main`.** |
| CI run | all green on every merged PR | see individual PR check runs |
| Merge commit | 10 merge commits, one per PR #1011-#1020 | see `git log --merges main` |
| Release / tag | none yet — no release cut since these merges | pending |
| Smoke test | not yet run against a deployed environment | pending |

## Requirement coverage

No delta spec exists for this change (`skip_specs: true` — pure schema
rename, no capability's observable behavior changes). Per-slice
verification instead uses:

| Verification | Test | Status |
|---------------------------------|------|--------|
| Schema matches JPA `@Table`/`@Column` mapping after each slice | `FlywaySchemaValidationIntegrationTest` (`mvn test -Ppg-integration`) | passing on every slice, re-verified after each sequential merge against the growing real-Postgres schema (V1 through V37) |
| No regression in existing backend/integration/E2E suite per slice | `mvn verify -pl backend-api`; `bash testing/scripts/test.sh`; Playwright suite | 1051/1051 unit+integration tests green and Checkstyle clean on every slice and after every merge; CI (`ci.yml`/`pr-validation.yml`) green on all 10 merged PRs including Bruno API tests; Playwright n/a (no UI surface) |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/205-data-model/Diccionario de Datos.md` | yes — table/column names and TOC anchors updated for all 35 renamed tables across all 11 slices; also corrected a pre-existing staleness (`personas`/`id_persona` → `people`/`id`) predating this epic, found while fixing Slice 11 | see per-slice commits, each PR's own commit list |
| `docs/200-architecture/205-data-model/ERD/*` (puml/svg/csv) | no — diagram regeneration deferred; tracked as non-blocking follow-up issue [#1021](https://github.com/matiaspakua/notaire/issues/1021) | — |
| `CHANGELOG.md` | n/a — not user-visible | — |
| `@NamedQuery` name strings (cosmetic) | no — deferred; tracked as non-blocking follow-up issue [#1022](https://github.com/matiaspakua/notaire/issues/1022) | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | passed | proposal.md written; issue #973 narrowed to schema-only scope |
| 2 | Failing tests written, test cases designed | passed (per-slice) | `FlywaySchemaValidationIntegrationTest` verified against real Postgres for every slice |
| 3 | Suite green, coverage held, docs updated | passed (per-slice) | 1051/1051 tests green, Checkstyle clean, Diccionario de Datos updated for all 11 slices |
| 4 | CI green, review approved, no conflicts | passed for Slices 1-10 (all merged); pending for Slice 11 | code owner merged each PR directly (counts as review per Constitution §5 step 20) |
| 5 | Deployed, smoke test passed, Issue closed | pending | Slice 11 must merge first; smoke test and issue close are the last remaining steps |

## Exceptions

No CONSTITUTION.md §12 process exception was taken (no step was skipped).
However, a real bug shipped in Slice 1 (`V26`, PR #1011, already merged):
`notebooks.anio` was renamed to the literal `year` in the migration, but
`Notebook.java`'s `@Column` annotation was never updated to match — the
entity was silently out of sync with the real Postgres schema from the
moment `V26` merged. Undetected by the existing test suite because the H2
unit-test profile builds its schema from the (stale) entity annotation via
`ddl-auto=create` rather than from Flyway, and
`FlywaySchemaValidationIntegrationTest` doesn't happen to exercise the
`notebooks` endpoint. Found and fixed in Slice 7 (`V33`, PR #1017) while
discovering that `YEAR` is itself an H2 reserved keyword (renaming
`folios.anio` the same way failed loudly, unlike `notebooks`). Recorded
here per Constitution transparency norms even though it was self-corrected
within this same change, not left for a human to catch.
