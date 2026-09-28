# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the feature's ledger — SpecKit has no native equivalent, so it is a
> project-owned addition (mirrors `openspec/schemas/notaire-sdlc/templates/
> traceability.md`). It is created during `/speckit-plan` with the upstream
> links filled in, and completed as the feature moves through the gates. Rows
> below Tasks stay `pending` until the corresponding step actually happens —
> never pre-fill them.

## Chain

```
Issue → spec.md → plan.md → tasks.md → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #<!-- number --> | <!-- open / in-progress / closed --> |
| Use Case | <!-- CU-XX / RF-XX / RNF-XX --> | <!-- exists / created / updated --> |
| Specification | `speckit/specs/<NNN-feature-name>/` | <!-- --> |
| Branch | `<type>/<issue-number>_<description>` | <!-- --> |
| Tasks | `tasks.md` | <!-- n/N complete --> |
| Commits | <!-- SHAs, one per line --> | pending |
| Pull Request | #<!-- number --> | pending |
| CI run | <!-- workflow run URL --> | pending |
| Merge commit | <!-- SHA --> | pending |
| Release / tag | <!-- vX.Y.Z --> | pending |
| Smoke test | <!-- environment + result --> | pending |

## Requirement coverage

<!-- Every Given/When/Then Acceptance Scenario in spec.md is an Acceptance
     Criterion and must appear here with the test that proves it. Requirement
     coverage is not code coverage: a green suite with an unmapped scenario
     means the criterion is unverified. -->

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| <!-- scenario name --> | <!-- test class / spec file --> | <!-- pending / passing --> |

## Permanent documentation updated

<!-- Confirmed at Gate 3. Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| <!-- path --> | <!-- yes/no --> | <!-- SHA --> |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + spec.md + Acceptance Criteria | <!-- --> | <!-- --> |
| 2 | Failing tests written, test cases designed | <!-- --> | <!-- --> |
| 3 | Suite green, coverage held, docs updated | <!-- --> | <!-- --> |
| 4 | CI green, review approved, no conflicts | <!-- --> | <!-- --> |
| 5 | Deployed, smoke test passed, Issue closed | <!-- --> | <!-- --> |

## Exceptions

<!-- Constitution §12 allows exceptions only in extreme circumstances and with
     explicit human approval. Record any exception here: what was skipped, who
     approved it, and why. An empty section means no exception was taken. -->
