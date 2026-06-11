# Complete Documentation Navigation Map

Use this guide to find exactly what you need.

## By Task / Use Case

### Business & Requirements
- **I need to understand what the system does** → [01-business/README.md](01-business/README.md)
- **I need to understand a specific business process** → [01-business/02-use-cases/](01-business/02-use-cases/) (search for CU-XX)
- **I need the functional requirements** → [01-business/01-requirements/01_RF - Requerimientos Funcionales/](01-business/01-requirements/01_RF%20-%20Requerimientos%20Funcionales/)
- **I need to understand the data model** → [01-business/04-data-model/04_MD - Modelo de Datos/Diccionario de Datos.md](01-business/04-data-model/04_MD%20-%20Modelo%20de%20Datos/Diccionario%20de%20Datos.md)
- **I need the user manual** → [01-business/05-manuals/C_Manual de Usuario/Manual de Usuario Notaire.doc.md](01-business/05-manuals/)

### System Architecture & Design
- **I need high-level system architecture** → [02-architecture/02-overview/sad.md](02-architecture/02-overview/sad.md)
- **I need to understand why we chose technology X** → [02-architecture/01-adr/](02-architecture/01-adr/) (read relevant ADRs)
- **I need the project structure** → [02-architecture/02-overview/PROJECT-STRUCTURE.md](02-architecture/02-overview/PROJECT-STRUCTURE.md)
- **I need design patterns** → [02-architecture/03-design/](02-architecture/03-design/)
- **I need ER diagram** → [02-architecture/02-overview/ER-DIAGRAM.md](02-architecture/02-overview/ER-DIAGRAM.md)

### Development & Setup
- **I need to set up the development environment** → [03-development/01-setup/README.md](03-development/01-setup/README.md)
- **I need coding standards** → [03-development/04-code-standards/README.md](03-development/04-code-standards/README.md)
- **I need to understand the testing strategy** → [03-development/03-testing/TEST_STRATEGY.md](03-development/03-testing/TEST_STRATEGY.md)
- **I need to write E2E tests** → [03-development/03-testing/E2E-TEST-PLAN.md](03-development/03-testing/E2E-TEST-PLAN.md)
- **I need to write unit tests** → [03-development/04-code-standards/TESTING.md](03-development/04-code-standards/TESTING.md)
- **I need to add a new API endpoint** → [05-api/REST-API-REFERENCE.md](05-api/REST-API-REFERENCE.md)
- **I need the development workflow** → [.claude/rules/ai-agent-workflow.md](../.claude/rules/ai-agent-workflow.md)

### Operations & Deployment
- **I need to deploy the system** → [04-operations/README.md](04-operations/README.md)
- **I need to monitor the system** → [04-operations/01-monitoring/README.md](04-operations/01-monitoring/README.md)
- **I need to set up alerts** → [04-operations/01-monitoring/ALERTS.md](04-operations/01-monitoring/ALERTS.md)
- **I need to configure logging** → [04-operations/03-logging/README.md](04-operations/03-logging/README.md)
- **I need security guidelines** → [04-operations/03-security/README.md](04-operations/03-security/README.md)

### API Documentation
- **I need REST API reference** → [05-api/REST-API-REFERENCE.md](05-api/REST-API-REFERENCE.md)
- **I need API examples** → [05-api/](05-api/) (browse examples)
- **I need error handling details** → [03-development/04-code-standards/ERROR-HANDLING-STRATEGY.md](03-development/04-code-standards/ERROR-HANDLING-STRATEGY.md)

### Learning & Onboarding
- **I'm new to the project** → [GETTING-STARTED.md](GETTING-STARTED.md)
- **I need a quick reference** → [06-learning/QUICK-REFERENCE.md](06-learning/QUICK-REFERENCE.md)
- **I need the roadmap** → [06-learning/ROADMAP.md](06-learning/ROADMAP.md)

---

## By Directory

### 📋 01-business/ — Business Requirements & Use Cases
```
01-business/
├── 00-FUNCTIONAL-BASELINE.md      ← Overview of business model
├── README.md                       ← Start here for business context
├── 01-requirements/
│   ├── 01_RF - Requerimientos Funcionales/     ← RF-01 to RF-95
│   ├── 02_RNF - Requerimientos No Funcionales/ ← RNF-01 to RNF-24
│   └── 03_QA - Asuntos de Calidad/
├── 02-use-cases/
│   ├── 03_CU - Casos de Uso/  ← CU-01 to CU-78 (all 78 business workflows)
│   └── README.md
├── 03-actors/                 ← Personas, roles, stakeholders
├── 04-data-model/
│   ├── 04_MD - Modelo de Datos/   ← Entity definitions
│   └── ER diagrams
├── 05-manuals/
│   ├── A_Manual de Sistema/       ← For system administrators
│   ├── B_Manual Técnico/          ← For developers
│   └── C_Manual de Usuario/       ← For end users (notaries, staff)
└── 06-scope/                  ← What's in/out of scope
```

**Use for**: Business analysts, product managers, end users who need to understand workflows.

### 🏗️ 02-architecture/ — System Design & Decisions
```
02-architecture/
├── 01-adr/                    ← Architecture Decision Records
│   ├── ADR-001-java-and-spring-boot-for-backend.md
│   ├── ADR-005-modern-frontend-migration.md
│   └── [11 total ADRs]
├── 02-overview/
│   ├── README.md
│   ├── sad.md                 ← Software Architecture Document (START HERE)
│   ├── PROJECT-STRUCTURE.md   ← Directory layout explanation
│   ├── ER-DIAGRAM.md          ← Database schema
│   └── DEPLOYMENT.md          ← How it runs in production
├── 03-design/
│   ├── DESIGN-SYSTEM.md       ← Frontend design tokens & patterns
│   ├── DATABASE-DESIGN.md     ← Schema design rationale
│   └── API-DESIGN.md          ← REST API patterns
└── 04-patterns/               ← (Placeholder for design patterns documentation)
```

**Use for**: Architects, senior engineers, tech leads.

### 👨‍💻 03-development/ — Implementation, Testing, CI/CD
```
03-development/
├── 01-setup/
│   ├── README.md              ← Environment setup guide
│   └── Docker-setup.md        ← Docker-specific setup
├── 02-build/                  ← Build configuration
│   └── Maven-guide.md
├── 03-testing/
│   ├── TEST_STRATEGY.md       ← Overall testing approach
│   ├── E2E-TEST-PLAN.md       ← Playwright E2E tests
│   ├── PLAYWRIGHT-QUICKSTART.md ← Getting started with Playwright
│   ├── GAP-ANALYSIS.md        ← E2E test coverage analysis
│   └── PROGRESS-REPORT.md     ← E2E test progress
├── 04-code-standards/
│   ├── README.md              ← Overview of standards
│   ├── CODING-STANDARDS.md    ← Java & TypeScript conventions
│   ├── TESTING.md             ← How to write tests
│   ├── DOCUMENTATION.md       ← How to document code
│   ├── ERROR-HANDLING-STRATEGY.md ← Error handling patterns
│   └── DATABASE-MIGRATIONS.md ← Flyway migrations guide
└── README.md                  ← Development overview
```

**Use for**: Developers, QA engineers.

### 🔧 04-operations/ — Monitoring, Logging, Security, Deployment
```
04-operations/
├── 01-monitoring/
│   ├── README.md              ← Monitoring overview
│   ├── PROMETHEUS.md          ← Prometheus configuration
│   └── ALERTS.md              ← Alert rules & escalation
├── 02-scaling/                ← Kubernetes & scaling strategies
├── 03-logging/
│   ├── README.md              ← Logging architecture
│   ├── STRUCTURED-LOGGING.md
│   └── LOG-QUERIES.md         ← Loki query examples
├── 03-security/
│   ├── README.md              ← Security overview
│   ├── AUTHENTICATION.md      ← Auth implementation
│   ├── OWASP-TOP-10.md        ← Security checklist
│   └── SECRETS-MANAGEMENT.md  ← How to handle credentials
├── 04-incident-response/      ← Runbooks & procedures
└── 05-disaster-recovery/      ← Backup & recovery procedures
```

**Use for**: Operators, SREs, DevOps engineers, security engineers.

### 📡 05-api/ — REST API Specifications
```
05-api/
├── README.md                     ← API overview
├── REST-API-REFERENCE.md         ← All endpoints documented
├── 01-overview/                  ← (Will contain overview docs)
├── 02-endpoints/                 ← (Will contain endpoint schemas)
├── 03-schemas/                   ← (Will contain data type definitions)
└── [Example collections & tests]
```

**Use for**: Backend developers, API consumers, integration partners.

### 📚 06-learning/ — Quick References, Onboarding, Roadmap
```
06-learning/
├── GETTING-STARTED.md        ← 5-minute intro (START HERE for new people)
├── QUICK-REFERENCE.md        ← Cheat sheets & quick answers
├── ROADMAP.md                ← Product roadmap & timeline
├── 01-onboarding/            ← Detailed onboarding materials
└── 02-learning-paths/        ← Structured learning by role
```

**Use for**: All team members, especially new contributors.

### 📦 archive/ — Deprecated & Historical Documentation
```
archive/
├── README.md                    ← Guide to archived content
├── e2e-swing/                   ← Swing GUI E2E testing (deprecated)
├── outdated-plans/              ← Old plans no longer active
│   └── FRONTEND-GAPS-AND-PLAN.md
└── [Other deprecated content]
```

**Use for**: Historical reference only. Don't rely on these for current work.

---

## By Audience

### 👨‍💻 **Backend Developers**
1. Start: [GETTING-STARTED.md](GETTING-STARTED.md)
2. Setup: [03-development/01-setup/README.md](03-development/01-setup/README.md)
3. Learn: [02-architecture/02-overview/sad.md](02-architecture/02-overview/sad.md)
4. Code: [03-development/04-code-standards/README.md](03-development/04-code-standards/README.md)
5. Test: [03-development/03-testing/TEST_STRATEGY.md](03-development/03-testing/TEST_STRATEGY.md)
6. APIs: [05-api/REST-API-REFERENCE.md](05-api/REST-API-REFERENCE.md)

### 🎨 **Frontend Developers**
1. Start: [GETTING-STARTED.md](GETTING-STARTED.md)
2. Setup: [03-development/01-setup/README.md](03-development/01-setup/README.md)
3. Design: [02-architecture/03-design/DESIGN-SYSTEM.md](02-architecture/03-design/DESIGN-SYSTEM.md)
4. Code: [03-development/04-code-standards/README.md](03-development/04-code-standards/README.md)
5. Test: [03-development/03-testing/E2E-TEST-PLAN.md](03-development/03-testing/E2E-TEST-PLAN.md)

### 🏭 **Operations / SRE**
1. Start: [GETTING-STARTED.md](GETTING-STARTED.md)
2. Architecture: [02-architecture/02-overview/sad.md](02-architecture/02-overview/sad.md)
3. Monitoring: [04-operations/01-monitoring/README.md](04-operations/01-monitoring/README.md)
4. Security: [04-operations/03-security/README.md](04-operations/03-security/README.md)
5. Logging: [04-operations/03-logging/README.md](04-operations/03-logging/README.md)

### 📊 **Product Manager / Business Analyst**
1. Start: [01-business/README.md](01-business/README.md)
2. Use Cases: [01-business/02-use-cases/](01-business/02-use-cases/)
3. Requirements: [01-business/01-requirements/](01-business/01-requirements/)
4. Roadmap: [06-learning/ROADMAP.md](06-learning/ROADMAP.md)
5. Proposals: [.claude/rules/ai-agent-workflow.md](../.claude/rules/ai-agent-workflow.md)

### 🏢 **Executive / Stakeholder**
1. Overview: [02-architecture/02-overview/sad.md](02-architecture/02-overview/sad.md) (sections 1-3)
2. Roadmap: [06-learning/ROADMAP.md](06-learning/ROADMAP.md)
3. Use Cases: [01-business/02-use-cases/](01-business/02-use-cases/) (search for relevant ones)

---

## Search Tips

| If you're looking for... | Try searching in... |
|---|---|
| Specific REST endpoint | [05-api/REST-API-REFERENCE.md](05-api/REST-API-REFERENCE.md) |
| A use case (CU-XX) | [01-business/02-use-cases/](01-business/02-use-cases/) |
| A functional requirement (RF-XX) | [01-business/01-requirements/01_RF/](01-business/01-requirements/01_RF%20-%20Requerimientos%20Funcionales/) |
| A non-functional requirement (RNF-XX) | [01-business/01-requirements/02_RNF/](01-business/01-requirements/02_RNF%20-%20Requerimientos%20No%20Funcionales/) |
| Entity definitions | [01-business/04-data-model/Diccionario de Datos.md](01-business/04-data-model/04_MD%20-%20Modelo%20de%20Datos/Diccionario%20de%20Datos.md) |
| Architecture decisions | [02-architecture/01-adr/](02-architecture/01-adr/) |
| Coding standards | [03-development/04-code-standards/](03-development/04-code-standards/) |
| How to test something | [03-development/03-testing/](03-development/03-testing/) |
| How to deploy | [04-operations/](04-operations/) |
| Design patterns | [02-architecture/03-design/](02-architecture/03-design/) |

---

## Common Navigation Scenarios

### Scenario: "I need to add a new endpoint for trámites"
1. [01-business/02-use-cases/](01-business/02-use-cases/) — Find relevant use case (CU-XX)
2. [01-business/04-data-model/](01-business/04-data-model/) — Understand data structures
3. [05-api/REST-API-REFERENCE.md](05-api/REST-API-REFERENCE.md) — See existing endpoint patterns
4. [03-development/04-code-standards/](03-development/04-code-standards/) — Follow coding standards
5. [03-development/03-testing/](03-development/03-testing/) — Write tests
6. [.claude/rules/ai-agent-workflow.md](../.claude/rules/ai-agent-workflow.md) — Follow workflow

### Scenario: "The system is slow, where do I look?"
1. [04-operations/01-monitoring/](04-operations/01-monitoring/) — Check Grafana dashboards
2. [02-architecture/03-design/DATABASE-DESIGN.md](02-architecture/03-design/DATABASE-DESIGN.md) — Understand schema
3. [03-development/04-code-standards/](03-development/04-code-standards/) — Check for N+1 queries
4. [04-operations/03-logging/LOG-QUERIES.md](04-operations/03-logging/LOG-QUERIES.md) — Query logs for bottlenecks

### Scenario: "How does feature X work?"
1. [01-business/02-use-cases/](01-business/02-use-cases/) — Find the use case
2. [02-architecture/02-overview/sad.md](02-architecture/02-overview/sad.md) — Understand system flow
3. [05-api/REST-API-REFERENCE.md](05-api/REST-API-REFERENCE.md) — Find relevant endpoints
4. [02-architecture/03-design/](02-architecture/03-design/) — Check design details

---

## Getting Help

- **Quick answers**: See [FAQ.md](FAQ.md)
- **Lost in docs**: Come back to this file
- **Not in docs yet**: Open a GitHub issue describing what you need

---

**Last updated**: June 11, 2026
