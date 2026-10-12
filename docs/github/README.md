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

The schedule, the history of the two old phase schemes, priority, and the lanes
agents use to work in parallel are in
[PROJECT-MANAGEMENT.md](PROJECT-MANAGEMENT.md). Counts and the chart are on the
GitHub Pages technical docs tab (`/docs/management/`).

Short version:

| Train | Due | Holds |
|-------|-----|--------|
| 2026-11 · Release blockers | 2026-11-30 | Critical work and security NFRs |
| 2026-12 · Frontend product | 2026-12-31 | Phase 4 scope: use cases, functional requirements, Next.js behavior |
| 2027-01 · Quality & traceability | 2027-01-31 | Tests, docs, performance and process NFRs |
| 2027-02 · Platform & operability | 2027-02-28 | CI and operability implementation |
| 2027-03 · Legacy retirement | 2027-03-31 | `jpa` retirement (#899) |
| 2027-06 · Backlog | 2027-06-30 | Low priority and environment NFRs |

Requirements and use cases are **on** a train so scope is visible. They are not
pickable. Apply the assignment with
`python3 workspace/sdlc/apply_milestone_policy.py --apply`
(needs Issues and Projects write). `--check` reports drift.

## Labels

| Kind | Labels |
|------|--------|
| Area (one or more) | `BACKEND`, `FRONTEND`, `DB`, `DEVOPS`, `TEST`, `DOC` |
| Type | `bug`, `enhancement`, `REFACTOR`, `tech-debt`, `chore`, `ci`, `dependencies`, `TAREA` |
| Priority (exactly one) | `priority:critical`, `priority:high`, `priority:medium`, `priority:low` |
| Concern | `security`, `a11y`, `i18n`, `risk` |
| Workflow | `in-progress`, `in-review`, `blocked`, `needs-info`, `ready-for-dev` |
| Traceability | `requerimiento-funcional`, `requerimiento-no-funcional`, `CASO-DE-USO` |
| Historical | `phase:1-analysis` … `phase:6-deprecation` (do not schedule with these) |
| Source | `audit-2026-09` (findings of the [2026-09 audit](PRODUCTION-READINESS-AUDIT-2026-09.md)) |

Run `gh label list` for the live list. Do not create a label that repeats an existing one in
another language.

## Issue checklist

Every work issue has: a Use Case reference (CU), acceptance criteria, one area label,
one priority label and a scheduled milestone. Catalog anchors (RF / RNF / CU) have a
milestone and no priority. Issue templates live in
[`.github/ISSUE_TEMPLATE/`](../../.github/ISSUE_TEMPLATE/).
Lanes and the foreman pick rule: [PROJECT-MANAGEMENT.md](PROJECT-MANAGEMENT.md).
