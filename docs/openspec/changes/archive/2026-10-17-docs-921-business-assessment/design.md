> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #921. The October audit checked that use-case files and links exist. It
did not check that the case activity matches the seeded graph or the English
HTTP paths from #1490.

## Goals / Non-Goals

**Goals:**

- One case-process schema with a use-case view, a dependency table, and one
  sequence per step the system executes.
- The legacy activity no longer archives after testimony.
- A diff check that a traceability row marked yes names a file in that diff.

**Non-Goals:**

- A second state machine. States stay in `CICLO-DE-VIDA.md`.
- Closing #921.
- Revalidating every legacy sequence `.puml`.

## Decisions

1. Mermaid in `GESTION.md`. The `.puml` files are corrected in place because
   readers still open them, and ADR-027 keeps them legacy.
2. `skip_specs: true`. No product behaviour changes.
3. The check reads only `traceability.md` files in the diff, section
   "Permanent documentation updated", rows whose second cell is yes, sí, or si.
   A follow-up that does not touch the ledger is not re-checked.

## Riesgos / Trade-offs

- [A later rename of an HTTP path leaves the sequences stale] → The sequences
  cite the paths on `main` after #1490. A path change must edit `GESTION.md`.
- [The check is skipped when traceability.md is not in the diff] → The plan
  validator still requires the section to exist on an active change.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| A yes row whose file is absent fails | unit | `workspace/tests/test_pr_checks.py` |
| A yes row whose file is in the diff passes | unit | `workspace/tests/test_pr_checks.py` |
| A diff with no traceability file passes | unit | `workspace/tests/test_pr_checks.py` |
| Doc links resolve | static | `docs/tests/test_docs_links.py` |

- New unit tests: the three cases above. Observed failing (exit 2, script missing) before the script existed.
- New integration tests: none
- Coverage impact: JaCoCo not affected

## Regression Strategy

- Existing tests affected: none weakened
- Full suite command: `python3 -m unittest workspace.tests.test_pr_checks.DocTraceabilityTest`
- HTTP/Bruno API suite: unchanged
- Legacy paths at risk: none

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: single pull request
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): Process Checks run the new step

## Rollback Strategy

- Revert safe: yes
- Database rollback: none
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: readers keep the corrected activity
