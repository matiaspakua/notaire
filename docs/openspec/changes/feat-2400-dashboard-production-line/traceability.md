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
| Issue | #2400 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/feat-2400-dashboard-production-line/` | proposed (Gate 1 owner-pending) |
| Branch | `feat/2400_dashboard_production_line` | pending |
| Tasks | `tasks.md` | Gate 1 done; implementation pending |
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
| DELETE proxied with CSRF and If-Match | `server.test.ts` | pending |
| SSE proxy forwards Last-Event-ID | `server.test.ts` | pending |
| SSE proxy cleans up on client disconnect | `server.test.ts` | pending |
| Production line timeline renders stations and gates | `tests/e2e/production-line.spec.ts` | pending |
| Station expand shows agents, skills, tools | `tests/e2e/production-line.spec.ts` | pending |
| Current run position from /factory/status | `tests/e2e/production-line.spec.ts` | pending |
| Intent bar always visible | `tests/e2e/intent-bar.spec.ts` | pending |
| Intent drawer shows full intent | `tests/e2e/intent-bar.spec.ts` | pending |
| Intake form posts to /factory/intents | `tests/e2e/intent-bar.spec.ts` | pending |
| Live sync via SSE updates status | `tests/e2e/live-sync.spec.ts` | pending |
| Polling fallback when SSE unavailable | `hooks.test.ts` | pending |
| Config file tree renders | `tests/e2e/config-viewer.spec.ts` | pending |
| Config file viewer shows content with line numbers | `tests/e2e/config-viewer.spec.ts` | pending |
| Delete with inline confirmation sends If-Match | `tests/e2e/catalog-delete.spec.ts` | pending |
| Telemetry panel shows time, cost, failures | `tests/e2e/telemetry.spec.ts` | pending |
| WCAG AA no colour-only status | `tests/e2e/a11y.spec.ts` | pending |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/factory-dashboard/README.md` | yes (views table) | pending |
| `framework/ai-sdlc/factory-dashboard/DESIGN.md` | yes (components) | pending |
| `docs/openspec/specs/factory-dashboard/spec.md` | yes (new requirements) | pending |
| `CHANGELOG.md` | yes (Features entry) | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | owner-pending | #2400, this change, CU76 |
| 2 | Failing tests written, test cases designed | <!-- --> | <!-- --> |
| 3 | Suite green, coverage held, docs updated | <!-- --> | <!-- --> |
| 4 | CI green, review approved, no conflicts | <!-- --> | <!-- --> |
| 5 | Deployed, smoke test passed, Issue closed | <!-- --> | <!-- --> |

## Exceptions

<!-- Constitution §12 allows exceptions only in extreme circumstances and with
     explicit human approval. Record any exception here: what was skipped, who
     approved it, and why. An empty section means no exception was taken. -->

None taken. This spec PR leaves implementation task groups 3–8 unchecked by design
(Gate 1 is owner-pending); `check-openspec-archive-hygiene.py` reports them until
implementation lands on this change.
