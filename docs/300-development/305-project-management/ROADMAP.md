# Roadmap and Project views

Live board: [Notaire — Delivery Board](https://github.com/users/matiaspakua/projects/1).

The board has four delivery views and three catalog views. Each view answers
one question; adding another view requires removing duplication.

## Delivery views

The catalog exclusion shared below is:

```text
-label:requerimiento-funcional -label:requerimiento-no-funcional -label:CASO-DE-USO
```

| View | Layout | Grouping | Purpose | Filter |
|------|--------|----------|---------|--------|
| Roadmap | Roadmap | Milestone | Full story from project history through future trains | catalog exclusion only |
| KANBAN | Board | Status | Current execution state | `is:open` + catalog exclusion |
| Agent queue | Table | none | Open claimable candidates with planning fields | `is:open` + catalog exclusion |
| Needs triage | Table | none | Open delivery items missing one `priority:*` | delivery filter + four negative priority filters |
| Todo by priority | Table | none | Todo work ordered/read by urgency | `is:open status:Todo` + catalog exclusion |

The policy also limits visible fields where GitHub supports it. Kanban omits
timeline fields. Agent queue includes Assignees, Labels, Priority, Area, Start,
and Target. GitHub Roadmap layouts choose their own rendered fields.

The three dedicated catalog views remain:

- **Requerimientos Func.** — open `requerimiento-funcional`
- **Casos de Uso** — `CASO-DE-USO`
- **RNF** — `requerimiento-no-funcional`

Status options are **Todo / In Progress / Done** only. The Kanban is an
execution view, so closed work is intentionally absent; completed history is
visible on the Roadmap.

## Full-lifecycle dates

GitHub's roadmap renders Project item date fields, not only milestone due dates.
`Start` and `Target` are therefore derived from the canonical milestone window
in `workspace/sdlc/milestone_policy.py` for both:

- closed history milestones, from the first issue on 2026-03-01; and
- open delivery trains, through the 2027-06 backlog horizon.

The Roadmap must be grouped by **Milestone**. The public Project v2 mutation can
write the filter and layout but not group-by configuration, so grouping is a
one-time UI setting verified by `--check`.

## Agent reading order

1. Open **Roadmap** to understand completed cohorts and train order.
2. Open **KANBAN** to see Todo and In Progress ownership.
3. Open **Agent queue** to select candidates.
4. Use **Needs triage** only for missing metadata; never claim from it blindly.
5. Apply [PARALLEL-AGENTS.md](PARALLEL-AGENTS.md) before changing Status.

## Mechanical management

```bash
# Safe, read-only drift check.
python3 workspace/sdlc/apply_milestone_policy.py --check

# Preview issue moves and label repairs.
python3 workspace/sdlc/apply_milestone_policy.py --dry-run

# Owner-only mutation: milestones, dates, views, snapshots.
python3 workspace/sdlc/apply_milestone_policy.py --apply
```

The apply command writes identical generated snapshots to:

- `docs/github/milestone-snapshot.json`
- `github-page/lib/milestone-snapshot.json`

The public GitHub Pages roadmap reads the second file. Counts are observations,
not policy; regenerate them whenever the live issue set changes.

## Required permissions

Use `gh auth status` before `--apply`. The active login must have repository
Issues write permission and user Project write permission. If an agent app
token receives 403, it must return an owner handoff with the failed operation;
it must not create a parallel board or tracker.
