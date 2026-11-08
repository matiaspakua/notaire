/**
 * The engineering artifact chain of Notaire, told stage by stage (#2067).
 * Words only: every count is a key into `ArtifactFacts` (lib/artifact-facts.ts), read at build
 * time, and every path goes through `src()` so workspace/tests/test_pages_tech_docs.py can prove
 * it exists. The nine stages are also the nine drawers of the artifact cabinet figure.
 */
import type { ArtifactFacts } from "./artifact-facts";
import { src } from "./ai-sdlc-content";

export type FactKey = keyof ArtifactFacts;

export type Artifact = {
  /** The artifact's name as the repository uses it. */
  name: string;
  /** The identifier it carries, when it has one (RF #n, CUxx, ADR-nnn…). */
  id?: string;
  what: string;
  why: string;
  /** Method or standard, then the tool that holds or checks it. */
  how: string;
  links: { label: string; href: string }[];
  count?: { key: FactKey; unit: string };
  /** What is not finished, said plainly. Empty when nothing is known to be missing. */
  gap?: string;
};

export type Stage = {
  key: string;
  n: number;
  verb: string;
  title: string;
  /** The drawer label: one or two words. */
  drawer: string;
  /** The count the drawer's card stack is built from. */
  drawerCount: FactKey;
  drawerUnit: string;
  story: string;
  /** Where this stage sits in the AI SDLC (CONSTITUTION §5 steps and gates). */
  sdlc: string;
  artifacts: Artifact[];
};

export const STAGES: Stage[] = [
  {
    key: "analyze",
    n: 1,
    verb: "Analyze",
    title: "What the notary office needs",
    drawer: "Requirements",
    drawerCount: "requirements",
    drawerUnit: "requirements",
    story:
      "Notaire began as a desktop system for a notary office. Its first artifacts were a survey of how the office worked and a requirements specification. The requirements were later moved into one CSV, one row per GitHub Issue, so that every requirement has a number a change can cite.",
    sdlc: "Steps 1–2. Every Issue must cite a requirement (RF #n or RNF) or a use case before any work starts.",
    artifacts: [
      {
        name: "Requirements list",
        id: "RF #n · RNF",
        what: "Every functional and non-functional requirement, one row each, with the Issue that tracks it.",
        why: "A requirement with a number can be cited by a use case, an Issue and a test. A paragraph in a document cannot.",
        how: "Requirements engineering: survey, then SRS, then a catalog. Kept as CSV so a test can read it.",
        links: [{ label: "requerimientos.csv", href: src("docs/100-business/101-requirements/requerimientos.csv") }],
        count: { key: "requirements", unit: "rows" },
      },
      {
        name: "System survey and SRS",
        id: "RS · SRS",
        what: "The original survey of the office and the software requirements specification written from it.",
        why: "They record what the business asked for in its own words, before any design.",
        how: "A structured SRS. Kept as the historical baseline: the live list is the CSV above.",
        links: [
          { label: "RS — Relevamiento del Sistema", href: src("docs/100-business/101-requirements/RS - Relevamiento del Sistema.md") },
          { label: "SRS", href: src("docs/100-business/101-requirements/SRS - Especificacion de Requerimientos.md") },
        ],
        gap: "The SRS still names the desktop-era stack (MySQL). It is marked as a historical baseline; the running stack is on the Technology stack page.",
      },
    ],
  },
  {
    key: "model",
    n: 2,
    verb: "Model",
    title: "Who does what with the system",
    drawer: "Use cases",
    drawerCount: "useCases",
    drawerUnit: "use cases",
    story:
      "Each requirement became one or more use cases: an actor, a goal, a main flow and its alternatives. The use case is the unit every change in Notaire is tied to, from the Issue to the end-to-end test.",
    sdlc: "Step 2, refine the requirement. No use case means writing it first; Gate 1 checks the Issue names one.",
    artifacts: [
      {
        name: "Use cases",
        id: "CUxx",
        what: "One file per use case, from managing a case to the audit log and the AI assistant.",
        why: "It is the shared language between the business, the code and the tests. Every Issue, commit and E2E suite cites one.",
        how: "Use-case modelling (actor, goal, flows). A test keeps every file on the template.",
        links: [{ label: "102-use-cases/", href: src("docs/100-business/102-use-cases") }],
        count: { key: "useCases", unit: "use cases" },
      },
      {
        name: "Actors and the traceability matrix",
        what: "The list of actors, and the matrix from each requirement to the use cases that cover it.",
        why: "It shows backward and forward coverage: a requirement with no use case, or a use case with no requirement, is visible.",
        how: "RF ↔ CU matrix, checked by docs/tests/test_business_docs_traceability.py.",
        links: [
          { label: "103-actors/", href: src("docs/100-business/103-actors") },
          { label: "104-traceability/", href: src("docs/100-business/104-traceability") },
        ],
      },
    ],
  },
  {
    key: "design",
    n: 3,
    verb: "Design",
    title: "How a case moves through the office",
    drawer: "Design",
    drawerCount: "designDocs",
    drawerUnit: "design documents",
    story:
      "The domain was modelled before it was coded: the states a case, a deed and a payment pass through, one sequence per step of a case, the C4 views and the data model. Diagrams started in PlantUML; Mermaid inside Markdown became the canonical language so a diagram lives next to the text that explains it.",
    sdlc: "Steps 4–5, impact analysis and architecture review. A change that moves a state edits the lifecycle model in the same pull request.",
    artifacts: [
      {
        name: "Lifecycle model",
        id: "DOC-CICLO",
        what: "State machines for every domain object, sequences and the C4 views of the product.",
        why: "It is the reference a change checks against: if a status moves, this file moves with it.",
        how: "UML state machines and C4 levels 1–3, in Mermaid.",
        links: [{ label: "CICLO-DE-VIDA.md", href: src("docs/200-architecture/203-design/CICLO-DE-VIDA.md") }],
      },
      {
        name: "Case walkthrough",
        id: "DOC-GESTION",
        what: "One notarial case from start to finish, with one sequence diagram and the real API call per step.",
        why: "It ties the domain story to the endpoints that implement it.",
        how: "Sequence diagrams against the live REST API.",
        links: [{ label: "GESTION.md", href: src("docs/200-architecture/203-design/GESTION.md") }],
      },
      {
        name: "Diagrams",
        what: "Class, state, sequence and use-case diagrams, plus the Mermaid views inside the architecture documents.",
        why: "A picture of the structure catches what prose hides: a missing state, a cycle, a call across a boundary.",
        how: "Mermaid is canonical (ADR-027); the PlantUML sources are kept and checked, not extended.",
        links: [{ label: "204-diagrams/", href: src("docs/200-architecture/204-diagrams") }],
        count: { key: "plantUml", unit: "PlantUML sources" },
      },
      {
        name: "Data model",
        what: "The entity-relationship diagram and the data dictionary for the PostgreSQL schema.",
        why: "The database outlives every screen. Its model must match what Flyway actually created.",
        how: "ERD regenerated from the schema and checked by docs/tests/test_erd_current_schema.py.",
        links: [{ label: "205-data-model/", href: src("docs/200-architecture/205-data-model") }],
      },
    ],
  },
  {
    key: "decide",
    n: 4,
    verb: "Decide",
    title: "The architecture and every decision behind it",
    drawer: "Architecture",
    drawerCount: "adrs",
    drawerUnit: "decision records",
    story:
      "The move from a desktop application to a Spring Boot API and a Next.js client was a chain of decisions: the frontend framework, the authentication model, the container images, the hexagonal architecture. Each one was written as an ADR with its context, options and consequences, and the SAD gathers the whole system in the arc42 structure.",
    sdlc: "Step 5. An architectural change writes an ADR in the same pull request; an accepted ADR is never rewritten, only superseded.",
    artifacts: [
      {
        name: "Software Architecture Document",
        id: "DOC-SAD",
        what: "The system in the twelve arc42 sections: goals, constraints, context, building blocks, runtime, deployment, quality and risks.",
        why: "One place to read the whole architecture before reading any code.",
        how: "arc42 template.",
        links: [{ label: "sad.md", href: src("docs/200-architecture/201-SAD/sad.md") }],
        count: { key: "sadSections", unit: "top-level sections" },
      },
      {
        name: "Architecture decision records",
        id: "ADR-nnn",
        what: "One record per significant decision, with its status: proposed, accepted or superseded.",
        why: "The reason behind a choice is what a later maintainer needs most and finds least.",
        how: "Context, options and trade-offs for each decision, indexed with its status in the folder README.",
        links: [{ label: "202-ADR/", href: src("docs/200-architecture/202-ADR") }],
        count: { key: "adrs", unit: "ADRs" },
      },
    ],
  },
  {
    key: "specify",
    n: 5,
    verb: "Specify",
    title: "A contract for each change, before the code",
    drawer: "Specs",
    drawerCount: "specs",
    drawerUnit: "living specifications",
    story:
      "Once AI agents started writing code, a use case was not precise enough to hand over. Every change now starts as an OpenSpec change: a proposal, a traceability table, delta specifications with Given-When-Then scenarios, a design and a task list. When the change merges, its deltas fold into the living specifications and the change is archived in the same pull request.",
    sdlc: "Step 3, Gate 1. validate-sdlc-plan.sh resolves the Issue live, and openspec validate --strict must pass before a branch carries code.",
    artifacts: [
      {
        name: "Living specifications",
        what: "The current behaviour of each capability, as requirements with scenarios.",
        why: "The scenarios are the acceptance criteria. A test can be traced to one, and a reviewer can check a change against them.",
        how: "OpenSpec on the notaire-sdlc schema; scenarios in Given-When-Then.",
        links: [
          { label: "openspec/specs/", href: src("docs/openspec/specs") },
          { label: "notaire-sdlc schema", href: src("docs/openspec/schemas/notaire-sdlc/schema.yaml") },
        ],
        count: { key: "scenarios", unit: "scenarios" },
      },
      {
        name: "Archived changes",
        what: "Every change that shipped, with its proposal, design, tasks and traceability as they were at merge.",
        why: "It is the decision log at the scale of a single change: why this, why now, what was left out.",
        how: "openspec archive inside the implementing pull request (#1623).",
        links: [{ label: "changes/archive/", href: src("docs/openspec/changes/archive") }],
        count: { key: "archivedChanges", unit: "archived changes" },
      },
    ],
  },
  {
    key: "build",
    n: 6,
    verb: "Build",
    title: "Code that keeps its shape",
    drawer: "Code",
    drawerCount: "migrations",
    drawerUnit: "schema migrations",
    story:
      "The backend is a Spring Boot API organised as ports and adapters; the frontend reaches it only over HTTP. The schema is versioned by Flyway, one migration per change, and the OpenAPI contract is generated from the code and compared with the previous one on every change.",
    sdlc: "Steps 9–12. Production code comes only after its failing test; ArchUnit and ESLint stop a call across a boundary.",
    artifacts: [
      {
        name: "Flyway migrations",
        id: "V{n}__",
        what: "Every change to the PostgreSQL schema, in order, never edited after it ships.",
        why: "Any environment can be rebuilt to the exact schema of any release.",
        how: "Versioned SQL migrations, validated at startup.",
        links: [{ label: "db/migration/", href: src("backend-api/src/main/resources/db/migration") }],
        count: { key: "migrations", unit: "migrations" },
      },
      {
        name: "API contract",
        what: "The OpenAPI document of the REST API and the TypeScript types generated from it.",
        why: "A breaking change to the API fails a check instead of a screen.",
        how: "Code-first OpenAPI with springdoc; breaking-change diff; generated client types.",
        links: [{ label: "openapi.yaml", href: src("backend-api/openapi/openapi.yaml") }],
      },
      {
        name: "Architecture rules",
        what: "Tests that fail when the domain depends on a framework or an adapter skips its port.",
        why: "A boundary that is not tested erodes, especially when many agents write code.",
        how: "Hexagonal architecture (ADR-030), enforced by ArchUnit.",
        links: [{ label: "architecture tests", href: src("backend-api/src/test/java/com/licensis/notaire/architecture") }],
      },
    ],
  },
  {
    key: "verify",
    n: 7,
    verb: "Verify",
    title: "Proof at every level of the pyramid",
    drawer: "Tests",
    drawerCount: "e2eSpecs",
    drawerUnit: "Playwright specs",
    story:
      "Testing follows a master plan: unit tests first, then integration and contract, then the API suite and the browser. Each automated test cites its use case. Tests are written before the code and must be seen failing; coverage may only go up.",
    sdlc: "Steps 8–15, Gates 2 and 3. A test you never saw fail proves nothing; preflight --full is the pull-request proof.",
    artifacts: [
      {
        name: "Master test plan",
        id: "DOC-MTP",
        what: "Scope, strategy, levels, coverage floors, environments, evidence and exit criteria.",
        why: "It decides what is tested where, so the same behaviour is not tested five times or not at all.",
        how: "A master test plan with a fail-fast test pyramid and coverage floors.",
        links: [{ label: "TEST-PLAN.md", href: src("docs/300-development/303-testing/TEST-PLAN.md") }],
      },
      {
        name: "Test cases and the CU → API matrix",
        id: "TC-nnn",
        what: "Logical test cases for the main business flows, and the matrix from each use case to its endpoints.",
        why: "They show which use case a test proves and which endpoint has no test.",
        how: "Four-phase test cases; validate-cu-api-matrix.py checks the matrix.",
        links: [
          { label: "test-cases/", href: src("docs/300-development/303-testing/test-cases") },
          { label: "CU-API-MATRIX.csv", href: src("docs/300-development/303-testing/CU-API-MATRIX.csv") },
        ],
        count: { key: "testCases", unit: "logical test cases" },
        gap: "The logical test cases are examples, not a full catalogue; the executable suites below are the complete record.",
      },
      {
        name: "Unit and integration tests",
        what: "JUnit tests for the domain, the use cases and the adapters, run against a real PostgreSQL in containers.",
        why: "They are the fastest proof and the base of the pyramid.",
        how: "JUnit 5, Testcontainers, JaCoCo with a coverage ratchet.",
        links: [{ label: "backend-api/src/test", href: src("backend-api/src/test") }],
        count: { key: "backendTests", unit: "test classes" },
      },
      {
        name: "API and browser suites",
        id: "TS-nnnn",
        what: "Bruno requests against the running API, and Playwright suites through the browser at three widths.",
        why: "They prove the use case end to end, the way a user and a client meet it.",
        how: "Bruno collections; Playwright with axe accessibility checks.",
        links: [
          { label: "api-test/", href: src("backend-api/api-test") },
          { label: "testing/e2e/tests", href: src("testing/e2e/tests") },
          { label: "E2E-TEST-MAPPING.md", href: src("docs/300-development/303-testing/E2E-TEST-MAPPING.md") },
        ],
        count: { key: "brunoRequests", unit: "Bruno requests" },
      },
    ],
  },
  {
    key: "secure",
    n: 8,
    verb: "Secure",
    title: "A notary's record, protected by design",
    drawer: "Security",
    drawerCount: "securityRequirements",
    drawerUnit: "security requirements",
    story:
      "A notary office holds identity documents, deeds and payments. Security was designed in, not added: threat models for the system and for login and audit, numbered security requirements with their status, and a catalog of security cases that agents run locally before a pull request.",
    sdlc: "Step 17.5. The security case catalog runs in the local pipeline; a change to authentication, a secret or the schema goes through the threat model.",
    artifacts: [
      {
        name: "System threat model",
        what: "Trust boundaries from the browser to the database, STRIDE threats and LINDDUN privacy notes.",
        why: "It says what can go wrong and which control answers it, before an attacker does.",
        how: "STRIDE and LINDDUN, with a structured register a test reads.",
        links: [
          { label: "THREAT-MODEL-SYSTEM.md", href: src("docs/200-architecture/206-security/THREAT-MODEL-SYSTEM.md") },
          { label: "system.stride.yml", href: src("docs/200-architecture/206-security/threat-model/system.stride.yml") },
          { label: "trust-boundaries.puml", href: src("docs/200-architecture/206-security/diagrams/trust-boundaries.puml") },
        ],
        count: { key: "strideEntries", unit: "register entries" },
      },
      {
        name: "Security requirements",
        id: "SR-nn",
        what: "Numbered requirements for login and audit, each marked mitigated, partial or open.",
        why: "A requirement with a status can be checked; a good intention cannot.",
        how: "Threat model for authentication and the audit trail.",
        links: [{ label: "THREAT-MODEL-AUTH-AUDIT.md", href: src("docs/200-architecture/206-security/THREAT-MODEL-AUTH-AUDIT.md") }],
        count: { key: "securityRequirements", unit: "SR identifiers" },
      },
      {
        name: "Security program and tools",
        what: "Which scanner runs locally, on a version tag or on a schedule, and the security cases agents run.",
        why: "It makes the cadence explicit, so a missing scan is a visible gap rather than a surprise.",
        how: "Semgrep, Trivy, CodeQL and ZAP; security/run_gates.py runs the catalog.",
        links: [
          { label: "SECURITY-TOOLS.md", href: src("docs/200-architecture/206-security/SECURITY-TOOLS.md") },
          { label: "CYBERSECURITY-PROGRAM.md", href: src("docs/200-architecture/206-security/CYBERSECURITY-PROGRAM.md") },
          { label: "security/cases/catalog.yml", href: src("security/cases/catalog.yml") },
        ],
        count: { key: "securityCases", unit: "security cases" },
      },
    ],
  },
  {
    key: "operate",
    n: 9,
    verb: "Release and operate",
    title: "From a version tag to a running office",
    drawer: "Operate",
    drawerCount: "workflows",
    drawerUnit: "workflows",
    story:
      "A version tag runs the full product pipeline, publishes signed images with their SBOMs and records the release. Applying a release to a cluster is still a person's decision. Once it runs, metrics, logs and traces feed alerts, and an incident comes back as an Issue at the start of the chain.",
    sdlc: "Steps 21–24, Gates 4 and 5. An Issue closes only after the deploy and its smoke test.",
    artifacts: [
      {
        name: "Pipelines",
        what: "The CI, CD, release, security and process workflows, with what each one blocks.",
        why: "A gate that is written down but not run is not a gate.",
        how: "GitHub Actions; local preflight is the pull-request proof (#2001), product CI runs on a version tag.",
        links: [
          { label: ".github/workflows", href: src(".github/workflows") },
          { label: "CI-PREFLIGHT.md", href: src("docs/300-development/CI-PREFLIGHT.md") },
        ],
        count: { key: "workflows", unit: "workflows" },
      },
      {
        name: "Release and deployment plan",
        what: "How a version is cut, what it contains, and how it reaches each environment.",
        why: "A release that cannot be repeated cannot be rolled back.",
        how: "Release Please with semantic versions; signed images on GHCR; Compose and Kustomize.",
        links: [
          { label: "RELEASE.md", href: src("docs/300-development/RELEASE.md") },
          { label: "DEPLOYMENT-PLAN.md", href: src("docs/300-development/DEPLOYMENT-PLAN.md") },
          { label: "209-deployment/", href: src("docs/200-architecture/209-deployment/README.md") },
        ],
        gap: "Applying a release to the cluster is manual; there is no automatic deploy yet.",
      },
      {
        name: "Monitoring, runbooks and manuals",
        what: "The observability stack and its alerts, operational runbooks, and the installation, system and user manuals.",
        why: "The people who run and use the system need their own documents, not the developers'.",
        how: "Prometheus, Grafana, Loki and OpenTelemetry; runbooks per procedure; manuals per audience.",
        links: [
          { label: "207-monitoring/", href: src("docs/200-architecture/207-monitoring/README.md") },
          { label: "HTTPS-TLS-RUNBOOK.md", href: src("docs/200-architecture/206-security/HTTPS-TLS-RUNBOOK.md") },
          { label: "105-manuals/", href: src("docs/100-business/105-manuals") },
        ],
      },
    ],
  },
];

/** The reading path on the Technical docs overview: from what Notaire is, down to how it was made. */
export const READING_PATH: { href: string; title: string; question: string }[] = [
  { href: "/product/", title: "Product", question: "What does Notaire do, and for whom?" },
  { href: "/product/story/", title: "Story", question: "Where did it come from?" },
  { href: "/docs/artifacts/", title: "Engineering artifacts", question: "Which documents define it, and how do they connect?" },
  { href: "/docs/architecture/", title: "Architecture", question: "How is it structured, and why?" },
  { href: "/ai-sdlc/lifecycle/", title: "AI SDLC lifecycle", question: "How does one change go from Issue to production?" },
  { href: "/docs/testing/", title: "Testing", question: "How is each change proved?" },
  { href: "/security/", title: "Security", question: "How is the record protected?" },
  { href: "/docs/releases/", title: "Releases", question: "What shipped, and when?" },
];
