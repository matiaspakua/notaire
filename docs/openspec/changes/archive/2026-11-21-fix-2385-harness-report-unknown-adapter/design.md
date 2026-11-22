> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`harness.report_event()` in `aisdlc/harness.py` currently accepts any string for the `adapter` parameter and passes it directly to `runs.append_harness_event()`. The CLI handler in `cli.py` catches `KitError` and returns exit 2, but no validation of the adapter id exists. `harness.list_adapters()` already builds the authoritative id list from profile adapters and harness-kind plugins.

## Goals / Non-Goals

**Goals:**
- `harness report` rejects unknown adapter ids with exit 2, code `unknown_adapter`, naming known ids.
- No ledger record is written when the adapter is unknown.
- Known adapters work unchanged.

**Non-Goals:**
- Registering new adapters.
- Changing `harness list` output.

## Decisions

- **Validate in `report_event()`, not in the CLI handler.** The service layer is the single place for business-rule validation; the CLI only formats the error. This matches the existing `KitError` pattern used for `not_found`, `unknown_kind`, etc.
- **Reuse `list_adapters()` to build the known-id set.** Calling the same function that `harness list` uses ensures the validation and the list command stay in sync. No separate registry is needed.
- **New `KitError` code `unknown_adapter`.** Added to `errors.CODES`. Message lists known ids so the operator can correct the typo.

## Riesgos / Trade-offs

- [Risk] `list_adapters()` could be slow if called on every report. → Mitigation: it reads in-memory profile data; no I/O beyond what the profile already loaded.
- [Risk] Existing callers of `report_event()` with hardcoded `"local-worker"` could break if the profile changes. → Mitigation: `local-worker` is declared in the Notaire profile and will remain; this is a correctness fix, not a breaking change.

## Testing Strategy

TDD is mandatory: regression test in `framework/ai-sdlc/tests/` fails before the fix and passes after.

| Scenario (spec) | Test level | Test file |
|-----------------|------------|-----------|
| Unknown adapter refused | integration | `tests/test_harness_report.py` |
| Known adapter still works | integration | `tests/test_harness_report.py` |

- New test in `framework/ai-sdlc/tests/test_harness_report.py`: two scenarios (unknown adapter exits 2 with `unknown_adapter`; known adapter `local-worker` succeeds).
- Coverage impact: adds lines to `harness.py` and `errors.py`; test covers both branches.

## Regression Strategy

- Existing tests affected: `test_harness_report.py` — existing tests use `local-worker`, which remains a known adapter; no changes needed.
- Full suite command: `cd framework/ai-sdlc && python3 -m pytest tests/ -v`
- Legacy paths at risk: none — Python framework only.

## Playwright Strategy

n/a — no UI surface. This is a CLI validation change in the ai-sdlc framework.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: none — framework code only.
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): `env -u TRACEPARENT bash framework/ai-sdlc/verify.sh`

## Rollback Strategy

- Revert safe: yes — pure validation addition; reverting restores the previous permissive behavior.
- Database rollback: none needed
- Data written under the new behavior after revert: none — the fix prevents writes, so reverting only re-allows them.
- Blast radius if rollback is delayed: none

## Open Questions

None.
