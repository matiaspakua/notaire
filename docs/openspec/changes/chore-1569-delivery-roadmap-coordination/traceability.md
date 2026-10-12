# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1569 | in-progress |
| Use Case | RNF-21 and RNF-22 — Methodology and documentation | exists |
| Specification | `docs/openspec/changes/chore-1569-delivery-roadmap-coordination/` | Gate 1 passed |
| Branch | `chore/1569_delivery_roadmap_coordination` | created |
| Tasks | `tasks.md` | planning complete |
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
| Closed issue coverage is complete | policy unit tests + `--check` | passing |
| Open issue coverage is complete | policy unit tests + `--check` | passing |
| Roadmap shows history through future planning | policy unit tests + Project v2 query | passing |
| Kanban remains an operational queue | policy unit tests + Project v2 query | passing |
| Agent selects the next issue | policy unit tests | passing |
| Agent claims and releases work coherently | board coherence checker | passing for #1569; live drift repaired |
| Agent or human finds current guidance | docs traceability + preflight | passing |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/305-project-management/` | yes | pending |
| `docs/github/README.md` | yes | pending |
| `docs/300-development/304-ai-sdlc-cloud/` | yes | pending |
| `github-page/app/docs/roadmap/page.tsx` | yes | pending |
| `AGENTS.md` | yes | pending |
| `CONSTITUTION.md` | yes | pending |
| `CHANGELOG.md` | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | strict OpenSpec + `validate-sdlc-plan.sh` |
| 2 | Failing tests written, test cases designed | yes | red import/assertion failures recorded before implementation |
| 3 | Suite green, coverage held, docs updated | <!-- --> | <!-- --> |
| 4 | CI green, review approved, no conflicts | <!-- --> | <!-- --> |
| 5 | Deployed, smoke test passed, Issue closed | <!-- --> | <!-- --> |

## Exceptions

None.
