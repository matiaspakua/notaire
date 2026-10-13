# Fleet identity (GitHub App)

> Issue #1648 · CU76 / CU78 (least privilege). Process authority: [CONSTITUTION.md](../../../CONSTITUTION.md).

Fleet agents must write issue labels and the Project Status field to claim,
release and complete work ([PARALLEL-AGENTS.md](../305-project-management/PARALLEL-AGENTS.md)).
Personal and cloud-agent tokens got `403` on those writes (#1551). Every such
event then needed a human handoff, and the board drifted. The fleet therefore
acts as its own GitHub App, **notaire-fleet**.

## Permissions (nothing more)

| Scope | Permission | Why |
|-------|-----------|-----|
| Repository · Issues | Read & write | Labels, claim/release comments, closing |
| Repository · Pull requests | Read & write | Open/update PRs, merge under the Foreman merge rule |
| Repository · Contents | Read & write | Push feature branches. `protect-main` still blocks `main` |
| Repository · Actions, Checks, Commit statuses | Read | `check-heavy-ci.sh`, CI watch |
| Repository · Metadata | Read | Required |
| Account · Projects | Read & write | Project Status (user project 1) |

No administration, no secrets, no workflows write, no ruleset bypass. The App
is **not** a `protect-main` bypass actor.

## Owner setup (one time)

1. GitHub → Settings → Developer settings → GitHub Apps → **New GitHub App**:
   name `notaire-fleet`, no webhook, the permissions above, "Only on this account".
2. Install it on `matiaspakua/notaire` only.
3. Generate a private key. Store `FLEET_APP_ID` and `FLEET_APP_PRIVATE_KEY` (PEM
   text) in the fleet runtime's secret store (Cursor Cloud environment, local
   `.env`, Actions secrets if a workflow needs it). Never commit them.

## Use

```bash
eval "$(bash workspace/sdlc/fleet-token.sh)"   # GH_TOKEN = 1-hour installation token
python3 workspace/sdlc/claim-issue.py <n> --agent <id>
```

Without the two variables, `fleet-token.sh` prints a warning on stderr and
leaves the ambient `gh` auth in place. Board writes may then fall back to a
handoff line. Tests: `workspace/tests/test_fleet_token.py`.
