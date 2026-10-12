# Parallel agents

Several Cloud agents may work at the same time. They stay apart by **lane**,
not by hoping the files do not overlap.

## Claim rule

1. Ignore issues whose milestone starts with `Scope —`.
2. Ignore issues labeled `roadmap` unless the foreman was asked to split that umbrella into child issues.
3. A lane is **free** when no OPEN issue in that lane has `in-progress`.
4. `lane:fullstack` occupies **three** lanes at once: fullstack, backend, and frontend.
5. `lane:security` is exclusive even when the issue also has `BACKEND`, `FRONTEND`, or `DEVOPS`.
6. The foreman claims one issue: add `in-progress`, set board Status to In Progress, then dispatch the specialist for that lane.
7. A second agent does not take another issue in a busy lane.

Claimable open work on 2026-10-12, after classification:

| Lane | Open issues the foreman may pick | Specialist |
|------|-----------------------------------|------------|
| `lane:devops` | 25 | devops-engineer |
| `lane:backend` | 24 | backend-implementer |
| `lane:docs` | 19 | efficiency fallback, docs only |
| `lane:owner` | 14 | nobody — a person decides |
| `lane:frontend` | 10 | frontend-design |
| `lane:test` | 7 | testing-qa |
| `lane:fullstack` | 6 | backend then frontend, serialized |
| `lane:security` | 3 | security-auditor |

`lane:owner` is never picked by an agent. It holds decisions such as
[#1445](https://github.com/matiaspakua/notaire/issues/1445) and probe issues
with no area label.

## What "no conflict" means

| Allowed together | Not allowed together |
|------------------|----------------------|
| One backend issue and one frontend issue | Two backend issues both `in-progress` |
| One devops issue and one docs issue | A fullstack issue plus any backend or frontend issue |
| Security review of an open PR (read-only) while another lane implements | Two writers on the same branch |
| | A new product PR while heavy CI for another PR is still queued |

Heavy CI stays serialized. See
[CI-MERGE-GATE.md](../304-ai-sdlc-cloud/CI-MERGE-GATE.md). Lanes remove
**edit** collisions. They do not remove the single heavy-CI queue.

## Pick order inside a free lane

1. `ready-for-dev`, then
2. `priority:critical`, then `priority:high`, then `priority:medium`, then
3. the earlier delivery train (Phase 4 and Phase 6 before v1.0, v1.0 before v1.1, v1.1 before Backlog).

Skip `needs-info` and `blocked`.

## Module boundary

The lane is the lock. The module map is the file boundary once the issue is
claimed: `python3 workspace/modules.py affected <path>`. A backend lane issue
does not edit `frontend/`. A fullstack issue may, and it is the only writer
on both modules until it merges.
