# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2086 | open |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/docs-2086-factory-routing/` | active |
| Branch | `cursor/factory-phase1-routing-b7c4` | created |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | Local `preflight.sh --full`. GitHub does not re-run it (#2001) | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | n/a until the Owner deploys | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Architect prompt follows ADR-030 | `test_factory_phase1_routing.FactoryPhase1RoutingTest.test_architect_follows_adr_030` | passing |
| Backend implementer does not prescribe legacy layers | `test_backend_implementer_does_not_send_new_code_to_legacy_layers` | passing |
| Manifest routes hex, forms, and security | `test_manifest_routes_hex_forms_and_security` | passing |
| Impeccable provenance is on the frontend skill row | `test_frontend_and_skills_name_impeccable_provenance` | passing |
| Local Cursor handoff names the worker command and #1693 #577 #580 | `test_handoff_names_the_local_cursor_worker` | passing |
| Forms plan is code inspection only | `test_forms_plan_is_a_code_inventory` | passing |
| Marker-less WIP stays held | `test_claim_issue.ClaimFlowTest.test_markerless_wip_stays_held_and_is_reported` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/304-ai-sdlc-cloud/LOCAL-CURSOR-HANDOFF.md` | yes | pending |
| `docs/300-development/304-ai-sdlc-cloud/FORMS-AUDIT-PLAN.md` | yes | pending |
| `docs/300-development/304-ai-sdlc-cloud/README.md` | yes | pending |
| `docs/300-development/304-ai-sdlc-cloud/FLEET-ARCHITECTURE.md` | yes | pending |
| `AGENTS.md` | yes | pending |
| `.claude/skills/README.md` | yes | pending |
| `CHANGELOG.md` | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `validate-sdlc-plan.sh` and `openspec validate --strict` exit 0 |
| 2 | Failing tests written, test cases designed | yes | red commit `90bff81b`, then green unit run |
| 3 | Suite green, coverage held, docs updated | pending | `preflight.sh --full` |
| 4 | CI green, review approved, no conflicts | pending | Owner review. No per-PR heavy CI (#2001) |
| 5 | Deployed, smoke test passed, Issue closed | no | Owner merges. Not this run |

## Exceptions

The cloud runtime requires the branch name `cursor/factory-phase1-routing-b7c4` instead of `docs/2086_factory_routing`. No `sdlc-exception` label. No gate is skipped.

The GitHub integration cannot add labels (`addLabelsToLabelable` denied). #2086, #2087, and #2088 were created without labels. Dependencies were set: #2087 blocked by #2086, #2088 blocked by #2087.
