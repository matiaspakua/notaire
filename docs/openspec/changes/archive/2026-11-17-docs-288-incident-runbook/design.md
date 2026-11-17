> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`infra/docs/OPERATION.md` already documents start, stop, health and Grafana panel proof. #288 asks for incident scenarios, escalation, communication, a post-incident template and a recovery checklist. CU77 marks runbooks done and still points the incident runbook at #288.

## Goals / Non-Goals

**Goals:**

- One runbook an operator can follow with commands that exist today.
- A test that the runbook names those commands and does not claim a finished drill.

**Non-Goals:**

- A paging vendor, a rota, or a recorded drill.
- New alerts.

## Decisions

Escalation is "the repository owner" because no rota is in the repo. Scenarios are only the ones the existing scripts already check: application health, Postgres, and empty Grafana panels. Recovery is `curl` of `/actuator/health` plus `check-infra.sh`. No numeric SLO is copied in unless OPERATION.md already states it.

## Riesgos / Trade-offs

- [The runbook lags a renamed script] → the unit test fails when `check-infra.sh` or `verify-signals.sh` is no longer named.
- [Readers treat "owner" as a staffed on-call] → the runbook says no rota is configured.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Runbook names real commands and does not claim a drill | unit | `infra/tests/test_incident_runbook.py` |

- New unit tests: one module.
- New integration tests: none.
- Coverage impact (JaCoCo raise-only floor; Owner >90% target #2046): none.

TDD: the test fails while the file is missing, then the runbook is added.

## Regression Strategy

- Existing tests affected: none.
- Full suite command: `python3 -m unittest infra.tests.test_incident_runbook`
- HTTP/Bruno API suite: unchanged
- Legacy paths at risk: none

## Playwright Strategy

n/a — no UI surface. Documentation only.

- Specs to add/update under `testing/e2e/tests/`: none
- Golden path covered: n/a
- Edge / error paths covered: n/a
- Viewports: n/a
- Command: not added; `preflight.sh --full` still runs the suite

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: none
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): the new unit test

## Rollback Strategy

- Revert safe: yes
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: operators keep the runbook

## Migration Plan

One pull request. #288 stays open.

## Open Questions

None.
