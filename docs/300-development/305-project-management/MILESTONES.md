# Milestones

One milestone per issue. Titles below are the canonical strings. The em dash
(`—`) and the hyphen (`-`) are different milestones; do not merge them by eye.

Counts are the **target** layout from the 2026-10-12 dry run
([roadmap-snapshot.json](roadmap-snapshot.json)), not the live board until a
maintainer applies [ROADMAP.md](ROADMAP.md).

## Delivery trains (open)

These are what a manager schedules. Progress uses closed ÷ (open + closed)
**on that train only**.

| Milestone | Open | Closed | Closed share | Due marker | What belongs here |
|-----------|------|--------|--------------|------------|-------------------|
| Phase 4 — Modern Frontend | 15 | 15 | 50% | 2026-12-31 | Remaining Next.js use-case screens, including [#898](https://github.com/matiaspakua/notaire/issues/898). Reopen milestone **#9**. |
| Phase 6 — Deprecation | 3 | 2 | 40% | 2027-01-31 | Legacy `jpa` retirement that is not a critical production blocker. Reopen milestone **#11**. [#899](https://github.com/matiaspakua/notaire/issues/899) moves here from v1.1. |
| v1.0 — Production Ready | 33 | 32 | 49% | 2027-03-31 | Critical and high security, integrity, and release blockers (milestone **#12**). |
| v1.1 — Hardening | 40 | 27 | 40% | 2027-06-30 | Medium quality, performance, tests, and docs (milestone **#13**). |
| Backlog | 17 | 9 | 35% | none | Low priority and owner decisions (milestone **#14**). |

Due dates are **sequence markers** so the GitHub roadmap view can draw bars.
They are not an effort estimate.

**108** open issues are claimable across those trains. The other open issues are
the scope catalog below.

## Scope catalogs (open, not claimable)

Agents do not pick these. They stay open so requirements stay traceable.
Putting them on a delivery train would freeze that train below 100% forever.

| Milestone | Open | Contains |
|-----------|------|----------|
| Scope — Functional requirements | 94 | Issues titled `RF-*` or `[RF]` |
| Scope — Use cases | 69 | `CASO-DE-USO` issues titled `CU*` |
| Scope — Non-functional requirements | 24 | Issues titled `RNF-*` |

The requirements tracker [#902](https://github.com/matiaspakua/notaire/issues/902)
is not itself an RF/RNF row. It stays claimable work on **v1.0** (priority high).

NFR rows still name a delivery train in the snapshot field `nfr_trains`
(6 security → v1.0, 6 experience → Phase 4, 4 performance → v1.1, 8 platform → Backlog).

## History (closed)

Closed issues that **already** had a milestone keep it. That preserves the
older numbering, including titles that clash with the SAD:

| Existing milestone | Closed | Note |
|--------------------|--------|------|
| Phase 0 - Foundations (#1) | 6 | Early platform |
| Phase 1 - Backend Complete (#2) | 15 | Older "phase 1" name |
| Phase 2 - Kubernetes & Observability (#3) | 5 | Older observability slice |
| Phase 3 - Modern Frontend (#4) | 3 | Older numbering. Not SAD Phase 3 |
| Phase 4 - Production Ready (#5) | 2 | Older numbering. Not the open Phase 4 train |
| Phase 6 — Deprecation (#11) | 2 | Stays; the milestone is reopened for the 3 open items |

Closed issues with **no** milestone are grouped by `phase:*` label, otherwise
by close date:

| New closed milestone | Closed | Rule |
|----------------------|--------|------|
| Phase 1 — Analysis & foundation | 31 | Closed on or before 2026-05-31, or `phase:1-analysis` |
| Phase 2 — Foundation | 0 in this snapshot | Only `phase:2-foundation` (none unlabeled in that way on this date) |
| Phase 3 — Backend API | 49 | Closed in June 2026, or `phase:3-backend-api`. This is SAD Phase 3 |
| Phase 4 — Frontend deliveries (history) | 114 | Closed in July–August 2026, or closed `phase:4-frontend`. Separate from the open Phase 4 train so the progress bar is not inflated |
| Phase 5 — Observability & production audit | 77 | Closed in September 2026, or `phase:5-observability` |
| Cloud AI SDLC | 117 | Closed from 2026-10-01 onward with no phase label |

Historical volume in those closed cohorts: **419** issues. Together with the
closed issues already sitting on delivery milestones, that is the full **504**
closed set.

SAD milestones **#6, #7, #8, and #10** were deleted. Do not recreate those
numbers. The titles in this file replace those links.

## What the classifier will not do

- It will not close an issue, even when the title looks like a probe
  (`test-permission`, `test create permission`, `test-agent-create`). Those
  land on **Backlog** / `lane:owner` for a person to judge.
- It will not finish [#1248](https://github.com/matiaspakua/notaire/issues/1248).
  That issue still requires a per-issue decision: still valid, done, duplicate,
  or obsolete.
