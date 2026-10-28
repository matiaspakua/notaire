# Traceability

> Governed by [CONSTITUTION.md](../../../../../CONSTITUTION.md) — P4 Traceability.
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
| Issue | #1971 | in-progress |
| Use Case | CU-76 | exists |
| Specification | `docs/openspec/changes/fix-1971-main-ci-ratchet/` | written (`skip_specs: true`) |
| Branch | `fix/1971_main_ci_ratchet_regressions` | created from `b11667c91` |
| Tasks | `tasks.md` | groups 1–9 complete |
| Commits | see PR | pending |
| Pull Request | see #1971 | pending |
| CI run | PR checks | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | `main` CI + Pages | pending |

## Requirement coverage

<!-- Every `#### Scenario:` in the delta spec is an Acceptance Criterion and must
     appear here with the test that proves it. Requirement coverage is not code
     coverage: a green suite with an unmapped scenario means the criterion is
     unverified. -->

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Catalog item ids copied, unmodifiable, null kept | `CatalogItemsRequestTest` | passing |
| Locale-independent not-found; null message → 400 | `IllegalArgumentResponsesTest` | passing |
| Document type defaults and stored flags | `DocumentTypeControllerTest` | passing |
| Config builds with JWT filter only | `SecurityAndCorsConfigTest` | passing |
| Checkstyle ≤ 3157 / SpotBugs ≤ 1183 | CI Coverage Gate / Code Quality | passing locally (3141 / 1180) |
| Generated types match `openapi.yaml` | Frontend CI TypeScript Check | passing locally |
| Runtime images upgrade Alpine packages | `workspace/tests/test_backend_image_os_patches.py` | passing |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | see PR |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1971, `proposal.md` |
| 2 | Failing tests written, test cases designed | yes | test compile failure; ratchets 3173 / 1187 |
| 3 | Suite green, coverage held, docs updated | yes (local) | 3141 / 1180; CHANGELOG |
| 4 | CI green, review approved, no conflicts | pending | PR checks |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

<!-- Constitution §12 allows exceptions only in extreme circumstances and with
     explicit human approval. Record any exception here: what was skipped, who
     approved it, and why. An empty section means no exception was taken. -->

None.
