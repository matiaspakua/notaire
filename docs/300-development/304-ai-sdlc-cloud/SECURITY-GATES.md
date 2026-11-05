# Security gates for cloud agents

AI Factory procedure. This page is not the product threat model.

The product assessment, the STRIDE register, and the control status live in
`docs/200-architecture/206-security/ASSESSMENT-2026-11-05.md` (CU78, #2003).
Read that file when you need the evidence. Do not paste it here.

The skill agents should load is `.claude/skills/notaire-cybersec/SKILL.md`.

## What an agent can prove before a pull request

`bash workspace/sdlc/preflight.sh --full` is the proof (#2001). It includes
`python3 security/run_gates.py --profile local --skip-dast` (threat-model
evidence, secret scan, Semgrep, Trivy filesystem). The DAST baseline runs only
inside `bash workspace/sdlc/run_pipeline.sh`, after the stack is up, and only
checks health plus an anonymous 401.

GitHub product workflows **do not run on pull_request**. A green pull request
page does not mean CodeQL, Trivy, or ZAP ran. `check-heavy-ci.sh` treats a
missing heavy check as success when no workflow run exists, and it fails if a
run on that head is pending or red. The Owner still has to drop retired required
checks from the live `protect-main` ruleset or GitHub will refuse the merge
(#2001). Agents do not edit the live ruleset.

## When to update the threat model

Same change, both files, when the diff touches authentication, authorization,
personal data, secrets, dependencies, or a public endpoint:

- `docs/200-architecture/206-security/THREAT-MODEL-SYSTEM.md` (and the auth model
  when the change is login or audit)
- `docs/200-architecture/206-security/threat-model/system.stride.yml`

## When to file an issue

File a CU78 issue with acceptance criteria when the gap is outside the change.
The open list from the 2026-11-05 assessment is in the product program, section 9.
Do not weaken a gate to close one of those issues.

## Release-only consequence

| Want to know | Where it actually runs |
|--------------|------------------------|
| Secrets, Semgrep, Trivy | Local preflight, and again on a version tag in `ci.yml` |
| CodeQL | Version tag, Monday 06:30 UTC, or manual dispatch. Not locally |
| ZAP | Monday 05:00 UTC or manual dispatch. Not on the tag (#2004) |
| Signed images | `cd.yml` on a version tag. Not on a merge to `main` |
| This portfolio | `deploy-github-page.yml` on push to `main` |

Do not reintroduce `pull_request` on those product workflows.
