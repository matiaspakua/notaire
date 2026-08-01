# Issue Loop — Lessons Learned

Read this file in full at the start of every cycle (`.claude/agents/issue-loop.md`
step 0.5). Append a dated entry when a cycle hits real, observed friction — a wrong
assumption, a tool quirk, a process gap, a mistake that had to be corrected. Only
record things that actually happened; never hypothetical process changes. This file
travels through the normal branch/PR workflow like any other change — never edited
directly on main.

---

## 2026-08-01 — Corrupted loop-state.md from two parallel Routines (issue #750)

**What happened:** `.claude/loop-state.md` on `main` was found with a duplicated,
malformed `## Completed` section (repeated 3 times, two copies were just orphaned
`pr_url`/`blocked_reason` lines) and a `## Current` block claiming issue #592/PR
#749 was still `pr_open` — two days after that PR had actually merged. Root cause:
two Routines have been running against this repo in parallel since 2026-07-26 (the
every-8h one from #728, and an older twice-daily one that couldn't be disabled via
available tooling) — two independent processes doing read-modify-write on the same
file with no locking produces exactly this kind of interleaved/duplicated content.
Despite the corruption, the loop kept working overall (a long streak of clean
merges, #700-#749) — this was a real but non-fatal bug, not something that stopped
the loop.

**Fix applied:** Cleaned up the file (removed duplication, reset `## Current` to
idle since nothing was actually in flight — confirmed via a live GitHub check, zero
open PRs at the time).

**Lesson:** Don't trust `## Current`/`## Completed` blindly if they look
inconsistent with GitHub reality (e.g. `status: pr_open` for a PR that's actually
already merged, or a section header that appears more than once). At the start of
a cycle (step 0/0.5), if `loop-state.md` looks malformed or its `Current` block
references a PR/issue that GitHub shows as already closed/merged, treat that as
corruption from a concurrent writer rather than real in-flight state — verify
against GitHub directly (which is always the source of truth for issue/PR state
per the 2026-07-26 lesson above) and self-heal the file rather than propagating
the stale pointer forward. This can't be fully prevented without a real locking
mechanism the loop doesn't have; the deeper fix is ensuring only one Routine runs
against this repo at a time, which is outside what any single cycle can enforce.

## 2026-07-26 — Triage stalled on legacy backlog (issue #728)

**What happened:** Two consecutive scheduled cycles (after the run that merged PR
#726) produced nothing — no branch, no PR, no `blocked` status, no email. The prior
TRIAGE step fetched exactly one open issue at a time
(`list_issues(..., perPage=1)`, oldest-first) and re-queried after each skip. This
repo has 290 open issues, and the oldest ~100 are legacy `RF-XX`/`RNF-XX`
requirement-tracking issues that the skip-policy explicitly excludes. A run had to
step through the entire legacy wall one issue at a time before ever reaching
actionable work, and apparently never got there.

**Fix applied:** TRIAGE now batch-fetches `perPage=100`, filters `completed[]` and
skip-policy matches client-side against the whole page, and picks the first
survivor — paging further (capped at 5 pages) only if an entire page is filtered
out. See `.claude/agents/issue-loop.md` step 1.

**Lesson:** When a filter is applied client-side after a paginated fetch, always
verify the filter can't consume an entire page (or many pages) of results before
finding a match. A "fetch one, check one" loop with no cursor-advancement logic is
a stall risk whenever the unfiltered data is large and unevenly distributed — check
the actual shape of the backlog (label distribution, creation-date clustering)
before trusting `perPage=1`-style triage on it.

## 2026-07-26 — Abandoned malformed PR left stale state (issue #728)

**What happened:** PR #719 (`ci/688_harden_github_actions`, 130 files changed) was
opened with a completely unfilled PR template body and closed 16 seconds later. The
underlying issue (#688) had already been closed a day earlier — its scope had been
split into smaller issues (#671–675, #661) that were each completed individually.
The stale branch was left on the remote and the empty-bodied PR sat closed-but-
visible in PR history.

**Lesson:** Before opening a PR, confirm the target issue is still open
(`issue_read` immediately before `create_pull_request`, not just once at TRIAGE
time — issues can be closed by other work between when a cycle starts and when it
gets to step 7). Always fill in the PR body per the template before creating it —
never call `create_pull_request` with placeholder/template text as the body. If a
PR turns out to target already-completed work, close it with a comment explaining
why, and delete the branch in the same step rather than leaving both dangling.

## 2026-07-26 — `loop-state.md`'s Completed log had gone stale (issue #728)

**What happened:** The "Completed" section in `loop-state.md` stopped being updated
at issue #651 (merged 2026-07-21) despite dozens of later merges (#671–#726) — step
9 says to append to it every cycle, but it silently fell out of sync.

**Lesson:** `completed[]` is a redundant cache — GitHub's own issue state (closed
vs. open) is the real source of truth, and TRIAGE already filters on `state=OPEN`
regardless of `completed[]`. Its only job is to prevent re-triaging an issue whose
GitHub-close hasn't propagated yet within the same cycle. Keep entries terse (issue
number + one line) specifically so "append every cycle" stays cheap enough to
actually happen — a log that costs real effort to update every time is a log that
stops getting updated.
