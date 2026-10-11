# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1366 | open → in progress |
| Use Case | RF-23 – Saber el estado de un trámite; RNF-09 – Interfaz consistente | exists |
| Specification | `docs/openspec/changes/feat-1366-status-badge/` | Gate 1 draft |
| Branch | `feat/1366_status_badge` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | pushed |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Every status column uses the badge | `status-badge.test.tsx` (source guard), `TS-0124` | passing |
| Tone, icon and text, never color alone | `status-badge.test.tsx`, `TS-0124` | passing |
| Unknown value shown as stored | `status-badge.test.tsx` | passing |
| Labels translated in es and en | `status-badge.test.tsx` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `frontend/messages/{es,en}.json` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh feat-1366-status-badge` |
| 2 | Failing tests written, test cases designed | yes | Vitest red (module missing); TS-0124 failing on the pre-change build |
| 3 | Suite green, coverage held, docs updated | yes | `bash frontend/verify.sh` green; full chromium suite (see PR) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and a backend from current `main` on :8080.
