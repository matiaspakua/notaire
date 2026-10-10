# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #307 | open |
| Use Case | CU78 — Seguridad, Privacidad y Cumplimiento | exists |
| Specification | `docs/openspec/changes/docs-307-security-policy-as-code/` | drafted |
| Branch | `cursor/security-policy-307-81d6` | created |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | `python3 security/scan_secrets.py` on the branch | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Policy declares the control set | `test_policy_declares_tools_sla_secrets_exceptions_and_review` | passing |
| Program follows Sec by Design | `test_program_documents_exist_and_are_indexed` | passing |
| Planted key and token are findings | `test_scanner_flags_private_key_and_github_token` | passing |
| Placeholders are not findings | `test_scanner_accepts_placeholders` | passing |
| The tree is clean and CI enforces it | `test_repository_tree_has_no_high_confidence_secret`, `test_ci_and_module_verify_enforce_the_scan` | passing |
| Dashboard shows abuse signals | `test_dashboard_shows_login_failures_and_firing_alerts` | passing |
| Lockout alert is actionable | `test_lockout_alert_is_actionable` | passing |
| System model names the open object-level gap and the secret control | `test_system_threat_model_records_stride_linddun_and_new_requirements` | passing |
| Authorization docs match the code | same test (auth model + SAD) | passing |

Guideline: [final_projects_specialization.md](https://github.com/matiaspakua/tech.notes.io/blob/main/pages/sw_eng_specialization/final_projects_specialization.md) §2.5.13, §2.5.16, §3.4, §3.5, §3.6, §3.9, §4.

## Permanent documentation updated

| Document | Change |
|----------|--------|
| `docs/200-architecture/206-security/CYBERSECURITY-PROGRAM.md` | Sec by Design program |
| `docs/200-architecture/206-security/THREAT-MODEL-SYSTEM.md` | STRIDE + LINDDUN, SR-11..SR-16 |
| `docs/200-architecture/206-security/SECURITY-TOOLS.md` | OSS toolchain and rejections |
| `docs/200-architecture/206-security/MONITORING.md` | Dashboard and alerts |
| `docs/200-architecture/206-security/OWASP-TOP10.md` | Top 10 map, #267 stays open |
| `docs/200-architecture/206-security/README.md` | Index and corrected access-control row |
| `docs/200-architecture/206-security/THREAT-MODEL-AUTH-AUDIT.md` | SR-06 Partial |
| `docs/200-architecture/201-SAD/sad.md` | Authorization matches the code |
| `docs/100-business/102-use-cases/CU78 – Security and Compliance.md` | Program pointer |
| `docs/300-development/304-ai-sdlc-cloud/FLEET-ARCHITECTURE.md` | Security gate |
| `CHANGELOG.md` | Unreleased security note |

## Gate log

| Gate | Evidence | Status |
|------|----------|--------|
| Gate 1 | `bash workspace/sdlc/validate-sdlc-plan.sh docs-307-security-policy-as-code` | pending |
| Gate 2 | `python3 security/tests/test_security_policy.py` red (7 errors, 2 failures), then green | passing |
| Gate 3 | Permanent docs listed above | passing |
| Gate 4 | PR CI | pending |
| Gate 5 | Merge smoke | pending |

## Exceptions

The cloud agent runtime requires the branch name `cursor/security-policy-307-81d6`.
That differs from the constitution pattern `<type>/<issue-number>_<description>`.
The issue and use case are still #307 and CU78. Issue label `in-progress` was
not applied because this agent cannot edit GitHub issues.
