> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`train_for()` in `workspace/release/product_release.py` performs a prefix
lookup against `RELEASE_TRAINS`. When v0.8.0 shipped, the tuple contained only
entries through v0.7, so every coherence check and product-record command failed
with a `ValueError`. The same gap existed in `milestone_policy.py`'s `SCHEDULED`
list and in the MILESTONE-NOMENCLATURE table.

## Goals / Non-Goals

**Goals:**
- Restore `train_for("0.8.0")` to return the correct train title.
- Keep `RELEASE_TRAINS` and `milestone_policy.SCHEDULED` in sync (enforced by test).

**Non-Goals:**
- Creating or modifying the actual GitHub milestone (out of scope for a code fix).
- Changing the train theme or delivery date.

## Decisions

- Train title follows the established pattern: `vX.Y · <Theme> (YYYY-MM)`.
- `OBSERVABLE_FACTORY = "v0.8 · Observable self-managing factory (2027-03)"` matches
  the existing constant naming convention in `milestone_policy.py`.
- No new module; the constant is inserted in the existing SCHEDULED tuple, preserving order.

## Riesgos / Trade-offs

- [Risk] RELEASE_TRAINS and milestone_policy.SCHEDULED can drift → Mitigated by
  `workspace/sdlc/test_milestone_policy.py` which asserts both lists agree.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Every minor has a train | unit | `workspace/sdlc/test_milestone_policy.py` |
| train_for("0.8.0") returns v0.8 train | unit | `workspace/tests/test_product_release.py` |

- New unit tests: `workspace/sdlc/test_milestone_policy.py`, `workspace/tests/test_product_release.py`
- Coverage impact: existing Python test files; no JaCoCo floor affected.

## Regression Strategy

- Existing tests affected: `test_milestone_policy.py` SCHEDULED assertion (updated).
- Full suite: `python3 -m pytest workspace/sdlc/test_milestone_policy.py workspace/tests/test_product_release.py`
- HTTP/Bruno API suite: n/a — no API endpoint changed.
- Legacy paths at risk: none.

## Playwright Strategy

n/a — no UI surface. This is a pure Python/data fix in SDLC tooling scripts.

## Deployment Strategy

- Flyway migration required: no
- Deployment order: none — tooling-only change, takes effect on next script invocation.
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy: `python3 workspace/release/product_release.py --check` exits 0.

## Rollback Strategy

Revert commit 55d876f9. The revert is safe: no migration, no stored state; only
in-memory lookup tables are affected.
