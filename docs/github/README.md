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

## Milestones

| Milestone | Contains |
|-----------|----------|
| v1.0 — Production Ready | Open `priority:critical` / `priority:high` work |
| v1.1 — Hardening | Open `priority:medium` work |
| Backlog | Open `priority:low` work |

Anchor issues get no milestone. These are the traceability catalog: requirements RF/RNF
(#3–#121, labels `requerimiento-funcional` / `requerimiento-no-funcional`) and use cases
CU01–CU68 (#153–#221, label `CASO-DE-USO`). They stay open, and work issues reference them.

## Labels

| Kind | Labels |
|------|--------|
| Area (one or more) | `BACKEND`, `FRONTEND`, `DB`, `DEVOPS`, `TEST`, `DOC` |
| Type | `bug`, `enhancement`, `REFACTOR`, `tech-debt`, `chore`, `ci`, `dependencies`, `TAREA` |
| Priority (exactly one) | `priority:critical`, `priority:high`, `priority:medium`, `priority:low` |
| Concern | `security`, `a11y`, `i18n`, `risk` |
| Workflow | `in-progress`, `in-review`, `blocked`, `needs-info`, `ready-for-dev` |
| Traceability | `requerimiento-funcional`, `requerimiento-no-funcional`, `CASO-DE-USO` |
| Source | `audit-2026-09` (findings of the [2026-09 audit](PRODUCTION-READINESS-AUDIT-2026-09.md)) |

Run `gh label list` for the live list. Do not create a label that repeats an existing one in
another language.

## Issue checklist

Every work issue has: a Use Case reference (CU), acceptance criteria, one area label,
one priority label and a milestone. Issue templates live in
[`.github/ISSUE_TEMPLATE/`](../../.github/ISSUE_TEMPLATE/).
