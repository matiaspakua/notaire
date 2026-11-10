# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
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
| Issue | #2205 | in-progress |
| Use Case | CU76 | exists |
| Specification | `docs/openspec/changes/feat-2205-approvalport-runtime/` | passed |
| Branch | `cursor/feat-2205-approvalport-runtime-1dd2` | created |
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
| without keys → no-evaluable / not live | ApprovalPortTest | pending |
| doctor reports Approval without failing for unevaluable | ApprovalPortTest | pending |
| agent self-approval forbidden / mint refuses agent | ApprovalPortTest | pending |
| mint→verify round-trip sets approvals_live | ApprovalPortTest | pending |
| tampered subject fails verify | ApprovalPortTest | pending |
| missing signer tooling / force-off degrades honestly | ApprovalPortTest | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/docs/adr/ADR-005-approval-port.md` | no | pending |
| `framework/ai-sdlc/docs/adr/ADR-002-ports-and-trust.md` | no | pending |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-FOUNDATION.md` | no | pending |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-ROADMAP.md` | no | pending |
| `framework/ai-sdlc/README.md` | no | pending |
| `docs/openspec/specs/approval/spec.md` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2205, this folder |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
