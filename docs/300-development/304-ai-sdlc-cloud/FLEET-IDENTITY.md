# Fleet identity (GitHub App)

> Issue #1648 · #2221 · CU76 / CU78 (least privilege). Process authority:
> [CONSTITUTION.md](../../../CONSTITUTION.md).

Fleet agents must write issue labels and the Project Status field to claim,
release and complete work ([PARALLEL-AGENTS.md](../305-project-management/PARALLEL-AGENTS.md)).
Personal and cloud-agent tokens got `403` on those writes (#1551, #2221). Every
such event then needed a human handoff, and the board drifted. The fleet
therefore acts as its own GitHub App, **notaire-fleet**.

## Permissions (nothing more)

| Scope | Permission | Why |
|-------|-----------|-----|
| Repository · Issues | Read & write (`issues: write`) | Labels via GraphQL `addLabelsToLabelable`, claim/release comments, closing |
| Repository · Pull requests | Read & write | Open/update PRs, merge under the Foreman merge rule |
| Repository · Contents | Read & write | Push feature branches. `protect-main` still blocks `main` |
| Repository · Actions, Checks, Commit statuses | Read | `check-heavy-ci.sh`, CI watch |
| Repository · Metadata | Read | Required |
| Account · Projects | Read & write | Project Status (user project 1) |

No administration, no secrets, no workflows write, no ruleset bypass. The App
is **not** a `protect-main` bypass actor.

## Cursor Cloud ambient token vs notaire-fleet (#2221)

Cursor Cloud agents often authenticate `gh` as a **platform GitHub App /
integration** that can push branches and open PRs but **cannot** mutate Issue
labels. Symptom:

```text
gh issue edit <n> --add-label "status:in-progress"
# GraphQL: Resource not accessible by integration (addLabelsToLabelable)
```

| Identity | Who configures it | Label writes |
|----------|-------------------|--------------|
| Ambient Cursor Cloud `gh` token | Cursor dashboard / GitHub App installation for the cloud agent | Often **no** `issues: write` → 403 on `addLabelsToLabelable` |
| **notaire-fleet** installation token | Owner: GitHub App + `FLEET_APP_ID` / `FLEET_APP_PRIVATE_KEY` in the cloud environment secrets | Yes — Issues Read & write |

**Owner grant (pick one):**

1. Prefer: store `FLEET_APP_ID` + `FLEET_APP_PRIVATE_KEY` on the Cursor Cloud
   Environment card and have agents `eval "$(bash workspace/sdlc/fleet-token.sh)"`
   before claim / Gate 1.6.
2. Or: widen the Cursor / cloud GitHub App so Repository · Issues is
   **Read & write** (`issues: write`, GraphQL `addLabelsToLabelable`) on
   `matiaspakua/notaire`. Agents cannot change Cursor dashboard secrets mid-run.

Until that grant exists, Gate 1.6 must **honestly SKIP** — never fake
"in progress". Use:

```bash
bash workspace/sdlc/add-issue-wip-label.sh <n>
```

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
bash workspace/sdlc/add-issue-wip-label.sh <n>  # Gate 1.6; honest SKIP on 403
python3 workspace/sdlc/claim-issue.py <n> --agent <id>
```

Without the two variables, `fleet-token.sh` prints a warning on stderr and
leaves the ambient `gh` auth in place. Board writes may then fall back to a
handoff line / Gate 1.6 SKIP. Tests: `workspace/tests/test_fleet_token.py`,
`workspace/tests/test_add_issue_wip_label.py`.
