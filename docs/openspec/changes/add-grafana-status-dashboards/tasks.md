> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**; change-specific work
> goes in group 4.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1408 exists and is linked to CU77
- [x] 1.2 CU77 "Operations Monitoring and Incident Management" documentation exists
- [x] 1.3 Acceptance Criteria defined as scenarios in `specs/observability-status-dashboards/spec.md`
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md` (`infra` only)
- [x] 1.5 No new ADR; ADR-016 inventory updated (group 8)
- [x] 1.6 Move the Issue to IN PROGRESS: `gh issue edit 1408 --add-label "in-progress"`

## 2. Create branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b feat/1408_grafana_status_dashboards`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Write tests (TDD, failing first)

- [x] 3.1 Enumerate test cases from the spec scenarios (see `design.md` — Testing Strategy)
- [x] 3.2 Write `infra/tests/test_grafana_status_dashboards.py` with the eight guard tests
- [x] 3.3 Run `python3 infra/tests/test_grafana_status_dashboards.py` and **observe it fail**
- [x] 3.4 Confirm every `#### Scenario:` maps to at least one test (`traceability.md`)

## 4. Implementation

- [x] 4.1 Add pinned `prom/blackbox-exporter:v0.28.0` service and `blackbox.yml` (`http_2xx`, `tcp_connect`)
- [x] 4.2 Add `probe_success` scrape jobs with a stable `service` label to `prometheus.yml`
- [x] 4.3 Add `ServiceDown` alert rule on `probe_success == 0`
- [x] 4.4 Add provisioned dashboard `notaire-stack-status.json`
- [x] 4.5 Add provisioned dashboard `notaire-status.json`
- [x] 4.6 Register the new guard test in `infra/verify.sh`
- [x] 4.7 Add the Grafana/Prometheus links to the Homer dashboard if applicable

## 5. Update existing tests

- [x] 5.1 Confirm existing `infra/tests/test_*.py` still pass (`bash infra/verify.sh`)

## 6. Run regression

- [x] 6.1 `bash infra/verify.sh`
- [ ] 6.2 `bash workspace/sdlc/preflight.sh`
- [ ] 6.3 `bash workspace/sdlc/run_pipeline.sh`

## 7. Playwright

- [x] 7.1 Not applicable (no product UI change)

## 8. Gate 3 — Update permanent documentation

- [x] 8.1 `infra/MODULE.md`
- [x] 8.2 `infra/docs/DEFINITION.md`
- [x] 8.3 `infra/docs/OPERATION.md`
- [x] 8.4 `docs/200-architecture/207-monitoring/README.md`
- [x] 8.5 `docs/200-architecture/202-ADR/ADR-016-observability-stack.md`

## 9. Atomic commits

- [ ] 9.1 Conventional Commits; only the final commit carries `Closes #1408`

## 10. Pull Request and CI validation

- [ ] 10.1 Push and open the PR `[#1408] feat: ...`
- [ ] 10.2 Verify the PR is `MERGEABLE` and CI is green

## 11. Deploy

- [ ] 11.1 Start the application stack, then the observability stack; Grafana reloads the dashboards

## 12. Gate 5 — Smoke and close

- [ ] 12.1 One `probe_success` series per configured service; both dashboard UIDs resolve in Grafana
- [ ] 12.2 Stop one service and confirm its probe is `0` and the other services are unaffected
- [ ] 12.3 Close #1408 after merge, deploy and smoke test

## Definition of Done

- [ ] All eight guard tests green; existing infra tests green
- [ ] Both dashboards visible in Grafana with live data
- [ ] Permanent documentation updated
