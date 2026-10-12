---
name: project-management
description: Schedule Notaire milestones, priorities, roadmap views, and parallel-agent lanes. Use for project management, roadmap, milestone planning, backlog triage, label hygiene, or when several agents must work without conflicts.
triggers:
  - project management
  - roadmap
  - milestone
  - prioritize backlog
  - label hygiene
  - parallel agents
  - delivery board
---

# Project management

Schedule and label Notaire work so a manager can read the roadmap and so
Cloud agents do not edit the same surface.

A milestone title is not a git tag. Product cuts are
`docs/300-development/305-project-management/RELEASES.md`.

## When to use

- Organizing milestones, the roadmap view, or the delivery board
- Assigning priority or a lane
- Explaining how closed work was grouped
- Deciding whether an open issue is delivery or catalog (RF / CU / RNF)

## Authority

Read these before changing a rule:

- `docs/300-development/305-project-management/README.md`
- `docs/300-development/305-project-management/MILESTONES.md`
- `docs/300-development/305-project-management/LABELS.md`
- `docs/300-development/305-project-management/PARALLEL-AGENTS.md`
- `docs/300-development/305-project-management/ROADMAP.md`

The executable rules are `workspace/sdlc/milestone_policy.py`. If the prose and
the function disagree, fix both in the same change and extend
`workspace/sdlc/test_milestone_policy.py` first.

## Steps

1. Read Roadmap, KANBAN, and Agent queue.
2. Run `apply_milestone_policy.py --check`.
3. Confirm every issue has one milestone and one derived lane result.
4. Apply only with owner credentials; regenerate both snapshot JSON files.
5. Do not close issues from a classification run.
6. Do not pick catalog, epic, blocked, needs-info, or owner-decision work.
7. Claim at most one issue per non-conflicting derived lane. Cross blocks
   backend and frontend; security blocks backend.

## Priority versus milestone

Priority is urgency. The milestone is the train. A high-priority frontend
screen is Phase 4, not automatically v1.0. A critical security finding is v1.0
even when it also touches the frontend.

## Tests

```bash
python3 -m unittest workspace/sdlc/test_milestone_policy.py
```
