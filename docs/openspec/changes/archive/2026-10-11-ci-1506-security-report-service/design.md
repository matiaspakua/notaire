## Context

#1500 added `security/run_gates.py` and a CI artifact. The HTML report was not
on the infra hub, and the security job used `needs: build`, so path-filtered
pull requests that skip the Java build also skipped every security gate.
CU78 is the use case. `ci.yml` is forbidden from holding GitHub Pages
permissions.

## Goals / Non-Goals

**Goals:**

- Keep secret scan, Semgrep catalog, Trivy, CodeQL, and scheduled ZAP present and enabled.
- Run the CI security job whenever the path-filter job succeeds.
- Serve the local report from the existing observability compose and list it in Homer.
- Show the case table on the GitHub Actions run summary.

**Non-Goals:**

- Making Trivy or ZAP fail the build.
- A public GitHub Pages site for the report.
- Changing product authorization.

## Decisions

1. **nginx in the existing compose, not a new stack.** Service
   `security-reports` uses pinned `nginx:1.27.5-alpine`, mounts
   `reports/security` read-only, and listens on host port 8890. Homer links to
   it. Blackbox probes `http://security-reports:80/` like Homer. The stack
   status dashboard gains one stat panel so the probe invariant stays true.

2. **Security job depends on `changes`, not `build`.** The job already checks
   out the repo and warms Maven itself. Skipping it when Java is skipped left
   secret detection off for docs-only pull requests.

3. **Remote view is the job summary plus the existing artifact.**
   `security/publish_summary.py` writes Markdown to `$GITHUB_STEP_SUMMARY`.
   A hosted copy would need Pages permissions this workflow must not have.
   That hosted copy stays a later step.

## Riesgos / Trade-offs

- [Security job now runs on every CI start, including docs-only pull requests] → Semgrep and Trivy add minutes. The gates are the point of the job.
- [Report directory may be empty until the first local run] → The landing page still returns 200 and tells the operator the command. The report link 404s until then.
- [nginx is another container] → It is the same compose project and the same probe pattern as Homer.

## Testing Strategy

- `security/tests/test_security_gate_surface.py` failed first on the missing
  service, the `needs: build` dependency, and the missing summary module.
- `infra/tests/test_grafana_status_dashboards.py` covers the new probe name.
- A local `docker compose up` of `security-reports` plus curl of port 8890 is
  the service check. No Playwright: the page is infra nginx, not the product UI.

## Regression Strategy

- `python3 security/tests/test_security_policy.py` and
  `python3 security/tests/test_security_cases.py` stay green.
- `workspace/tests/test_ci_workflow_invariants.py` still requires the security
  job and still forbids Pages permissions on `ci.yml`.
- Image pin tests accept `nginx:1.27.5-alpine` because the tag contains a minor version.

## Playwright Strategy

No product UI change. The report page is an infra static file. Playwright is
not required.

## Deployment Strategy

`bash infra/scripts/start-infra.sh` (or compose up in `infra/observability`)
starts the new container with the rest of the stack. No migration. CI behavior
changes on the next pull request: the security job runs after `changes`.

## Rollback Strategy

Revert the pull request. Remove the container with the stack. No schema.
Reports under `reports/security` are local files and can be deleted.

## Migration Plan

None. Operators open `http://localhost:8890` after the stack is up and after
one local catalog run.

## Open Questions

- A later change can publish the HTML somewhere other than the Actions artifact
  without adding `pages` permission to `ci.yml`.
