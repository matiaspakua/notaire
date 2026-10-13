# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1571 | open |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | updated |
| Specification | `docs/openspec/changes/chore-1571-product-release/` | Gate 1 |
| Branch | `cursor/release-product-schema-db96` | from `origin/main` |
| Tasks | `tasks.md` | groups 1–8 ticked; 9–12 pending PR/merge |
| Commits | `673be81d` | recorded |
| Pull Request | https://github.com/matiaspakua/notaire/pull/1572 | draft |
| CI run | | pending |
| Merge commit | | pending |
| Release / tag | | pending — do not merge the open release-please PR first |
| Smoke test | | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Versions on main match the manifest | `test_manifest_matches_maven_npm_and_latest_changelog`, `test_committed_record_matches_the_tree` | covered |
| Database schema is a Flyway id | `test_database_schema_is_recorded_and_not_the_semver` | covered |
| Pre-1.0 and 1.0.x share the production train | `test_pre_one_and_one_zero_advance_the_production_train` | covered |
| An unmapped minor fails | `test_unmapped_minor_fails_closed` | covered |
| Config and workflow accept both tag generations | `test_future_tags_are_v_and_legacy_tags_still_publish` | covered |
| Product bill composition | `test_product_sbom_lists_backend_frontend_and_database` | covered |
| Database bill lists migrations | `test_database_sbom_lists_every_migration` | covered |
| Snapshot heading is ignored | `test_snapshot_heading_is_not_a_product_release` | covered |
| Actuator section has three components | `shouldPublishThreeComponents`, `shouldContributeSchemaFromFlyway` | covered |
| OpenAPI uses the injected version | `shouldConfigureApiInfo` | covered |
| Sidebar shows the package version | `shows the product version from package.json` | covered |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/305-project-management/RELEASES.md` | yes | pending |
| `docs/300-development/SBOM.md` | yes | pending |
| `docs/300-development/RELEASE.md` | yes | pending |
| `CONSTITUTION.md` | yes | pending |
| `docs/github/README.md` | yes | pending |
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | yes | pending |
| `CHANGELOG.md` | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1571 + this change |
| 2 | Failing tests written, test cases designed | yes | unittest red on Maven drift before the pom edit |
| 3 | Suite green, docs updated | pending | commands in tasks group 6 and 8 |
| 4 | CI green, review approved | no | pending PR |
| 5 | Deployed, smoke test passed, Issue closed | no | pending |

## Related / Refs

- Release under review: release-please PR for 0.2.0 (tag would have been `notaire-v0.2.0`)
- Prior semver work: #1043
- Train schema: #1561, #1562, handbook `305-project-management/`
- Board columns stay Todo / In Progress / Done: #1550

## Planned Files

| Path | Role |
|------|------|
| `workspace/release/product_release.py` | record, train map, CycloneDX, Pages history |
| `release/product-release.json` | committed three-component record |
| `workspace/tests/test_product_release.py` | Gate 2 |
| `.github/workflows/cd.yml` | tag filters and SBOM assets |
| `release-please-config.json` | `include-component-in-tag: false` and extra-files |

## Exceptions

| Item | Reason | Owner |
|------|--------|-------|
| `in-progress` label on #1571 | May 403 for this token | human / next sync |
| Live milestone rename | Token cannot edit milestones (#1569) | human |
| Playwright not run | No new route or form; sidebar label is a Vitest | recorded in tasks 7.4 |
