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
| Issue | #2217 | open (in-progress label waived — #2221) |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/improve-2217-cloud-env-preflight-deps/` | draft |
| Branch | `improve/2217_cloud-env-preflight-deps` | created |
| Tasks | `tasks.md` | Gate 1–2 in progress |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | local `preflight.sh --full` | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| install.sh installs python3-venv | `workspace/tests/test_cloud_env_preflight_deps.py::test_install_sh_installs_python3_venv` | pending |
| install.sh installs e2e chromium and webkit | `workspace/tests/test_cloud_env_preflight_deps.py::test_install_sh_installs_e2e_chromium_and_webkit` | pending |
| install.sh installs github-page chromium | `workspace/tests/test_cloud_env_preflight_deps.py::test_install_sh_installs_github_page_chromium` | pending |
| checklist documents python3-venv and Playwright browsers | `workspace/tests/test_cloud_env_preflight_deps.py::test_checklist_documents_venv_and_playwright_browsers` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/304-ai-sdlc-cloud/ENVIRONMENT-CHECKLIST.md` | pending | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | validate-sdlc-plan.sh |
| 2 | Failing tests written, test cases designed | pending | unittest red-then-green |
| 3 | Suite green, coverage held, docs updated | pending | preflight --full |
| 4 | CI green, review approved, no conflicts | pending | PR + local preflight |
| 5 | Deployed, smoke test passed, Issue closed | pending | after merge |

## Exceptions

- Issue label `in-progress` could not be added (`gh issue edit` — label not found /
  GraphQL Resource not accessible). Tracked separately as #2221; work continued.
