# GitHub Organization

How work is tracked on [matiaspakua/notaire](https://github.com/matiaspakua/notaire).
The process that uses this structure (issue → OpenSpec → branch → PR) is defined in
[CONSTITUTION.md](../../CONSTITUTION.md).

## Board

Everything is on **[Notaire — Delivery Board](https://github.com/users/matiaspakua/projects/1)**.

| Status | Meaning |
|--------|---------|
| Todo | Open, not started |
| In Progress | Branch open; the issue also carries the `in-progress` label |
| Done | Issue closed |

Board status is the **work item** lifecycle. It is not a management state.
`In Progress` on an issue is not `En Tramite` on a deed file. Domain states
live in [CICLO-DE-VIDA.md](../200-architecture/203-design/CICLO-DE-VIDA.md).

**Coherence (mandatory):** Issue `state`, Project Status, and workflow labels
must stay aligned (CONSTITUTION Gate 5 / #1550). Project Status options are
**Todo / In Progress / Done only** — there is no Blocked column; encode blocked
work with the `blocked` label.

| Signal | Status | Labels | Issue state |
|--------|--------|--------|-------------|
| Backlog / ready | Todo | domain labels only | OPEN |
| Active WIP | In Progress | `in-progress` | OPEN |
| Waiting on dependency | Todo *(or In Progress if still owned)* | `blocked` | OPEN |
| Shipped / AC met | Done | no `in-progress` / `blocked` | **CLOSED** |

Validate:

```bash
python3 workspace/sdlc/check-board-status-label-coherence.py --scope pr   # CI / preflight
python3 workspace/sdlc/check-board-status-label-coherence.py --scope wip  # In Progress column
python3 workspace/sdlc/check-board-status-label-coherence.py --scope all  # full board hygiene
```

Without `gh` auth the checker skips with a clear message (never silent pass).

The `in-review` label marks a pull request under review. The board column
stays In Progress until the issue is closed.

```mermaid
stateDiagram-v2
  [*] --> Todo
  Todo --> InProgress: Branch and in-progress label
  InProgress --> Done: Merged and issue closed
```

## Milestones

Scheduling, history, and the requirements catalog are defined in
[Project management](../300-development/305-project-management/README.md).
A semver release is not a milestone
([RELEASES.md](../300-development/305-project-management/RELEASES.md)).
Short form:

| Kind | Milestones |
|------|------------|
| Release trains (open) | `v0.3 · Hexagonal core & security (2026-11)` through `v1.0 · GA: legacy-free (2027-03)` |
| Continuous (open) | `Backlog · Unscheduled`, `Catalog · Requirements & use cases` |
| History (closed) | Stages `H0`–`H8`, shipped cuts `v0.1`/`v0.2`, and Q2/Q3/Q4 close-date cohorts |

Every issue has exactly one milestone. The full list, assignment precedence,
and read-only verification are in
[MILESTONES.md](../300-development/305-project-management/MILESTONES.md).

The Project views (#1860; filters and readable group/sort checked by
`apply_milestone_policy.py --check`). Contracts live in
`workspace/sdlc/milestone_policy.py` (`VIEW_CONTRACTS`, `VIEW_FIELDS`,
`VIEW_LAYOUT`). **Current train** uses `current_train()` (earliest open entry in
`SCHEDULED`); a train cut updates `SCHEDULED` / `RELEASE` and the filter follows.

| View | Group / columns | Sort (when API returns it) |
|------|-----------------|----------------------------|
| Kanban | columns by Status | Milestone ASC |
| Current train | group by Status | Priority DESC |
| Agent queue | group by Milestone | Priority DESC |
| In flight | — | Updated at DESC |
| Needs triage | group by Milestone | Created at ASC |
| Blocked / needs info | — | Updated at DESC |
| Release roadmap | group by Milestone | — |
| Shipped | group by Milestone | Closed at DESC |
| Catalog | — | Title ASC |

**UI-only** (GraphQL `updateProjectV2View` only sets name/layout/filter/
`visibleFieldIds` — confirm in the Project UI after each Owner apply).
`--check` prints `UI step — …` when group/sort is still empty (not a mismatch);
it fails when the API returns a non-empty value that disagrees with `VIEW_LAYOUT`:

- horizontal group-by on table views (Agent queue, Needs triage, Shipped, …)
- table sort when `sortByFields` is empty
- column width / order beyond `visibleFieldIds`
- roadmap date-field mapping (Start/Target on the roadmap axis)
- board card fields beyond Status columns

Owner scripts: `workspace/sdlc/owner/` (preview default; see that README).
Closed issues stuck off Done: `python3 workspace/sdlc/sync_closed_status_done.py`.

Priority, area and kind are labels; the project has no Priority/Area/Kind fields.

## Labels

Prefixed families; until phase 2 every legacy name (right) is still read as its new name.

| Kind | Labels | Legacy |
|------|--------|--------|
| Type (exactly one) | `type:feature`, `type:bug`, `type:refactor`, `type:chore`, `type:ci`, `type:epic` | `enhancement`, `bug`, `REFACTOR`, `chore`/`TAREA`, `ci`, `roadmap` |
| Area (one or more) | `area:backend`, `area:frontend`, `area:db`, `area:devops`, `area:test`, `area:docs` | `BACKEND` … `DOC` |
| Priority (exactly one on claimable work) | `priority:critical`, `priority:high`, `priority:medium`, `priority:low` | unchanged |
| Status (workflow) | `status:ready`, `status:in-progress`, `status:in-review`, `status:blocked`, `status:needs-info` | `ready-for-dev`, `in-progress`, `in-review`, `blocked`, `needs-info` |
| Concern | `concern:security`, `concern:a11y`, `concern:i18n`, `concern:ux`, `concern:risk`, `concern:tech-debt` | unprefixed |
| Traceability | `trace:rf`, `trace:rnf`, `trace:cu` | `requerimiento-funcional`, `requerimiento-no-funcional`, `CASO-DE-USO` |
| Source / process / release | `source:audit-2026-09`, `process:sdlc-exception`, `release:breaking` | `audit-2026-09`, `sdlc-exception` |

Agent lanes are derived from area/type labels; `lane:*` labels do not exist.
The full contract, including labels that must not be duplicated, is
[LABELS.md](../300-development/305-project-management/LABELS.md).
Parallel claims are [PARALLEL-AGENTS.md](../300-development/305-project-management/PARALLEL-AGENTS.md).

Run `gh label list` for the live list. Do not create a label that repeats an existing one in
another language. `FRONTEND` means the Next.js client. Swing is not a target.

## Issue checklist

Every claimable issue has a Use Case/RF/RNF reference, acceptance criteria, an
area label, one priority, and one delivery milestone. Catalog anchors are
identified by traceability labels and are not picked by agents. Issue templates
live in [`.github/ISSUE_TEMPLATE/`](../../.github/ISSUE_TEMPLATE/).
