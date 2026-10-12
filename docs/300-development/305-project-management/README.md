# Project management

This directory is the durable source for how humans and every Notaire agent
fleet read history, coordinate current work, and select what comes next.

| Start here | Question answered |
|------------|-------------------|
| [MILESTONES.md](MILESTONES.md) | Which history cohort or delivery train owns an issue? |
| [ROADMAP.md](ROADMAP.md) | Which GitHub Project view answers history, execution, queue, or triage? |
| [PARALLEL-AGENTS.md](PARALLEL-AGENTS.md) | May another agent claim this work now? |
| [LABELS.md](LABELS.md) | Which labels express area, urgency, concern, and workflow? |
| [RELEASES.md](RELEASES.md) | How do delivery trains differ from SemVer releases? |

Generated counts live in
[`docs/github/milestone-snapshot.json`](../../github/milestone-snapshot.json)
and are copied to `github-page/lib/milestone-snapshot.json`. Never edit either
snapshot by hand.

## Shared operating model

Every issue answers four independent questions:

| Question | Signal | Cardinality |
|----------|--------|-------------|
| When is it planned or when was it completed? | Milestone | exactly one |
| How urgent is open delivery work? | `priority:*` | exactly one on claimable work |
| Which implementation surface does it occupy? | Derived lane from area/type labels | exactly one result |
| What is its current execution state? | Issue state + Project Status + WIP labels | one coherent state |

Milestones are trains or history cohorts, never release tags. Priority orders
work **inside** a train. A lane is computed by
`workspace/sdlc/milestone_policy.py`; there are deliberately no `lane:*`
labels to synchronize.

Project Status remains **Todo / In Progress / Done**. Blocked work uses the
`blocked` label, not another column.

## One coordination loop

```text
Roadmap (history → future trains)
  → Agent queue (open non-catalog work)
  → choose earliest train + highest priority in a free derived lane
  → claim: OPEN + in-progress + Status In Progress
  → Constitution gates + PR
  → complete: CLOSED + Status Done + no WIP labels
```

The foreman owns selection and synchronization. Specialists implement only the
claimed issue. The project manager maintains milestones, views, priorities,
and policy; it does not implement product work.

Before claiming:

1. Exclude catalog anchors, roadmap umbrellas, `blocked`, and `needs-info`.
2. Derive the lane and verify no conflicting open issue has `in-progress`.
3. Prefer `ready-for-dev`, then priority, then the earlier delivery train.
4. Add `in-progress` and set Status to In Progress before dispatch.

The exact conflict rules are in [PARALLEL-AGENTS.md](PARALLEL-AGENTS.md).

## Mechanical checks

```bash
# Read-only: all issue assignments and Project view contracts.
python3 workspace/sdlc/apply_milestone_policy.py --check

# Pure scheduling/view-policy tests.
python3 -m unittest workspace/sdlc/test_milestone_policy.py

# Issue state, Status, and WIP-label agreement.
python3 workspace/sdlc/check-board-status-label-coherence.py --scope all
```

`--apply` is owner-only and explicit. It requires an authenticated `gh` user
with repository Issues and user Project write permission. Agent installation
tokens that return 403 produce a handoff; they do not invent a second tracker.

## Authority and navigation

- Process authority: [`CONSTITUTION.md`](../../../CONSTITUTION.md)
- GitHub mechanics: [`docs/github/README.md`](../../github/README.md)
- Cloud fleet entry point: [304-ai-sdlc-cloud](../304-ai-sdlc-cloud/README.md)
- Project manager agent:
  [`.claude/agents/project-manager.md`](../../../.claude/agents/project-manager.md)
- Use cases: RNF-21, RNF-22, and board lifecycle #1550 / CU76
