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
| Specification | `docs/openspec/changes/fix-2389-e2e-rate-limit/` | implemented |
| Branch | `fix/2389_e2e_rate_limit` | created |
| Tasks | `tasks.md` | Gates 1-3 complete; PR/CI pending |
| Commits | 75bddf7c, 9a6d46c0, 26e3f75a, 6ce2758a, 1af11739, 8b1c884a | recorded |
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
| 429 logged with key type and path, no PII (user key) | `ApiRateLimitFilterTest` — user-key scenario | passed |
| 429 logged with key type and path, no PII (ip key) | `ApiRateLimitFilterTest` — ip-key scenario | passed |
| Concurrent first requests all counted | `JpaApiRateWindowStoreConcurrencyTest` | passed |
| E2e stack rate limit is 20000, prod stays 2000 | `test_e2e_rate_limit_env.py` | passed |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes (Fix entry) | 8b1c884a |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2389, this change, CU76 |
| 2 | Failing tests written, test cases designed | yes | 75bddf7c (red tests) |
| 3 | Suite green, coverage held, docs updated | yes | mvn verify 2628 tests, JaCoCo gate met; CHANGELOG 8b1c884a |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

<!-- Constitution §12 allows exceptions only in extreme circumstances and with
     explicit human approval. Record any exception here: what was skipped, who
     approved it, and why. An empty section means no exception was taken. -->

None taken.
