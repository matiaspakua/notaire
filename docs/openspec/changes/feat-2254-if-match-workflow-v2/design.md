> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

A.2 (#2253 / #2272) shipped read-only `aisdlc serve`. Catalogs allocate
`version + 1` without expected-version checks; workflows store bare id lists;
entities lack `schema_version` / actor audit. Branch fresh from `main` as
`cursor/2254-if-match-workflow-v2-55fb` — do **not** continue
`feat/2254_workflow-v2-schema-pins-migrate`.

## Goals / Non-Goals

**Goals:**
- Envelope: `schema_version`, `created_at`, server-derived `created_by`
- `aisdlc migrate` workflow v1→v2 + evidence
- Workflow body v2 + pinned refs + digest-guarded approve
- If-Match / expected-version writes (CLI + HTTP)
- Serve mutating PUT behind `--write`, CSRF, write/owner scope
- PRESERVE Phase A governance and entity-versioning tests unmodified

**Non-Goals:**
- Project/Run (#2255); Phase B–D SPA
- Context Lake / Port clone / Wilson vanity / fake Isolator
- Second OpenAPI tree; changing `required=true`
- Splitting into two PRs unless counted-line budget forces it mid-flight

## Decisions

1. **Envelope defaults** — `add` / `_commit_revision` stamp `schema_version`
   (default `1`), `created_at` (UTC ISO), `created_by` from an `Actor` passed by
   the adapter. Strip any client-supplied `created_by` / `created_at` from
   payloads before commit.
2. **Actor** — `created_by = "{channel}:{identity}"` where channel ∈
   `{cli,ui,agent}` (agent form `agent:<id>`). CLI uses channel `cli` and
   identity from `AISDLC_ACTOR` or `local`. HTTP uses channel `ui` and identity
   from token `identity=` (fallback: token scope). Body fields ignored.
3. **Workflow v1 PRESERVE** — `workflow create` still writes v1 id-list body +
   `schema_version=1` so existing CLI/governance tests stay green. Callers may
   pass a v2 body explicitly; migrate upgrades remaining v1 entries.
4. **Migrate v1→v2** — For each workflow with `schema_version < 2` or v1 body
   shape: build one default step from id lists, pin refs as
   `kind:id@currentVersion`, set `inputs=[]` / `outputs=[]`,
   `schema_version=2`, write a new revision (governance unchanged unless body
   edit semantics require draft — migrate is schema-only, keeps governance).
   Idempotent: already-v2 entries skipped. Evidence ledger record:
   `{ts, kind:"migrate", from, to, entities:[…], ok:true}`.
5. **Pinning** — `request_validation` on v2 walks every ref in steps/inputs/outputs,
   loads catalog digests at pinned versions, stores
   `validation.pins[{ref, digest}]`. `approve` re-loads and compares; mismatch →
   `KitError("pin_digest_mismatch", …)`.
6. **new_version → draft** — Body edits allocate a revision and set
   `governance=draft` (issue AC).
7. **If-Match** — `catalogs.update` / `write_new_revision` accept
   `expected_version: int | None`. When provided and ≠ current →
   `KitError("version_conflict", …)` with current version in detail (HTTP 409).
   CLI: `--if-match N`. HTTP: `If-Match: N` (or quoted). Missing If-Match on
   PUT → 428/`precondition_required` (or 400 `if_match_required`).
8. **Serve writes** — `aisdlc serve --write` enables mutating methods. Without
   it, mutates stay 405 (PRESERVE A.2 behaviour for default serve). Mutates need
   scope ≥ `write`, header `X-AISDLC-CSRF` equal to the bearer token value
   (double-submit), and If-Match on PUT. Approve transition via HTTP (if wired)
   needs `owner` when using owner_override.
9. **OpenAPI** — single tree; add If-Match parameter, 409 Problem, envelope
   fields, WorkflowBodyV2 schemas. Bump info description for A.3.
10. **Committer** — Matias Miguez / matiaspakua@gmail.com; leave
    `required=true` alone.

## Riesgos / Trade-offs

- [Line budget ≤400 counted] → Prefer focused modules (`migrate.py`, actor helper);
  drop optional POST create/transition HTTP if PUT + CLI cover AC.
- [PRESERVE governance] → Keep create on v1; migrate opt-in.
- [Stale tip] → Never rebase/force-push `feat/2254_workflow-v2-schema-pins-migrate`.
- [CHANGELOG fence] → Omit Unreleased if #2271 holds the fence.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| entity carries schema_version | unit | `tests/test_schema_migrate.py` |
| migrate v1→v2 idempotent + evidence | unit | `tests/test_schema_migrate.py` |
| workflow body v2 shape / refs | unit | `tests/test_workflow_v2.py` |
| request_validation pins digests | unit | `tests/test_workflow_v2.py` |
| approve fails on digest drift | unit | `tests/test_workflow_v2.py` |
| new_version returns draft | unit | `tests/test_workflow_v2.py` |
| If-Match mismatch → conflict | unit | `tests/test_if_match_writes.py` |
| two-writer no lost update | unit | `tests/test_if_match_writes.py` |
| created_by server-derived | unit | `tests/test_if_match_writes.py` |
| serve PUT needs --write, CSRF, write scope | unit | `tests/test_serve_writes.py` |
| PRESERVE governance / versioning | unit | existing files unmodified |
| OpenAPI additive | unit | `tests/test_contract.py` |

## Regression Strategy

- Do not edit `test_workflow_governance.py` or `test_entity_versioning.py`.
- CLI goldens: keep v1 shapes until migrate.
- `bash framework/ai-sdlc/verify.sh` then `bash workspace/sdlc/preflight.sh --full`.

## Playwright Strategy

n/a — no product UI surface; kit serve writes are HTTP contract tests only.

## Deployment Strategy

Ships with the kit tree; no Notaire deploy change.

## Alternatives considered

- Force all creates to v2 immediately → rejected (breaks PRESERVE goldens / governance).
- ETag digests instead of version If-Match → rejected (issue / UI-SPEC specify version).

## Rollback Strategy

Revert the PR. Adopters who already ran `aisdlc migrate` keep v2 bodies; reverts
do not auto-downgrade stored catalog files.

## Migration Plan

Run `aisdlc migrate` once per adopter profile after upgrade. Idempotent.

## Open Questions

None — FACTORY-UI-SPEC §3–5 is SoR.
