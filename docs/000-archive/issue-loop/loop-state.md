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

GitHub issue state (closed vs. open) is the real source of truth — TRIAGE filters
on `state=OPEN` regardless of what's listed here. This log's only job is to skip
re-triaging an issue within the same run before its GitHub-close has propagated, so
keep entries to one line each (issue# — one-line summary — PR#) or this goes stale
again (see `.claude/loop-lessons.md`, 2026-07-26 entry). Don't hand-write history
for old cycles — `git log --oneline main` and closed issues on GitHub already have
the full record.

592 and many others (#591, #661, #671-675, #692-693, #699, #703-731 range) —
completed 2026-07-24 through 2026-07-30, see git log / closed issues on GitHub for
the full list; not hand-transcribed here per the lesson above.

688 — CI/CD hardening umbrella issue — closed 2026-07-23 (scope split into
671-675/661 above, each completed individually). A later cycle mistakenly reused
the stale `ci/688_harden_github_actions` branch after this closed and opened a
malformed empty-bodied PR (#719); see loop-lessons.md.

## Policy (skip rules)

- Skip RF-XX / RNF-XX requirement-tracking issues and CU-* use-case issues (documentation only)
- Skip TAREA-labeled and large architectural issues
- Skip pure `docs(...)`-only guide issues
- Defer issues requiring 25+ file touches or multi-week cleanup (flag with written reason, don't partially close)
