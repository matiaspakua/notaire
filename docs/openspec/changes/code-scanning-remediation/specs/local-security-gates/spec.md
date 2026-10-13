## MODIFIED Requirements

### Requirement: Local tools fail closed on planted defects

The secret case SHALL fail when a planted GitHub token is scanned and SHALL
pass on a clean tree, without printing the token. The SAST case SHALL fail when
Semgrep is missing or when the tree scan exits non-zero, and SHALL require a
finding on each planted SAST fixture. The local SCA case SHALL fail when Trivy
is not installed. The DAST baseline SHALL accept an anonymous 401 on a protected
path and SHALL reject a response body that contains a stack trace. The local
security policy SHALL reject disabled CSRF protection for browser-cookie API
authentication and CD jobs that omit explicit permissions.

#### Scenario: Planted secret fails and the tree passes

- **WHEN** the secret case scans a file that contains a `ghp_` token of the live length
- **THEN** the case status is FAIL and the detail does not contain the token

#### Scenario: DAST baseline distinguishes a leak from a 401

- **WHEN** the baseline calls a server that returns 401 without a stack trace, and then a server that returns a `java.lang` exception stack
- **THEN** the first call has no failures and the second call reports a failure

#### Scenario: Planted disabled CSRF configuration fails SAST

- **WHEN** the SAST fixture contains a browser-cookie API configuration that disables CSRF
- **THEN** the local SAST case fails and identifies the policy violation

#### Scenario: CD job without explicit permissions fails policy validation

- **WHEN** a CD workflow job lacks an explicit permissions declaration
- **THEN** the local security policy fails before the pull request is opened

#### Scenario: Dynamic regex construction without quoting fails SAST

- **WHEN** the SAST fixture constructs a regular expression from unquoted dynamic text
- **THEN** the local SAST case fails before the pull request is opened
