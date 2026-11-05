# Security toolchain

Selection for CU78 / #307, reviewed 2026-11-05 (#2003). The categories are
the specialization guide §4 (`final_projects_specialization.md`). The bias is
open source, already operable in this repository, and able to fail
`bash workspace/sdlc/preflight.sh --full` or a version-tag workflow without a
new vendor account. GitHub product workflows do not run on a pull request
(#2001).

## Selected

| Guide category | Concern | Tool | Where | Why this one |
|----------------|---------|------|-------|----------------|
| 4.1 Model | Threat model | PlantUML | `206-security/diagrams/` | Diagrams as code, already the project standard |
| 4.2 Build | CI | GitHub Actions | `.github/workflows/` | The forge is GitHub. GitLab CI from the guide is the same role, not a second server |
| 4.3 Test | Unit / API | JUnit, Vitest, Bruno, Playwright | existing suites | Bruno is the guide's Postman alternative and is already the API suite |
| 4.3 SAST | Java, JS | Semgrep (local and release job) | `security/semgrep/notaire.yml` | High-signal rules in preflight and in `ci.yml` on a version tag. CodeQL stays the deeper SAST |
| 4.3 SAST | Java, JS, Actions | CodeQL | `codeql.yml` | Version tag, Monday 06:30 UTC, and manual dispatch. Uploads SARIF. Does not fail on findings (#2011). Not on pull_request |
| 4.3 DAST | Running API, anonymous | `security/dast_baseline.py` | `run_pipeline.sh` after the stack is up | Health plus a 401 on `/api/v1/people`, with no stack trace |
| 4.3 DAST | Running API | OWASP ZAP baseline | `dast-zap.yml` + `.zap/rules.tsv` | Monday 05:00 UTC and manual dispatch. Fails on non-allowlisted alerts (#1738). Not on the version tag (#2004) |
| 4.3 Secrets | High-confidence tokens | `security/scan_secrets.py` | local gate and `ci.yml` security job | Same families as Gitleaks (private key, `ghp_`, `AKIA`, Slack). No extra action pin |
| 4.3 SCA | Dependencies and images | Trivy + Dependabot | `ci.yml`, `dependabot.yml` | Filesystem scan fails on fixed CRITICAL/HIGH. Image scan fails on CRITICAL. Both on the release workflow. Dependabot opens the update |
| 4.4 Package | Image integrity | cosign + Trivy CycloneDX | `cd.yml` | SBOM and keyless signature on GHCR |
| 4.7 Monitor | Metrics, logs, traces, dashboard | Prometheus, Grafana, Loki, Tempo, OpenTelemetry | `infra/observability/` | The guide's monitoring pair, plus traces already landed |
| Policy | Severity and exceptions | `security/policy/security-policy.yml` | `security/verify.sh` | Policy as code for pipeline level 2 |

Severity SLA in the policy file: critical 7 days, high 30 days, medium 90,
low 180. The clock starts when the alert is visible (Dependabot, CodeQL, or a
failed secret scan, or a Trivy source scan). The Trivy filesystem scan blocks on
fixed CRITICAL/HIGH findings (#712). The Docker image scan blocks on CRITICAL
(#566). Checkstyle and SpotBugs use shrink-only ratchets in CI (`workspace/ci/quality-ratchet-baseline.json`);
absolute zero-debt is tracked in #710 and #711.

## Considered and not adopted

| Tool | Guide mention | Decision |
|------|---------------|----------|
| Gitleaks | OSS secret scanner, closest rule source | Reference only. The Python scanner enforces a small high-confidence subset in preflight and on the release job. Add the Gitleaks action when the team wants the full rule pack |
| FindSecBugs | §4.3 Java SAST | Candidate if CodeQL repeatedly misses a Java bug class. A second SAST on day one duplicates noise |
| OWASP Dependency-Check | §3.4.1 example | Trivy already fills SCA. A second database does not change the remediation path |
| OSV-Scanner | Lockfile advisory database | Same remediation path as Trivy filesystem. Not added |
| Checkov / tfsec | IaC policy | No Terraform or CloudFormation in this repository. Image pins are tested in Python |
| OWASP Threat Dragon | Diagram threat model | Evaluated for #2003. A checked-in YAML register plus PlantUML is what CI can test without the Dragon binary. Import is #2009 |
| pytm | Python DFD generator | Would duplicate `diagrams/trust-boundaries.puml`. Not added |
| Threagile | YAML risk model | A good schema, and a Go binary this pipeline does not pin. The register in `threat-model/system.stride.yml` is smaller and tested by unittest |
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
bash security/tools/ensure_local_tools.sh
python3 security/run_gates.py --profile local
bash workspace/sdlc/preflight.sh --full
bash workspace/sdlc/run_pipeline.sh
bash security/verify.sh
python3 security/scan_secrets.py
```

`preflight.sh` runs the local profile with `--skip-dast`. `run_pipeline.sh`
runs that profile and then the DAST baseline against the stack. The release
job runs `python3 security/run_gates.py --profile ci`. CodeQL does not run
locally.

Optional, when Gitleaks is installed locally: run it on the same tree and
compare. CI does not require the binary.
