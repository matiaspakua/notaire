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
| Issue | #805 | open (in-progress label ACL denied for bot) |
| Use Case | CU22, CU02 | exists / CU22 updated |
| Related | #836 (complete-case suplencia — shipped) | referenced |
| Specification | `openspec/changes/feat-805-suplencia-assignment/` | Gate 1 complete |
| Branch | `cursor/feat-805-suplencia-assignment-69d3` | active |
| Tasks | `tasks.md` | implementation + docs done; merge pending |
| Commits | `26c641cd` openspec; `79f8843f` TDD red; `7c6fb646` feat; `84615a72` docs | recorded |
| Pull Request | https://github.com/matiaspakua/notaire/pull/1206 | draft |
| CI run | pending (coordinator: `bash scripts/check-heavy-ci.sh 1206`) | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Creación de gestión sin suplencia activa | `ManagementSubstitutionServiceTest#shouldAssignRequestedNotaryWhenNoActiveSubstitution` | passing |
| Creación de gestión con suplencia activa | `ManagementSubstitutionServiceTest#shouldAssignSuplenteWhenNotaryHasActiveSubstitution` | passing |
| Edición de gestión con suplencia activa | `ManagementControllerIntegrationTest#shouldRedirectToSuplenteWhenUpdatingManagementNotary` | passing |
| Plain POST create redirects under active substitution | `ManagementControllerIntegrationTest#shouldRedirectNotaryOnPlainCreateWhenActiveSubstitution` | passing |
| Plain PUT update redirects under active substitution | `ManagementControllerIntegrationTest#shouldRedirectNotaryOnPlainUpdateWhenActiveSubstitution` | passing |
| Observaciones registran el redireccionamiento | unit `shouldRecordRedirectionInNotes` + complete-case IT | passing |
| Plain path notes record redirection | same new plain POST/PUT IT methods | passing |
| TS-0092 complete-case UI toast | `TS-0092-gestion-suplencia-redirect.spec.ts` | regression (toast marker updated; CI) |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU22 – Registrar Suplencia.md` | yes | `84615a72` |
| `CHANGELOG.md` | yes | `84615a72` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh feat-805-suplencia-assignment` |
| 2 | Failing tests written, test cases designed | yes | TDD red: 2/2 failures before fix |
| 3 | Suite green, coverage held, docs updated | yes | `mvn verify -pl backend-api` — 1978 tests, 0 failures |
| 4 | CI green, review approved, no conflicts | pending | draft PR #1206 |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None. `in-progress` label / issue comment denied for `cursor[bot]`.
