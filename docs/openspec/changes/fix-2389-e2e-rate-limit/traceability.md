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
| Issue | #2389 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/fix-2389-e2e-rate-limit/` | proposed |
| Branch | `fix/2389_e2e_rate_limit` | created |
| Tasks | `tasks.md` | Gate 1 complete; implementation pending |
| Commits | <!-- SHAs, one per line --> | pending |
| Pull Request | #<!-- number --> | pending |
| CI run | <!-- workflow run URL --> | pending |
| Merge commit | <!-- SHA --> | pending |
| Release / tag | <!-- vX.Y.Z --> | pending |
| Smoke test | <!-- environment + result --> | pending |

## Requirement coverage

<!-- Every `#### Scenario:` in the delta spec is an Acceptance Criterion and must
     appear here with the test that proves it. Requirement coverage is not code
     coverage: a green suite with an unmapped scenario means the criterion is
     unverified. -->

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| 429 logged with key type and path, no PII (user key) | `ApiRateLimitFilterTest` — user-key scenario | pending |
| 429 logged with key type and path, no PII (ip key) | `ApiRateLimitFilterTest` — ip-key scenario | pending |
| Concurrent first requests all counted | `JpaApiRateWindowStoreConcurrencyTest` | pending |
| E2e stack rate limit is 20000, prod stays 2000 | `test_e2e_rate_limit_env.py` | pending |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes (Fix entry) | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | #2389, this change, CU76 |
| 2 | Failing tests written, test cases designed | <!-- --> | <!-- --> |
| 3 | Suite green, coverage held, docs updated | <!-- --> | <!-- --> |
| 4 | CI green, review approved, no conflicts | <!-- --> | <!-- --> |
| 5 | Deployed, smoke test passed, Issue closed | <!-- --> | <!-- --> |

## Exceptions

<!-- Constitution §12 allows exceptions only in extreme circumstances and with
     explicit human approval. Record any exception here: what was skipped, who
     approved it, and why. An empty section means no exception was taken. -->

None taken.
