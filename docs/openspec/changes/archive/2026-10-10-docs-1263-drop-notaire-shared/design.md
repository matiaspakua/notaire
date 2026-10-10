> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #1263, Use Case CU76. #1255 / ADR-025 retired `notaire-shared`. The Constitution
Impact Analysis example list was split out of that PR because Constitution amendments
need their own Owner-reviewed PR (§12).

## Goals / Non-Goals

**Goals:** Constitution §5 step 4 lists only live modules; the retirement guard covers
`CONSTITUTION.md`.
**Non-Goals:** Englishize the whole §5 flowchart; touch SAD/DTO guides (follow-on);
#1197 / #1259 context packing.

## Decisions

1. Reuse `NotaireSharedRetiredTest.test_docs_do_not_present_the_module_as_live` by adding
   `CONSTITUTION.md` to `DOCS_AS_NON_LIVE` — rejected inventing a second guard.
2. Edit only the Impact Analysis example list — rejected rewriting the whole §5 diagram
   in this PR (keeps the amendment reviewable).

## Riesgos / Trade-offs

- Historical OpenSpec archives and ADR-025 still contain the string `notaire-shared`;
  the guard allows lines that also carry retired/deprecated/ADR-025 markers.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Constitution Impact Analysis does not list notaire-shared as live | static | `workspace/tests/test_notaire_shared_retired.py` `test_docs_do_not_present_the_module_as_live` |
| Existing retirement scenarios stay green | static | same class |

- New unit tests (`src/test/java/.../unit/`): n/a — no Java
- New integration tests: n/a
- Coverage impact: none

## Regression Strategy

- Existing tests affected: `NotaireSharedRetiredTest` only (assertion surface grows).
- Full suite command: `python3 -m unittest workspace.tests.test_notaire_shared_retired` then
  `bash workspace/sdlc/preflight.sh` as environment allows.
- HTTP/Bruno API suite: unchanged.
- Legacy paths at risk: none.

## Playwright Strategy

No UI surface — n/a.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: docs/guard only; merge when heavy CI green
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): CI green on the merge commit; issue auto-closes via `Closes #1263`

## Rollback Strategy

- Revert the PR; documentation and one guard list entry only.
