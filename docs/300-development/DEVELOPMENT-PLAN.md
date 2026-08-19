# Development Plan — Notaire

Synthesizes how the project is actually built: the mandatory process, the
conventions every change must follow, and the roadmap this codebase is
migrating along. This is a process document — it does not duplicate the
authoritative source for any of these; it points to it.

## 1. Governing process

`CONSTITUTION.md` (repo root) is the single highest authority for the
development process — this plan, `CLAUDE.md`, and every file under
`.claude/rules/` implement it, and any conflict is resolved in its favor.

- **SDLC workflow** (Issue + Use Case → OpenSpec Specification → branch →
  TDD → implement → refactor → full test suite → commit → push → docs →
  PR → merge): `CONSTITUTION.md` §5, detailed step-by-step in
  [`.claude/rules/ai-agent-workflow.md`](../../.claude/rules/ai-agent-workflow.md).
- **Definition of Done** (10 checkboxes, from "Issue linked to a Use Case"
  to "merged, smoke-tested, Issue closed"): `CONSTITUTION.md` §3.
- **Quality Gates 1–5** (Specification review → tests-first → full suite
  green → CI/code-review → smoke test): `CONSTITUTION.md` §6.
- **Mandatory conventions** (issue labeling, branch naming, commit format,
  PR title format, code style, DTO/URL naming, test naming, Flyway
  migration naming): `CONSTITUTION.md` §4 — the same table is summarized
  in `CLAUDE.md`'s Git Workflow section for quick reference.
- **AI agent-specific rules** (this applies whether the change is made by
  Claude Code, OpenCode, or GitHub Copilot): `CONSTITUTION.md` §10 and
  [`.claude/rules/ai-agent-workflow.md`](../../.claude/rules/ai-agent-workflow.md).

No step in that workflow may be skipped outside the narrow exceptions in
`CONSTITUTION.md` §12 (emergency security hotfixes, one-time migration
scripts, trivial doc typo fixes) — and even those require the exception to
be documented in the commit.

## 2. Environment setup

See [`301-setup/README.md`](301-setup/README.md) for prerequisites,
installation, module structure, and the day-to-day command reference
(build, run, test, coverage). In short:

```bash
cp .env.example .env        # once, then fill in real values
bash scripts/start.sh       # PostgreSQL + backend + frontend (Docker)
cd backend-api && mvn spring-boot:run   # or run backend directly
```

## 3. Code standards

Language-level conventions (naming, structure, SOLID, immutability, etc.)
are in [`.claude/rules/programming.md`](../../.claude/rules/programming.md).
Notaire-specific patterns that go beyond generic Java/Spring conventions are
documented individually rather than duplicated here:

| Standard | Reference |
|----------|-----------|
| DTO ↔ Entity mapping | [`302-code-standards/DTO-MAPPING-GUIDE.md`](302-code-standards/DTO-MAPPING-GUIDE.md) |
| Error handling / exception hierarchy | [`302-code-standards/ERROR-HANDLING-STRATEGY-code-standards.md`](302-code-standards/ERROR-HANDLING-STRATEGY-code-standards.md), cross-linked with [ADR-010](../200-architecture/202-ADR/ADR-010-error-handling.md) |
| JPA lazy loading | [`302-code-standards/JPA-LAZY-LOADING-GUIDE.md`](302-code-standards/JPA-LAZY-LOADING-GUIDE.md) |
| Spring `@Transactional` usage | [`302-code-standards/SPRING-TRANSACTION-GUIDE.md`](302-code-standards/SPRING-TRANSACTION-GUIDE.md) |
| Database migrations (Flyway) | [`.claude/rules/database-migrations.md`](../../.claude/rules/database-migrations.md) |
| Frontend design system | [`200-architecture/203-design/FRONTEND-DESIGN-SYSTEM.md`](../200-architecture/203-design/FRONTEND-DESIGN-SYSTEM.md), [`.claude/rules/ui-ux-design.md`](../../.claude/rules/ui-ux-design.md) |

## 4. Testing

Covered in full by [`303-testing/TEST-PLAN.md`](303-testing/TEST-PLAN.md)
(test levels, use-case-oriented test-case catalog, reporting process) and
the day-to-day suite inventory in
[`303-testing/README.md`](303-testing/README.md).

## 5. Architecture migration roadmap

The project is mid-migration from a legacy monolithic Java Swing
application to a three-tier architecture (PostgreSQL + Spring Boot REST API
+ standalone web/Swing clients) — see
[ADR-001](../200-architecture/202-ADR/ADR-001-microservices-architecture.md)
for the full rationale and rejected alternatives (keep-and-improve monolith;
big-bang rewrite). Its 7-phase implementation plan:

| Phase | Description | Status |
|-------|-------------|--------|
| 1 | Setup Spring Boot backend with shared module | ✅ Done — `backend-api` + `notaire-shared` |
| 2 | Migrate business entities and repositories | ✅ Done — `negocio`/`repository` packages |
| 3 | Implement business services | 🔶 Partial — `service` package thin; legacy `jpa` package still does heavy data access (see `CLAUDE.md`'s architecture note) |
| 4 | Create REST endpoints | ✅ Done — 189 endpoints, see [REST-API-ENDPOINT_REGISTRY.md](../200-architecture/203-design/REST-API-ENDPOINT_REGISTRY.md) |
| 5 | Refactor GUI to consume the API | ✅ Done for the new Next.js frontend; the legacy `frontend-swing` client is deprecated and excluded from the root Maven reactor (see `pom.xml`'s reactor-exclusion comment) |
| 6 | Deprecate legacy code | 🔶 Partial — `frontend-swing` deprecated; `jpa` package migration to `repository` ongoing |
| 7 | Monitoring and optimization | ✅ Done — see [ADR-016](../200-architecture/202-ADR/ADR-016-observability-stack.md) |

Related decisions: [ADR-002](../200-architecture/202-ADR/ADR-002-module-structure.md)
(Maven module structure), [ADR-005](../200-architecture/202-ADR/ADR-005-modern-frontend-migration.md)
(Next.js frontend migration).

**New feature work targets `backend-api`/`notaire-shared` and the `frontend/`
Next.js client. Do not build new features in `frontend-swing` (deprecated,
excluded from the build) or the `jpa` package** — both are
migration-targets-for-deprecation, not extension points.

## Navigation

- [← Development](README.md)
- [CONSTITUTION.md](../../CONSTITUTION.md)
- [Test Plan](303-testing/TEST-PLAN.md)
- [Deployment Plan](DEPLOYMENT-PLAN.md)
