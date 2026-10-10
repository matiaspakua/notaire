## ADDED Requirements

### Requirement: Security policy is executable

The repository SHALL keep a versioned security policy that names the selected
SAST, SCA, secret, DAST, SBOM, and signing controls, a severity remediation
SLA, a quarterly review, and an owner-approved exception procedure. The policy
SHALL trace to CU78.

#### Scenario: Policy declares the control set

- **WHEN** an engineer reads `security/policy/security-policy.yml`
- **THEN** the file names CodeQL, Trivy, the in-repo secret scan, ZAP, a CycloneDX SBOM, and cosign, and it sets critical remediation to at most 7 days and high remediation to at most 30 days

#### Scenario: Program follows Sec by Design

- **WHEN** an engineer opens the cybersecurity program
- **THEN** the document applies STRIDE, LINDDUN, OWASP SAMM, and Sec by Design, and it cites `final_projects_specialization.md` as the guideline

### Requirement: High-confidence secrets fail the build

The secret scan SHALL report a private key and a GitHub personal access token
planted in a file, SHALL ignore placeholders and the public AWS sample key id,
SHALL redact secret values in its output, and SHALL exit non-zero when the
working tree contains a finding. CI SHALL run that scan without
`continue-on-error`.

#### Scenario: Planted key and token are findings

- **WHEN** the scanner reads a PEM private key or a `ghp_` token of the live length
- **THEN** it records `private-key` or `github-pat` and the formatted report does not contain the secret

#### Scenario: Placeholders are not findings

- **WHEN** the scanner reads `changeme`, `your-secret-here`, `ghp_example`, or `AKIAIOSFODNN7EXAMPLE`
- **THEN** it records no finding

#### Scenario: The tree is clean and CI enforces it

- **WHEN** the scanner walks the repository and CI defines the security job
- **THEN** the tree has no finding and `.github/workflows/ci.yml` runs `python3 security/scan_secrets.py` on a step that is not `continue-on-error`

### Requirement: Security posture is visible

Grafana SHALL provision a `notaire-security` dashboard that shows login
failures, locked logins, and firing security alerts including
`LoginLockoutSpike`. Prometheus SHALL define `LoginLockoutSpike` on
`notaire_operation_total` with `status="locked"` at warning severity, with a
summary and a description.

#### Scenario: Dashboard shows abuse signals

- **WHEN** an operator opens the security dashboard
- **THEN** the dashboard uid is `notaire-security` and its queries include login totals, `bad_credentials`, `locked`, and `ALERTS` for `LoginLockoutSpike`

#### Scenario: Lockout alert is actionable

- **WHEN** Prometheus loads `alert-rules.yml`
- **THEN** `LoginLockoutSpike` matches locked login outcomes, is warning severity, and has a summary and a description

### Requirement: System threat model stays current

The system threat model SHALL record STRIDE and LINDDUN, SHALL identify
`SR-11` and `SR-14`, and SHALL state that user type Escribano receives
administrator authority. The auth model and the SAD SHALL describe
function-level `ROLE_ADMIN` checks and SHALL NOT claim that per-role
authorization is absent.

#### Scenario: System model names the open object-level gap and the secret control

- **WHEN** an engineer reads the system threat model and the trust-boundary diagram
- **THEN** the model contains STRIDE, LINDDUN, SR-11, SR-14, and Escribano, and the diagram marks a trust boundary

#### Scenario: Authorization docs match the code

- **WHEN** an engineer reads the auth threat model and SAD §8.1
- **THEN** both describe `ROLE_ADMIN` as Partial or present, and neither says per-role authorization is missing
