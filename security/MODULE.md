# security

**Purpose:** the security fleet's workspace: repository protection as code, and the map of every security control in the system.

**Verify:** `bash security/verify.sh`.

## Contract (what other modules may rely on)

- `rulesets/protect-main.desired.json` is the desired state of the `protect-main` ruleset; `apply-protect-main-ruleset.sh` applies it and `assert-protect-main-ruleset.sh` checks it (both need an admin `gh` session).
- `enable-gh-secure.sh` reports and applies the GitHub security baseline (admin `gh` session).
- `tests/` also guards Dependabot hygiene and image pins.
- Required status check names (`CI`, `Frontend CI`, `Playwright E2E`, `Code Lint`, `PR Validation`) are guarded against the workflow job names.

## Where the other security controls live

They stay next to what they protect, because the tool reads them from a fixed place or must run where the source is (ADR-024).

| Control | Location | Why it stays there |
|---------|----------|--------------------|
| Vulnerability policy | `SECURITY.md` | GitHub reads it from the repository root |
| Code owners | `.github/CODEOWNERS` | GitHub reads it from `.github/` |
| Dependabot | `.github/dependabot.yml` | GitHub reads it from `.github/` |
| CodeQL, Trivy, DAST (ZAP) | `.github/workflows/codeql.yml`, `ci.yml`, `dast-zap.yml` | scans run where the source is |
| SonarQube analysis | `infra/scripts/run-sonar.sh` | part of the quality stack in `infra` |
| Auth, input validation, SQL injection, threat model | `docs/200-architecture/206-security/` | knowledge base |
| Threat-modeling skill, security auditor agent | `.claude/skills/secure-threat-modeling`, `.claude/agents/security-auditor.md` | central catalogs |
| Application security code | `backend-api/src/main/java/.../security` | product code |

## Seams (what this module reads from outside)

- `.github/workflows/*.yml` job names and `.claude/rules/hooks.md`, read by the guard.

## Must not

- Hold product code or copies of the controls listed above.

Manifest entry: [`workspace/modules.yaml`](../workspace/modules.yaml). Rationale: ADR-026.
