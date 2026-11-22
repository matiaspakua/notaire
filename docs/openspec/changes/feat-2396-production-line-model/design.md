> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

The kit already has the building blocks: `profile.py` knows stage order, gate
declarations, and protected paths; `stages.py` serialises per-stage knobs; `catalogs.py`
lists catalog entries; `isolator.py` probes sandbox status; `cli.py` and `serve.py` have
established dispatch patterns. What is missing is a single composition function that
combines these into a stable, schema-versioned production-line payload — and the
`inputs`/`outputs` dimension on each stage.

The design constraint (from AGENTS.md / CONSTITUTION §2 KISS) is to compose, not
duplicate: `line.py` calls existing functions and assembles their output; it owns no
new data extraction logic.

## Goals / Non-Goals

**Goals:**

- One `build_line(ctx)` function in `line.py` that returns the full payload.
- Additive `inputs`/`outputs` on `StageProfile` with kit defaults for known stage ids.
- CLI `aisdlc line [--json]` and `GET /factory/line` both backed by `build_line`.
- OpenAPI schema updated for `/factory/line`.
- Notaire profile declares `inputs`/`outputs` for its four declared stages.
- Tests on a sample profile that covers all acceptance scenarios.

**Non-Goals:**

- Mutable line endpoints.
- Executing gates or agents.
- Reading secrets or env vars.
- Live-state or SSE (#2393 D).

## Decisions

### D1 — `line.py` composes existing sources; no logic duplication

`line.py` calls:

- `stages.stage_dicts(profile)` for agents / skills / mcp / model / effort.
- `profile.gates_for(stage)` for gate lists.
- `profile.stages()` for canonical lifecycle order.
- `profile.protected` for the protected-paths guardrail.
- `catalogs.list_entries(ctx, "rules")` for the rules catalog (tolerate missing
  catalogs dir → empty list; never fail on absent catalogs).
- `isolator.isolator_status()` for sandbox/committer status.

Rationale: extracting the same data again in `line.py` would create two diverging
implementations. Composing from the existing layer keeps the production-line payload
provably consistent with the rest of the kit.

### D2 — Gate `id` derived as a slug of the gate name

`profile.Gate` has no `id` field. A stable id is derived: lowercase the name,
replace every non-alphanumeric character with `-`, collapse repeated `-`, strip
leading/trailing `-`. Example: `"OpenSpec plans conform (live issue check)"` →
`"openspec-plans-conform-live-issue-check"`.

Rationale: gate names are stable (they appear in the ledger and in reports), so a
deterministic slug gives a stable id without changing the profile schema.

### D3 — `io_source` distinguishes profile-declared from kit-default inputs/outputs

When a stage declares `inputs`/`outputs` in `[[stages]]`, `io_source = "profile"`.
When neither is declared and the kit has a default for the stage id,
`io_source = "default"`. When neither applies (no declaration and no default),
`io_source = "default"` and the lists are empty.

Default I/O per known stage id (`DEFAULT_IO` in `line.py` or `stages.py`):

| Stage | Inputs | Outputs |
|-------|--------|---------|
| `plan` | `["GitHub issue", "use case"]` | `["OpenSpec change"]` |
| `red` | `["OpenSpec change"]` | `["failing tests"]` |
| `green` | `["failing tests"]` | `["passing tests"]` |
| `pre-push` | `["branch with passing tests"]` | `["preflight evidence"]` |
| `pre-pr` | `["preflight evidence"]` | `["pull request"]` |
| `merge` | `["pull request", "CI green"]` | `["merged commit"]` |
| `release` | `["merged commit"]` | `["deployed release"]` |

### D4 — Guardrails: hooks are protected paths that contain `hook` in the value

The profile has no dedicated `[hooks]` section. The convention used by Notaire is to
list hook scripts under `[protected].paths`. Paths whose value contains the substring
`hook` (case-insensitive) are exposed as `guardrails.hooks` as strings. This is
explicitly documented in the payload so consumers understand the derivation.

Rationale: does not require a schema change; accurately reflects what the profile
declares; avoids inventing structure that does not exist.

### D5 — `GET /factory/line` uses existing read-scope dispatch

`serve.py`'s `_dispatch_get` already handles read-scope paths. The new block is added
adjacent to the existing stage/workflow code (a small `elif path == "/factory/line":`
block) to minimise merge conflicts with parallel workers (#2394 store, #2395 intents).

### D6 — Stage appears if declared in `[[stages]]` OR has gates

`line.py` computes the union of `{sp.id for sp in profile.stage_profiles}` and
`profile.stages()` (which already returns stages that have gates). The union is ordered
by `profile.stages()` first, then alphabetically for any purely `[[stages]]`-only extras
that have no gates.

## Riesgos / Trade-offs

- [Naming collision on slug ids] If two gates have names that produce the same slug,
  their ids collide. Mitigation: deduplicate by appending `-2`, `-3` etc. if needed
  (simple suffix counter in `line.py`).
- [Missing catalogs dir] `catalogs.list_entries` may raise if the directory is absent.
  Mitigation: wrap in try/except and return `[]` for the rules list.
- [Parallel merge conflicts] Workers #2394 and #2395 also touch `cli.py` and `serve.py`.
  Mitigation: each addition is a separate `elif` block / handler function adjacent to
  existing related code; no reformatting of existing lines.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Line lists ordered stages with gates | unit | `tests/test_line.py` |
| inputs/outputs from profile override kit defaults | unit | `tests/test_line.py` |
| Stage without inputs/outputs gets defaults and io\_source default | unit | `tests/test_line.py` |
| Guardrails expose protected paths, hooks, rules, isolator | unit | `tests/test_line.py` |
| GET /factory/line read scope | unit | `tests/test_serve_line.py` |
| Stage with no gates still listed (via \[\[stages\]\]) | unit | `tests/test_line.py` |
| Text and --json output | unit | `tests/test_line.py` |

- New unit tests: `framework/ai-sdlc/tests/test_line.py`,
  `framework/ai-sdlc/tests/test_serve_line.py`
- New integration tests: none (stdlib-only kit; unit tests cover the full stack)
- Coverage impact: additive; kit's existing floor is not lowered

## Regression Strategy

- `stages.stage_dicts` is changed additively (new keys `inputs`, `outputs`); existing
  callers receive new keys but are not broken. Existing test `tests/test_stages.py`
  must be updated to assert the new keys are present.
- `StageProfile` gains two new frozen fields with default `()`. Frozen dataclass
  construction is backward-compatible; existing construction sites that do not pass
  `inputs`/`outputs` get the empty-tuple default.
- `cli.py` and `serve.py` additions are new dispatch branches; no existing branch is
  modified.
- Full suite command: `python3 -m pytest framework/ai-sdlc/tests/`
- HTTP/Bruno API suite: n/a (kit-level tests only)
- Legacy paths at risk: none

## Playwright Strategy

n/a — no UI surface. This change adds CLI and HTTP API surface only. The future
dashboard (issue #2393 G) will cover the visual production-line timeline.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: none; the kit is a Python package used locally
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): `aisdlc line --json` exits 0 and returns valid JSON

## Rollback Strategy

- Revert safe: yes — the change is additive (new module, new fields with defaults,
  new CLI subcommand, new HTTP path). Reverting removes the new surface without
  affecting existing behavior.
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: none; read-only surface

## Migration Plan

No staged rollout required. Additive change; deploy as a single commit.

## Open Questions

None — all decisions are captured above and in the design decisions in the issue.
