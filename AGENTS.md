# AGENTS.md — Notaire Agent Reference

Coding agents for the Notaire project. All agents enforce the mandatory development workflow defined in `AUDITORIA.md` and `.claude/rules/ai-agent-workflow.md`.

---

## Available Agents

| Agent | File | Role |
|-------|------|------|
| **efficiency_config_agent** | `.claude/agents/efficiency_config_agent.md` | **Primary coding agent.** Implementation, debugging, refactoring. Enforces AUDITORIA.md workflow. |
| **Code Reviewers** | `.claude/agents/code-reviewer.md` | Code review: correctness, security, KIS/SRP, workflow compliance. |
| **java-architect** | `.claude/agents/java-architect.md` | Java/Spring Boot architecture decisions, package structure, migration from legacy `jpa`. |
| **devops-engineer** | `.claude/agents/devops-engineer.md` | Docker, CI/CD, observability (Prometheus/Grafana/Loki), scripts. |
| **Security Auditors** | `.claude/agents/security-auditor.md` | OWASP Top 10, auth/authz, dependency CVEs, configuration security. |
| **Sync Issues and Code** | `.claude/agents/sync_issues_and_code.md` | GitHub issue ↔ code sync: Use Case validation, IN PROGRESS state, PR linkage. |

---

## Mandatory Development Workflow (all agents follow this)

```
0. Issue + Use Case (Caso de Uso) — MANDATORY, no exceptions
1. Branch from updated main: <type>/<issue-number>_<description>
1.5 Move issue to IN PROGRESS
2. TDD — write failing tests first (watch them fail)
3. Implement (make tests pass)
4. Refactor — KIS, SRP, remove dead/duplicate code
5. Run ALL tests: unit + integration + E2E Playwright
6. Commit (Conventional Commits + Closes #issue)
7. Push
8. Update documentation (archive outdated docs → docs/archive/)
9. PR + Close issue
```

Full details: `.claude/rules/ai-agent-workflow.md`

---

## Project Quick Reference

### Build

```bash
mvn clean install                       # all modules
mvn clean install -pl backend-api -am   # backend + shared
```

### Run

```bash
bash scripts/start.sh                   # DB + backend (Docker)
bash scripts/stop.sh
bash scripts/logs.sh
bash scripts/start-all.sh               # app + observability infra
```

### Test

```bash
mvn test -pl backend-api                          # unit + integration
mvn test -pl backend-api -Dtest=ClassName         # single class
mvn jacoco:check -pl backend-api                  # coverage ≥ 80%
mvn verify -pl backend-api                        # all quality checks
bash integration-test/scripts/test.sh                              # HTTP integration (API running)
cd frontend && npx playwright test                # E2E
```

### Code Quality

```bash
mvn checkstyle:check -pl backend-api
mvn spotbugs:check -pl backend-api -DskipSpotBugs=false
bash infra/scripts/run-sonar.sh                         # SonarQube
```

### Key URLs (local)

| Service | URL |
|---------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Grafana | http://localhost:3001 |
| Prometheus | http://localhost:9090 |
| pgAdmin | http://localhost:5050 |
| SonarQube | http://localhost:9000 |
| Homer | http://localhost:8888 |

---

## Architecture Summary

**Backend** (`backend-api`): Spring Boot 4.1.0, Java 21, PostgreSQL 16.

Package root: `com.licensis.notaire`

| Package | Role |
|---------|------|
| `api` | REST controllers |
| `service` | Business logic |
| `repository` | Spring Data JPA — use for new code |
| `negocio` | JPA entities |
| `jpa` | Legacy data-access — being replaced |
| `config` | Spring configuration |

**Frontend** (`frontend`): Next.js 16, React 19, TypeScript, Tailwind CSS.
- Design system: `src/theme/tokens.ts` (single source of truth).
- Forms: `FormContainer → FormSection → FormField → FormActions`.

**Database**: Flyway is the single source of truth. Docker starts PostgreSQL empty and Flyway applies V1→V11+ sequentially. The old `init-db/` scripts are archived at `docs/archive/init-db/`.

---

## Rules Reference

| Rule | File |
|------|------|
| Development Workflow | `.claude/rules/ai-agent-workflow.md` |
| General | `.claude/rules/general.md` |
| Programming | `.claude/rules/programming.md` |
| Code Quality | `.claude/rules/code-quality.md` |
| UI/UX Design | `.claude/rules/ui-ux-design.md` |
| DB Migrations | `.claude/rules/database-migrations.md` |
| Refactoring | `.claude/rules/refactoring.md` |

---

## Violations (all agents enforce these)

- ❌ Code without an associated issue + Use Case
- ❌ Implement before writing failing tests (TDD)
- ❌ Commit directly to `main`
- ❌ Skip or `@Disabled` tests without justification
- ❌ Leave dead code or duplicate code
- ❌ Leave documentation out of date
- ❌ Hardcode credentials or secrets
- ❌ Push without creating PR
- ❌ Skip E2E Playwright tests for UI changes
- ❌ Schema change without creating a new Flyway migration

## Imported Claude Cowork project instructions

java-based modernization project
