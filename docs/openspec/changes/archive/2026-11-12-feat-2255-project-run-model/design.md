> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

A.3 (#2254 / #2273) shipped If-Match, schema_version, workflow body v2, and
serve writes. Catalogs/workflows are versioned; foreman still `executes=false`;
capabilities report `runner: false`. Phase D UI must not invent run progress.
Branch fresh from `main` @ A.3 tip as `cursor/2255-project-run-model-b926`.

## Goals / Non-Goals

**Goals:**
- File-backed Project with `profile_ref` and repos (`mode` default `dry_run`)
- Run + RunRequest: start appends evidence event on approved workflow+digest
- Evidence-derived status; harness reporting contract + local-worker adapter
- HTTP GET/POST projects/runs + events; cancel → 501 while runner false
- PRESERVE evidence chain, report, governance, and A.2/A.3 serve tests

**Non-Goals:**
- Phase D SPA chrome (#2242)
- Context Lake / Port clone / Wilson vanity / fake Isolator
- Second OpenAPI tree; changing `required=true`
- Making foreman execute agents

## Decisions

1. **Project store** — Projects live under
   `.aisdlc/projects/{id}/current.json` (file-backed, not SaaS tenancy).
   Fields: `id`, `profile_ref`, `repos[{id,forge,url,default_branch,mode}]`,
   `created_at`, `created_by`. Missing `mode` defaults to `dry_run` on write.
2. **Run store** — Runs live under `.aisdlc/runs/{id}/current.json` with
   `workflow{id,version,digest}`, `project`, `status`, `requested_by`,
   `requested_at`, `evidence_head`, `steps[{id,status}]`. The authoritative
   status is recomputed from the evidence ledger whenever shown/listed.
3. **RunRequest event** — Start-run appends a ledger record:
   `{kind:"run.request", run_id, project, workflow{id,version,digest},
   requested_by, ts, …}` via `evidence.append_record`. Non-approved workflow →
   `KitError("workflow_not_approved", …)`.
4. **Status derivation** — From events for that `run_id`:
   - only `run.request` → `queued` (after accept) / `requested` at append
   - `run.started` → `running`
   - `run.finished` with ok → `succeeded`; fail → `failed`
   - cancel event (future) → `cancelled`
   - budget overrun / blocked signal → `blocked`
   Kit start path never writes `status: running`.
5. **Harness contract** — Documented in kit README +
   `framework/ai-sdlc/docs/HARNESS-REPORTING.md`. Event names:
   `run.started`, `step.started`, `step.finished`, `run.finished`.
   Reference adapter: `aisdlc harness report --run <id> --event <name>`
   (local worker) which hash-chain-appends via `evidence.append_record`.
   Skip on a step is never recorded as pass.
6. **HTTP** — `GET/POST /factory/projects`, `GET/POST /factory/runs`,
   `GET /factory/runs/{id}`, `GET /factory/runs/{id}/events`.
   POST needs `--write`, CSRF, write scope (same as A.3).
   `POST /factory/runs/{id}/cancel` → 501 / `not_implemented` with detail
   from capabilities.runner reason.
7. **CLI** — `aisdlc project create|list|show|add-repo`;
   `aisdlc run start|list|show|events`; harness report as above.
8. **OpenAPI** — single tree; bump description for A.4; add schemas/paths;
   update contract test that previously forbade cancel path (cancel now
   declared, honesty via 501).
9. **Committer** — Matias Miguez / matiaspakua@gmail.com; leave
   `required=true` alone.
10. **CHANGELOG** — omit Unreleased while #2271 holds the fence.

## Riesgos / Trade-offs

- [Line budget] → Prefer focused `projects.py` / `runs.py` modules; keep serve
  dispatch thin.
- [PRESERVE evidence] → New kinds are additive; `verify` still validates chain.
- [Two-PR note on issue] → Ship model+HTTP+adapter in one PR if counted lines
  stay coherent; otherwise split HTTP second — prefer one PR for A.4 AC.
- [Cancel in OpenAPI] → Declaring cancel with 501 is honest; update
  `test_contract` assertion that previously excluded the path.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| project create defaults dry_run | unit | `tests/test_projects.py` |
| start run requires approved workflow | unit | `tests/test_runs.py` |
| start run appends RunRequest evidence | unit | `tests/test_runs.py` |
| status stays queued without harness events | unit | `tests/test_runs.py` |
| harness report moves to running then finished | unit | `tests/test_harness_report.py` |
| budget overrun → blocked; skip ≠ pass | unit | `tests/test_harness_report.py` |
| evidence verify covers new events | unit | `tests/test_runs.py` |
| HTTP GET/POST projects and runs | unit | `tests/test_serve_runs.py` |
| cancel returns 501 | unit | `tests/test_serve_runs.py` |
| OpenAPI Project/Run schemas present | unit | `tests/test_contract.py` |
| PRESERVE evidence chain / report | regression | `tests/test_evidence_chain.py` |

## Regression Strategy

- Do not weaken `test_evidence_chain.py`, `test_workflow_governance.py`, or A.2/A.3 serve tests.
- Update only the contract assertion that previously forbade `/factory/runs/{id}/cancel`.
- `bash framework/ai-sdlc/verify.sh` then `bash workspace/sdlc/preflight.sh --full`.

## Playwright Strategy

n/a — no product UI surface; Phase D SPA is #2242. Kit HTTP covered by serve unit tests.

## Deployment Strategy

Ships with the kit tree; no Notaire deploy change.

## Alternatives considered

- Store runs only as evidence rows without a Run document → rejected (OpenAPI Run
  schema and list/show need a stable entity with `evidence_head`).
- Fake `running` on start for UX → rejected (FACTORY-UI-SPEC honesty rule).

## Rollback Strategy

Revert the PR. Empty `.aisdlc/projects` / `.aisdlc/runs` dirs are inert; evidence
rows already appended remain (hash chain) and are still verifiable.

## Migration Plan

No Flyway. Adopters gain project/run directories on first create; no schema migrate
step beyond A.3 workflow migrate already shipped.

## Open Questions

None — FACTORY-UI-SPEC §4–5 and #2255 AC are SoR.
