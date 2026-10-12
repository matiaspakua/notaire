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

The executable rules are `workspace/sdlc/roadmap_classify.py`. If the prose and
the function disagree, fix both in the same change and extend
`workspace/tests/test_roadmap_classify.py` first.

## Steps

1. Export issues (`organize-roadmap.py --fetch` or a JSONL inventory).
2. Classify. Confirm each issue has one milestone and one lane.
3. Write the snapshot JSON. Copy it to `github-page/lib/roadmap-snapshot.json`.
4. Update the chart page and `MILESTONES.md` counts when the snapshot changes.
5. Do not close issues from a classification run.
6. Do not pick `Scope —` milestones or `lane:owner` for an agent.
7. Claim at most one `in-progress` issue per lane. Fullstack blocks backend and frontend too.

## Priority versus milestone

Priority is urgency. The milestone is the train. A high-priority frontend
screen is Phase 4, not automatically v1.0. A critical security finding is v1.0
even when it also touches the frontend.

## Tests

```bash
python3 -m unittest workspace.tests.test_roadmap_classify
```
