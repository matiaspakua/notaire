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
| Issue | #2208 | open |
| Use Case | CU76 | exists |
| Specification | `docs/openspec/changes/feat-2208-forgeport-write-attest/` | passed |
| Branch | `cursor/feat-2208-forgeport-write-attest-44ce` | created |
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
| dry-run default: write_enabled=false, no gh mutation | ForgePortAttestTest | pending |
| without Approval verify → refuse; attested=false | ForgePortAttestTest | pending |
| verified Approval dry-run → attested=true, write_enabled=false | ForgePortAttestTest | pending |
| --write without verify → refuse write | ForgePortAttestTest | pending |
| --write + verify → scoped comment/label/check-run only | ForgePortAttestTest | pending |
| unscoped mutation verbs still refused | ForgePortAttestTest | pending |
| doctor reports forge write honesty without failing alone | ForgePortAttestTest | pending |
| JSON honesty fields present | ForgePortAttestTest / CliTest | pending |

## Permanent documentation updated

| Document | Action | Status |
|----------|--------|--------|
| ADR-002 | Update ForgePort row | pending |
| ADR-005 | Pointer to write gated by Approval | pending |
| FACTORY-FOUNDATION / FACTORY-ROADMAP | Honesty update | pending |
| kit README / METRICS.md | CLI + boundary | pending |

## Commits

| SHA | Message | Status |
|-----|---------|--------|
| pending | pending | pending |

## Pull Request

| Field | Value |
|-------|-------|
| URL | pending |
| CI | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2208, this folder |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
