> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Notaire already declares `stage = "signal"` for local ratchet/skills ledger
recording (#2159 / `preflight-evidence`). Doctor still warns that `signal` is not
in the canonical set. Foreman plan only maps stages with `[[stages]]` knobs
(#2196) — today only `plan` — without saying the rest are expected-unmapped.
Lead-time is Issue-keyed (#2138/#2184); ops habit passed a PR number (#2210).

## Goals / Non-Goals

**Goals:**
- Doctor accepts `signal` as documented local alias (no WARN)
- Foreman plan/doctor honestly lists unmapped stages; no fake knobs
- Lead-time help + doctor note: Issue number, not PR
- Document choices in FACTORY-ROADMAP

**Non-Goals:**
- Wilson L1, `[committer] required=true`, portal/Context Lake, fake attest
- Inventing `[[stages]]` rows for every gate stage
- Renaming `signal` → canonical id
- Changing lead-time event-pair semantics

## Decisions

1. **Accept `signal` as kit-known local alias** (preferred over renaming profile
   gates). Keep `STAGES` canonical set unchanged; doctor skips the non-canonical
   WARN for ids in a small `LOCAL_STAGE_ALIASES = {"signal"}` set. Document in
   FACTORY-ROADMAP / README. Other custom stages still warn.
2. **Foreman honesty over fake knobs.** Enrich `build_plan` with
   `unmapped_stages` = gate stage ids without a `[[stages]]` row (respect
   `--stage` filter). Extend `PLAN_NOTE` / `doctor_note` to say unmapped is
   expected until knobs are filled. Do not add Notaire `[[stages]]` for
   red/green/pre-pr solely to raise `mapped_count`.
3. **Lead-time hint only.** Clarify `--issue` help text and add a short doctor
   INFO line (or fold into an existing metrics note) — no forge behavior change.

## Riesgos / Trade-offs

- [Operators miss that other custom stages still warn] → Mitigation: test still
  asserts non-`signal` custom stages WARN.
- [Readers think `unmapped_stages` means broken profile] → Mitigation: note says
  "expected until knobs are filled"; doctor INFO matches.
- [Spanish template headings remain for validate] → Mitigation: English body
  content; schema headings unchanged (validate-sdlc-plan).

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| doctor accepts signal local alias | unit (kit) | `framework/ai-sdlc/tests/test_doctor_signal_alias.py` |
| unknown custom stage still warns | unit (kit) | same |
| foreman plan lists unmapped stages | unit (kit) | `framework/ai-sdlc/tests/test_foreman_plan.py` |
| foreman doctor note mentions unmapped | unit (kit) | same |
| lead-time issue help says Issue not PR | unit (kit) | `framework/ai-sdlc/tests/test_metrics_lead_time_hint.py` |
| doctor INFO mentions Issue not PR | unit (kit) | same |

- New unit tests: kit unittest only (temp git repos, no network)
- New integration tests: n/a
- Coverage impact: kit Python only; JaCoCo N/A

## Regression Strategy

- Existing tests affected: `test_foreman_plan.py` (note shape), `test_init_doctor.py`
  (Notaire doctor still OK), `test_lifecycle.py` (Notaire doctor green)
- Full suite command: `python3 -m unittest discover -s framework/ai-sdlc/tests`
- HTTP/Bruno API suite: n/a (no API change)
- Legacy paths at risk: none

## Playwright Strategy

n/a — no UI surface (kit CLI / docs only)

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: ships with next kit release; profile unchanged
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): `aisdlc --profile .aisdlc/aisdlc.toml doctor`
  (no signal WARN; Foreman INFO mentions unmapped; lead-time Issue hint present)

## Rollback Strategy

Revert the PR. Safe — honesty/docs only; no schema or product API change.

## Migration Plan

None. Existing Notaire `signal` gates keep working; doctor warning disappears.

## Open Questions

None — acceptance already prefers documenting/accepting `signal` as the local alias.
