# Issue Loop State

Single source of truth for the autonomous issue-loop (`.claude/agents/issue-loop.md`).
Read at the start of every cycle; write before ending every cycle.

## Current

- current_issue: 592
- status: in_progress
- branch: fix/592-split-monolithic-e2e-tour
- pr_url: null
- blocked_reason: null

## Completed (most recent first)
- pr_url: null
- blocked_reason: null

## Completed (most recent first)

GitHub issue state (closed vs. open) is the real source of truth — TRIAGE filters
on `state=OPEN` regardless of what's listed here. This log's only job is to skip
re-triaging an issue within the same run before its GitHub-close has propagated, so
keep entries to one line each (issue# — one-line summary — PR#) or this goes stale
again (see `.claude/loop-lessons.md`, 2026-07-26 entry). Don't hand-write history
for old cycles — `git log --oneline main` and closed issues on GitHub already have
the full record.

675, 674, 673 — infra/security hardening (postgres-exporter creds, Prometheus
root+docker.sock, CORS wildcard Authorization) — PRs #724, #723, #722, merged
2026-07-24/25.
720, 717, 661, 672, 671 — test/security fixes (HTTP integration auth, Swagger
prod-test context, H2 test isolation, default creds, Swagger prod guard) — PRs
#721, #718, #709, #708, #707, merged 2026-07-24.
705, 703 — CI pipeline hardening, scheduled-loop settings fix — PRs #706, #704,
merged 2026-07-24.
715, 713 — agent-loop lessons + changelog docs — PRs #716, #714, merged 2026-07-24.
701 — ESLint circular crash fix — PR #702, merged 2026-07-23.
651, 650, 648-649 and earlier — see git log / closed issues on GitHub.

688 — CI/CD hardening umbrella issue — closed 2026-07-23 (scope split into
671-675/661 above, each completed individually). A later cycle mistakenly reused
the stale `ci/688_harden_github_actions` branch after this closed and opened a
malformed empty-bodied PR (#719); see loop-lessons.md.

## Policy (skip rules)

- Skip RF-XX / RNF-XX requirement-tracking issues and CU-* use-case issues (documentation only)
- Skip TAREA-labeled and large architectural issues
- Skip pure `docs(...)`-only guide issues
- Defer issues requiring 25+ file touches or multi-week cleanup (flag with written reason, don't partially close)
