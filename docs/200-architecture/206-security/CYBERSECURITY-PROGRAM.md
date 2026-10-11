# Cybersecurity program — Sec by Design

Notaire applies the specialization guide
[TPF: DevSecOps desde la perspectiva de QA Automation](https://github.com/matiaspakua/tech.notes.io/blob/main/pages/sw_eng_specialization/final_projects_specialization.md)
(`final_projects_specialization.md`) to this repository. The sections used as
guidelines are 2.5.13 (InfoSec), 2.5.16 (DevSecOps), 3.4 (Sec by Design and
OWASP SAMM), 3.5 (Ops by Design), 3.6 (pipeline levels), 3.9 (legal and
privacy), and 4 (toolchains). This page is the Notaire program. It does not
copy the thesis, and it does not authorize active testing outside a written
scope.

Use case: **CU78**. Policy as code: issue **#307**,
`security/policy/security-policy.yml`.

## 1. Current protection and exposure

Section 2.5.13 asks for two actions: know the exposure, then define
protections. Evidence below is from the tree on this change, not from a
production pentest.

| Area | Exposure today | Protection in the tree | Status |
|------|----------------|------------------------|--------|
| Identity | Login, JWT, cookie session | BCrypt cost 12, `LoginAttemptService`, JWT signature and expiry, HttpOnly cookie (#1051) | Partial — lockout is one JVM (#1028, SR-01) |
| Authorization | Every `/api/**` call except login/logout | `ROLE_ADMIN` on admin paths and admin writes (#559). `Escribano` is treated as admin | Partial — no object-level checks (SR-11) |
| Injection | JPA queries | Parameterized persistence. See [SQL injection prevention](SQL-INJECTION-PREVENTION.md) | Mitigated for known query paths |
| Secrets | Env and CI | `.env` gitignored, `ProductionCredentialsGuard`, this change's secret scan | Partial — GitHub push protection is an admin setting |
| Dependencies | Maven, npm, images | Dependabot, Trivy (report-only), image pins | Partial — Trivy does not fail the build |
| Privacy | `Persona` DNI and email; notarial records | Auth gate, request logs omit query, headers, and body (#1463) | Partial — reads of PII are not audited (SR-08) |
| Runtime | Login metrics | Prometheus alerts and Grafana `notaire-auth` | Partial — no general API rate limit (#280) |

Function-level RBAC is recorded in the
[auth threat model](THREAT-MODEL-AUTH-AUDIT.md) and in SAD §8.1. Object-level
authorization does not exist yet (SR-11).

## 2. SAMM snapshot

OWASP SAMM (guide §3.4, Fig. 30) is the maturity frame. Levels here are
qualitative: **0** absent, **1** ad hoc, **2** repeatable in git, **3**
measured and improved. They are not a certified score.

| SAMM practice | Notaire | Level |
|---------------|---------|-------|
| Strategy and metrics | CU78, this program, login-failure alerts | 1 |
| Policy and compliance | `security-policy.yml`, `SECURITY.md`, ruleset `protect-main` | 2 |
| Education | Security auditor agent and threat-modeling skill | 1 |
| Threat assessment | Auth/audit STRIDE model; system model added here | 2 |
| Security requirements | `SR-01`..`SR-16` with a verification column | 2 |
| Security architecture | Interceptor-validator in `SecurityAndCorsConfig`; trust-boundary diagram | 2 |
| Secure build | CodeQL, SpotBugs, secret scan failing in CI | 2 |
| Secure deployment | Pinned images, prod compose without public DB, cosign + SBOM on CD | 2 |
| Defect management | Dependabot and CodeQL alerts; Trivy and ZAP are report-only | 1 |
| Security testing | JUnit security tests, Playwright TS-0044, ZAP baseline, Bruno | 2 |
| Incident management | CU77, Alertmanager local UI, no external paging | 1 |
| Environment and operations | Grafana, Loki, Prometheus, OpenTelemetry | 2 |

## 3. Sec by Design

### 3.1 Threat assessment

STRIDE covers security properties. LINDDUN covers privacy of `Persona` and
notarial records. The system model is
[THREAT-MODEL-SYSTEM.md](THREAT-MODEL-SYSTEM.md). Authentication and the audit
trail stay in [THREAT-MODEL-AUTH-AUDIT.md](THREAT-MODEL-AUTH-AUDIT.md). The
diagram is [diagrams/trust-boundaries.puml](diagrams/trust-boundaries.puml).

Attack classes named in the guide §2.5.13 (credential abuse, IDOR, injection,
XSS, cookie theft, session hijacking, upload abuse, denial of service) are
mapped there to existing controls. Exploitation steps are not part of this
knowledge base.

### 3.2 Security requirements

`SR-*` identifiers are stable. `SR-01`..`SR-10` belong to the auth and audit
model. `SR-11` onward belong to the system model. Each row has an asset, a
mitigation, a verification method, an owner, and a residual risk. A mitigation
is incomplete until the test or control file is named.

### 3.3 Security architecture

Architectural analysis for security (guide §3.4.3) is applied at three depths:

| Technique | Notaire application |
|-----------|---------------------|
| ToAA | CU78 and the SAD security section, reviewed when a change touches auth, PII, secrets, or dependencies |
| PoAA | Interceptor-validator: `JwtAuthenticationFilter` then `UserAuthorityResolver` then the controller. Browser traffic enters through the Next.js BFF |
| VoAA | CodeQL, SpotBugs, the secret scan, and Trivy on the sources and images |

Defense in depth is the working principle: validation is not authorization,
CSP is not a complete XSS control, and TLS does not replace object-level
checks. Obscurity is not a control. Hiding Swagger in production (#671) only
reduces casual reconnaissance.

## 4. Security testing (QA automation)

Guide §3.4.4..§3.4.10 and §2.5.14. The pipeline automates the checks that can
fail the same way twice. Interactive and runtime agent testing stay out until
a written decision adds them.

| Family | Selected practice | Evidence | Not selected, and why |
|--------|-------------------|----------|------------------------|
| SAST | CodeQL advanced workflow; SpotBugs report | `.github/workflows/codeql.yml`, CI quality job | FindSecBugs is a candidate Java complement, not a second mandatory scanner |
| DAST | OWASP ZAP baseline, weekly, warn-first | `.github/workflows/dast-zap.yml` | Exploitation scanners are not CI jobs |
| IAST | None | — | Needs an agent in the test JVM; no current signal that CodeQL plus ZAP miss a class of defect we can name |
| RASP | None | — | Runtime blocking agents are a later production control, not a test |
| SCA | Trivy fs and image; Dependabot | `ci.yml`, `.github/dependabot.yml` | Commercial SCA was not added beside Trivy |
| Secrets | `security/scan_secrets.py` fails CI | `security/policy/security-policy.yml` | Gitleaks is the reference OSS rule set; the Python gate has no extra action to pin |
| Pentest | Rules of engagement only | [SECURITY-TOOLS.md](SECURITY-TOOLS.md) | No standing authorization to attack a shared environment |

Pentest phases, when an owner later authorizes one, follow the six CIPHER
steps in the guide §3.4.10: pre-engagement, reconnaissance, threat mapping,
exploitation inside the written scope, risk write-up, report. The pre-engagement
record must name the environment, the time window, the rate limit, the data
rules, the stop condition, and who receives the report. This repository does
not store exploit procedures.

## 5. DevSecOps pipeline

Guide §3.6. Feedback is a failed check, a Grafana panel, or an issue, not a
slide.

### Nivel 0 — ecosystem

Plan and develop (OpenSpec, TDD) → build (Maven, Next.js) → test (JUnit,
Vitest, Bruno, Playwright) → security scan (secret scan, CodeQL, Trivy, ZAP) →
release (release-please, GHCR, cosign, CycloneDX) → operate (Prometheus,
Grafana, Loki, Tempo, Alertmanager). QA evidence is the test file named on
each `SR-*` row.

### Nivel 1 — where a defect enters

| Stage | Failure mode from the guide | Notaire control |
|-------|-----------------------------|-----------------|
| Developer tree | Secret or vulnerable dependency | Secret scan in `security/verify.sh`; Dependabot |
| Pull request | Broken policy or missing threat update | SDLC process checks plus this program |
| Build | Vulnerable library or base image | Trivy report; image pin tests |
| Registry | Unsigned image | cosign on CD |
| Runtime | Login abuse, unavailable API | Prometheus alerts, Grafana |

### Nivel 2 — what runs where

| Phase | Practice | Now | Next, tracked |
|-------|----------|-----|----------------|
| Build | Policy as code | `security-policy.yml` enforced by tests and CI | FindSecBugs only if CodeQL leaves a repeated Java gap |
| Test | ZAP baseline | Weekly artifact, does not fail the PR | Fail on agreed high findings (#281) |
| Production | Runtime scan, CIS, compliance packs | Not claimed | Falco is the OSS candidate from the guide §4.9; legal mapping stays #309 |
| Production | Access control | Function-level RBAC | Object-level checks (SR-11) |
| Production | Alerts and review | Login alerts, local Alertmanager | External paging is an owner decision (#1465) |

## 6. Operations, monitoring, and the dashboard

Guide §3.5 and §4.7. Logs are event streams (twelve-factor), metrics are
Prometheus, traces are OpenTelemetry/Tempo, and the operator view is Grafana.
The security dashboard and the alert catalogue are specified in
[MONITORING.md](MONITORING.md). Abnormal situations use CU77. A post-incident
note records start, end, who was affected, what failed, and what will change.
That template is the guide §3.7; it is not a new product feature.

## 7. Privacy, licenses, and the law

Guide §3.9. Notarial records are retained indefinitely (SAD §2.3). `Persona`
holds regulated identifiers. LINDDUN rows in the system threat model are the
engineering view. Mapping those rows to a specific statute, and choosing
product licenses with counsel, remains issue **#309**. This page does not
give legal advice.

Third-party code is tracked by Dependabot and the SBOM attached to releases.
The scanner ignores the public AWS documentation key id
`AKIAIOSFODNN7EXAMPLE` and nothing else by default.

## 8. AI SDLC gate

Cloud agents follow the same Sec by Design loop:

1. Confirm CU78 or the use case of the change, plus an open issue.
2. If the diff touches authentication, authorization, personal data, secrets,
   dependencies, or a public endpoint, update the threat model and the `SR-*`
   row in the same change.
3. Run `bash security/verify.sh` before push.
4. Do not weaken Trivy, CodeQL, the secret scan, or the image-pin tests to
   make a pipeline green.
5. Leave active testing inside the rules of engagement. Do not add exploit
   procedures to docs or to CI.

The fleet note is in
[FLEET-ARCHITECTURE.md](../../300-development/304-ai-sdlc-cloud/FLEET-ARCHITECTURE.md).

## 9. Toolchain and open work

Tool choice, including what was rejected, is
[SECURITY-TOOLS.md](SECURITY-TOOLS.md). OWASP Top 10 mapping is
[OWASP-TOP10.md](OWASP-TOP10.md) and stays open for a human review (#267).

| Issue | Remaining work |
|-------|----------------|
| #254 | Production TLS material and the operator guide |
| #267 | Human sign-off of the Top 10 checklist |
| #280 | General API rate limiting (SR-05, SR-15) |
| #281 | ZAP from warn-first to a failing gate |
| #282 | Remediation SLA tracked on a live vulnerability board |
| #283 | Rotation runbook and a vault, if the owner chooses one |
| #309 | Privacy statute mapping and audit retention reporting |
| #1028 follow-ups | Shared lockout store, read audit, tamper-evident audit log |

Review cadence is quarterly, owned by the security officer named in the policy
file.
