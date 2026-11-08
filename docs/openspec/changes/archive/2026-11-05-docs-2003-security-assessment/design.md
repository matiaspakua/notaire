> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Release-only product CI is already merged (#2001). Module grants are already enforced (#1683). The prose and the portfolio still described the previous pipeline. This change is documentation, a checked-in register, and a static site. It does not add a scanner.

## Goals / Non-Goals

**Goals:**

- One assessment a reader can check against files and workflow triggers.
- A YAML register unittest can compare to the markdown threat model.
- A public Security section that states it is not a live scan.
- An agent skill that says how to run the local gate and when to file an issue.

**Non-Goals:**

- Re-enabling pull-request workflows.
- Installing Threat Dragon, pytm, Threagile, OSV-Scanner, or Gitleaks.
- Publishing SARIF or Trivy JSON on GitHub Pages.

## Decisions

- PlantUML stays the diagram. Mermaid on the site repeats the same boundaries. The YAML register is the id list, not a Threagile model. Threat Dragon is #2009 because CI has no Dragon binary.
- The portfolio cadence is a JSON file the page renders and a Python test checks against each workflow `on:` block.
- AI Factory text is corrected in place so the site does not teach pull-request product CI. The product threat model is not copied into the factory doc.
- `fail_on_findings: true` records the Trivy `--exit-code 1` the runner already uses. The runner does not read the flag. The test reads both.

## Riesgos / Trade-offs

- [Portfolio copy drifts from workflows again] → `test_public_security_cadence.py` fails when a trigger class and the workflow disagree.
- [A reader treats the snapshot as a scan] → every public surface says it is not a live scan, and the JSON disclaimer is tested.
- [Owner ruleset still requires retired checks] → documented as #2001. This change does not apply the ruleset.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Register ids match the markdown models | unit | `security/tests/test_threat_register.py` |
| Public cadence matches workflow triggers | unit | `security/tests/test_public_security_cadence.py` |
| Program docs drop the stale claims | unit | `security/tests/test_assessment_currency.py` |
| CodeQL is not a pull_request workflow | unit | `security/tests/test_security_gate_surface.py` |
| Security is a primary route | unit | `github-page/tests/site.test.ts` |
| STRIDE explorer and cadence filter | unit | `github-page/tests/security-explorer.test.tsx` |
| Pages have one header and no axe violations | E2E | `github-page/e2e/site.spec.ts` |

- New unit tests (`src/test/java/.../unit/`): none. No Java change.
- New integration tests (`src/test/java/.../integration/`): none.
- Coverage impact (JaCoCo ratchet floor; 80% target): unchanged. No `backend-api` production code.

## Regression Strategy

- Existing tests affected: `test_security_gate_surface.py` expected `pull_request` on CodeQL. That expectation contradicted `test_ci_workflow_invariants.py` and the workflow. The assertion now matches the workflow.
- Full suite command: `python3 -m unittest discover -s security/tests` and `cd github-page && npm test`. `mvn verify -pl backend-api` is not required for this diff.
- HTTP/Bruno API suite: unchanged. No API change.
- Legacy paths at risk: none.

## Playwright Strategy

- Specs to add/update under `testing/e2e/tests/`: none. The product app is unchanged.
- The portfolio smoke is `github-page/e2e/site.spec.ts`, which now includes the Security routes and the security-gates page.
- Golden path covered: open `/security/`, `/security/threats/`, `/security/toolchain/`.
- Edge / error paths covered: axe and horizontal overflow at 375 and 1280.
- Viewports: the portfolio suite uses 375 and 1280. Product 320/768/1024 is n/a because the product UI did not change.
- Command: `cd github-page && npm run build && npm run test:smoke`

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: GitHub Pages publishes on push to `main` after merge. No image publish.
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): open the published `/security/` route. Not done in this change. The Owner merges.

## Rollback Strategy

- Revert safe: yes. Docs, tests, and static pages only.
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: the site would keep explaining the corrected cadence. No runtime behavior changes.

## Migration Plan

No staged rollout. One pull request.

## Open Questions

None that change the spec. Follow-up issues hold the deferred work.
