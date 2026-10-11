## Context

CU78 already has JWT sessions, function-level RBAC, Dependabot, CodeQL, Trivy,
ZAP, and a login dashboard. The specialization guide asks for Sec by Design
(threat assessment, security requirements, security architecture) and a
toolchain a QA loop can re-run. Issue #307 asks for that policy to be
enforced, not only described. Several docs still say role checks do not exist.

## Goals / Non-Goals

**Goals:**

- One program page that maps the guide onto Notaire without copying it.
- A system threat model that continues the existing `SR-*` register.
- A secret scan that fails CI and is covered by a unit test.
- A Grafana view and a lockout alert for signals the API already emits.
- An AI SDLC step that points agents at `bash security/verify.sh`.

**Non-Goals:**

- Rate limiting, a vault, production certificates, Falco, or a second SAST.
- Changing the `Escribano` → `ROLE_ADMIN` mapping.
- Exploit procedures or an unauthorized pentest.

## Decisions

1. **In-repo scanner instead of a Gitleaks action.** The guide's rule families
   are the reference. A Python scan has no new GitHub action to pin and runs
   inside `security/verify.sh` and the SDLC process job. Gitleaks remains the
   optional local deep scan.
2. **Do not add Snyk, sqlmap, IAST, or RASP.** Trivy and ZAP already occupy
   SCA and DAST. Exploitation tools stay out of CI. The design records that
   rejection in `SECURITY-TOOLS.md`.
3. **Correct SR-06 instead of opening a duplicate.** Function-level RBAC
   shipped in #559. The remaining gap is object-level access, numbered SR-11.
4. **Dashboard uses existing Micrometer series.** No new application metric.
   `LoginLockoutSpike` watches `status="locked"`.
5. **PyYAML is installed in the CI step** (`pyyaml==6.0.1`) because the
   security job does not otherwise depend on it. Application artifacts do not
   gain the dependency.

## Riesgos / Trade-offs

- [High-confidence patterns miss a secret that does not look like a key, a
  GitHub PAT, an AWS access key id, or a Slack token] → Residual risk is
  stated on SR-14. Gitleaks can be added later without changing the policy id.
- [Trivy stays report-only] → SR-13 stays Partial. This change does not pretend
  the SLA clock is enforced for Trivy.
- [Escribano is an administrator] → Documented as SR-16. Silent code change
  would surprise notaries.
- [Quoting the guide could be read as permission to store exploit steps] →
  The program names phases and forbids those procedures in-repo.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Policy declares the control set | unit | `security/tests/test_security_policy.py::PolicyDocumentTest::test_policy_declares_tools_sla_secrets_exceptions_and_review` |
| Program follows Sec by Design | unit | `test_program_documents_exist_and_are_indexed` |
| Planted key and token are findings | unit | `test_scanner_flags_private_key_and_github_token` |
| Placeholders are not findings | unit | `test_scanner_accepts_placeholders` |
| The tree is clean and CI enforces it | unit | `test_repository_tree_has_no_high_confidence_secret`, `test_ci_and_module_verify_enforce_the_scan` |
| Dashboard shows abuse signals | unit | `test_dashboard_shows_login_failures_and_firing_alerts` |
| Lockout alert is actionable | unit | `test_lockout_alert_is_actionable` |
| System model names the open object-level gap and the secret control | unit | `test_system_threat_model_records_stride_linddun_and_new_requirements` |
| Authorization docs match the code | unit | same test, auth model and SAD assertions |

- New unit tests: `security/tests/test_security_policy.py` (red, then green).
- New integration tests: none.
- Coverage impact: no Java change. JaCoCo ratchet is untouched.

TDD: `python3 security/tests/test_security_policy.py` failed first (missing
policy, scanner, docs, dashboard, and alert), then passed after the
implementation.

## Regression Strategy

- Existing tests affected: Grafana status tests still require the specialist
  dashboards. The new dashboard is an extra link on `notaire-status`, which
  the subset assertion allows. Alertmanager tests require every alert to have
  severity, summary, and description. `LoginLockoutSpike` has all three.
- Full suite command: `bash security/verify.sh` and
  `python3 infra/tests/test_grafana_status_dashboards.py` and
  `python3 infra/tests/test_alertmanager.py`.
- HTTP/Bruno API suite: n/a — no API change.
- Legacy paths at risk: none.

## Playwright Strategy

n/a — no UI surface. The Grafana JSON is provisioned configuration, not a
Next.js screen. `github-page` copy is a documentation card, not a product flow.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: docs and the scanner ship with the PR. The
  dashboard and alert load the next time the observability compose stack
  starts. No application restart is required for the scan.
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): `python3 security/scan_secrets.py` exits 0
  on the merged tree. Operators confirm `notaire-security` appears in Grafana
  after the stack starts.

## Rollback Strategy

- Revert safe: yes. The scan, dashboard, and docs revert together. No schema
  and no persisted security data were added.
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: a false-positive secret finding would
  block CI until the allowlist or the pattern is fixed. The pattern set is
  intentionally narrow.

## Migration Plan

No staged rollout. The scanner starts failing CI on the merge of this change.

## Open Questions

None that change the spec. Whether `Escribano` should remain `ROLE_ADMIN` is
a product decision tracked as SR-16, not an unstated assumption of this change.
