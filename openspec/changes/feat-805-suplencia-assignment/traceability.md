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
| Use Case | CU22, CU02 | exists / CU22 to update |
| Related | #836 (complete-case suplencia — shipped) | referenced |
| Specification | `openspec/changes/feat-805-suplencia-assignment/` | Gate 1 complete |
| Branch | `cursor/feat-805-suplencia-assignment-69d3` | active |
| Tasks | `tasks.md` | pending |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | pending (coordinator: `bash scripts/check-heavy-ci.sh <pr>`) | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Creación de gestión sin suplencia activa | `ManagementSubstitutionServiceTest#shouldAssignRequestedNotaryWhenNoActiveSubstitution` | covered (existing) |
| Creación de gestión con suplencia activa | `ManagementSubstitutionServiceTest#shouldAssignSuplenteWhenNotaryHasActiveSubstitution` + complete-case IT | covered (existing) |
| Edición de gestión con suplencia activa | `ManagementControllerIntegrationTest#shouldRedirectToSuplenteWhenUpdatingManagementNotary` | covered (existing) |
| Plain POST create redirects under active substitution | `ManagementControllerIntegrationTest#shouldRedirectNotaryOnPlainCreateWhenActiveSubstitution` | pending |
| Plain PUT update redirects under active substitution | `ManagementControllerIntegrationTest#shouldRedirectNotaryOnPlainUpdateWhenActiveSubstitution` | pending |
| Observaciones registran el redireccionamiento | existing complete-case IT + unit `redirectionNote` | covered (existing) |
| Plain path notes record redirection | same new plain POST/PUT IT methods | pending |
| TS-0092 complete-case UI toast | `TS-0092-gestion-suplencia-redirect.spec.ts` | regression (no UI change) |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU22 – Registrar Suplencia.md` | pending | — |
| `CHANGELOG.md` | pending | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh feat-805-suplencia-assignment` |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | draft PR |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None. `in-progress` label could not be applied (GitHub: label not found /
integration ACL). Issue comment also denied for `cursor[bot]`.
