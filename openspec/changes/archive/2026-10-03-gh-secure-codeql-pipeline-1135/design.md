# Design — GitHub Security Lab baseline in the pipeline

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`gh-secure` is a GitHub CLI extension. It does not ship a reusable workflow.
It calls the repository API to enable branch protection, private vulnerability
reporting, secret-scanning push protection, Dependabot alerts and security
updates, and CodeQL default setup. The cloud agent token can read the
repository and can see that private vulnerability reporting is already
enabled. The same token receives HTTP 403 on administration endpoints.

## Goals / Non-Goals

**Goals:**
- Run CodeQL on pull requests and on `main` for Java, JavaScript/TypeScript, and Actions, and upload results to code scanning.
- Cover `frontend/package-lock.json` with Dependabot version updates.
- Publish `SECURITY.md` for the already-enabled private reporting channel.
- Give an admin `scripts/enable-gh-secure.sh` for the API-only settings, without turning on branch protection by default.

**Non-Goals:**
- Enabling branch protection, secret-scanning push protection, or Dependabot security updates from this agent (the token cannot).
- Enabling CodeQL default setup in addition to the workflow.
- Adding Gitleaks, OWASP ZAP, or a new product dependency.
- Failing the pipeline when CodeQL finds an alert. Analysis failure fails the job; findings are Security-tab alerts.

## Decisions

- **Advanced setup, not default setup.** Default setup is an API flag and is a poor fit for a multi-module Maven build. The workflow compiles with `mvn -B compile -DskipTests` after `codeql-action/init`, which is what CodeQL needs to see Java bytecode. JavaScript/TypeScript and Actions use `build-mode: none`.
- **Do not call `gh secure code-scanning`.** Default setup plus this workflow would analyze the same commits twice. The script's feature list omits `code-scanning` on purpose.
- **Branch protection stays opt-in.** The extension's default rule requires one approving review. That would stop an unattended merge. `--with-branch-protection` is the only way the script requests it.
- **`skip_specs: true`.** No product requirement changes. CU78's existing "continuous vulnerability scanning" line is the business rule being made operational.
- **CodeQL is not in local preflight.** The analysis uploads SARIF with `security-events: write` on a GitHub-hosted runner. Preflight records a skip so the local/CI map stays honest.

## Riesgos / Trade-offs

- [Java analysis time] → The job timeout is 45 minutes and the matrix is `fail-fast: false`, so a slow Java build does not cancel the JavaScript or Actions jobs.
- [Admin never runs the script] → Push protection and Dependabot security updates stay off until an owner with administration scope runs `--apply`. The pipeline still gains CodeQL and npm Dependabot without that step.
- [Someone enables default setup later] → GitHub rejects advanced SARIF processing and the Analyze job fails. The workflow uses `wait-for-processing: false` so leftover default setup does not hard-fail CI after a successful analysis; `enable-gh-secure.sh --apply` disables default setup. An admin must still turn it off for findings to appear in the Security tab.
- [CodeQL alerts on existing code] → Alerts do not fail CI. Triage happens in the Security tab.

## Testing Strategy

No application tests. Verification:

- `bash -n scripts/enable-gh-secure.sh`
- `bash scripts/enable-gh-secure.sh --help`
- `bash scripts/validate-sdlc-plan.sh gh-secure-codeql-pipeline-1135`
- `python3 -c` parse of `.github/workflows/codeql.yml` and `.github/dependabot.yml`
- `bash scripts/preflight.sh --list` includes the CodeQL row

The CodeQL job itself runs on GitHub-hosted runners after the pull request is opened.

## Regression Strategy

No production code. Existing CI workflows are untouched. Dependabot gains one npm block and keeps the Maven and GitHub Actions blocks.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Nothing is deployed. The workflow starts on the next pull request and on the next push to `main`. SARIF upload needs `security-events: write`, which the analyze job sets. An admin enables the remaining GitHub settings with `bash scripts/enable-gh-secure.sh --apply` outside this pull request.

## Rollback Strategy

Revert the merge commit. That removes the workflow, the script, `SECURITY.md`, and the npm Dependabot block. Code scanning alerts already uploaded remain in GitHub until dismissed. No schema or runtime rollback.
