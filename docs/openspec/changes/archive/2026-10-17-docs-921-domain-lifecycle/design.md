> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #921 asks for documentation that is coherent from requirements through
operations. The product behaviour is already in the seeded workflow and the
services. The gap is a single description, plus a Process Checks false pass:
the job diffs `github.event.pull_request.base.sha` while checkout is
`refs/pull/N/merge` into current `main`.

## Goals / Non-Goals

**Goals:**

- One lifecycle document and one engineering-method document, linked from the indexes.
- Process Checks decide OpenSpec presence from the files this pull request changed.

**Non-Goals:**

- Changing workflow, deed, testimony, payment, or document behaviour.
- Closing #921.
- Rewriting every use case that mentions a state.

## Decisions

1. Mermaid in the permanent docs, not a new PlantUML set. ADR-027 already made Mermaid
   the active diagram language. The legacy `.puml` files stay, with a pointer.
2. `skip_specs: true`. No product requirement changes, so a delta spec would invent
   one. The acceptance proof is the existing docs link and traceability tests, plus
   the new process-check unit test.
3. When `HEAD` has two parents, `check-sdlc-exception.sh` diffs from `HEAD^1`.
   The workflow also rewrites `BASE_SHA` so the commit-message and TDD checks use
   that same parent. Rejected: trusting the event base SHA, which is what let
   `fix-1340-deed-pickers` satisfy this pull request's gate.

## Riesgos / Trade-offs

- [A reader treats the lifecycle doc as a new rule] → The document cites the seed and
  the service, and §8 lists the disagreements instead of silently picking one.
- [The merge-parent rule hides a real OpenSpec path that exists only on the stale
  base] → That path is already on `main` and is not this PR. The gate is about the PR.
- [#921 stays open] → This slice does not meet the audit's remaining acceptance criteria.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| A merge checkout does not count an OpenSpec path that landed on main | unit | `workspace/tests/test_pr_checks.py` |
| A merge checkout accepts an OpenSpec path the PR itself added | unit | `workspace/tests/test_pr_checks.py` |
| Doc links and the requirement-to-use-case cross-check still hold | static | `docs/tests/test_docs_links.py`, `docs/tests/test_business_docs_traceability.py` |

- New unit tests (`src/test/java/.../unit/`): none — no Java change
- New integration tests: none
- Coverage impact: JaCoCo ratchet not affected

The new unit test was observed failing (`✓ PR carries an OpenSpec change` on a
pull request that only touched `docs/README.md`) before the script used `HEAD^1`.

## Regression Strategy

- Existing tests affected: `SdlcExceptionTest` — assertions unchanged; two cases added
- Full suite command: `python3 -m unittest discover -s workspace/tests`
- HTTP/Bruno API suite: unchanged
- Legacy paths at risk: none

## Playwright Strategy

n/a — no UI surface. The change is documentation and a CI script.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: single pull request; the workflow change takes effect on the next run
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): Process Checks green on this pull request, with the OpenSpec step citing this change folder

## Rollback Strategy

- Revert safe: yes — docs plus a gate that returns to the previous (looser) base
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: readers keep the lifecycle doc; the gate stays strict
