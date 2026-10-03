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
| Issue | #945 | open (implement in progress; in-progress label ACL 403) |
| Use Case | CU17 – Dar Alta Persona; CU61 – Buscar persona o cliente | exists |
| Related | #1054 (shared mutation errors — closed); #928 E2E audit surface | referenced |
| Specification | `openspec/changes/fix-945-persona-validation-toast/` | Gate 1 validated |
| Branch | `cursor/fix-945-persona-validation-toast-69d3` | pushed |
| Tasks | `tasks.md` | implement complete; PR open |
| Commits | `f876351a4c10af46580a69e16c9914fa31ed839b` | pushed |
| Pull Request | https://github.com/matiaspakua/notaire/pull/1172 | draft |
| CI run | PR checks pending | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Backend 400 validation message is toasted | unit `persona-save-error.test.ts` + E2E Dedup-EDGE | proven |
| Fallback when no message is extractable | unit `persona-save-error.test.ts` | proven |
| 409 keeps curated duplicate toast | unit `persona-save-error.test.ts` + Dedup-GW01/GW02 | proven |
| Dialog stays open after empty-DNI validation failure | E2E Dedup-EDGE | proven |
| Dedup-EDGE passes in the Playwright suite | `TS-0015-personas-clientes-workflow.spec.ts` | proven (local 21/21) |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU17 – Dar Alta persona.md` | yes (alt 6.3) | pending |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | yes (Dedup-EDGE/#945 note) | pending |
| `CHANGELOG.md` | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `validate-sdlc-plan.sh` PASS |
| 2 | Failing tests written, test cases designed | yes | RED: missing `@/lib/persona-save-error`; then GREEN |
| 3 | Suite green, coverage held, docs updated | yes (local) | Vitest 361; TS-0015 21/21; docs updated |
| 4 | CI green, review approved, no conflicts | pending | wait main PW `37109679235` then push/PR |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

Push/PR deliberately deferred until main Playwright E2E on SHA `24cbe4b0`
(run `37109679235`) reaches a terminal conclusion (serialize Playwright-heavy).
