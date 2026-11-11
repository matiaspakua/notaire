> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

FORMS-AUDIT-PLAN Phase 1 was source-only. #2087 rendered the people dialog and raised CheckboxField to the WCAG 2.2 AA 24px floor. Administration catalogs already compose FormContainer/FormField/FormActions; this change proves the rendered desk and hardens shared drift once.

Surfaces in scope: concepts, documents, folios, identification-types, management-statuses, procedures (+ RequiredDocumentsDialog), roles, templates, users, workflows (+ workflows/[id]).

## Goals / Non-Goals

**Goals:** Rendered Impeccable/Playwright audit evidence (1280 + 320); contract compliance for admin catalogs; TDD locks; update FORMS-AUDIT-PLAN.
**Non-Goals:** People (#2087); managements/deeds/protocol/case modules; GitHub Pages; inventing a new form system.

## Decisions

Prefer one Playwright suite that walks each catalog create dialog over many one-off specs. Fix shared gaps in form-patterns (and CheckboxField reuse) rather than per-page copies. Mirror people-form locks: labels/required/errors, aria-busy on pending save, primary `h-11`, 320px overflow check. Add `data-testid` on primary saves where missing so E2E is stable.

## Riesgos / Trade-offs

[False green from source-only checks] → Record rendered audit engine and viewports. [Scope creep into case modules] → Explicit OOS. [Heavy Playwright wall time] → One parameterized suite; serialize with other heavy PRs.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Required fields announce and show errors | unit | `admin-catalog-forms-rendered-a11y.test.tsx` |
| Labels are not placeholder-only | unit | `admin-catalog-forms-rendered-a11y.test.tsx` |
| Shared checkbox controls meet the touch floor | unit | `admin-catalog-forms-rendered-a11y.test.tsx` (+ CheckboxField if changed) |
| Save disables while pending | unit | `admin-catalog-forms-rendered-a11y.test.tsx` |
| Primary save meets touch floor at desktop and 320px | E2E | `TS-0158-admin-catalog-forms-rendered-a11y.spec.ts` |
| Narrow viewport does not clip the primary action | E2E | `TS-0158-admin-catalog-forms-rendered-a11y.spec.ts` |

- New unit tests (`frontend/src/tests/unit/`): admin catalog rendered a11y
- New integration tests: n/a — frontend-only UI contract
- Coverage impact (JaCoCo raise-only floor; Owner >90% target #2046): frontend Vitest only; no JaCoCo change

## Regression Strategy

- Existing tests affected: `people-form-rendered-a11y`, `checkbox-field-touch-target`, `form-field`, TS-0156/TS-0157 must stay green
- Full suite command: `mvn verify -pl backend-api` (no backend change expected)
- HTTP/Bruno API suite: `bash integration-test/scripts/test.sh` (unchanged)
- Legacy paths at risk: none

## Playwright Strategy

- Specs to add/update under `testing/e2e/tests/`: `TS-0158-admin-catalog-forms-rendered-a11y.spec.ts`
- Golden path covered: open create dialog on each in-scope catalog page; measure primary height; empty-required submit where applicable
- Edge / error paths covered: pending busy/disabled (unit); 320px overflow
- Viewports: 320px and 1280px (desktop); 768 optional smoke via existing suite
- Command: `cd testing/e2e && npx playwright test TS-0158-admin-catalog-forms-rendered-a11y.spec.ts`

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: ships with next release
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): open Administration → Concepts create dialog; confirm labels and Save height

## Rollback Strategy

- Revert safe: yes — UI/a11y locks only
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: clerks keep previous catalog UX

## Migration Plan

None.

## Open Questions

None.
