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
| Delivery (open) | Six dated trains from `2026-11 · Release blockers` through `2027-06 · Backlog` |
| History (closed) | Curated phase buckets plus Q2, Q3, and Q4 close-date cohorts |

Every issue has exactly one milestone. The full list, assignment precedence,
and read-only verification are in
[MILESTONES.md](../300-development/305-project-management/MILESTONES.md).

The Project views separate concerns:

- **Roadmap** — closed history through future delivery, grouped by Milestone
- **KANBAN** — open non-catalog work, grouped by Status
- **Agent queue** — open candidates
- **Needs triage** — open candidates missing priority metadata
- RF / CU / RNF — dedicated catalog views, never an agent queue

## Labels

| Kind | Labels |
|------|--------|
| Area (one or more) | `BACKEND`, `FRONTEND`, `DB`, `DEVOPS`, `TEST`, `DOC` |
| Type | `bug`, `enhancement`, `REFACTOR`, `tech-debt`, `chore`, `ci`, `dependencies`, `TAREA` |
| Priority (exactly one on claimable work) | `priority:critical`, `priority:high`, `priority:medium`, `priority:low` |
| Concern | `security`, `a11y`, `i18n`, `risk`, `ux` |
| Phase | `phase:1-analysis` through `phase:6-deprecation` |
| Workflow | `in-progress`, `in-review`, `blocked`, `needs-info`, `ready-for-dev` |
| Traceability | `requerimiento-funcional`, `requerimiento-no-funcional`, `CASO-DE-USO` |
| Source | `audit-2026-09` (findings of the [2026-09 audit](PRODUCTION-READINESS-AUDIT-2026-09.md)), `roadmap` |

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
