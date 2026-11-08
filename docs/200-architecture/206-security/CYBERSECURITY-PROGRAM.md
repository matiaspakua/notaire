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
`security/policy/security-policy.yml`. The evidence review dated 2026-11-05
is [ASSESSMENT-2026-11-05.md](ASSESSMENT-2026-11-05.md) (#2003). Agent
procedure, kept apart from this product program, is
[SECURITY-GATES.md](../../300-development/304-ai-sdlc-cloud/SECURITY-GATES.md).

## 1. Current protection and exposure

Section 2.5.13 asks for two actions: know the exposure, then define
protections. Evidence below is from the tree on this change, not from a
production pentest.

| Area | Exposure today | Protection in the tree | Status |
|------|----------------|------------------------|--------|
| Identity | Login, JWT, cookie session | BCrypt cost 12, `LoginAttemptService`, JWT signature and expiry, HttpOnly cookie (#1051) | Partial — lockout is shared through the database (SR-01, #1634); no MFA |
| Authorization | Every `/api/**` call except login/logout | `ROLE_ADMIN` on admin paths and admin writes (#559). `Escribano` resolves to `ROLE_NOTARY`, not admin (#1638). `RoleModuleAuthorizationFilter` enforces module grants (#1683) | Partial — one installation per notary; inside the office, object sharing is accepted (SR-11) |
| Injection | JPA queries | Parameterized persistence. See [SQL injection prevention](SQL-INJECTION-PREVENTION.md) | Mitigated for known query paths |
| Secrets | Env and CI | `.env` gitignored, `ProductionCredentialsGuard`, this change's secret scan | Partial — GitHub push protection is an admin setting |
| Dependencies | Maven, npm, images | Dependabot, Trivy filesystem (fixed CRITICAL/HIGH) and Trivy image (CRITICAL), image pins | Partial — both scans fail the release job (`exit-code: '1'` in `ci.yml`). They do not run on a pull request (#2001) |
| Privacy | `Person` DNI and email; notarial records | Auth gate, request logs omit query, headers, and body (#1463) | Partial — `Person` reads are audited without PII (SR-08, #1635); other entities opt in |
| Runtime | Login metrics | Prometheus alerts and Grafana `notaire-auth` | Partial — no general API rate limit (#2010). Issue #280 is closed and was a guide |

Function-level RBAC and module grants are recorded in the
[auth threat model](THREAT-MODEL-AUTH-AUDIT.md), in SAD §8.1, and in
`RoleModuleAuthorizationFilter` (#1683). Object-level authorization is not
built by design: one installation serves one notary and the office shares its
files (SR-11, ADR-029).

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
| Secure build | Secret scan, Semgrep and Trivy in local preflight; the same checks plus CodeQL on a version tag. CodeQL uploads SARIF and does not fail on findings (#2011) | 2 |
| Secure deployment | Pinned images, prod compose without public DB, cosign + SBOM on CD | 2 |
| Defect management | Dependabot and CodeQL alerts; Trivy fails the release job on fixed CRITICAL/HIGH (filesystem) and CRITICAL (image); ZAP weekly fails on non-allowlisted alerts (#1738, closed). No in-repo SLA board | 1 |
| Security testing | JUnit security tests, Playwright TS-0044, ZAP baseline, Bruno | 2 |
| Incident management | CU77, Alertmanager local UI, no external paging | 1 |
| Environment and operations | Grafana, Loki, Prometheus, OpenTelemetry | 2 |

## 3. Sec by Design

### 3.1 Threat assessment

STRIDE covers security properties. LINDDUN covers privacy of `Person` and
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
| PoAA | Interceptor-validator: `JwtAuthenticationFilter`, then `UserAuthorityResolver`, then `RoleModuleAuthorizationFilter`, then the controller. Browser traffic enters through the Next.js BFF |
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
| DAST | OWASP ZAP baseline, weekly, fails on non-allowlisted alerts (`fail_action: true`, `.zap/rules.tsv`) | `.github/workflows/dast-zap.yml` | Exploitation scanners are not CI jobs |
| IAST | None | — | Needs an agent in the test JVM; no current signal that CodeQL plus ZAP miss a class of defect we can name |
| RASP | None | — | Runtime blocking agents are a later production control, not a test |
| SCA | Trivy fs and image; Dependabot | `ci.yml`, `.github/dependabot.yml` | Commercial SCA was not added beside Trivy |
| Secrets | `security/scan_secrets.py` fails CI and the local gate | `security/policy/security-policy.yml` | Gitleaks is the reference OSS rule set; the Python gate has no extra action to pin |
| SAST, local | Semgrep rules in `security/semgrep/notaire.yml` | `python3 security/run_gates.py` | CodeQL stays the deeper CI SAST |
| DAST, local | Unauthenticated baseline | `security/dast_baseline.py` | ZAP stays the weekly CI DAST |
| Pentest | Rules of engagement only | [SECURITY-TOOLS.md](SECURITY-TOOLS.md) | No standing authorization to attack a shared environment |

### Local gate before the pull request

The security test schema is `security/cases/catalog.yml`. Each case names a
local tool and the CI control. `python3 security/run_gates.py --profile local`
runs the threat-model, secret, Semgrep, and Trivy cases and writes
`reports/security/latest/index.html`. `bash workspace/sdlc/run_pipeline.sh`
runs that command before the rest of the pipeline, then runs the DAST baseline
against the API. The HTML report is also served by the infra service
`security-reports` at `http://localhost:8890` (#1506). Install the pinned tools with
`bash security/tools/ensure_local_tools.sh` (the local profile does this
itself). Trivy in that local profile and in the release `security` job fails
on fixed CRITICAL/HIGH. The image scan in `docker-build` fails on CRITICAL.
Both use `exit-code: '1'` and `ignore-unfixed: true`. The catalog flag
`fail_on_findings` records the same choice. The baseline checks health and an
anonymous 401. It does not exploit the API. GitHub does not run the security
job on a pull request (#2001): the local command is the pull-request proof.

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
| Pull request | Broken policy or a skipped local gate | `bash workspace/sdlc/preflight.sh --full`, which runs `security/run_gates.py`. Product workflows do not run on pull_request (#2001) |
| Build | Vulnerable library or base image | Trivy on the version tag (`ci.yml`); image pin tests locally |
| Registry | Unsigned image | cosign on CD |
| Runtime | Login abuse, unavailable API | Prometheus alerts, Grafana |

### Nivel 2 — what runs where

| Phase | Practice | Now | Next, tracked |
|-------|----------|-----|----------------|
| Build | Policy as code | `security-policy.yml` enforced by tests and CI | FindSecBugs only if CodeQL leaves a repeated Java gap |
| Test | ZAP baseline | Weekly workflow fails on non-allowlisted alerts (#1738). It does not run on the version tag (#2004) | Authenticated ZAP stays a later decision |
| Production | Runtime scan, CIS, compliance packs | Not claimed | Falco is the OSS candidate from the guide §4.9. Statute mapping is counsel, not this table |
| Production | Access control | Function-level RBAC and module grants (#1683) | Object-level sharing inside one office stays accepted (SR-11). General rate limit is #2010 |
| Production | Alerts and review | Login alerts, local Alertmanager | External paging is an owner decision (#1465) |

## 6. Operations, monitoring, and the dashboard

Guide §3.5 and §4.7. Logs are event streams (twelve-factor), metrics are
Prometheus, traces are OpenTelemetry/Tempo, and the operator view is Grafana.
The security dashboard and the alert catalogue are specified in
[MONITORING.md](MONITORING.md). Abnormal situations use CU77. A post-incident
note records start, end, who was affected, what failed, and what will change.
That template is the guide §3.7; it is not a new product feature.

## 7. Privacy, licenses, and the law

Guide §3.9. Notarial records are retained indefinitely (SAD §2.3). `Person`
holds regulated identifiers. LINDDUN rows in the system threat model are the
engineering view. Issue **#309** is closed. It did not add a statute map,
and this page still does not give legal advice. The engineering follow-through
for testimony and personal data is **#2007**. Counsel owns any statute reading.

Third-party code is tracked by Dependabot and the SBOM attached to releases.
The scanner ignores the public AWS documentation key id
`AKIAIOSFODNN7EXAMPLE` and nothing else by default.

## 8. AI SDLC gate

Cloud agents follow the same Sec by Design loop. The steps below are the
product rule. The agent procedure, including the release-only CI consequence,
lives in
[SECURITY-GATES.md](../../300-development/304-ai-sdlc-cloud/SECURITY-GATES.md)
and in `.claude/skills/notaire-cybersec/SKILL.md`. That procedure is not a
second copy of this program.

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
[OWASP-TOP10.md](OWASP-TOP10.md). Issue #267 is closed. The mapping is the
engineering record. It is not a certification.

Open follow-ups from the 2026-11-05 assessment (#2003):

| Issue | Remaining work |
|-------|----------------|
| #2001 | Owner updates the live `protect-main` ruleset. Desired JSON in git already dropped checks that no longer run on a pull request |
| #2004 | Run the ZAP baseline on version tags as well as Monday |
| #2005 | `Referrer-Policy` and `Permissions-Policy` on the frontend |
| #2006 | Authorization regression pack across modules |
| #2007 | LINDDUN follow-through for testimony and personal data |
| #2008 | Redacted findings snapshot for the portfolio |
| #2009 | Threat Dragon (or equivalent) model checked against this register |
| #2010 | General API rate limit (SR-05, SR-15) |
| #2011 | Decide whether CodeQL findings fail the release |
| #1640 | MFA for admins and notaries |

Closed issues that this program used to list as open work: #254 (TLS runbook),
#267 (Top 10 mapping), #280 (rate-limit guide, not the limiter), #282, #283,
#309, #1465, #1738. Do not reopen them to point at the rows above.

Review cadence is quarterly, owned by the security officer named in the policy
file.
