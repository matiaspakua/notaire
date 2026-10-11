# Traceability — ci-1506-security-report-service

| Field | Value |
|-------|-------|
| GitHub Issue | #1506 |
| Use Case | CU78 — Security and Compliance |
| Branch | `cursor/security-report-service-81d6` |

## Chain

| Step | Evidence |
|------|----------|
| Issue | https://github.com/matiaspakua/notaire/issues/1506 |
| Use Case | `docs/100-business/102-use-cases/CU78 – Security and Compliance.md` |
| Proposal | `proposal.md` |
| Spec | `specs/security-report-service/spec.md` |
| Design | `design.md` |
| Tests | `security/tests/test_security_gate_surface.py` |
| Implementation | `c1cac28a7960c6f18616a04d6b3219759e58dc6d` — `infra/observability/docker-compose.yml` service `security-reports`, `security/publish_summary.py` |
| CI | `.github/workflows/ci.yml` security job |
| Pull request | pending |
| Merge | pending |
| Release | pending |

## Requirement coverage

| Requirement | Spec scenario | Test |
|-------------|----------------|------|
| Gates stay enabled | CI security does not wait for the Java build | `test_ci_security_job_is_enabled_without_the_java_build` |
| Gates stay enabled | CodeQL and ZAP workflows are not disabled | `test_codeql_and_zap_workflows_are_enabled` |
| Gates stay enabled | Local pipeline runs catalog and DAST | `test_local_pipeline_runs_the_catalog_and_the_dast_baseline` |
| Report is an infra service | Homer and the probe name the report service | `test_homer_and_compose_publish_the_report_service` |
| CI shows the summary | Summary lists status and hides secrets | `test_summary_renders_case_status_without_secret_values` |

## Permanent documentation updated

| Document | Change |
|----------|--------|
| `docs/200-architecture/206-security/MONITORING.md` | Local URL and CI summary |
| `docs/200-architecture/206-security/CYBERSECURITY-PROGRAM.md` | Port 8890 |
| `infra/docs/DEFINITION.md` | Service row and probe count |
| `docs/100-business/102-use-cases/CU78 – Security and Compliance.md` | Pointer |
| `CHANGELOG.md` | Security bullet |

## Gate log

| Gate | Status | Evidence |
|------|--------|----------|
| Gate 1 | passed | OpenSpec artifacts for #1506 / CU78 |
| Gate 2 | passed | Red run: `needs: ['build']`, missing `publish_summary.py`, missing `security-reports` |
| Gate 3 | passed | Permanent docs listed above |
| Gate 4 | pending | Pull request |
| Gate 5 | pending | Smoke after merge |

## Exceptions

- Branch name is `cursor/security-report-service-81d6` because this cloud agent
  must use that prefix and the `-81d6` suffix.
- The `in-progress` label was not added. The integration cannot call
  `addLabelsToLabelable`.
