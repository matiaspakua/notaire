# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2398 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/feat-2398-config-as-code/` | complete |
| Branch | `feat/2398_config_as_code` | created |
| Tasks | `tasks.md` | 9/12 complete (10.1 archive done; 10.2–12 pending post-PR) |
| Commits | see below | 9 commits |
| Pull Request | #<!-- number --> | pending |
| CI run | <!-- workflow run URL --> | pending |
| Merge commit | <!-- SHA --> | pending |
| Release / tag | <!-- vX.Y.Z --> | pending |
| Smoke test | <!-- environment + result --> | pending |

**Commits (oldest first):**

| SHA (short) | Message |
|-------------|---------|
| `7e425d53` | docs(openspec): feat-2398-config-as-code plan |
| `f51ab233` | test(aisdlc): red tests for config as code and catalog delete |
| `588f329a` | feat(aisdlc): config files tree and read with redaction |
| `ebd73754` | feat(aisdlc): catalog delete with If-Match, CSRF and evidence |
| `e07818df` | refactor(aisdlc): revert formatter churn in serve and cli |
| `8551fe1c` | fix(aisdlc): confine config files to profile roots, key-aware redaction, single catalog delete |
| `88c48bbe` | refactor(aisdlc): one config root enumeration for listing and read confinement |
| `c6f32f89` | docs(aisdlc): OpenAPI 1.3.0, README and CHANGELOG for config as code (#2398) |
| `dfeeb51e` | refactor(aisdlc): single config entry enumeration |

## Requirement coverage

<!-- Every `#### Scenario:` in the delta spec is an Acceptance Criterion and must
     appear here with the test that proves it. Requirement coverage is not code
     coverage: a green suite with an unmapped scenario means the criterion is
     unverified. -->

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| tree listing returns config roots | `ConfigFilesTreeTest.test_tree_listing_returns_config_roots` | passing |
| tree listing requires read scope | `ConfigFilesTreeTest.test_tree_listing_requires_read_scope` | passing |
| file read returns redacted content | `ConfigFileReadTest.test_file_read_returns_redacted_content` | passing |
| file read with no secrets | `ConfigFileReadTest.test_file_read_no_secrets` | passing |
| file read requires read scope | `ConfigFileReadTest.test_file_read_requires_read_scope` | passing |
| traversal with .. is refused | `ConfigFilePathTraversalTest.test_traversal_with_dotdot_refused` | passing |
| absolute path is refused | `ConfigFilePathTraversalTest.test_absolute_path_refused` | passing |
| encoded separator %2f is refused | `ConfigFilePathTraversalTest.test_encoded_separator_percent2f_refused` | passing |
| encoded separator %5c is refused | `ConfigFilePathTraversalTest.test_encoded_separator_percent5c_refused` | passing |
| symlink escape is refused | `ConfigFilePathTraversalTest.test_symlink_escape_refused` | passing |
| delete with matching If-Match succeeds | `CatalogDeleteHttpTest.test_delete_with_matching_if_match_succeeds` | passing |
| delete with stale If-Match returns 409 | `CatalogDeleteHttpTest.test_delete_with_stale_if_match_returns_409` | passing |
| delete without If-Match returns 428 | `CatalogDeleteHttpTest.test_delete_without_if_match_returns_428` | passing |
| delete requires write scope | `CatalogDeleteHttpTest.test_delete_requires_write_scope` | passing |
| delete requires CSRF | `CatalogDeleteHttpTest.test_delete_requires_csrf` | passing |
| delete unknown entry returns 404 | `CatalogDeleteHttpTest.test_delete_unknown_entry_returns_404` | passing |
| delete appends evidence record | `CatalogDeleteHttpTest.test_delete_appends_evidence_record` | passing |
| GET config/files routes to config_files | `ConfigFilesTreeTest.test_tree_listing_returns_config_roots` | passing |
| GET config/files/{path} routes to config_files | `ConfigFileReadTest.test_file_read_returns_redacted_content` | passing |
| DELETE catalog entry routes to catalogs.delete | `CatalogDeleteHttpTest.test_delete_with_matching_if_match_succeeds` | passing |
| CLI config files lists tree | `CliConfigFilesTest.test_cli_config_files_lists_tree` | passing |
| CLI config show displays file | `CliConfigFilesTest.test_cli_config_show_displays_file` | passing |
| CLI catalog remove with --if-match | `CliConfigFilesTest.test_cli_catalog_remove_with_if_match` | passing |
| OpenAPI declares config file paths | `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | done |
| OpenAPI declares catalog delete path | `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | done |
| no second OpenAPI tree | `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | done |
| projects root files are listed | `ConfigFileConfinementTest.test_projects_root_files_are_listed` | passing |
| skill rows are read_only | `ConfigFileConfinementTest.test_skill_rows_are_read_only` | passing |
| JSON file with secret key is redacted | `ConfigFileConfinementTest.test_json_file_with_secret_key_is_redacted` | passing |
| env:NAME reference stays visible | `ConfigFileConfinementTest.test_env_reference_stays_visible` | passing |
| symlink outside not listed | `ConfigFileConfinementTest.test_symlink_outside_not_listed` | passing |
| delete without --write returns 405 | `CatalogDeleteOverHttpReadOnlyTest.test_delete_without_write_flag_returns_405` | passing |
| CLI config show redacts secrets | `CliConfigFilesTest.test_cli_config_show_redacts_secrets_in_policy` | passing |
| CLI config show refuses file outside roots | `CliConfigFilesTest.test_cli_config_show_refuses_file_outside_roots` | passing |
| CLI catalog remove stale if-match nonzero | `CliConfigFilesTest.test_cli_catalog_remove_stale_if_match_nonzero` | passing |
| repo file outside config roots refused | `ConfigFilePathTraversalTest.test_repo_file_outside_config_roots_refused` | passing |
| file read media type by extension | `ConfigFileReadTest.test_file_read_media_type_by_extension` | passing |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/README.md` | yes | `c6f32f89` |
| `framework/ai-sdlc/CHANGELOG.md` | yes | `c6f32f89` |
| `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | yes | `c6f32f89` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `7e425d53` — OpenSpec plan committed |
| 2 | Failing tests written, test cases designed | yes | `f51ab233` — red tests in `test_config_files.py` |
| 3 | Suite green, coverage held, docs updated | partial | Implementation + docs done; `preflight.sh --full` pending |
| 4 | CI green, review approved, no conflicts | pending | PR not yet opened |
| 5 | Deployed, smoke test passed, Issue closed | pending | Post-merge |

## Exceptions

- Gate 3 item 8.6 (`preflight.sh --fix`) not yet run locally — will be run before PR.
- Groups 6.2–6.4 (Maven/JaCoCo/Checkstyle) are n/a for this Python framework module.
- Group 7 (Playwright) is n/a — no UI surface (design.md §Playwright Strategy).
