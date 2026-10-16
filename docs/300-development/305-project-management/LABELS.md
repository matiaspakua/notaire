# Labels

`gh label list` is the live set. This page is the contract: what each family
means, and which labels must not be duplicated. The legacy → new table lives in
code, `workspace/sdlc/label_taxonomy.py` (#1860).

## Migration status

Phase 1 (#1860): every script reads a legacy name and its new name as the same
label. The Owner runs the rename (`notaire-labels-apply.sh`). Phase 2 drops the
legacy names from code and docs. Until then, either name is correct; new work
uses the new name once it exists.

## Families

| Family | Cardinality | Labels | Legacy |
|--------|-------------|--------|--------|
| `type:` | exactly one on claimable work | `type:feature`, `type:bug`, `type:refactor`, `type:chore`, `type:ci`, `type:epic` | `enhancement`, `bug`, `REFACTOR`, `chore` + `TAREA`, `ci`, `roadmap` |
| `priority:` | exactly one on claimable work | `priority:critical`, `priority:high`, `priority:medium`, `priority:low` | unchanged |
| `area:` | one or more | `area:backend`, `area:frontend`, `area:db`, `area:devops`, `area:test`, `area:docs` | `BACKEND`, `FRONTEND`, `DB`, `DEVOPS`, `TEST`, `DOC` |
| `status:` | workflow flags | `status:ready`, `status:in-progress`, `status:in-review`, `status:blocked`, `status:needs-info` | `ready-for-dev`, `in-progress`, `in-review`, `blocked`, `needs-info` |
| `concern:` | zero or more | `concern:security`, `concern:a11y`, `concern:i18n`, `concern:ux`, `concern:risk`, `concern:tech-debt` | unprefixed |
| `trace:` | catalog anchors only | `trace:rf`, `trace:rnf`, `trace:cu` | `requerimiento-funcional`, `requerimiento-no-funcional`, `CASO-DE-USO` |
| `source:` | optional | `source:audit-2026-09` | `audit-2026-09` |
| `process:` | human only | `process:sdlc-exception` | `sdlc-exception` |
| `release:` | optional | `release:breaking` | new |

Scope-catalog issues (milestone `Catalog · Requirements & use cases`) carry a
`trace:` label, need no priority, and are not claimable.

Deleted: `phase:*` (milestones schedule work), `Cursor`, `duplicate`,
`invalid`, `question`, `good first issue`, `help wanted`. `wontfix` stays as a
close reason.

## Semver and CHANGELOG

| Label | Version bump | CHANGELOG section |
|-------|--------------|-------------------|
| `type:feature` | minor | Features |
| `type:bug` | patch | Bug Fixes |
| `release:breaking` | major | Breaking |
| `type:refactor`, `type:chore`, `type:ci` | none | Code Refactoring / not listed |

The PR title type (`feat`, `fix`, …) matches the type label. Milestones are the
release trains; there are no `vX.Y` labels.

## Derived lanes

There are no `lane:*` labels. A lane is computed from existing labels by
`workspace/sdlc/milestone_policy.py`:

| Derived lane | Signal | Conflict |
|--------------|--------|----------|
| `security` | `concern:security`, or critical backend work | blocks security and backend |
| `cross` | both `area:backend` and `area:frontend` | blocks cross, backend, and frontend |
| `backend` | `area:backend` | blocks backend |
| `frontend` | `area:frontend` | blocks frontend |
| `platform` | `area:devops` implementation | blocks platform |
| `quality` | `area:docs`, `area:test`, or remaining process work | blocks quality |
| `catalog` / `epic` | `trace:` anchor or `type:epic` | never claimable |

## Status lifecycle (agent fleet)

```
new ─triage─▶ Todo + type/priority/area ─ready─▶ Todo + status:ready
  ─claim-issue.py─▶ In Progress + status:in-progress
  ─PR opened─▶ In Progress + status:in-progress + status:in-review
  ─merged, issue closed─▶ Done (status:* labels removed)
held: status:blocked or status:needs-info ⇒ not claimable
```

`claim-issue.py` refuses issues without `status:ready` and issues with a hold label.

## Status field on the project

Project **Notaire — Delivery Board** (user project 1) keeps three Status options:

| Status | Issue state | Labels |
|--------|-------------|--------|
| Todo | OPEN | no `status:in-progress` |
| In Progress | OPEN | `status:in-progress` |
| Done | CLOSED | no `status:in-progress`, no `status:blocked` |

Do not add a Blocked, Review, or Ready column. Those signals are labels. The
project has no Priority / Area / Kind fields; labels carry them.

Coherence: `python3 workspace/sdlc/check-board-status-label-coherence.py`
(R1–R5, plus R6 `status:in-review` needs an open PR, R7 ready and blocked are
exclusive, R8 a ready issue has at most one `type:` label).

## Do not invent a second name

| Use | Do not also create |
|-----|--------------------|
| `area:docs` | `documentation`, `docs`, `DOCUMENTACION` |
| `type:feature` | `MEJORAS`, `enhancement` after phase 2 |
| `trace:rf` / `trace:rnf` | `req:*` |
| Derived lane | `lane:*` |

## Reserved for bots

Leave these in place. Agents do not apply them to product work.

| Label | Owner |
|-------|--------|
| `javascript`, `java`, `maven` | Dependabot |
| `autorelease: pending`, `autorelease: tagged` | Release automation |
| `dependencies` | Dependency updates |

## Views

Nine views on project 1: Kanban, Current train, Agent queue, In flight, Needs
triage, Blocked / needs info, Release roadmap, Shipped, Catalog. Filters are the
`VIEW_CONTRACTS` in `milestone_policy.py`; see `docs/github/README.md`.
