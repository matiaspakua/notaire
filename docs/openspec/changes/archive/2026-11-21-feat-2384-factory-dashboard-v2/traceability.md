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
| Issue | #2384 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/feat-2384-factory-dashboard-v2/` | passed |
| Branch | `feat/2381_factory_dashboard` | created |
| Tasks | `tasks.md` | groups 1–9 done; deviations D1–D7 recorded (D1 awaits the Owner) |
| Commits | `57e04e5f`…`11c65d08` on `feat/2381_factory_dashboard` (test/feat pairs, `Refs #2384`) | done |
| Pull Request | #2383 | open |
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
| a catalog edit | dashboard `tests/server.test.ts` "requires the dashboard CSRF header on writes and adds the kit's"; e2e "a writer creates a provider…" | passed |
| the kit is not reachable | `tests/server.test.ts` "answers kit_unreachable when the kit is down"; e2e "a workspace whose kit is down…" | passed |
| no fabricated run | e2e "an owner sees the production line from the kit's evidence" (counts come from `/factory/runs`; no demo data in the UI) | passed |
| two workspaces | `tests/server.test.ts` "lists workspaces…", "marks a workspace whose kit does not answer as unreachable" | passed |
| two named tokens | kit `tests/test_serve_tokens.py` (8 tests) | passed |
| a read-only user | kit `test_a_read_token_cannot_write_and_the_problem_names_the_scope`; e2e "a reader gets read-only screens" | passed |
| a Claude Code session | kit `tests/test_coach.py` (9 tests) | passed |
| through the contract | kit `test_serve_returns_the_cli_report_to_the_owner_only` | passed |
| another user's token | kit `test_serve_returns_the_cli_report_to_the_owner_only` (403 naming owner) | passed |
| a knowledge search | `tests/server.test.ts` "searches RepoLens for a signed-in user and keeps the repository label" | passed |
| a literal secret | kit `tests/test_serve_dashboard_paths.py` "a_literal_secret_is_refused"; e2e "a literal secret is refused" | passed |
| independence | `framework/repolens/tests/test_boundaries.py`; kit `tests/test_boundaries.py`; `workspace/sdlc/check-factory-boundaries.py` | passed |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/factory-dashboard/README.md`, `DESIGN.md`, `MODULE.md` | yes | `a136302b`, `11c65d08`, `05f3d259` |
| `framework/ai-sdlc/docs/adr/ADR-006-dashboard-contract-client.md` | yes | `a136302b` |
| `framework/ai-sdlc/improvements/REVIEW-2384.md` (review of SESSION-FEATURES.md) | yes | `a136302b` |
| `framework/ai-sdlc/README.md`, `CHANGELOG.md`, `openapi/factory-control.openapi.yaml` | yes | `a136302b`, `a9feda12`, `c906135c` |
| `framework/repolens/README.md`, `MODULE.md`, `CHANGELOG.md` | yes | `ca11902a`, `a136302b` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2384, this folder |
| 2 | Failing tests written, test cases designed | yes | test commits precede each feat/fix commit; red runs recorded in tasks 3.4 |
| 3 | Suite green, coverage held, docs updated | yes | kit 479 passed; RepoLens 142 passed; dashboard 16 server + 7 e2e; boundary check ok |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
