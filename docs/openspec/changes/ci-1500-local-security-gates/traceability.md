# Traceability — ci-1500-local-security-gates

| Field | Value |
|-------|-------|
| GitHub Issue | #1500 |
| Use Case | CU78 — Security and Compliance |
| Branch | `cursor/local-security-sdlc-81d6` |

## Chain

| Step | Evidence |
|------|----------|
| Issue | https://github.com/matiaspakua/notaire/issues/1500 |
| Use Case | `docs/100-business/102-use-cases/CU78 – Security and Compliance.md` |
| Proposal | `proposal.md` |
| Spec | `specs/local-security-gates/spec.md` |
| Design | `design.md` |
| Tests | `security/tests/test_security_cases.py` |
| Implementation | `62126cf25746614be6805a4f04fa39d08717c8ac` — `security/run_gates.py`, `security/cases/catalog.yml`, `workspace/sdlc/run_pipeline.sh` |
| CI | `.github/workflows/ci.yml` security job, artifact `security-case-report` |
| Pull request | pending |
| Merge | pending |
| Release | pending |

## Requirement coverage

| Requirement | Spec scenario | Test |
|-------------|----------------|------|
| Cases are specified and reported | Catalog names the control families | `CatalogSchemaTest` |
| Cases are specified and reported | The runner writes a report | `test_report_lists_every_case` |
| Local tools fail closed | Planted secret fails and the tree passes | `test_secret_case_fails_on_a_planted_token_and_passes_on_the_repo` |
| Local tools fail closed | DAST baseline distinguishes a leak from a 401 | `test_dast_baseline_accepts_401_and_rejects_a_stack_trace` |
| AI SDLC runs the catalog | Process documents point at the gate | `test_ai_sdlc_names_the_local_gate` |
| SAST fixture and clean tree | Design decision 2 | `test_sast_requires_a_fixture_finding_and_a_clean_tree`, `RealSemgrepTest` |
| Trivy must be present locally | Design decision 4 | `test_local_sca_fails_when_trivy_is_missing_and_passes_when_it_runs` |

## Permanent documentation updated

| Document | Change |
|----------|--------|
| `docs/200-architecture/206-security/CYBERSECURITY-PROGRAM.md` | Local gate section |
| `docs/200-architecture/206-security/SECURITY-TOOLS.md` | Semgrep and DAST baseline rows |
| `docs/200-architecture/206-security/MONITORING.md` | Local report and CI artifact |
| `docs/100-business/102-use-cases/CU78 – Security and Compliance.md` | Catalog pointer |
| `docs/300-development/304-ai-sdlc-cloud/FLEET-ARCHITECTURE.md` | Item 10 |
| `CONSTITUTION.md`, `AGENTS.md`, `CONSTITUTION-AGENT-CARD.md` | Pre-PR security phase |
| `.claude/agents/openspec.md`, `.claude/rules/ai-agent-workflow.md` | Security is part of every change |
| `CHANGELOG.md` | Security bullet |

## Gate log

| Gate | Status | Evidence |
|------|--------|----------|
| Gate 1 | passed | OpenSpec artifacts for #1500 / CU78 |
| Gate 2 | passed | Red run of `test_security_cases.py` before `run_gates.py` existed (FileNotFoundError and missing pipeline string) |
| Gate 3 | passed | Permanent docs listed above |
| Gate 4 | pending | Pull request and CI |
| Gate 5 | pending | Smoke after merge |

## Exceptions

- Branch name is `cursor/local-security-sdlc-81d6` because this cloud agent must
  use that prefix and the `-81d6` suffix. The constitution pattern
  `<type>/<issue>_<description>` is the repository default for local agents.
- The `in-progress` label was not added. `gh issue edit` returned
  `Resource not accessible by integration` for `addLabelsToLabelable`.
