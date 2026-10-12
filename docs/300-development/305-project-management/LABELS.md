# Labels

`gh label list` is the live set. This page is the contract: what each family
means, and which labels must not be duplicated.

## Required on claimable work

| Family | Cardinality | Labels |
|--------|-------------|--------|
| Priority | exactly one | `priority:critical`, `priority:high`, `priority:medium`, `priority:low` |
| Area | one or more | `BACKEND`, `FRONTEND`, `DB`, `DEVOPS`, `TEST`, `DOC` |

Scope-catalog issues (RF / CU / RNF milestones) do not need a priority label.
They are not claimable.

## Derived lanes

There are no `lane:*` labels. A lane is computed from existing labels by
`workspace/sdlc/milestone_policy.py`:

| Derived lane | Signal | Conflict |
|--------------|--------|----------|
| `security` | `security`, or critical backend work | blocks security and backend |
| `cross` | both `BACKEND` and `FRONTEND` | blocks cross, backend, and frontend |
| `backend` | `BACKEND` | blocks backend |
| `frontend` | `FRONTEND` | blocks frontend |
| `platform` | `DEVOPS` implementation | blocks platform |
| `quality` | `DOC`, `TEST`, or remaining process work | blocks quality |
| `catalog` / `epic` | requirement anchor or roadmap umbrella | never claimable |

Deriving the lane keeps area ownership and concurrency from drifting apart.

## Workflow and board

| Label | Meaning |
|-------|---------|
| `ready-for-dev` | Acceptance criteria and Use Case are present. Foreman may pick it |
| `in-progress` | An agent owns it. Board Status is In Progress. Issue stays OPEN |
| `in-review` | A pull request is open. Board Status stays In Progress |
| `blocked` | Waiting on a dependency. There is no Blocked column |
| `needs-info` | Cannot be picked until a person answers |

Coherence with Status and issue state:
`python3 workspace/sdlc/check-board-status-label-coherence.py`.

## Type, concern, source

| Family | Labels |
|--------|--------|
| Type | `bug`, `enhancement`, `REFACTOR`, `tech-debt`, `chore`, `ci`, `dependencies`, `TAREA` |
| Concern | `security`, `a11y`, `i18n`, `risk`, `ux` |
| Phase (migration stream) | `phase:1-analysis`, `phase:2-foundation`, `phase:3-backend-api`, `phase:4-frontend`, `phase:5-observability`, `phase:6-deprecation` |
| Roadmap umbrella | `roadmap` |
| Traceability | `requerimiento-funcional`, `requerimiento-no-funcional`, `CASO-DE-USO` |
| Source | `audit-2026-09` |
| Process | `sdlc-exception` |

## Do not invent a second name

| Use | Do not also create |
|-----|--------------------|
| `DOC` | `documentation`, `docs`, `DOCUMENTACION` |
| `enhancement` | `MEJORAS` |
| `requerimiento-funcional` / `requerimiento-no-funcional` | `req:*` |
| Derived lane | `lane:*` or a second area code such as `area:backend` |

`documentation` still exists from older automation. New work uses `DOC`.
When applying the roadmap, move any leftover `documentation` issues onto `DOC`
and delete `documentation` only after it has zero issues.

## Reserved for bots

Leave these in place. Agents do not apply them to product work.

| Label | Owner |
|-------|--------|
| `javascript`, `java` | Dependabot |
| `autorelease: pending`, `autorelease: tagged` | Release automation |
| `dependencies` | Dependency updates |

## Description fix

`FRONTEND` still describes Swing forms. The description to set:

> Next.js web client, forms, and pages. Swing is not a target.

## Status field on the project

Project **Notaire — Delivery Board** (user project 1) keeps three Status options:

| Status | Issue state | Labels |
|--------|-------------|--------|
| Todo | OPEN | no `in-progress` |
| In Progress | OPEN | `in-progress` |
| Done | CLOSED | no `in-progress`, no `blocked` |

Do not add a Blocked, Review, or Ready column. Those signals are labels.
