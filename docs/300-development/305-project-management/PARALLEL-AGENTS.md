# Parallel agents

Every Notaire agent runtime—cloud, local, or human-supervised—uses the same
derived-lane lock. No runtime may create a private queue or interpret labels
differently.

## Claim protocol

1. Read the **Roadmap**, then the **KANBAN**, then the **Agent queue**.
2. Consider only OPEN issues on the six delivery trains.
3. Skip requirement anchors, roadmap epics, `blocked`, and `needs-info`.
4. Derive the lane with `workspace/sdlc/milestone_policy.py`.
5. A lane is free when no other OPEN issue in a conflicting derived lane has
   `in-progress`.
6. Prefer `ready-for-dev`, then higher priority, then the earlier train.
7. Claim atomically: add `in-progress` and set Project Status to In Progress.
8. Dispatch the matching specialist with the issue, OpenSpec change, branch,
   acceptance criteria, and prior gate evidence.

An unlabeled or owner-decision issue is not agent-ready. It remains Todo until a
person supplies area, priority, and acceptance criteria.

## Conflict matrix

| Active lane | Blocks | Typical specialist |
|-------------|--------|--------------------|
| `security` | security, backend | security auditor |
| `cross` | cross, backend, frontend | backend then frontend, serialized |
| `backend` | backend | backend implementer |
| `frontend` | frontend | frontend design |
| `platform` | platform | DevOps engineer |
| `quality` | quality | testing/QA or docs specialist |
| `catalog`, `epic` | not claimable | project manager splits/refines |

Read-only review may run beside implementation. Two writers never share a
branch, and a second agent never claims a blocked lane.

## Completion and handoff

| Event | Issue | Status | Labels |
|-------|-------|--------|--------|
| Claimed | OPEN | In Progress | `in-progress` |
| Waiting but still owned | OPEN | In Progress | `in-progress`, `blocked` |
| Released back to queue | OPEN | Todo | no WIP label |
| Completed | CLOSED | Done | no `in-progress`, no `blocked` |

Validate synchronization with:

```bash
python3 workspace/sdlc/check-board-status-label-coherence.py --scope wip
```

If an agent token cannot write GitHub, the coordinator performs the Status and
label mutation. The PR body with `Closes #N` is the fallback synchronization
channel, not a substitute board.

## Module and CI boundaries

After claim, use `python3 workspace/modules.py affected <path>` to establish the
file boundary. A backend lane does not edit `frontend/`; only a cross lane may
own both.

Lanes prevent edit collisions. Heavy CI remains one serialized queue:
[CI-MERGE-GATE.md](../304-ai-sdlc-cloud/CI-MERGE-GATE.md).
