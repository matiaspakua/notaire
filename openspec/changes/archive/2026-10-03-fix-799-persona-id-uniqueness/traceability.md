# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #799 | open (in-progress label ACL denied for bot) |
| Use Case | CU17, CU18 | exists / updated |
| Related | #835 (service-layer duplicate reject — shipped) | referenced |
| Specification | `openspec/changes/fix-799-persona-id-uniqueness/` | Gate 1 complete |
| Branch | `cursor/fix-799-persona-id-uniqueness-69d3` | active |
| Tasks | `tasks.md` | implementation + docs + verify done; merge pending |
| Commits | `94f18c22` openspec; `f52a4b90` fix(db); `56d3bbbd` docs; `67aa1836` test #804 fallout | recorded |
| Pull Request | https://github.com/matiaspakua/notaire/pull/1202 | draft |
| CI run | pending (coordinator: `bash scripts/check-heavy-ci.sh 1202`) | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Second insert with same type and number is rejected by the database | `PersonIdentificationUniquenessIntegrationTest#shouldRejectSecondInsertWithSameTypeAndNumber` | passing |
| Migration refuses to proceed when duplicate groups exist | `PersonIdentificationUniquenessPgIntegrationTest#shouldFailPrecheckWhenDuplicateGroupsExist` | passing |
| Concurrent create race still surfaces as HTTP 409 | `PersonServiceTest#shouldMapDataIntegrityRaceToDuplicatePersonException` | passing |
| Alta exitosa con documento no registrado | existing `PersonServiceTest` (#835) | covered (existing) |
| Rechazo de alta con documento ya registrado | existing `PersonServiceTest` (#835) | covered (existing) |
| Unique index present after Flyway | `PersonIdentificationUniquenessPgIntegrationTest#shouldExposeUniqueIndexAfterFlywayMigrates` | passing |
| JDBC second insert rejected on Postgres | `PersonIdentificationUniquenessPgIntegrationTest#shouldRejectSecondJdbcInsertWithSameTypeAndNumber` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU17 – Dar Alta persona.md` | yes | `56d3bbbd` |
| `docs/100-business/102-use-cases/CU18 – Dar Alta Cliente.md` | yes | `56d3bbbd` |
| `CHANGELOG.md` | yes | `56d3bbbd` |
| `openspec/specs/persona-validacion-duplicados/spec.md` | yes | `94f18c22` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh fix-799-persona-id-uniqueness` |
| 2 | Failing tests written, test cases designed | yes | TDD red: uniqueness IT expected throwable; race unit got DataIntegrityViolation |
| 3 | Suite green, coverage held, docs updated | yes | `mvn verify -pl backend-api` — 1973 tests, 0 failures; instr 85.04% / branch 73.64% |
| 4 | CI green, review approved, no conflicts | pending | draft PR #1202 |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None. `in-progress` label could not be applied (GitHub GraphQL: Resource not accessible by integration).
