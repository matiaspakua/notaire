# Getting Started with Notaire

A 5-minute guide to understanding the Notaire notarial management system.

## What is Notaire?

Notaire is a modern software system for notarial offices in Latin America. It manages:
- **Trámites** (legal procedures) — document preparation, signing, registration
- **Presupuestos** (budgets) — cost estimates for services
- **Documentación** (documentation) — certificates, requirements, archives
- **Usuarios** (users) — notaries, staff, administrative roles

## System Architecture (High Level)

```
┌─────────────────┐
│   Next.js Web   │  ← Modern frontend (browser)
│   (TypeScript)  │
└────────┬────────┘
         │ REST API
┌────────▼────────┐
│  Spring Boot    │  ← Business logic, database access
│    REST API     │
└────────┬────────┘
         │ SQL
┌────────▼────────┐
│   PostgreSQL    │  ← Data storage
│   Database      │
└─────────────────┘
```

**Key principle**: Frontend is stateless (REST client), all logic in backend.

## Key Concepts

### Use Cases (CU-01 to CU-78)
Business processes documented as use cases. Examples:
- **CU-01**: Preparar Presupuesto (Prepare Budget)
- **CU-05**: Preparar escritura (Prepare Document)
- **CU-42**: Informar próximos vencimientos (Inform Upcoming Due Dates)

See [01-business/02-use-cases/](01-business/02-use-cases/) for all 78 use cases.

### Architecture Decisions (ADRs)
Why we chose specific technologies. Key decisions:
- **ADR-001**: Java/Spring Boot for backend (stability, enterprise support)
- **ADR-005**: Next.js for frontend (modern, performant)
- **ADR-010**: PostgreSQL over MySQL (better JSON support, reliability)

See [02-architecture/01-adr/](02-architecture/01-adr/) for all decisions.

## For Different Roles

### 👨‍💻 If you're a **Developer**

1. **Set up your environment** (15 min):
   ```bash
   git clone <repo>
   cd notaire
   bash scripts/start.sh          # Start database + backend
   bash infra/scripts/start-infra.sh    # Optional: monitoring stack
   cd frontend && npm run dev     # Start frontend
   ```
   Details: [03-development/01-setup/README.md](03-development/01-setup/README.md)

2. **Understand the codebase** (30 min):
   - Backend architecture: [02-architecture/02-overview/sad.md](02-architecture/02-overview/sad.md)
   - Project structure: [02-architecture/02-overview/PROJECT-STRUCTURE.md](02-architecture/02-overview/PROJECT-STRUCTURE.md)
   - Coding standards: [03-development/04-code-standards/README.md](03-development/04-code-standards/README.md)

3. **Pick a task and start coding**:
   - See workflow: [.claude/rules/ai-agent-workflow.md](../.claude/rules/ai-agent-workflow.md)
   - Example: "Add a new API endpoint" → See [05-api/REST-API-REFERENCE.md](05-api/REST-API-REFERENCE.md)

### 🏭 If you're an **Operator/SRE**

1. **Understand the deployment** (20 min):
   - Deployment architecture: [04-operations/README.md](04-operations/README.md)
   - How to monitor: [04-operations/01-monitoring/README.md](04-operations/01-monitoring/README.md)
   - How to scale: [04-operations/02-scaling/](04-operations/02-scaling/)

2. **Set up monitoring** (30 min):
   - Prometheus dashboards: [04-operations/01-monitoring/PROMETHEUS.md](04-operations/01-monitoring/PROMETHEUS.md)
   - Alert rules: [04-operations/01-monitoring/ALERTS.md](04-operations/01-monitoring/ALERTS.md)
   - Log aggregation: [04-operations/03-logging/README.md](04-operations/03-logging/README.md)

3. **Troubleshoot production issues**:
   - See [FAQ.md](../FAQ.md) — "How do I debug X?"
   - Check [04-operations/](04-operations/) for runbooks

### 📊 If you're a **Product Manager/Business Analyst**

1. **Understand the business model** (20 min):
   - Business context: [01-business/README.md](01-business/README.md)
   - All use cases: [01-business/02-use-cases/](01-business/02-use-cases/) (78 workflows)
   - Requirements: [01-business/01-requirements/](01-business/01-requirements/) (RF-01 to RF-95, RNF-01 to RNF-24)

2. **Check the roadmap** (10 min):
   - [06-learning/ROADMAP.md](06-learning/ROADMAP.md)
   - Architecture plans: [02-architecture/01-adr/](02-architecture/01-adr/)

3. **Propose changes**:
   - Create a GitHub issue (use [.github/ISSUE_TEMPLATE/issue.md]../.github/ISSUE_TEMPLATE/issue.md))
   - Reference the relevant use case or requirement
   - Follow workflow: [.claude/rules/ai-agent-workflow.md](../.claude/rules/ai-agent-workflow.md)

---

## Common Questions

**Q: Where do I find X?**  
A: See [NAVIGATION.md](NAVIGATION.md) — complete map of all documentation.

**Q: How do I add a new feature?**  
A: Follow the workflow in [.claude/rules/ai-agent-workflow.md](../.claude/rules/ai-agent-workflow.md) — issue → branch → tests → implementation → PR.

**Q: Why did we choose Spring Boot over Node.js?**  
A: See [ADR-001](02-architecture/01-adr/ADR-001-java-and-spring-boot-for-backend.md) — stability and enterprise support.

**Q: How is the database organized?**  
A: See [01-business/04-data-model/](01-business/04-data-model/) — entity definitions and relationships.

**Q: Where are the API docs?**  
A: [05-api/REST-API-REFERENCE.md](05-api/REST-API-REFERENCE.md) — all endpoints documented.

More questions? Check [FAQ.md](FAQ.md).

---

**Next Step**: Pick your role above and dive in! 🚀
