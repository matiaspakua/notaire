# Issue Loop State

Single source of truth for the autonomous issue-loop (`.claude/agents/issue-loop.md`).
Read at the start of every cycle; write before ending every cycle.

## Current

- current_issue: null
- status: idle
- branch: null
- pr_url: null
- blocked_reason: null

## Completed (most recent first)

651 — security(config): DataInitializer ignores APP_ADMIN_USER/APP_ADMIN_PASSWORD
env vars — the follow-up filed while doing #565. Wired app.admin.username/
password into DataInitializer, extended ProductionCredentialsGuard. PR #652
merged 2026-07-21.

650 — security(config): added ProductionCredentialsGuard. PR #650 merged 2026-07-21.

648-649 — accessibility and test fixes. PRs merged 2026-07-21.

All 29+ PRs documented in prior cycles (see history below for complete log through #633).

## Policy (skip rules)

- Skip RF-XX / RNF-XX requirement-tracking issues and CU-* use-case issues (documentation only)
- Skip TAREA-labeled and large architectural issues
- Skip pure `docs(...)`-only guide issues
- Defer issues requiring 25+ file touches or multi-week cleanup (flag with written reason, don't partially close)
