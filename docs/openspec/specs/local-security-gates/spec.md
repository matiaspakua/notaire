# local-security-gates Specification

## Purpose
TBD - created by archiving change ci-1500-local-security-gates. Update Purpose after archive.
## Requirements
### Requirement: Security cases are specified and reported

The repository SHALL keep a versioned security case catalog that specifies
threat-model, secret, SAST, SCA, and DAST cases. Each case SHALL name the local
tool and the CI control. The runner SHALL write a report that lists every case
with a status. The catalog SHALL trace to CU78.

#### Scenario: Catalog names the control families

- **WHEN** an engineer reads `security/cases/catalog.yml`
- **THEN** the file contains cases `SEC-TM-01`, `SEC-SEC-01`, `SEC-SAST-01`, `SEC-SCA-01`, and `SEC-DAST-01`, and each case names a local tool and a CI tool

#### Scenario: The runner writes a report

- **WHEN** `python3 security/run_gates.py` finishes a profile
- **THEN** `reports/security/latest/report.json` and `index.html` list every case id and a status of PASS, FAIL, or SKIP

### Requirement: Local tools fail closed on planted defects

The secret case SHALL fail when a planted GitHub token is scanned and SHALL
pass on a clean tree, without printing the token. The SAST case SHALL fail
when Semgrep is missing or when the tree scan exits non-zero, and SHALL require
a finding on the planted fixture. The local SCA case SHALL fail when Trivy is
not installed. The DAST baseline SHALL accept an anonymous 401 on a protected
path and SHALL reject a response body that contains a stack trace.

#### Scenario: Planted secret fails and the tree passes

- **WHEN** the secret case scans a file that contains a `ghp_` token of the live length
- **THEN** the case status is FAIL and the detail does not contain the token

#### Scenario: DAST baseline distinguishes a leak from a 401

- **WHEN** the baseline calls a server that returns 401 without a stack trace, and then a server that returns a `java.lang` exception stack
- **THEN** the first call has no failures and the second call reports a failure

### Requirement: The AI SDLC runs the catalog before a pull request

`workspace/sdlc/run_pipeline.sh`, `workspace/sdlc/preflight.sh`, `AGENTS.md`,
`CONSTITUTION.md`, and `.claude/agents/openspec.md` SHALL name
`security/run_gates.py`. The pipeline SHALL run the DAST baseline with
`--only dast` after the stack is up. CI SHALL run the same catalog.

#### Scenario: Process documents point at the gate

- **WHEN** an agent reads the Constitution, AGENTS.md, or the OpenSpec agent
- **THEN** each file contains `security/run_gates.py` and the OpenSpec agent names `security/cases/catalog.yml`

