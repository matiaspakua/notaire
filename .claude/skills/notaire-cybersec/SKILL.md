---
name: notaire-cybersec
description: Run Notaire security gates locally, update the STRIDE register, and file security issues without weakening release-only CI. Use when a change touches authentication, authorization, personal data, secrets, dependencies, security headers, threat models, or security workflows.
---

# Notaire cybersec

Product controls live in `docs/200-architecture/206-security/`. This skill is
how an agent uses them. The AI Factory procedure is
`docs/300-development/304-ai-sdlc-cloud/SECURITY-GATES.md`. Do not copy the
threat register into a factory doc.

## Run the gates

Before a pull request:

```bash
python3 security/run_gates.py --profile local --skip-dast
bash workspace/sdlc/preflight.sh --full
```

`preflight.sh` already runs the local profile with `--skip-dast`. `--full` is
the pull-request proof (#2001). GitHub does not re-run `ci.yml`, CodeQL, or
ZAP on the pull request.

When the Docker stack is up, `bash workspace/sdlc/run_pipeline.sh` runs the
same catalog and then `security/dast_baseline.py` (health and an anonymous 401
on `/api/v1/people`). That baseline does not exploit the API.

Install pinned local tools with `bash security/tools/ensure_local_tools.sh`
if the local profile says Trivy or Semgrep is missing.

## What does not run on a pull request

- `ci.yml` security job (secrets, Semgrep, Trivy filesystem and image)
- `codeql.yml` (version tag, Monday 06:30 UTC, or `workflow_dispatch`)
- `dast-zap.yml` (Monday 05:00 UTC or `workflow_dispatch`, not on the tag — #2004)
- `cd.yml` (no image publish on a merge to `main`)

Do not add `pull_request` to those workflows to make a check green. Do not set
`continue-on-error` on the secret scan, Semgrep, or Trivy.

CodeQL uploads SARIF and does not fail the job on findings (#2011). Default
CodeQL setup must stay off or the advanced upload is rejected.

## Update the threat model

Update both, in the same change, when the diff touches authentication,
authorization, personal data, secrets, dependencies, or a public endpoint:

- `docs/200-architecture/206-security/THREAT-MODEL-SYSTEM.md` and, for login or
  audit, `THREAT-MODEL-AUTH-AUDIT.md`
- `docs/200-architecture/206-security/threat-model/system.stride.yml`

`security/tests/test_threat_register.py` fails if an `SR-*` or `LD-*` id is
only in one of them. Keep `procedures: none`. Do not add exploit steps, payloads,
or real secrets.

`RoleModuleAuthorizationFilter` enforces module grants (#1683). Do not document
that work as still pending. Intra-office object sharing stays accepted (SR-11)
until a use case says otherwise.

## File an issue

File a new issue, linked to CU78, when a gap is real and out of the current
change. Do not bury it in a comment. Say what file proves the gap, and give
acceptance criteria a test can check. Do not reopen a closed issue that was a
different deliverable (#280 was a guide; the limiter is #2010).

The Owner updates the live `protect-main` ruleset (#2001). Agents do not apply
`security/rulesets/protect-main.desired.json`.

## Portfolio

`github-page/` security pages use checked-in demo data. They are not a scan
result. Do not commit `reports/security/`, SARIF, or a finding that contains a
secret shape (`ghp_`, a private-key banner).
