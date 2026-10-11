> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Audit of `main` (2026-10-11) against specialization guidelines (MTP, trazabilidad,
identificación, pirámide, QA Automation) and AI SDLC:

| Finding | Detail |
|---------|--------|
| MTP is E2E-only | `TEST-PLAN.md` titled “E2E Test Suite”; missing product-wide MTP sections |
| Inventory drift | Docs claim 35 suites / 448 tests; disk has 74 `TS-*` + 9 unprefixed = 83 specs |
| Coverage lies | CONSTITUTION Vitest “75%”; enforced floor is 14/9/10/14; README still cites 70/25 JaCoCo |
| ADR-006 stale | Trophy + POM + Gatling + ~10–20 E2E; practice is GherkinSteps + Bruno + k6 + large E2E |
| TC orphans | Nine `TC-00x` markdowns + LOGIN report not wired to automation IDs |
| No inventory guard | CU-API matrix is gated; E2E naming/inventory is not |

## Goals / Non-Goals

**Goals:**

- One Master Test Plan as process authority for all test levels.
- Process architecture doc (levels, owners, triggers, evidence).
- Generated inventory + CI drift guard (same spirit as CU-API matrix).
- All Playwright specs use `TS-nnnn` IDs.
- Constitution, ADR-006, and coverage docs state enforced floors only.

**Non-Goals:**

- Redesigning CI path filters or moving E2E to nightly.
- Closing Bruno gaps or raising Vitest floors.
- Auto-generating Java `UseCaseRouteCatalog` from OpenAPI.

## Decisions

1. **MTP = `TEST-PLAN.md`** — rewrite in place (keep path; Constitution already links it).
   Structure follows specialization MTP: purpose, scope, strategy, IDs, repository,
   levels, environments, evidence, flaky policy. E2E TS catalog becomes a section
   that points to generated `INVENTORY.md` instead of a hand-maintained full table.
2. **Generated `INVENTORY.md`** — `generate-testing-inventory.py` scans:
   - `testing/e2e/tests/TS-*.spec.ts`
   - counts of backend unit/integration `*Test.java`, Vitest files, Bruno yml
   - writes markdown tables. `validate-testing-inventory.py` fails if the file is
   stale vs disk or if any `*.spec.ts` lacks the `TS-nnnn-` prefix.
3. **Rename nine unprefixed specs to TS-0130–TS-0138** — free high range avoids
   colliding with intentional gaps in 0025–0089. Update active OpenSpec change
   references that cite the old filenames.
4. **ID scheme** — `CU-xx` (use case) → `TC-*` (logical case, optional narrative) →
   `TS-nnnn` / Bruno path / JUnit class (automation) → Issue. Archive
   `LOGIN-TEST-REPORT.md` (ephemeral snapshot per #1041); keep `LOGIN-TEST-SUITE.md`
   and `TC-00x` as examples with a README in `test-cases/`.
5. **ADR-006** — status remains Accepted; body updated for current stack (not a
   new ADR number — same decision, corrected facts).
6. **Constitution §7** — replace fossil pyramid numbers with “see INVENTORY.md”;
   Vitest row = raise-only floors from `vitest.config.ts`; aspirational 80% labeled.
7. **TDD** — write `testing/tests/test_testing_inventory.py` red first (expects
   generator/validator + all-TS naming), then implement.

## Riesgos / Trade-offs

- [Renaming specs breaks local bookmarks / old PR comments] → git history retains
  old names; CHANGELOG lists mapping.
- [Generated INVENTORY committed] → same pattern as other living indexes; validate
  in preflight prevents silent drift.
- [MTP rewrite large] → keep E2E fixture/skip policy content; drop duplicated
  static TS tables in favor of inventory link.
- [Other active OpenSpec changes cite old spec names] → update only active
  references under `docs/openspec/changes/` that are not archive.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Inventory regenerates from disk | unit (Python) | `testing/tests/test_testing_inventory.py` |
| Stale INVENTORY fails validation | unit (Python) | same |
| Unprefixed Playwright spec fails validation | unit (Python) | same |
| All on-disk specs match `TS-nnnn-*.spec.ts` | unit (Python) | same |
| MTP exists and declares levels + ID scheme | static | same + doc presence asserts |
| Process architecture doc exists | static | same |
| Preflight wires inventory validation | static | `workspace/tests` or testing tests |

- New unit tests (`src/test/java/.../unit/`): none
- New integration tests (`src/test/java/.../integration/`): none
- Coverage impact (JaCoCo ratchet floor; 80% target): none (docs/tooling only)

## Regression Strategy

- Existing tests affected: Playwright rename only (same test bodies); OpenSpec
  active change path strings for renamed files.
- Full suite command: `mvn verify -pl backend-api` (unchanged product code)
- HTTP/Bruno API suite: not required for this docs/tooling change
- Meta regression: `python3 -m unittest discover -s testing/tests` and
  `python3 workspace/sdlc/validate-testing-inventory.py`
- Legacy paths at risk: none (Swing already retired)

## Playwright Strategy

- Specs to add/update under `testing/e2e/tests/`: rename nine unprefixed files to
  `TS-0130`–`TS-0138` (no new behaviors).
- Golden path covered: n/a — no UI product change
- Edge / error paths covered: n/a
- Viewports: n/a — no UI surface change
- Command: inventory validation replaces a full Playwright rerun for Gate 3 of
  this change; `n/a — no UI surface` for product UI. Spec rename verified by
  filesystem + validator.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: docs and CI check only; no runtime deploy dependency
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): `python3 workspace/sdlc/validate-testing-inventory.py`
  exit 0 on `main` after merge

## Rollback Strategy

- Revert safe: yes — revert the PR; inventory check disappears; rename can be
  reverted with git
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: CI may fail inventory if someone adds an
  unprefixed spec; product runtime unaffected

## Migration Plan

1. Gate 2: failing inventory tests.
2. Add generator/validator; generate INVENTORY; rename specs.
3. Rewrite MTP + process architecture; update ADR/Constitution/README.
4. Wire preflight; run unittest discover; validate OpenSpec.
5. PR → merge → archive change.

## Open Questions

None — scope is process/docs/tooling with mechanical guards.
