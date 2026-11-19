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
| Issue | #<!-- number --> | <!-- open / in-progress / closed --> |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/feat-2336-repolens-speed-accuracy-multirepo/` | passed |
| Branch | `feat/2336_repolens-speed-callers-multirepo` | created |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

<!-- Every `#### Scenario:` in the delta spec is an Acceptance Criterion and must
     appear here with the test that proves it. Requirement coverage is not code
     coverage: a green suite with an unmapped scenario means the criterion is
     unverified. -->

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| same-named methods on several classes | repolens tests (test_types, test_daemon, test_cache, test_repos, test_interfaces) | pending |
| chains, generics, lambdas and method references | repolens tests (test_types, test_daemon, test_cache, test_repos, test_interfaces) | pending |
| a library method is not linked | repolens tests (test_types, test_daemon, test_cache, test_repos, test_interfaces) | pending |
| measured against the compiler | repolens tests (test_types, test_daemon, test_cache, test_repos, test_interfaces) | pending |
| callers through a port | repolens tests (test_types, test_daemon, test_cache, test_repos, test_interfaces) | pending |
| the second query is served by the service | repolens tests (test_types, test_daemon, test_cache, test_repos, test_interfaces) | pending |
| the service follows HEAD and stops when idle | repolens tests (test_types, test_daemon, test_cache, test_repos, test_interfaces) | pending |
| disabled | repolens tests (test_types, test_daemon, test_cache, test_repos, test_interfaces) | pending |
| a new machine imports the cache | repolens tests (test_types, test_daemon, test_cache, test_repos, test_interfaces) | pending |
| skipped paths | repolens tests (test_types, test_daemon, test_cache, test_repos, test_interfaces) | pending |
| registry | repolens tests (test_types, test_daemon, test_cache, test_repos, test_interfaces) | pending |
| federated search | repolens tests (test_types, test_daemon, test_cache, test_repos, test_interfaces) | pending |
| schema budget | repolens tests (test_types, test_daemon, test_cache, test_repos, test_interfaces) | pending |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/repolens/README.md` | no | pending |
| `framework/ai-sdlc/repolens/bench/results/factory-kpis.json` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2336, this folder |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
