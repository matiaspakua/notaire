# Apply the AI SDLC audit fixes

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1083 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `chore/1083_apply_ai_sdlc_audit_fixes` |
| Gate 1 status | passed |

## Objetivo

`local-ai/AUDIT.md` (PR #1081) found that the gates tell *pass* from *fail* but
not a *correct* pass from a *cheap* one, and that several process rules are
enforced only by agent goodwill. The #1063 run also stopped on a harness bug: a
quoted `TEST_CMD="mvn ..."` in `tests.env` kept its quotes, so bash ran the
whole string as one command name (exit 127). This change applies the audit
fixes that fit one reviewable PR and records the rest as follow-ups.

## What Changes

Harness (`local-ai/sdlc/`):

- `bin/envfile.py`: one parser for `KEY=value` files, used by `kv()` and by the
  prompt renderer. It strips a trailing comment and one pair of surrounding quotes (H-bug).
- `bin/static_checks.py`, called by the red gate: rejects absolute home paths in
  added test lines, a new test class whose name already exists elsewhere, and
  test changes that add no assertion (E5).
- `foreman.sh <n> check`: runs every `CHECK:` line of the pending review note in
  the worker worktree and compares the output with `EXPECTED:` (H1).
- `metrics.jsonl`: one JSON line per gate result (phase, gate, result, attempt,
  seconds) next to `gates.log` (H3).
- `PROFILE_<PHASE>` overrides the Codex profile per phase (H2 routing hook).
- A bash ≥ 4 check at start-up (H6).
- `local-ai/sdlc/tests/`: harness self-tests, run by CI and preflight (H5, partial).

Process and CI:

- `scripts/check-commit-messages.sh`: every non-bot commit in the PR range is a
  Conventional Commit (E3).
- `scripts/check-tdd-evidence.sh`: a PR that changes production code also changes
  tests, and no production-code commit comes before the first test commit (E2).
- `scripts/check-sdlc-exception.sh`: a PR with no `openspec/changes/` path needs
  the human-set `sdlc-exception` label (S1).
- `scripts/check-agent-rules.sh`: always-loaded rule files are non-empty and the
  repo paths they reference exist (P2, P5).
- `validate-sdlc-plan.sh` fails a change that has no `schema:` line, instead of
  skipping it (S4).
- All five run in `pr-validation.yml` and `scripts/preflight.sh` (CLAUDE.md:
  a new CI gate is mirrored in preflight in the same PR).

Policy and cleanup:

- CONSTITUTION: a Roles section (human owner, foreman agent, worker agent) and
  the stale Swing text refreshed (P1, P4).
- `.claude/rules/ai-agent-workflow.md`: rewritten as a short pointer to the
  CONSTITUTION steps, with the OpenSpec step and "issue closes on merge" (P2).
- Retired: `.github/workflows/e2e-swing.yml`, `.claude/agents/issue-loop.md`,
  the SpecKit CI job, script and folder (moved to `docs/000-archive/`) (P3, P4, P6).
- `.claude/skills/README.md`: the evals claim matches reality (P7).
- `local-ai/sdlc/AI-SDLC.md`: the squash policy and the human's CD step (H4, E6).
- `local-ai/AUDIT.md`: a status column per finding.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Every commit on a PR is a Conventional Commit | CONSTITUTION §9 | Made explicit (now enforced) |
| A production-code PR carries test changes that come first | CONSTITUTION §7 (TDD) | Made explicit (now enforced) |
| A PR without an OpenSpec change needs a human-approved exception | CONSTITUTION §12 | Made explicit (now enforced) |
| Only the human owner merges and approves exceptions; the foreman reviews; the worker implements | CU76, AUDIT P1 | New |

## Capabilities

### New Capabilities

- `ai-sdlc-enforcement`: mechanical checks that enforce the SDLC on the harness
  and on every PR.

### Modified Capabilities

None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | yes | `pr-validation.yml` new checks, SpecKit job removed; `e2e-swing.yml` deleted |
| `local-ai/sdlc` harness | yes | see What Changes |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none (python3 and bash only)

### Architecture review

Follows the audit's target layering (§4): parsing and checks move out of
`foreman.sh` into small `bin/` programs. No ADR: no product architecture changes.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CONSTITUTION.md` | Roles section; Swing text; §12 label; tooling map |
| `.claude/rules/ai-agent-workflow.md` | Rewrite as pointer |
| `docs/300-development/CI-PREFLIGHT.md` | New checks |
| `local-ai/sdlc/AI-SDLC.md` | `check`, metrics, static gates, squash policy, CD step |
| `local-ai/AUDIT.md` | Status per finding |
| `CHANGELOG.md` | n/a — not user visible |

## Out of Scope

Tracked as follow-up issues: full branch protection (E1, needs the CI report
commits moved off `main`), semantic traceability check (S2), post-merge
auto-archive (S3), Claude `Stop`/`PostToolUse` hooks (E4), stronger-model routing
policy (H2 beyond the profile hook), fresh-clone harness dry run (H5 rest), and
the `.aisdlc/project.yml` adapter with the `foreman.sh` split (§3.5, §4).
