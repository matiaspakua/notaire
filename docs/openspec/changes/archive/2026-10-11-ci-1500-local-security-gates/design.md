## Context

#307 put the security program, the threat model, and a failing secret scan in
the repository. The pre-PR pipeline still did not execute a security case
schema, and the AI SDLC instructions did not require one. CU78 is the use case.
CodeQL and ZAP already run in GitHub Actions and are not local default binaries.

## Goals / Non-Goals

**Goals:**

- Specify threat-model, secret, SAST, SCA, and DAST cases in one catalog.
- Run the cases that can run on a workstation before a pull request, and write a report.
- Keep CI on the same catalog, with CodeQL and ZAP as the deeper SAST and DAST.
- Make the Constitution, AGENTS.md, and the OpenSpec agent require that gate.

**Non-Goals:**

- Turning Trivy findings into a failing gate (#282).
- Turning the weekly ZAP job into a PR gate (#281).
- Changing product authorization (SR-11, SR-16).
- Storing exploit procedures.

## Decisions

1. **One catalog, two profiles.** `security/cases/catalog.yml` is the schema.
   `--profile local` executes Semgrep, the secret scan, Trivy, and (when a base
   URL is set) the DAST baseline. `--profile ci` executes Semgrep and the secret
   scan, and checks that CodeQL, Trivy Action, and ZAP workflows are present.
   Same cases, deeper CI tools where a local binary is the wrong place (CodeQL
   is GitHub-hosted; ZAP needs a long-lived scan).

2. **Semgrep is the local SAST, not a second copy of CodeQL.** The ruleset is
   two high-signal rules (password value in a log call, credential key in web
   storage) plus a planted fixture that must fire. The repository scan must be
   clean. CodeQL remains the broad CI SAST.

3. **DAST baseline is not ZAP.** `security/dast_baseline.py` checks
   `/actuator/health` and an anonymous `401` or `403` on `/api/v1/personas`,
   and rejects a Java stack trace. `run_pipeline.sh` runs it after the stack
   starts. Preflight records the case as SKIP because the API is not up yet.

4. **Trivy must be installed locally and still exits 0.** A missing binary
   fails the local gate, so the tool is actually available. Findings stay
   report-only, matching CI, until #282.

5. **Pinned installers.** `security/tools/ensure_local_tools.sh` installs
   Semgrep 1.180.0 into `security/.tools/venv` and Trivy 0.75.0 into
   `security/.tools/bin`. Those directories are gitignored.

## Riesgos / Trade-offs

- [Semgrep rules are narrow] → They prove the gate. CodeQL remains the broad scan. A repeated miss becomes a new rule, not a silent skip.
- [Trivy database download on first local run] → The tool is cached under `security/.tools`. Findings do not fail the gate.
- [DAST baseline misses authenticated bugs] → ZAP remains the weekly deeper scan. The baseline only claims anonymous rejection and no stack trace.
- [Process job does not install Semgrep] → The unit tests inject a fake binary. The real rules run in the CI security job and when the tool is installed locally.

## Testing Strategy

- `security/tests/test_security_cases.py` is the Gate 2 suite. It was run before
  the runner existed and failed on the missing module and the missing pipeline
  string.
- Cases covered: catalog shape (`SEC-TM-01` through `SEC-DAST-01` and
  `SEC-SDLC-01`), planted secret, fake Semgrep pass and fail, missing Trivy,
  DAST 401 versus stack trace, report JSON and HTML, SDLC file wiring.
- `RealSemgrepTest` runs the pinned binary when `ensure_local_tools.sh` has
  installed it, including the planted fixture and a clean tree.
- No new product behavior, so no JaCoCo change.

## Regression Strategy

- `python3 security/tests/test_security_policy.py` still guards the #307 policy,
  dashboard, and secret scan.
- `bash security/verify.sh` runs the security module tests.
- Preflight's process self-tests discover `security/tests`, so the new test
  runs on every pull request.
- The CI security job keeps `python3 security/scan_secrets.py` and adds
  `python3 security/run_gates.py --profile ci`.

## Playwright Strategy

No product UI change. The github-page security card is a documentation sentence.
Playwright is not required. The DAST baseline is an HTTP check, not a browser flow.

## Deployment Strategy

No schema, no image contract change, no new runtime service. The CI security
job installs Semgrep on the runner and uploads `reports/security/latest/` as
the artifact `security-case-report`. Gate 5 smoke is
`python3 security/run_gates.py --profile ci --no-install` on a tree with Semgrep
installed, exit 0.

## Rollback Strategy

Revert the pull request. No migration to undo. The secret scan step in `ci.yml`
remains even if the catalog step is reverted. Local `security/.tools/` is
untracked and can be deleted.

## Migration Plan

None. Developers obtain local tools by running the gate or
`bash security/tools/ensure_local_tools.sh`. `python3-venv` is required for the
Semgrep virtualenv.

## Open Questions

- When #282 makes Trivy fail on CRITICAL/HIGH, the catalog flag `fail_on_findings`
  flips. This change leaves it false.
- When #281 makes ZAP fail the build, the CI DAST case can start requiring a
  clean ZAP artifact. This change only checks that the workflow exists.
