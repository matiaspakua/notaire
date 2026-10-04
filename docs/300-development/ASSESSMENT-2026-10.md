# System assessment — October 2026

| Field | Value |
|-------|-------|
| Baseline | `main` @ `d4473af2` (2026-10-04) |
| Tracking issue | #1242 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Method | Full test run on a native stack (PostgreSQL 16, backend jar, Next.js production build), static measurement of the repository, live API probes, comparison with open issues |

Every number below was measured on the baseline commit. Findings link to an issue: new
issues were created for gaps not yet tracked; existing open issues were commented with
current measurements instead of being duplicated.

## 1. Summary

| Dimension | Verdict | Main reason |
|-----------|---------|-------------|
| Functionality | Good, with gaps | 87 use cases, 230 endpoints, 39 pages; 10 endpoints without UI, 14 UI scenarios skipped |
| Test coverage | Backend strong, frontend weak | Backend 86.5% lines; frontend 16.4% lines |
| Software architecture | Mid-migration | 29 legacy `jpa` files (10,014 LOC) still referenced from 38 files |
| API architecture | Needs consistency work | Mixed language and singular collections; 15 of 38 controllers page results |
| Testing strategy | Solid pyramid, uneven | All suites green; skipped E2E, no accessibility checks, stale matrix |
| Security strategy | Strong perimeter, no authorization | No role checks anywhere; an `EMPLEADO` can create an `ESCRIBANO` |
| DevSecOps | Mature pipeline, advisory static analysis | Trivy, Checkstyle and SpotBugs never fail the build |
| Documentation | Well organised, partly stale | Guards for links, ERD and dictionary; 21.6% of doc lines Spanish; 276 open issues |
| Software design documentation | Good | SAD, 23 ADRs, generated data model; ADR-023 renames not executed |
| UX/UI | Good foundation | Design system, 879-key i18n parity, security headers; no automated accessibility checks |
| AI SDLC | Operational, bookkeeping drifts | 18 active OpenSpec changes, several merged but unarchived with unticked tasks |
| English translation | Code done, surroundings pending | Class names English; 4,000+ Spanish lines in code, tests and `testing/`, 4,537 in docs |

## 2. Test results (baseline run)

| Suite | Result |
|-------|--------|
| Backend (`mvn verify -pl backend-api -am`) | 1,784 tests, 0 failures, 0 skipped; JaCoCo 86.5% lines, 74.3% branches (floor 80/65) |
| Frontend unit (Vitest + coverage) | 376 tests in 40 files pass; statements 15.97%, branches 10.44%, functions 13.22%, lines 16.37% (floor 14/9/10/14) |
| Repository guards (`python3 -m unittest discover -s scripts/tests`) | 304 tests, 10 skipped, OK |
| CU-API matrix and SDLC plan validators | OK; 16 changes conform |
| HTTP integration (`testing/scripts/run.sh integration`) | OK |
| API (Bruno) | 300 requests, 512 assertions, all pass |
| UI E2E (Playwright, full config) | 540 passed, 0 failed, 0 flaky, **14 skipped**; 55 spec files; projects `chromium` 418, `smoke` 68, `health` 68 (the last two re-run tagged subsets) |

A first Bruno run showed 30 failures. The cause was the session overwriting the running jar
(`NoClassDefFoundError`); on a clean restart all requests pass. It is not a product defect.

## 3. Functionality and requirements validation

- 87 use case documents (CU01-CU87); `CU-API-MATRIX.csv` has 96 rows; 38 controllers expose
  230 mapped endpoints; 39 dashboard pages.
- 82 of 87 use cases are referenced by at least one Playwright spec. No spec references CU72
  (Documentos Presentados) or CU79 (Plantillas de Trámite); CU74-CU78 are non-functional and
  traced by prose only.
- The matrix is stale: 12 rows show `500-ERROR` although the endpoints return 200 and Bruno
  passes; 6 rows have no Bruno test. -> #1244
- 14 scenarios for CU15, CU23, CU24, CU25, CU39, CU42, CU47, CU48, CU59, CU62, CU65, CU66 are
  `test.skip`. -> #1243
- 10 endpoints have no UI consumer (heuristic scan): `pagos/fecha`, `presupuestos/persona`,
  `historial/gestion`, `reportes/documentos-por-vencer`, `usuarios/persona`, and the four
  `tipo-identificacion` operations. -> #1250
- Epics still open: #771 (cross-module case workflow), #773 and #774 (slices merged, issues
  open), #898 (73 use cases in Next.js).

## 4. Software architecture

Target: hexagonal (`adapter.in.web` -> `application.usecase` -> `repository`/`adapter.out`).

- Entities are English-named; 38 controllers, use cases per resource family under
  `application/usecase`.
- Legacy debt: `jpa` holds 29 files / 10,014 LOC; 38 files in `adapter`, `application`,
  `service` and `repository` still reference it; `BudgetTemplateController` builds a legacy
  controller from `JpaControllerProvider` and an `EntityManagerFactory`. -> #576, #574, #584
- 32 of 38 controllers contain `catch` blocks next to the global exception handler. -> #579
- 5 controllers still reference business entities in their signatures. -> #577
- 29 `FetchType.EAGER` associations. -> #595

## 5. API architecture

- Strengths: versioned path (`/api/v1`), committed OpenAPI artifact diffed in CI,
  `201` + `Location` helper, consistent DTO naming.
- ADR-023 defines the naming policy but defers all renames; paths remain mixed
  (`/people`, `/items` vs `/gestiones`, `/pagos`), singular (`/folio`, `/inmueble`, `/copia`),
  with `/buscar` routes. -> #1245
- Paging: 15 files use `Pageable`; other list endpoints are unbounded; no caching layer. -> #596, #597

## 6. Testing strategy

- Pyramid: JUnit (unit and H2 integration), Bruno (API), Playwright (UI), database V&V in
  Docker, ZAP DAST and performance workflows, repository guards.
- Gaps: skipped scenarios (#1243), stale matrix (#1244), frontend unit coverage (#1246), no
  accessibility checks (#1251), Bruno gaps for 6 matrix rows (#1244).
- Playwright runs `chromium` plus overlapping `smoke` and `health` projects, so raw counts
  overstate unique scenarios (418 unique chromium tests).

## 7. Security strategy

Strengths (verified): JWT in an HttpOnly cookie; login throttling and lockout; actuator and
Prometheus endpoints require authentication (`/actuator/prometheus` anonymous -> 401);
Swagger denied in production; CSP with nonce, `X-Frame-Options: DENY`, HSTS,
`X-Content-Type-Options: nosniff`; passwords hashed; secrets via `.env`; SECURITY.md with
private reporting.

Findings:

| Finding | Evidence | Issue |
|---------|----------|-------|
| No server-side authorization | No `@PreAuthorize`/`hasRole`; an `EMPLEADO` token got 200 on `/usuarios`, `/roles`, `/audit-log` and 201 creating an `ESCRIBANO` user | #559 (critical, commented) |
| Default `admin/admin` seed outside production | `app.admin.*` defaults in `application.properties`; login returns 200 | #1249 |
| JWT without refresh or revocation | existing | #676 |
| Outdated JasperReports | existing | #567 |
| Validation rolled out to 17 of 38 controllers | `@Valid` count | #655 |
| Observability traffic without TLS | existing | #684 |
| A test admin token remains in git history | flagged in #1237; rotate `JWT_SECRET` in shared environments | #1237 |

## 8. DevSecOps

17 workflows (CI, frontend CI, Playwright, CodeQL, DAST ZAP, DB V&V, OpenAPI contract,
performance, backup/restore smoke, CD, release-please, SDLC process, PR validation).
Required checks on `main`: CI, Frontend CI, Playwright E2E, Code Lint, PR Validation, Process
Checks. Dependabot, SBOM in CD and CodeQL are present.

- Static analysis and scanning are advisory: Checkstyle `failOnViolation=false`, SpotBugs
  skipped by default, Trivy `exit-code: '0'` (3 scans). -> #710, #711, #680, #712, #566
- `ci.yml` is a 531-line monolith. -> #679
- The merge API does not enforce required checks for the agent account; completion must be
  verified through check runs. -> #1252

## 9. Documentation

- Numbered structure (`100-business`, `200-architecture`, `300-development`), archive folders,
  guards for links, ERD and data dictionary sync, CHANGELOG structure and business-docs
  traceability.
- 276 open issues; many are documentation shells whose deliverable exists, and #599 reports
  issues that claim completion while contradicted by code. -> #1248, #599
- Spanish share of live doc lines: 21.6%. -> #1247
- Stray root files (`implement-801-status.md`, `implement-1022-status.md`); 509 tracked
  binaries; `.git` 166 MB. -> #682

## 10. Software design documentation

SAD, 23 ADRs (architecture, security, observability, rate limiting, secrets, OpenAPI exposure,
hexagonal pilot, REST naming), PlantUML/Mermaid diagrams, generated ERD and data dictionary
guarded in CI. Gaps: ADR-023 decisions not applied (#1245); the hexagonal pilot (ADR-021) has
no completion plan beyond the open arch issues (#576-#580).

## 11. UX/UI

- Strengths: design-system tokens and form patterns, i18n parity (879 keys in `en` and `es`),
  responsive overflow checks at 320/768/1024 px in 12 specs, strict security headers, icon
  button names (TS-0096).
- Gaps: no automated accessibility scan (#1251); the login page bypasses the form pattern
  (#611); 20 translation values identical in both locales and 6 hardcoded JSX literals
  (#1247); frontend unit coverage 16% (#1246).

## 12. AI SDLC

- Operating model: CONSTITUTION with five gates, OpenSpec schema `notaire-sdlc`, SessionStart
  status hook, push-to-main guard, `scripts/preflight.sh` mirroring CI, atomic Conventional
  Commits, issue comments as hand-off notes. 72 commits since 2026-09-01 followed this flow.
- Drift: 18 active OpenSpec changes against 118 archived; merged changes still show 9/46
  tasks. The status signal intended for resuming agents is unreliable. -> #1252
- Required-check enforcement on merge is manual. -> #1252

## 13. English translation backlog

Method: lines containing Spanish diacritics or Spanish domain vocabulary; excludes archives,
`deprecated/`, `node_modules`, `target`.

| Area | Files with Spanish / files | Spanish lines / lines |
|------|---------------------------|-----------------------|
| Backend main Java | 156 / 334 | 1,366 / 39,024 (3.5%) |
| Backend test Java | 163 / 227 | 1,448 / 41,456 (3.5%) |
| `notaire-shared` | 24 / 56 | 49 / 4,296 (1.1%) |
| Frontend `src/` | 101 / 155 | 1,175 / 19,863 (5.9%) |
| Frontend `messages/` | 2 / 2 | 655 / 2,250 (expected: Spanish locale) |
| `testing/` | 62 / 75 | 1,733 / 11,744 (14.8%) |
| Flyway migrations | 34 / 43 | 239 / 2,307 (10.4%) |
| Bruno API tests | 130 / 339 | 225 / 8,161 (2.8%) |
| `scripts/` | 12 / 96 | 49 / 10,978 (0.4%) |
| Live docs | 154 / 182 | 4,537 / 20,974 (21.6%) |

Structural: 38 of 39 tables are Spanish-named; REST paths mixed; 9 report files Spanish;
legacy names `Administrador*`, `Constantes*`. Plan and slices: #1247.

## 14. Findings index

| Issue | Type | Title |
|-------|------|-------|
| #1243 | new | 14 skipped Playwright scenarios |
| #1244 | new | stale CU-API matrix; CU72/CU79 untraced |
| #1245 | new | execute the ADR-023 REST rename |
| #1246 | new | frontend coverage 16% |
| #1247 | new | English-translation backlog umbrella |
| #1248 | new | triage 276 open issues |
| #1249 | new | default `admin/admin` seed |
| #1250 | new | 10 endpoints without UI |
| #1251 | new | no accessibility checks |
| #1252 | new | OpenSpec lifecycle drift and manual merge gate |
| #559, #576, #596, #682, #710 | existing, commented | RBAC, jpa retirement, paging, binaries, advisory gates |

## 15. Recommended order

1. #559 (RBAC) and #1249 (default admin): security.
2. #1243, #1244, #1250: close the requirement-to-UI-test loop.
3. #1252, #1248: restore a trustworthy status signal.
4. #576 and the other arch issues, then #1245 and #1247 slices.
5. #1246, #1251: raise frontend verification.
