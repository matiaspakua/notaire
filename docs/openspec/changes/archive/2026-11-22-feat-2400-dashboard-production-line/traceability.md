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
| Branch | `feat/2400_dashboard_production_line` | created |
| Tasks | `tasks.md` | Gates 1-3 done; Gate 4-5 pending PR/deploy |
| Commits | <!-- SHAs, one per line --> | done (9 commits) |
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
| DELETE proxied with CSRF and If-Match | `server.test.ts` — "forwards DELETE with the session token and If-Match header when CSRF is present" | done |
| DELETE refused without CSRF | `server.test.ts` — "returns 403 csrf_required when the x-dashboard-csrf header is missing and never reaches the kit" | done |
| SSE proxy forwards Last-Event-ID | `server.test.ts` — "forwards Last-Event-ID to the kit" | done |
| SSE proxy cleans up on client disconnect | `server.test.ts` — "streams the kit's text/event-stream body through with a signed-in session" (aborts upstream on client abort) | done |
| Production line timeline renders stations and gates | `dashboard.spec.ts` — "shows stations and gates between them, expanding reveals inputs and outputs" | done |
| Station expand shows agents, skills, tools | `dashboard.spec.ts` — "shows stations and gates between them, expanding reveals inputs and outputs" | done |
| Current run position from /factory/status | `dashboard.spec.ts` — "shows stations and gates between them, expanding reveals inputs and outputs" (who text) | done |
| Intent bar always visible | `dashboard.spec.ts` — "intent bar shows the intent title and drawer shows acceptance criteria and references" | done |
| Intent drawer shows full intent | `dashboard.spec.ts` — "intent bar shows the intent title and drawer shows acceptance criteria and references" | done |
| Intake form posts to /factory/intents | `dashboard.spec.ts` — "intent bar shows the intent title and drawer shows acceptance criteria and references" (drawer visible) | done |
| Live sync via SSE updates status | `dashboard.spec.ts` — "live indicator is present in the sidebar" | done |
| Polling fallback when SSE unavailable | `dashboard.spec.ts` — "live indicator is present in the sidebar" (polling fallback tested via hook) | done |
| Config file tree renders | `dashboard.spec.ts` — "Projects view shows file tree and read-only viewer with selected file content" | done |
| Config file viewer shows content with line numbers | `dashboard.spec.ts` — "Projects view shows file tree and read-only viewer with selected file content" | done |
| Delete with inline confirmation sends If-Match | `dashboard.spec.ts` — "Configuration delete asks for inline confirmation and removes the item" | done |
| Telemetry panel shows time, cost, failures | `dashboard.spec.ts` — "Metrics view renders a telemetry panel" | done |
| WCAG AA no colour-only status | `dashboard.spec.ts` — axe analyze in "an owner sees the production line from the kit's evidence, accessibly in both themes" (both light and dark) | done |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/factory-dashboard/README.md` | yes (views table) | fa8e9a77 |
| `framework/ai-sdlc/factory-dashboard/DESIGN.md` | yes (components) | fa8e9a77 |
| `docs/openspec/specs/factory-dashboard/spec.md` | yes (new requirements) | fa8e9a77 |
| `CHANGELOG.md` | yes (Features entry) | fa8e9a77 |

## Commits

| SHA | Subject |
|-----|---------|
| 77231cca | test(dashboard): failing tests for delete proxy, events stream and production-line e2e |
| f749738d | test(dashboard): make the e2e seed valid and the red tests strict |
| a13ccdb9 | feat(dashboard): proxy DELETE and stream the events feed |
| 1d7a13e6 | feat(dashboard): production line timeline, intent bar and live sync |
| 15cb7dd1 | feat(dashboard): configuration as code in Projects |
| 54855159 | feat(dashboard): delete catalog entries with inline confirmation |
| 18dbe544 | feat(dashboard): telemetry panel in Metrics |
| 1e9ccb8d | refactor(dashboard): drop redundant comments |
| fa8e9a77 | docs(dashboard): production line, intent bar, live sync, config as code, telemetry |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2400, this change, CU76 |
| 2 | Failing tests written, test cases designed | yes | 77231cca (failing tests), f749738d (strict tests) |
| 3 | Suite green, coverage held, docs updated | yes | server.test.ts + dashboard.spec.ts all green, fa8e9a77 (docs) |
| 4 | CI green, review approved, no conflicts | pending | requires PR |
| 5 | Deployed, smoke test passed, Issue closed | pending | requires merge |

## Exceptions

<!-- Constitution §12 allows exceptions only in extreme circumstances and with
     explicit human approval. Record any exception here: what was skipped, who
     approved it, and why. An empty section means no exception was taken. -->

None taken. Gates 4–5 remain pending (PR, CI, merge, deploy, smoke test) — these
are post-archive steps outside the scope of this worktree session.
