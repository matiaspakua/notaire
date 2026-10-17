# Owner scripts

Versioned copies of the Owner-only milestone, label and board consolidation
helpers (#1878). Preview is the default; pass `--apply` to mutate GitHub.

| Script | Role |
|--------|------|
| `notaire-milestones-apply.sh` | Renames, due dates, then `apply_milestone_policy.py` |
| `notaire-labels-apply.sh` | Label taxonomy apply (#1860) |
| `notaire-consolidate.sh` | End-to-end Owner consolidate + verify |

## Requirements

- `gh` authenticated as the repository Owner with `repo` and `project` scopes
  (`gh auth refresh -s repo -s project`).
- Run from a notaire checkout. Prefer a clean worktree dedicated to Owner apply
  so agent branches are not disrupted.

## Typical flow

```bash
# Preview (default)
bash workspace/sdlc/owner/notaire-consolidate.sh

# Mutate GitHub (Owner only)
bash workspace/sdlc/owner/notaire-consolidate.sh --apply

# Read-only verify
python3 workspace/sdlc/apply_milestone_policy.py --check
python3 workspace/sdlc/check-board-status-label-coherence.py --scope wip
```

After `--apply`, commit the refreshed snapshots on a PR branch (never on `main`
directly):

- `docs/github/milestone-snapshot.json`
- `github-page/lib/milestone-snapshot.json`

CI runs `bash -n` and shellcheck on every `workspace/sdlc/owner/*.sh`.

Policy source of truth: [`docs/300-development/305-project-management/`](../../../docs/300-development/305-project-management/).
