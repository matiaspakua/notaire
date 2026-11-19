> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

OWASP Threat Dragon publishes `td.vue/src/assets/schema/threat-dragon-v2.schema.json`. That file uses `type`, `required`, `properties`, `items`, `minimum`, `minLength`, and `maxLength`. The official sample `v2-threat-model.json` validates under a checker for those keywords. CI does not need the Dragon binary.

## Goals / Non-Goals

**Goals:**

- The Notaire model is a v2 document.
- Its threat ids equal the register's requirement ids.

**Non-Goals:**

- A layout that the Dragon editor would draw by hand.
- A second diagram generator.

## Decisions

Vendor the schema at the commit fetched on 2026-11-19 (`30dc0ff6541b8bac075b107263bbe6e6764bcabc`). One cell per register asset. Threat `id` is the `SR-*` id. Severity is the string `Medium` because the register does not score severity. Descriptions name the STRIDE property and the asset. Mitigations are the register's verification pointers.

## Riesgos / Trade-offs

- [The vendored schema lags upstream] → the test names the commit. A schema that adds a keyword this checker ignores would still pass; the checker is limited to the keywords this file uses, and the official sample is the calibration.
- [Severity is uniform] → the register status stays on the threat. The model does not invent a High/Low score.

## Testing Strategy

| Scenario | Test level | Test |
|----------|------------|------|
| Schema missing | unit | `test_threat_dragon_model` failed before the file existed |
| Ids match both ways and the model validates | unit | same test |
| Threat Dragon is no longer "not adopted" | unit | `test_threat_register` |

TDD: the test failed with `vendored Threat Dragon v2 schema is missing`.

## Regression Strategy

- `python3 -m unittest security.tests.test_threat_dragon_model security.tests.test_threat_register security.tests.test_assessment_currency`
- `bash workspace/sdlc/preflight.sh --full` before push.

## Playwright Strategy

n/a — no product UI.

- Specs under `testing/e2e/tests/`: none

## Deployment Strategy

- Flyway: no

## Rollback Strategy

- Revert safe: yes
- Database rollback: none
