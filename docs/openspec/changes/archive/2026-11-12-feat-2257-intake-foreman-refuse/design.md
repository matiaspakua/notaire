> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

#2261 shipped pure `lint()` + `aisdlc intake check` with configurable `[intake]`
sections and `[tracker] fetch`/`verify`. Foreman `plan|route` is read-only routing
only. Claim refuse and board cleanliness are still prose.

## Goals / Non-Goals

**Goals:** machine-checkable Foreman refuse on intake fail when enforced; clean-board
report; Notaire stays advisory until Owner raises after evidence.

**Non-Goals:** auto-fixing issues; flipping Notaire enforcement in this PR;
rewriting `workspace/sdlc/claim-issue.py` (Notaire-specific forge claim stays separate;
kit exposes the intake refuse signal).

## Decisions

1. **`aisdlc foreman claim`** reuses intake `lint()` / load helpers. JSON always
   reports `intake_status`, `required`, `dispatch_allowed`, `would_refuse`.
   `would_refuse = status != "pass"`; `dispatch_allowed = not would_refuse or not required`.
   Exit 1 only when `required` and not pass (including no-evaluable).
2. **`aisdlc intake board`** takes `[tracker] list` (JSON array) or `--file`.
   `clean=true` iff every issue status is `pass`. Advisory exit mirrors claim.
3. **`[intake] required` default false** (advisory). Raise-only: docs + board note
   say Owner may set true only after a clean report; this PR leaves Notaire false.
4. **Kit independence:** no Notaire paths in kit Python/templates; profile holds
   `gh … --repo matiaspakua/notaire` only under `.aisdlc/`.

## Riesgos / Trade-offs

- [Open board still dirty] → advisory keeps dispatch_allowed; board command surfaces debt.
- [Tracker list shape differs] → documented minimal array of `{number|title,body,labels}`; `--file` for tests.
- [no-evaluable treated as refuse when required] → axiom 3; skip is not pass.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| enforced claim refuses on fail | unit/CLI | `tests/test_intake.py` |
| advisory claim allows with would_refuse | unit/CLI | `tests/test_intake.py` |
| board clean / dirty / no list | unit/CLI | `tests/test_intake.py` |
| configured sections still work | unit/CLI | existing + preserve |
| kit has no Notaire product paths in intake/foreman | unit | `tests/test_intake.py` |

- New unit tests: kit `unittest` only (no Maven product suite for this change).
- Coverage impact: kit verify.sh; JaCoCo floor untouched (no Java).

## Regression Strategy

- Existing tests affected: `test_intake.py` extended; `test_foreman_plan.py` preserved.
- Full suite command: `bash framework/ai-sdlc/verify.sh`
- HTTP/Bruno / Playwright: n/a — kit-only, no product UI/API surface.

## Playwright Strategy

n/a — no UI surface (kit CLI/profile only).

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: ships with next kit/adopter pull; advisory by default
- Configuration: `.aisdlc/aisdlc.toml` `[intake] required=false`, `[tracker] list`
- Feature flag: no
- Smoke test after deploy (Gate 5): `aisdlc foreman claim <n> --json`; `aisdlc intake board --json`

## Rollback Strategy

Revert the PR. Safe: advisory-only profile keys; no enforcement flip.

## Migration Plan

Adopters already on #2261: add optional `required` / `list`. No breaking change.

## Open Questions

None.
