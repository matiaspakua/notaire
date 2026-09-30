# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows
> below Tasks stay `pending` until the corresponding step actually happens.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1049 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | n/a |
| Specification | `openspec/changes/delete-stale-jenkinsfile-1049/` | written |
| Branch | `chore/1049_delete_stale_jenkinsfile` | created |
| Tasks | `tasks.md` | pending |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | n/a | pending |
| Smoke test | pending | pending |

## Requirement coverage

<!-- skip_specs: true — no delta spec scenarios. Acceptance Criteria are the
     Issue's checklist items instead. -->

| Acceptance Criterion (Issue #1049) | Verification | Status |
|----------------------------------|--------------|--------|
| Legacy Jenkinsfile removed from repo | File no longer exists | working |
| CI pipeline passes after removal | `scripts/preflight.sh --fix`, `mvn test`, `npx vitest run` executed successfully | passing |
| No unrelated tests or code changed | No new test files added | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
<!-- No permanent documentation updates are required -->

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | Issue #1049; proposal file |
| 2 | Failing tests written, test cases designed | N/A | Not applicable; logic change only removes a file |
| 3 | Suite green, coverage held, docs updated | yes | Local preflight and test runs succeeded |
| 4 | CI green, review approved, no conflicts | pending | To be filled after PR |
| 5 | Deployed, smoke test passed, Issue closed | pending | To be filled after merge |

## Definition of Done

- Issue linked and in progress.
- Specification written (proposal.md).
- Acceptance Criteria satisfied through local checks.
- No code behavior changes; no new tests required.
- All CI gates succeed after merge.
- Commit and PR atomic and conventional.
- PR merged, smoke test passes.
- Traceability.md complete.

## Exceptions

This change only removes a legacy CI artifact; it does not modify application
behaviour or add new tests. The `skip_specs: true` marker in
`.openspec.yaml` signals that there are no delta spec scenarios to include.
