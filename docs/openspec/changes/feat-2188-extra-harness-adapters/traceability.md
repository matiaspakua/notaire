# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2188 | open |
| Use Case | CU76 | exists |
| Specification | `docs/openspec/changes/feat-2188-extra-harness-adapters/` | passed |
| Branch | `cursor/feat-2188-harness-stubs-7c58` | created |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| stubs on harness list with sandbox false | ExtraHarnessStubsTest | pending |
| existing adapters remain distinct | ExtraHarnessStubsTest / HarnessAdoptionTest | pending |
| show claude-code stub | ExtraHarnessStubsTest | pending |
| dry plan refused while out of allow | ExtraHarnessStubsTest | pending |
| dry plan prints describe argv when allowed | ExtraHarnessStubsTest | pending |
| two adapters on Notaire | HarnessAdoptionTest | pending |
| Notaire surfaces stubs out of allow | HarnessAdoptionTest | pending |
| inventory names the three stubs | migration inventory / adoption | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/README.md` | no | pending |
| `framework/ai-sdlc/docs/adr/ADR-002-ports-and-trust.md` | no | pending |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-FOUNDATION.md` | no | pending |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-ROADMAP.md` | no | pending |
| `CHANGELOG.md` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2188, this folder |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None. Label flip to `status:in-progress` may fail (integration GraphQL); noted in PR.
