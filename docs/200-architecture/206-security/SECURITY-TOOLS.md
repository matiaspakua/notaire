# Security toolchain

Selection for CU78 / #307. The categories are the specialization guide §4
(`final_projects_specialization.md`). The bias is open source, already
operable in this repository, and able to fail a pull request without a new
vendor account.

## Selected

| Guide category | Concern | Tool | Where | Why this one |
|----------------|---------|------|-------|----------------|
| 4.1 Model | Threat model | PlantUML | `206-security/diagrams/` | Diagrams as code, already the project standard |
| 4.2 Build | CI | GitHub Actions | `.github/workflows/` | The forge is GitHub. GitLab CI from the guide is the same role, not a second server |
| 4.3 Test | Unit / API | JUnit, Vitest, Bruno, Playwright | existing suites | Bruno is the guide's Postman alternative and is already the API suite |
| 4.3 SAST | Java, JS, Actions | CodeQL | `codeql.yml` | Covers the three languages in one advanced setup. SpotBugs stays a quality report |
| 4.3 DAST | Running API | OWASP ZAP baseline | `dast-zap.yml` | The guide's DAST tool. Warn-first until #281 |
| 4.3 Secrets | High-confidence tokens | `security/scan_secrets.py` | CI security job | Same families as Gitleaks (private key, `ghp_`, `AKIA`, Slack). No extra action pin |
| 4.3 SCA | Dependencies and images | Trivy + Dependabot | `ci.yml`, `dependabot.yml` | Trivy is the OSS scanner already in CI. Dependabot opens the update |
| 4.4 Package | Image integrity | cosign + Trivy CycloneDX | `cd.yml` | SBOM and keyless signature on GHCR |
| 4.7 Monitor | Metrics, logs, traces, dashboard | Prometheus, Grafana, Loki, Tempo, OpenTelemetry | `infra/observability/` | The guide's monitoring pair, plus traces already landed |
| Policy | Severity and exceptions | `security/policy/security-policy.yml` | `security/verify.sh` | Policy as code for pipeline level 2 |

Severity SLA in the policy file: critical 7 days, high 30 days, medium 90,
low 180. The clock starts when the alert is visible (Dependabot, CodeQL, or a
failed secret scan). Trivy is still report-only, so its clock is not enforced
yet. That gap is `SR-13`.

## Considered and not adopted

| Tool | Guide mention | Decision |
|------|---------------|----------|
| Gitleaks | OSS secret scanner, closest rule source | Reference only. The Python scanner enforces a small high-confidence subset in every PR. Add the Gitleaks action when the team wants the full rule pack |
| FindSecBugs | §4.3 Java SAST | Candidate if CodeQL repeatedly misses a Java bug class. A second SAST on day one duplicates noise |
| OWASP Dependency-Check | §3.4.1 example | Trivy already fills SCA. A second database does not change the remediation path |
| Snyk | Example in the guide's risk table | Commercial account. Not required while Trivy and Dependabot run |
| sqlmap and similar exploit tools | §4.3 | Not a CI tool. An authorized pentest may use them inside a written scope. The repository does not store those procedures |
| IAST / RASP agents | §3.4.7–3.4.8 | No agent in the JVM or in production. Revisit only with an operations owner |
| Falco | §4.9 runtime containers | Right OSS choice for container syscall alerts. Needs a cluster decision; not part of this change |
| Elastic, New Relic, Nagios | §4.7 examples | Prometheus and Grafana already answer the same questions |

## Exceptions

An exception is a time-boxed waiver on the pull request, approved by the
repository owner. It names the control, the residual risk, and the expiry.
`sdlc-exception` is the human label for a process waiver. It is not a way to
skip the secret scan.

## Local commands

```bash
bash security/verify.sh
python3 security/scan_secrets.py
```

Optional, when Gitleaks is installed locally: run it on the same tree and
compare. CI does not require the binary.
