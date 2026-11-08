# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2083 | closed by #2084; this change corrects the figure |
| Use Case | RNF-05 – Aspecto visual | exists |
| Specification | `docs/openspec/changes/docs-2083-hexagon-junction/` | passed |
| Branch | `feat/2083_hexagon-anatomy-figure` | created |
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
| The junction figure names the dependency rule | hexagon-figure.test.tsx | passing |
| Arrow keys move to the next gland | hexagon-figure.test.tsx | passing |
| Glands and cables stay on the skin | artifacts.test.ts | passing |
| The factory home points at the lifecycle figure | hexagon-figure.test.tsx | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | pending |
| `/docs/hexagonal/` and `/ai-sdlc/` | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + specification + acceptance criteria | yes | #2083, this folder |
| 2 | Tests written and green | yes | `cd github-page && npm test` |
| 3 | Docs updated | yes | CHANGELOG, public pages |
| 4 | CI green, review approved | pending | pending |
| 5 | Published and checked | pending | pending |

## Exceptions

#2083 was closed by #2084 before this correction. No process exception: the write set is the pages, the changelog and this change folder.
