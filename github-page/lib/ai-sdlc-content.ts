/**
 * AI Factory section (#1934): the AI SDLC that builds Notaire, kept apart from the product docs.
 *
 * Every repository path goes through src(), skill(), agent() or workflow(), so
 * workspace/tests/test_pages_ai_sdlc.py can prove each citation still exists.
 * Counts never live here: they are read from the repository at build time
 * (lib/ai-sdlc-facts.ts).
 */

export const REPO = "https://github.com/matiaspakua/notaire";

export function src(path: string): string {
  return `${REPO}/${path.endsWith("/") ? "tree" : "blob"}/main/${path}`;
}
export function skill(name: string): string {
  return `${REPO}/blob/main/.claude/skills/${name}/SKILL.md`;
}
export function agent(name: string): string {
  return `${REPO}/blob/main/.claude/agents/${name}.md`;
}
export function workflow(file: string): string {
  return `${REPO}/blob/main/.github/workflows/${file}`;
}
export function issue(n: number): string {
  return `${REPO}/issues/${n}`;
}

export type Source = { label: string; href: string };

export const FACTORY_NAV = [
  { href: "/ai-sdlc/", label: "Overview" },
  { href: "/ai-sdlc/case-study/", label: "Case study" },
  { href: "/ai-sdlc/framework/", label: "Framework" },
  { href: "/ai-sdlc/lifecycle/", label: "Lifecycle" },
  { href: "/ai-sdlc/guardrails/", label: "Guardrails" },
  { href: "/ai-sdlc/quality-gates/", label: "Quality gates" },
  { href: "/ai-sdlc/openspec/", label: "OpenSpec" },
  { href: "/ai-sdlc/agents/", label: "Agents & skills" },
  { href: "/ai-sdlc/governance/", label: "Governance" },
  { href: "/ai-sdlc/lessons/", label: "Lessons" },
  { href: "/ai-sdlc/playbook/", label: "Playbook" },
] as const;

/* ───────────────────────────── Diagrams (Mermaid, ADR-027) ───────────────────────────── */

export const FACTORY_VS_PRODUCT = `
flowchart TB
  ISSUE(["GitHub Issue + Use Case"])
  subgraph factory["AI FACTORY — how Notaire is built"]
    direction LR
    POL["Policy<br/>CONSTITUTION.md"] --> SPEC["Spec engine<br/>OpenSpec notaire-sdlc"] --> CREW["Agent crew<br/>.claude/agents + skills"] --> GATES["Gates<br/>preflight · CI · ruleset"]
  end
  subgraph product["NOTAIRE — the product"]
    direction LR
    FE["frontend<br/>Next.js"] --> BE["backend-api<br/>Spring Boot"] --> DB[("PostgreSQL 16<br/>Flyway")]
  end
  ISSUE --> factory
  factory ==>|"only a reviewed, green PR"| product
  product -.->|"incidents · findings · metrics become Issues"| ISSUE
  classDef fac fill:#ede9fe,stroke:#6d28d9,color:#2e1065
  classDef prod fill:#e0f2fe,stroke:#0369a1,color:#082f49
  class POL,SPEC,CREW,GATES fac
  class FE,BE,DB prod
`;

export const LIFECYCLE_FLOW = `
flowchart TB
  A(["1 · Issue + Use Case"]) --> B["2 · Refine requirement"]
  B --> C["3 · OpenSpec specification"]
  C --> G1{{"GATE 1<br/>Issue · Spec · AC"}}
  G1 --> D["4 · Impact analysis<br/>5 · Architecture review / ADR"]
  D --> E["6 · Branch type/N_desc<br/>7 · Acceptance criteria"]
  E --> F["8 · Design test cases"]
  F --> G2{{"GATE 2<br/>tests written and seen failing"}}
  G2 --> H["9–10 · Failing unit + integration tests"]
  H --> I["11 · Implement · 12 · Update tests"]
  I --> J["13–15 · Suite · regression · Playwright"]
  J --> K["16 · Permanent documentation"]
  K --> G3{{"GATE 3<br/>green suite · coverage · docs"}}
  G3 --> L["17 · Atomic commits<br/>17.5 · run_pipeline.sh"]
  L --> M["18 · Pull request<br/>19 · Green CI"]
  M --> G4{{"GATE 4<br/>heavy CI · review · no conflicts"}}
  G4 --> N["21 · Merge via PR"]
  N --> O["22 · Deploy (CD → GHCR)"]
  O --> G5{{"GATE 5<br/>smoke test · issue closed"}}
  G5 --> P(["24 · Close issue · archive spec"])
  classDef gate fill:#fde68a,stroke:#b45309,color:#451a03,font-weight:bold
  class G1,G2,G3,G4,G5 gate
`;

export const GUARDRAIL_LAYERS = `
flowchart TB
  H["9 · Human Owner<br/>approves specs · grants exceptions · merges policy"]
  R["8 · Repository rules<br/>protect-main: PR only · 5 required checks · no force-push"]
  CQ["7 · CI quality & security<br/>unit · integration · coverage · E2E · CodeQL · Trivy · ZAP"]
  CP["6 · CI process checks<br/>commits · TDD evidence · OpenSpec · traceability · board"]
  LG["5 · Local gates<br/>preflight.sh pre-push · run_pipeline.sh · validate-sdlc-plan.sh"]
  HK["4 · Agent-side hooks & harness<br/>block push to main · scope/ref guards · frozen TEST_CMD"]
  SK["3 · Skills & agents catalog<br/>.claude/skills · .claude/agents · fleet manifest"]
  CX["2 · Context injection<br/>openspec config.yaml · schema notaire-sdlc · 8k-token card"]
  P["1 · Policy<br/>CONSTITUTION.md — highest authority"]
  P --> CX --> SK --> HK --> LG --> CP --> CQ --> R --> H
  classDef human fill:#fee2e2,stroke:#b91c1c,color:#450a0a
  classDef machine fill:#e0f2fe,stroke:#0369a1,color:#082f49
  classDef policy fill:#ede9fe,stroke:#6d28d9,color:#2e1065
  class H human
  class R,CQ,CP,LG,HK machine
  class P,CX,SK policy
`;

export const QUALITY_GATE_PIPELINE = `
flowchart TB
  subgraph local["On the agent's machine"]
    direction LR
    T1["validate-sdlc-plan.sh<br/>live Issue check"] --> T2["unit + integration<br/>TDD red → green"] --> T3["preflight.sh<br/>pre-push hook"] --> T4["run_pipeline.sh<br/>stack · security cases · dashboard"]
  end
  subgraph light["Light CI — fast feedback"]
    direction LR
    L1["PR Validation<br/>branch · plan · lint"]
    L2["SDLC Process Checks"]
    L3["Frontend CI<br/>tsc · ESLint · Vitest · build"]
    L4["OpenAPI Contract<br/>oasdiff"]
    L1 ~~~ L2 ~~~ L3 ~~~ L4
  end
  subgraph heavy["Heavy CI — the merge gate"]
    direction LR
    H1["CI: Unit · Integration<br/>Coverage Gate (mvn verify)"]
    H2["Security Scan<br/>secrets · Semgrep · Trivy"]
    H3["Playwright E2E<br/>Bruno API + 3 UI shards"]
    H4["CodeQL"]
    H1 ~~~ H2 ~~~ H3 ~~~ H4
  end
  MERGE{{"check-heavy-ci.sh = 0<br/>+ Gate 4 review → merge"}}
  subgraph after["After merge on main"]
    direction LR
    A1["CD: sign + publish images<br/>SBOM on tags"] --> A2["Release Please<br/>semver + CHANGELOG"]
    A1 --> A3["Pages deploy"]
    A4["Weekly: ZAP · k6 · backup-restore"]
    A3 ~~~ A4
  end
  local --> light --> heavy --> MERGE --> after
  classDef gate fill:#fde68a,stroke:#b45309,color:#451a03
  class MERGE gate
`;

export const ROLES_CHART = `
flowchart TB
  OWNER["👤 Owner (human)<br/>approves specs · grants sdlc-exception<br/>merges Constitution & harness PRs"]
  FOREMAN["🧭 Foreman agent<br/>picks issue · runs gates · reviews<br/>merges Worker PRs under #1624"]
  subgraph crew["Specialist Workers"]
    direction LR
    PL["openspec-planner"]
    BI["backend-implementer"]
    FD["frontend-design"]
    QA["testing-qa"]
    DO["devops-engineer"]
    SA["security-auditor"]
    PM["project-manager"]
  end
  CR["code-reviewer<br/>Gate 4"]
  SY["sync_issues_and_code<br/>issue/board hygiene"]
  OWNER -->|"Issues, priorities, decisions"| FOREMAN
  FOREMAN -->|"brief: issue · UC · surface · AC"| crew
  crew -->|"result + exit-code evidence"| FOREMAN
  FOREMAN --> CR --> FOREMAN
  FOREMAN --> SY
  FOREMAN -->|"blocked / Owner-only step"| OWNER
`;

export const OPENSPEC_FLOW = `
flowchart LR
  X["/opsx:explore<br/>findings report"] --> T["openspec-triage<br/>real, estimated Issues + CU"]
  T --> V{"validate-sdlc-plan.sh<br/>Issue exists & open?"}
  V -- "no / invented #" --> STOP(["Gate 1 fails"])
  V -- "yes" --> P["/opsx:propose<br/>seed-openspec-change.sh"]
  P --> ART["proposal → traceability → specs → design → tasks"]
  ART --> AP["/opsx:apply<br/>TDD per tasks.md groups 3–8"]
  AP --> AR["openspec archive --yes<br/>in the same PR (#1623)"]
  AR --> SPECS[("docs/openspec/specs<br/>living capability specs")]
  SPECS -.->|"next change reads current behaviour"| X
`;

export const ISSUE_STATE_MACHINE = `
stateDiagram-v2
  [*] --> New
  New --> Triaged: type + priority + area
  Triaged --> Ready: status ready
  Ready --> InProgress: claim-issue.py<br/>status in-progress
  InProgress --> InReview: PR opened<br/>status in-review
  InReview --> InProgress: review notes / red CI
  InProgress --> Blocked: status blocked or needs-info
  Blocked --> InProgress: unblocked
  InReview --> Done: merged + smoke<br/>issue CLOSED
  Done --> [*]
  note right of Done
    CLOSED means board Done
    and no WIP labels (#1550)
  end note
`;

export const PR_SEQUENCE = `
sequenceDiagram
  autonumber
  participant W as Worker agent
  participant GH as GitHub
  participant CI as Actions CI
  participant F as Foreman
  participant O as Owner
  W->>GH: push branch type/N_desc (continuous publication #1681)
  W->>GH: comment on Issue: stage + what remains
  W->>GH: open PR "[#N] type(scope): …" with Closes #N
  GH->>CI: light + heavy workflows
  CI-->>W: red job logs
  W->>GH: fix commits (merge main in, never rebase)
  CI-->>F: all required checks green
  F->>F: check-heavy-ci.sh N = 0 · archive hygiene · board coherence
  F->>GH: Gate 4 review notes
  alt Worker PR under Foreman rule
    F->>GH: merge
  else Constitution / sdlc-exception / harness PR
    O->>GH: merge
  end
  GH->>CI: CD + Pages + Release Please on main
  F->>GH: smoke evidence, close Issue, board Done
`;

/* ───────────────────────────── Lifecycle ───────────────────────────── */

export type Step = { n: string; title: string; gate?: number; what: string; check?: string };

export const LIFECYCLE_PHASES: { phase: string; tone: string; steps: Step[] }[] = [
  {
    phase: "Plan",
    tone: "violet",
    steps: [
      { n: "1", title: "Issue", what: "One GitHub Issue per change, labeled and linked to a Use Case (CU-XX), RF-XX or RNF-XX, with acceptance criteria.", check: ".github/ISSUE_TEMPLATE/issue.md" },
      { n: "2", title: "Refine the requirement", what: "Confirm the Use Case is accurate; create or update it first if not." },
      { n: "3", title: "Specification", gate: 1, what: "An OpenSpec change on the notaire-sdlc schema describes only this change: proposal, traceability, delta specs, design, tasks.", check: "openspec validate <change> --strict · validate-sdlc-plan.sh" },
      { n: "4", title: "Impact analysis", what: "Affected modules from workspace/modules.yaml, entities, endpoints, schema, tests and docs.", check: "python3 workspace/modules.py affected <path>" },
      { n: "5", title: "Architecture review", what: "Follow the existing architecture (hexagonal, ADR-030); write an ADR when the change is architectural." },
      { n: "6", title: "Branch", what: "From an updated main: <type>/<issue>_<description>. Push it at once (continuous publication).", check: "Branch Naming Convention Check" },
      { n: "7", title: "Acceptance criteria", what: "Given-When-Then scenarios in the delta spec are the acceptance criteria." },
    ],
  },
  {
    phase: "Prove",
    tone: "cyan",
    steps: [
      { n: "8", title: "Design test cases", gate: 2, what: "Happy path, edge cases and error paths at unit, integration and E2E level." },
      { n: "9–10", title: "Write failing tests", what: "Unit and integration tests first, and watch them fail. A test you never saw fail proves nothing (P1).", check: "check-tdd-evidence.sh: production code needs test changes" },
      { n: "11", title: "Implement", what: "The minimum code that makes the tests pass, following the conventions." },
      { n: "12", title: "Update existing tests", what: "Never weaken an assertion to force green." },
      { n: "13–14", title: "Run tests and full regression", what: "Unit, integration, coverage ratchet, Bruno API tests. No @Disabled without approval.", check: "mvn verify -pl backend-api" },
      { n: "15", title: "Playwright E2E", what: "Every UI change: golden path and edge cases at mobile, tablet and desktop widths.", check: "testing/e2e — TS-nnnn suites" },
    ],
  },
  {
    phase: "Publish",
    tone: "emerald",
    steps: [
      { n: "16", title: "Permanent documentation", gate: 3, what: "Update the docs the change affects, in the same diff. Never duplicate; point at the permanent document.", check: "check-doc-traceability.py" },
      { n: "17", title: "Atomic commits", what: "Conventional Commits; Refs #N on intermediate commits, Closes #N on the closing one.", check: "check-commit-messages.sh" },
      { n: "17.5", title: "Full local pipeline", what: "One dashboarded pre-PR gate: plan check, security cases, preflight --full, markdown lint.", check: "bash workspace/sdlc/run_pipeline.sh" },
      { n: "18–19", title: "Pull request and green CI", what: "PR titled [#N] type(scope): description, with the PR template; wait for every required workflow." },
    ],
  },
  {
    phase: "Ship",
    tone: "amber",
    steps: [
      { n: "20", title: "Code review", gate: 4, what: "Code owner, or the Foreman's Gate 4 review with no open finding. Heavy CI must be terminal success.", check: "check-heavy-ci.sh <pr>" },
      { n: "21", title: "Merge", what: "Via the PR only. The OpenSpec change is archived in that same PR (#1623)." },
      { n: "22", title: "Deploy", what: "CI → CD builds, signs (cosign) and publishes images to GHCR; SBOMs on version tags." },
      { n: "23", title: "Smoke test", gate: 5, what: "Health endpoint and the key flow of the change on the target environment." },
      { n: "24", title: "Close the issue", what: "Only after deploy and smoke. Board Status Done, WIP labels removed." },
    ],
  },
];

export const GATES = [
  { n: 1, name: "Do not start implementation without", items: ["Issue linked to a Use Case", "Specification written", "Acceptance criteria defined"], machine: "validate-sdlc-plan.sh resolves the Issue live with gh: an invented or closed number fails" },
  { n: 2, name: "Do not implement without", items: ["Test cases designed", "Unit tests written and observed failing", "Integration tests where applicable"], machine: "check-tdd-evidence.sh; the local harness requires TEST_CMD to fail" },
  { n: 3, name: "Do not create a PR if", items: ["Docs not updated", "Any test failing", "Coverage below the JaCoCo ratchet", "Lint/format failing", "run_pipeline.sh not run or red"], machine: "preflight.sh pre-push hook + run_pipeline.sh dashboard" },
  { n: 4, name: "Do not merge if", items: ["CI red or pending", "Review unresolved", "Merge conflicts", "Docs pending"], machine: "protect-main ruleset (5 required checks) + check-heavy-ci.sh" },
  { n: 5, name: "Not finished until", items: ["Deploy succeeded", "Smoke test passed", "Issue closed", "Board Status and labels coherent"], machine: "check-board-status-label-coherence.py; archive hygiene on main (Main Health)" },
];

/* ───────────────────────────── Guardrails ───────────────────────────── */

export const GUARDRAILS: { layer: string; what: string; sources: Source[] }[] = [
  {
    layer: "1 · Policy",
    what: "The Engineering Constitution is the single, agent-agnostic process for every change, by a human or any AI tool. Where a lower document disagrees, the Constitution wins. A ≤3 KB agent card carries the non-negotiables into every session.",
    sources: [
      { label: "CONSTITUTION.md", href: src("CONSTITUTION.md") },
      { label: "CONSTITUTION-AGENT-CARD.md", href: src("CONSTITUTION-AGENT-CARD.md") },
      { label: "AGENTS.md", href: src("AGENTS.md") },
    ],
  },
  {
    layer: "2 · Context injection",
    what: "The openspec CLI injects the Constitution's non-negotiables into every artifact instruction, so an agent that never reads AGENTS.md still gets the rules. The always-loaded set is held under an 8,000-token budget.",
    sources: [
      { label: "docs/openspec/config.yaml", href: src("docs/openspec/config.yaml") },
      { label: "schema notaire-sdlc", href: src("docs/openspec/schemas/notaire-sdlc/schema.yaml") },
      { label: "agent-context-budget.py", href: src("workspace/ci/agent-context-budget.py") },
    ],
  },
  {
    layer: "3 · Skills & agents catalog",
    what: "One central catalog of skills and agents that every tool reads; no per-tool copies. Each lifecycle skill declares scope, inputs, outputs, validation and residual risks, and reports unknowns instead of inventing evidence.",
    sources: [
      { label: ".claude/skills/README.md", href: src(".claude/skills/README.md") },
      { label: ".claude/agents/", href: src(".claude/agents/") },
      { label: "fleet-manifest.yaml", href: src("docs/300-development/304-ai-sdlc-cloud/fleet-manifest.yaml") },
    ],
  },
  {
    layer: "4 · Agent-side hooks & harness",
    what: "Claude Code hooks refuse a push to main. The local-AI harness reverts out-of-scope edits, refuses branch switches and history rewrites, freezes the red-proven TEST_CMD and owns the ledger, because a model's own claims are never trusted.",
    sources: [
      { label: "block-push-to-main.sh", href: src(".claude/hooks/block-push-to-main.sh") },
      { label: "local-ai/sdlc/AI-SDLC.md", href: src("local-ai/sdlc/AI-SDLC.md") },
      { label: "local-ai/sdlc/githooks/", href: src("local-ai/sdlc/githooks/") },
    ],
  },
  {
    layer: "5 · Local gates",
    what: "preflight.sh mirrors CI and runs as the pre-push hook. run_pipeline.sh is the dashboarded pre-PR gate. validate-sdlc-plan.sh rejects an incomplete plan or an Issue that does not exist.",
    sources: [
      { label: "preflight.sh", href: src("workspace/sdlc/preflight.sh") },
      { label: "run_pipeline.sh", href: src("workspace/sdlc/run_pipeline.sh") },
      { label: "validate-sdlc-plan.sh", href: src("workspace/sdlc/validate-sdlc-plan.sh") },
      { label: "install-git-hooks.sh", href: src("workspace/sdlc/install-git-hooks.sh") },
    ],
  },
  {
    layer: "6 · CI process checks",
    what: "The process itself is tested on every PR: Conventional Commits, tests before production code, an OpenSpec change or an approved exception, traceability claims, archive hygiene, board coherence, Closes/Refs guard, agent rule files and product version coherence.",
    sources: [
      { label: "sdlc-process.yml", href: workflow("sdlc-process.yml") },
      { label: "pr-validation.yml", href: workflow("pr-validation.yml") },
      { label: "check-sdlc-exception.sh", href: src("workspace/sdlc/check-sdlc-exception.sh") },
    ],
  },
  {
    layer: "7 · CI quality & security",
    what: "Backend unit, integration and coverage gate; frontend typecheck, lint, Vitest and build; Bruno and Playwright; OpenAPI breaking-diff; CodeQL; the security case catalog; weekly ZAP, k6 and backup-restore.",
    sources: [
      { label: "ci.yml", href: workflow("ci.yml") },
      { label: "playwright-e2e.yml", href: workflow("playwright-e2e.yml") },
      { label: "security/cases/catalog.yml", href: src("security/cases/catalog.yml") },
    ],
  },
  {
    layer: "8 · Repository rules",
    what: "The protect-main ruleset requires a pull request and five status checks (CI, Frontend CI, Playwright E2E, Code Lint, PR Validation), and blocks deletion and non-fast-forward pushes. The desired state is code, and a script asserts it.",
    sources: [
      { label: "protect-main.desired.json", href: src("security/rulesets/protect-main.desired.json") },
      { label: "assert-protect-main-ruleset.sh", href: src("security/assert-protect-main-ruleset.sh") },
      { label: ".github/CODEOWNERS", href: src(".github/CODEOWNERS") },
    ],
  },
  {
    layer: "9 · Human Owner",
    what: "Approves specifications, is the only role that grants an exception (sdlc-exception), and alone merges Constitution amendments, exception PRs and changes to the Foreman's own harness or merge path.",
    sources: [{ label: "Constitution §10 Roles, §12", href: src("CONSTITUTION.md") }],
  },
];

export const NEVER_LIST = [
  "Skip or reorder steps of the workflow",
  "Implement from a prompt without an Issue and a Specification",
  "Write code before its failing tests",
  "Open a PR with failing CI, missing docs or reduced coverage",
  "Commit to main, or merge on light-only green",
  "Fabricate an Issue number, a Use Case, or a test result",
  "Close an Issue before deploy and smoke test",
  "Force-push or rebase a pushed branch (merge main in instead)",
];

/* ───────────────────────────── Workflows ───────────────────────────── */

export const WORKFLOWS: { file: string; name: string; when: string; role: string; tier: "light" | "heavy" | "after" | "scheduled" }[] = [
  { file: "pr-validation.yml", name: "PR Validation", when: "PR", role: "Title, branch naming, SDLC plan validation, Spotless/Checkstyle lint, quick build", tier: "light" },
  { file: "sdlc-process.yml", name: "SDLC Process", when: "PR · push · daily", role: "Process Checks on PRs; Main Health on main (archive hygiene, plans conform, harness self-tests)", tier: "light" },
  { file: "frontend-ci.yml", name: "Frontend CI", when: "PR · push", role: "TypeScript, ESLint, Vitest with raise-only coverage floors, Next.js build", tier: "light" },
  { file: "openapi-contract.yml", name: "OpenAPI Contract", when: "PR", role: "Committed OpenAPI matches the code; oasdiff flags breaking changes", tier: "light" },
  { file: "ci.yml", name: "CI - Build, Test & Security", when: "PR · push", role: "Build, unit, integration, coverage gate (mvn verify), security scan, Docker image, reports", tier: "heavy" },
  { file: "playwright-e2e.yml", name: "Playwright E2E — Full Suite", when: "PR · push · schedule", role: "Bruno API suite and Playwright UI in 3 shards, business coverage report", tier: "heavy" },
  { file: "codeql.yml", name: "CodeQL", when: "PR · push · schedule", role: "Advanced-setup static analysis (never alongside default setup)", tier: "heavy" },
  { file: "database-vv.yml", name: "Database V&V", when: "PR · push", role: "Flyway migrations verified against PostgreSQL", tier: "heavy" },
  { file: "cd.yml", name: "CD - Build & Publish Docker", when: "after CI on main · tags", role: "Release identity, sign and publish images to GHCR, SBOM assets on releases", tier: "after" },
  { file: "release-please.yml", name: "Release Please", when: "push to main", role: "Semver release PR: poms, frontend package.json, release/product-release.json and CHANGELOG from merged commits", tier: "after" },
  { file: "deploy-github-page.yml", name: "Deploy GitHub Page", when: "after CI on main", role: "Builds this site with fresh coverage and E2E reports", tier: "after" },
  { file: "dast-zap.yml", name: "DAST — OWASP ZAP Baseline", when: "weekly", role: "Blocking ZAP baseline with a reviewed allowlist", tier: "scheduled" },
  { file: "performance-test.yml", name: "Performance - k6 Load Test", when: "weekly", role: "k6 load test against a live API", tier: "scheduled" },
  { file: "backup-restore-smoke.yml", name: "Backup → Restore → Smoke", when: "weekly", role: "Restores a backup and smoke-tests it", tier: "scheduled" },
  { file: "test-coverage-report.yml", name: "Test Coverage Report", when: "push · schedule", role: "Coverage trend for the site and reports", tier: "scheduled" },
  { file: "leanup-actions.yml", name: "Cleanup Actions artifacts and logs", when: "schedule", role: "Keeps artifact storage under quota", tier: "scheduled" },
];

/* ───────────────────────────── Agents, skills, runtimes ───────────────────────────── */

export const CREW: { name: string; role: string; skills: string[] }[] = [
  { name: "cloud-foreman", role: "Orchestrates the loop: pick, Gate 1, dispatch, PR, CI, review, merge", skills: ["ai-agent-workflow", "devsecops-traceability", "ci-cd-quality-gates"] },
  { name: "openspec-planner", role: "Writes the specification that Gate 1 checks", skills: ["openspec-propose", "openspec-triage", "analyst", "product-owner"] },
  { name: "java-architect", role: "Package, port/adapter and migration decisions", skills: ["java", "hexagonal-arch", "architecture-decision-design", "flyway"] },
  { name: "backend-implementer", role: "TDD implementation in backend-api", skills: ["backend", "java", "programming", "maven-build"] },
  { name: "frontend-design", role: "Next.js UI with the design-system tokens", skills: ["frontend-design", "motion-design"] },
  { name: "testing-qa", role: "Test design and red proof; Bruno and Playwright", skills: ["testing", "qa-automation-strategy", "api-contract-testing"] },
  { name: "devops-engineer", role: "Compose, CI and observability", skills: ["devops", "ci-cd-quality-gates", "operations-observability-readiness"] },
  { name: "security-auditor", role: "Threat model, authz and secrets review", skills: ["secure-threat-modeling", "backend"] },
  { name: "code-reviewer", role: "Gate 4 diff review", skills: ["ai-agent-workflow", "programming"] },
  { name: "project-manager", role: "Milestones, priority and lanes; no product code", skills: ["project-management", "product-owner", "delivery-maturity-roadmap"] },
  { name: "sync_issues_and_code", role: "Issue, label and board hygiene", skills: ["devsecops-traceability", "product-owner"] },
];

export const SKILL_STAGES: { stage: string; skills: string[]; output: string }[] = [
  { stage: "Strategy & discovery", skills: ["delivery-maturity-roadmap", "analyst", "product-owner"], output: "roadmap hypothesis, Issue, Use Case, acceptance criteria" },
  { stage: "Traceability", skills: ["devsecops-traceability", "openspec-triage"], output: "IDs, traceability ledger, impact map" },
  { stage: "Specification", skills: ["openspec-propose"], output: "proposal, delta specs, scenarios" },
  { stage: "Architecture", skills: ["architecture-decision-design", "hexagonal-arch", "design-doc-mermaid"], output: "design, ADR, Mermaid/C4 views" },
  { stage: "Security design", skills: ["secure-threat-modeling"], output: "threat model, risk register, SR-* requirements" },
  { stage: "Test design", skills: ["qa-automation-strategy", "testing"], output: "test plan, cases, fixtures" },
  { stage: "Implementation", skills: ["openspec-apply-change", "programming", "java", "frontend-design"], output: "failing tests, implementation, updated ledger" },
  { stage: "Contract & integration", skills: ["api-contract-testing", "api-rest"], output: "OpenAPI, Bruno and contract evidence" },
  { stage: "Delivery", skills: ["ci-cd-quality-gates", "devops", "maven-build", "steward"], output: "gate log, reports, immutable artifact" },
  { stage: "Production", skills: ["operations-observability-readiness"], output: "SLOs, telemetry, runbook, smoke evidence" },
  { stage: "Completion", skills: ["openspec-archive-change"], output: "complete ledger, permanent docs, archived change" },
];

export const HARNESS_PHASES: { phase: string; who: string; gate: string }[] = [
  { phase: "triage", who: "worker", gate: "triage.env schema + every criterion has a proof" },
  { phase: "setup", who: "harness", gate: "branch from fresh origin/main, OpenSpec change, in-progress label" },
  { phase: "spec", who: "worker", gate: "openspec validate --strict + validate-sdlc-plan.sh → Gate 1" },
  { phase: "tests", who: "worker", gate: "tests committed, TEST_CMD must FAIL → Gate 2" },
  { phase: "implement", who: "worker", gate: "TEST_CMD green + full suite green + Closes #n" },
  { phase: "docs", who: "worker", gate: "CHANGELOG for feat/fix, plan still valid" },
  { phase: "quality", who: "harness → worker", gate: "preflight.sh" },
  { phase: "pipeline", who: "harness → worker", gate: "run_pipeline.sh (stack + E2E + Bruno) → Gate 3" },
  { phase: "pr", who: "worker", gate: "PR title, body references issue, branch pushed" },
  { phase: "ci", who: "harness → worker", gate: "check runs on the last non-[skip ci] commit settle green" },
  { phase: "review", who: "foreman", gate: "diff review → Gate 4; findings go back via fix" },
  { phase: "merge", who: "harness", gate: "CI + CD green, /actuator/health UP, issue closed → Gate 5" },
];

export const ENTRY_POINTS: { tool: string; entry: Source[] }[] = [
  { tool: "Claude Code", entry: [{ label: "CLAUDE.md → AGENTS.md", href: src("CLAUDE.md") }, { label: "ai-agent-workflow rule", href: src(".claude/rules/ai-agent-workflow.md") }] },
  { tool: "OpenCode", entry: [{ label: "opencode.json", href: src("opencode.json") }] },
  { tool: "Cursor Cloud fleet", entry: [{ label: "304-ai-sdlc-cloud/", href: src("docs/300-development/304-ai-sdlc-cloud/") }, { label: ".cursor/install.sh", href: src(".cursor/install.sh") }] },
  { tool: "Any agent via the CLI", entry: [{ label: "openspec instructions", href: src("docs/openspec/NOTAIRE-ADAPTATIONS.md") }, { label: "/opsx commands", href: src(".claude/commands/opsx/") }] },
  { tool: "Local model (Codex / OpenCode on oMLX)", entry: [{ label: "local-ai/sdlc/foreman.sh", href: src("local-ai/sdlc/foreman.sh") }, { label: "WORKER.md brief", href: src("local-ai/sdlc/WORKER.md") }] },
];

/* ───────────────────────────── Governance ───────────────────────────── */

export const LABEL_FAMILIES: { family: string; rule: string; examples: string }[] = [
  { family: "type:", rule: "exactly one on claimable work", examples: "feature · bug · refactor · chore · ci · epic" },
  { family: "priority:", rule: "exactly one on claimable work", examples: "critical · high · medium · low" },
  { family: "area:", rule: "one or more", examples: "backend · frontend · db · devops · test · docs" },
  { family: "status:", rule: "workflow flags", examples: "ready · in-progress · in-review · blocked · needs-info" },
  { family: "concern:", rule: "zero or more", examples: "security · a11y · i18n · ux · risk · tech-debt" },
  { family: "trace:", rule: "catalog anchors only, never claimable", examples: "rf · rnf · cu" },
  { family: "release:", rule: "optional", examples: "breaking (major bump)" },
];

export const MILESTONE_KINDS: { kind: string; pattern: string; example: string }[] = [
  { kind: "Release train", pattern: "v<MAJOR>.<MINOR> · <Theme> (<YYYY-MM>)", example: "v0.3 · Hexagonal core & security (2026-11)" },
  { kind: "Shipped cut", pattern: "same pattern, closed", example: "v0.2 · Release identity & product forms (2026-10)" },
  { kind: "History stage", pattern: "H<n> · <Stage>", example: "H0 Swing monolith … H8 pre-release hardening" },
  { kind: "Continuous", pattern: "Backlog · Unscheduled", example: "no due date" },
  { kind: "Catalog", pattern: "Catalog · Requirements & use cases", example: "RF / RNF / CU anchors" },
];

export const CONVENTIONS: { what: string; rule: string }[] = [
  { what: "Branch", rule: "<type>/<issue-number>_<description>, e.g. feat/253_user_auth" },
  { what: "Commit", rule: "<type>(<scope>): <description>, ending Refs #N (intermediate) or Closes #N (closing)" },
  { what: "PR title", rule: "[#<issue>] type(scope): description" },
  { what: "Stale branch", rule: "git merge origin/main — never rebase or force-push a pushed branch" },
  { what: "Exception", rule: "Only the Owner, only via the sdlc-exception label, only on a PR with no OpenSpec change" },
  { what: "Progress", rule: "Push after each group of commits and comment on the Issue (publish-progress.sh)" },
];

/* ───────────────────────────── Lessons learned ───────────────────────────── */

export type Lesson = { title: string; happened: string; rule: string; refs: number[]; source: Source };

export const LESSONS: Lesson[] = [
  {
    title: "Light green is not mergeable",
    happened: "A CI subscription reported \"all 18 checks success\" while the backend Integration suite and Playwright were still pending.",
    rule: "Merge only when check-heavy-ci.sh exits 0: Unit, Integration, Coverage Gate, Bruno and Playwright are terminal success.",
    refs: [1126, 1128, 1134, 1137],
    source: { label: "CI-MERGE-GATE.md", href: src("docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md") },
  },
  {
    title: "`Issue: #N` does not close anything",
    happened: "Fleet PRs merged and their issues stayed open, because commits said Issue: #N.",
    rule: "Closing commits and PR bodies use Closes #N; intermediate slices use Refs #N, and a guard checks them.",
    refs: [1111, 1112, 1116],
    source: { label: "ENVIRONMENT-CHECKLIST §7", href: src("docs/300-development/304-ai-sdlc-cloud/ENVIRONMENT-CHECKLIST.md") },
  },
  {
    title: "Bot commits with [skip ci] stall the merge",
    happened: "PR Validation committed wiki reports onto PR heads with [skip ci]; the head SHA had an empty check suite, and merge-when-green waited forever.",
    rule: "Report workflows never commit onto a PR head; harnesses judge CI on the last commit without [skip ci].",
    refs: [1111, 1117],
    source: { label: "FLEET-ARCHITECTURE §9", href: src("docs/300-development/304-ai-sdlc-cloud/FLEET-ARCHITECTURE.md") },
  },
  {
    title: "A stale tip is not a product bug",
    happened: "Integration and Playwright failures (Budget/person, undefined, undefined) on branches behind main were already fixed on main.",
    rule: "Merge origin/main first and re-run heavy CI before touching product code. Never rebase a pushed branch.",
    refs: [1132],
    source: { label: "CI-MERGE-GATE.md", href: src("docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md") },
  },
  {
    title: "An invented Issue number passed Gate 1",
    happened: "Gate 1 only checked the format #[0-9]+, so a proposal could cite #999999.",
    rule: "validate-sdlc-plan.sh resolves the Issue live via gh; exploration findings go through openspec-triage into real Issues first.",
    refs: [],
    source: { label: "NOTAIRE-ADAPTATIONS §4.3–4.5", href: src("docs/openspec/NOTAIRE-ADAPTATIONS.md") },
  },
  {
    title: "Closed issues left active changes that blocked every PR",
    happened: "After an issue closed, its unarchived OpenSpec change made plan validation fail on every open PR, and Main Health went red.",
    rule: "Archive the change in the PR that implements it; multi_pr changes are archived by their last PR. Main Health fails on leftovers.",
    refs: [1252, 1623, 1922],
    source: { label: "check-openspec-archive-hygiene.py", href: src("workspace/sdlc/check-openspec-archive-hygiene.py") },
  },
  {
    title: "Models report success they have not earned",
    happened: "Local workers wrote \"1,940 tests pass\" and \"PR created\" into tasks.md, copied SHAs from main, and claimed changes their commits never made.",
    rule: "The harness owns the ledger and the pushes; gates run real commands; Gate 4 compares every commit-body line with git show --stat.",
    refs: [1049, 1063],
    source: { label: "local-ai/sdlc/AI-SDLC.md", href: src("local-ai/sdlc/AI-SDLC.md") },
  },
  {
    title: "Too much always-loaded context",
    happened: "Bulk rule imports and alwaysApply skills crowded the context window of every session.",
    rule: "The always-loaded set stays ≤8,000 tokens: slim AGENTS.md, a 3 KB Constitution card, and on-demand rules.",
    refs: [1259],
    source: { label: "agent-context-budget.py", href: src("workspace/ci/agent-context-budget.py") },
  },
  {
    title: "Heavy suites on docs-only PRs",
    happened: "Docs and OpenSpec PRs queued full Java and Playwright runs and starved shared runners.",
    rule: "Path-scoped CI: docs paths skip leaf jobs, while aggregator checks keep their required names green; main always runs everything.",
    refs: [1257],
    source: { label: "test_ci_workflow_invariants.py", href: src("workspace/tests/test_ci_workflow_invariants.py") },
  },
  {
    title: "Parallel heavy PRs are slower",
    happened: "Several heavy PRs at once enqueued Playwright behind each other; every PR waited longer and cost more.",
    rule: "Serialize Playwright-heavy PRs; one domain-sized slice per PR; draft Dependabot PRs when they starve the queue.",
    refs: [1693, 1858, 1907],
    source: { label: "FLEET-LESSONS-HEX-1693.md", href: src("docs/300-development/304-ai-sdlc-cloud/FLEET-LESSONS-HEX-1693.md") },
  },
  {
    title: "Board, labels and state drift apart",
    happened: "Closed issues stayed In Progress on the board; open ones carried stale WIP labels.",
    rule: "One coherent state: CLOSED ⇒ Done without WIP labels; OPEN WIP ⇒ In Progress + status:in-progress; checked by a script.",
    refs: [1550],
    source: { label: "check-board-status-label-coherence.py", href: src("workspace/sdlc/check-board-status-label-coherence.py") },
  },
  {
    title: "Work that lives only on one disk",
    happened: "Agents stopped mid-task with commits that were never pushed, and nobody could resume.",
    rule: "Continuous publication: push the branch at once and after every group of commits, with an Issue comment on stage and what remains.",
    refs: [1681],
    source: { label: "publish-progress.sh", href: src("workspace/sdlc/publish-progress.sh") },
  },
  {
    title: "CodeQL default setup beside the advanced workflow",
    happened: "With both enabled, the SARIF upload was rejected and Analyze failed.",
    rule: "Advanced XOR default: keep codeql.yml, and disable default setup with security/enable-gh-secure.sh --apply.",
    refs: [],
    source: { label: "DevSecOps README", href: src("docs/200-architecture/208-devsecops/README.md") },
  },
  {
    title: "Toolchain majors from a bot",
    happened: "Dependabot proposed TypeScript 7; openapi-typescript needs ^5 and TS7 has no compiler API, so the PR could never go green.",
    rule: "Toolchain majors that break the build are planned changes, not bot PRs: an ignore rule with its removal condition.",
    refs: [1871, 1883],
    source: { label: ".github/dependabot.yml", href: src(".github/dependabot.yml") },
  },
  {
    title: "Anonymous image pulls hit rate limits",
    happened: "Docker Hub answered 429 on shared runners and service containers failed to start.",
    rule: "Pull CI images through mirror.gcr.io and retry browser installs.",
    refs: [1380],
    source: { label: "dast-zap.yml", href: workflow("dast-zap.yml") },
  },
  {
    title: "Evidence uploads must not fail a passing scan",
    happened: "The weekly k6 run passed, but the job went red because the artifact storage quota was hit. ZAP failed on an informational alert about non-storable 404 answers.",
    rule: "Report uploads are continue-on-error; the ZAP allowlist is a reviewed set with reasons, pinned by a test.",
    refs: [1738, 1932],
    source: { label: ".zap/rules.tsv", href: src(".zap/rules.tsv") },
  },
  {
    title: "A deploy cannot fix a setting",
    happened: "Pages deploys failed with 404 because GitHub Pages was disabled in repository settings; no workflow change could fix it.",
    rule: "Owner-only steps (settings, quotas, tokens) are written down as exact steps for the Owner, never worked around by an agent.",
    refs: [],
    source: { label: "deploy-github-page.yml", href: workflow("deploy-github-page.yml") },
  },
  {
    title: "Bots without write access still need issue hygiene",
    happened: "Agent tokens could open PRs but got 403 on issue labels or comments, so issue state silently drifted.",
    rule: "Keep status in the PR body with Closes/Refs as the sync channel, and hand the Owner a paste-ready template; never invent a parallel tracker.",
    refs: [1648],
    source: { label: "FLEET-LESSONS-HEX-1693.md", href: src("docs/300-development/304-ai-sdlc-cloud/FLEET-LESSONS-HEX-1693.md") },
  },
];

/* ───────────────────────────── Playbook ───────────────────────────── */

export const PLAYBOOK: { title: string; body: string; notaire: Source[] }[] = [
  {
    title: "Write one constitution, for humans and every agent",
    body: "Principles, Definition of Done, conventions, a sequential workflow, hard gates, roles, and how exceptions are granted. Make it the highest authority, and keep a small card that is always loaded.",
    notaire: [{ label: "CONSTITUTION.md", href: src("CONSTITUTION.md") }, { label: "agent card", href: src("CONSTITUTION-AGENT-CARD.md") }],
  },
  {
    title: "Fork a spec-driven schema, don't invent one",
    body: "Extend OpenSpec's spec-driven schema with your mandatory sections and a traceability artifact, and inject the constitution through config.yaml so every tool receives it through the same channel.",
    notaire: [{ label: "schema notaire-sdlc", href: src("docs/openspec/schemas/notaire-sdlc/schema.yaml") }, { label: "NOTAIRE-ADAPTATIONS §11", href: src("docs/openspec/NOTAIRE-ADAPTATIONS.md") }],
  },
  {
    title: "Turn every rule into an exit code",
    body: "Every gate an agent must pass is a script that fails: plan completeness, live Issue check, TDD evidence, commit format, doc traceability. Run the same scripts locally (pre-push) and in CI.",
    notaire: [{ label: "workspace/sdlc/", href: src("workspace/sdlc/") }, { label: "sdlc-process.yml", href: workflow("sdlc-process.yml") }],
  },
  {
    title: "Split the work into phases a model can hold",
    body: "One phase per run with a fresh context, a short brief, and a deterministic gate after each phase. A failing gate feeds its own output back into a retry, up to a limit, then a human steps in.",
    notaire: [{ label: "local-ai harness", href: src("local-ai/sdlc/AI-SDLC.md") }, { label: "project adapter", href: src(".aisdlc/project.yml") }],
  },
  {
    title: "Specialize the crew and match cost to the task",
    body: "A Foreman orchestrates; specialist Workers get the narrowest skill and an explicit scope. Use cheap models for mechanical work, mid-tier for tests and routine fixes, top-tier for architecture and security.",
    notaire: [{ label: "FLEET-ARCHITECTURE.md", href: src("docs/300-development/304-ai-sdlc-cloud/FLEET-ARCHITECTURE.md") }, { label: "fleet-manifest.yaml", href: src("docs/300-development/304-ai-sdlc-cloud/fleet-manifest.yaml") }],
  },
  {
    title: "Make the platform enforce the last mile",
    body: "Branch rules that require a PR and named checks, no force-push, CODEOWNERS, and a protection ruleset kept as code and asserted by a script.",
    notaire: [{ label: "security/rulesets/", href: src("security/rulesets/") }, { label: "CODEOWNERS", href: src(".github/CODEOWNERS") }],
  },
  {
    title: "Keep humans on decisions, not keystrokes",
    body: "The Owner approves specifications, grants exceptions and merges policy changes. Agents write down Owner-only steps instead of working around them.",
    notaire: [{ label: "Constitution §10–12", href: src("CONSTITUTION.md") }],
  },
  {
    title: "Learn in writing",
    body: "Every incident becomes a rule in a permanent document and, where possible, a test, so the next agent cannot repeat it.",
    notaire: [{ label: "FLEET-LESSONS-HEX-1693.md", href: src("docs/300-development/304-ai-sdlc-cloud/FLEET-LESSONS-HEX-1693.md") }, { label: "ENVIRONMENT-CHECKLIST §7", href: src("docs/300-development/304-ai-sdlc-cloud/ENVIRONMENT-CHECKLIST.md") }],
  },
];

export const PRODUCTION_READY: { need: string; evidence: Source }[] = [
  { need: "Test plan and pyramid with a generated inventory", evidence: { label: "TEST-PLAN.md · INVENTORY.md", href: src("docs/300-development/303-testing/TEST-PLAN.md") } },
  { need: "E2E suites traced to Use Cases", evidence: { label: "E2E-TEST-MAPPING.md", href: src("docs/300-development/303-testing/E2E-TEST-MAPPING.md") } },
  { need: "Security case catalog run locally and in CI", evidence: { label: "security/run_gates.py", href: src("security/run_gates.py") } },
  { need: "Contract stability for API clients", evidence: { label: "openapi-contract.yml", href: workflow("openapi-contract.yml") } },
  { need: "Signed images and SBOMs", evidence: { label: "SBOM.md", href: src("docs/300-development/SBOM.md") } },
  { need: "One product version everywhere", evidence: { label: "ADR-028", href: src("docs/200-architecture/202-ADR/ADR-028-release-and-version-identity.md") } },
  { need: "Restorable backups", evidence: { label: "backup-restore-smoke.yml", href: workflow("backup-restore-smoke.yml") } },
  { need: "Observability stack and quality dashboards", evidence: { label: "infra/README.md", href: src("infra/README.md") } },
];

export const SOURCES_OF_TRUTH: Source[] = [
  { label: "CONSTITUTION.md", href: src("CONSTITUTION.md") },
  { label: "AGENTS.md", href: src("AGENTS.md") },
  { label: "OpenSpec adaptations", href: src("docs/openspec/NOTAIRE-ADAPTATIONS.md") },
  { label: "Cloud fleet docs", href: src("docs/300-development/304-ai-sdlc-cloud/README.md") },
  { label: "Local-AI harness", href: src("local-ai/sdlc/AI-SDLC.md") },
  { label: "Project management", href: src("docs/300-development/305-project-management/README.md") },
  { label: "Labels", href: src("docs/300-development/305-project-management/LABELS.md") },
  { label: "Milestone nomenclature", href: src("docs/300-development/305-project-management/MILESTONE-NOMENCLATURE.md") },
  { label: "Engineering practice", href: src("docs/300-development/INGENIERIA.md") },
];

/* ───────────────────── Case study and reusable framework (#1958) ───────────────────── */

export const FRAMEWORK_ARCHITECTURE = `
flowchart TB
  subgraph runtime["Agent runtimes — interchangeable"]
    direction LR
    CC["Claude Code"] --- CU["Cursor"] --- CX["Codex · OpenCode"] --- LM["Local models"]
  end
  subgraph governance["Governance"]
    direction LR
    POL["Policy<br/>CONSTITUTION.md"] --> CARD["Agent card<br/>≤ 3 KB"]
    ROLES["Roles<br/>Owner · Foreman · Workers · Reviewer"]
  end
  subgraph kit["aisdlc kit — stdlib only"]
    direction LR
    PROF["Profile<br/>aisdlc.toml"] --> GATE["gate"] & RAT["ratchet"] & CHG["change"] & LINT["skills · budget<br/>commits · tdd"]
    GATE --> LED[("Evidence ledger")]
    LED --> REP["report · cost"]
  end
  subgraph tools["Your toolchain — unchanged"]
    direction LR
    T1["tests · linters"] --- T2["spec engine"] --- T3["CI · tracker · deploy"]
  end
  runtime -->|"read"| governance
  runtime -->|"run stages"| kit
  kit -->|"calls"| tools
  REP -.->|"learning loop"| POL
  classDef gov fill:#ede9fe,stroke:#6d28d9,color:#2e1065
  classDef k fill:#dcfce7,stroke:#15803d,color:#052e16
  class POL,CARD,ROLES gov
  class PROF,GATE,RAT,CHG,LINT,REP k
`;

export const LEARNING_LOOP = `
flowchart LR
  E["Incident · review note<br/>gate failure · metric"] --> L["Lesson<br/>what happened → why"]
  L --> Q{"Can a command<br/>detect it?"}
  Q -->|yes| G["New gate or ratchet<br/>in the profile"]
  Q -->|no| P["Policy line or<br/>role instruction"]
  G --> T["Test the check<br/>it must be able to fail"]
  P --> R["Each release:<br/>can it be mechanized now?"]
  T --> M["Measure in the ledger"]
  R --> Q
  M --> E
  classDef yes fill:#dcfce7,stroke:#15803d,color:#052e16
  class G,T,M yes
`;

export const ADOPTION_LEVELS = `
flowchart TD
  S(["Start"]) --> A{"More than one agent<br/>working at once?"}
  A -->|no| B{"Legacy debt or<br/>regulated domain?"}
  B -->|no| L1["Level 1 · Guided"]
  B -->|yes| L2["Level 2 · Gated"]
  A -->|yes| C{"Runs without a human<br/>between stages?"}
  C -->|no| L3["Level 3 · Fleet"]
  C -->|yes| D{"Pass-without-notes<br/>≥ 80% per stage?"}
  D -->|no| L3
  D -->|yes| L4["Level 4 · Autonomous"]
  classDef lvl fill:#fde68a,stroke:#b45309,color:#451a03
  class L1,L2,L3,L4 lvl
`;

export type Snippet = { label: string; lang: string; code: string; source: Source };
export type Chapter = {
  id: string;
  period: string;
  title: string;
  problem: string;
  move: string;
  result: string;
  refs: number[];
  before?: Snippet;
  after?: Snippet;
  sources: Source[];
};

export const CASE_CHAPTERS: Chapter[] = [
  {
    id: "legacy",
    period: "2014 → Dec 2025",
    title: "A university desktop app nobody could change safely",
    problem:
      "Notaire began in 2014 as a Java Swing monolith with direct database access: 24 commits, no tests, no pipeline. Rebuilding it as Spring Boot, Next.js and PostgreSQL was a multi-year job for one maintainer.",
    move: "December 2025: the first agent entry file lands, and AI assistants start writing most of the new code.",
    result: "The bet: agents bring the speed. The open question: who proves the work is right?",
    refs: [],
    sources: [{ label: "CONSTITUTION.md §1", href: src("CONSTITUTION.md") }, { label: "AGENTS.md", href: src("AGENTS.md") }],
  },
  {
    id: "speed",
    period: "Mar → Jul 2026",
    title: "Speed without proof",
    problem:
      "Merged PRs grew from 15 to 94 a month with rule files and prompts, but the rules disagreed with each other and nothing enforced them. In July, 54 bug issues were opened against 69 merged PRs.",
    move: "A local preflight began to mirror CI, so a push that passes locally passes on the server.",
    result: "Lesson: prose rules are wishes. The always-loaded context also grew past 16k tokens of imported rules.",
    refs: [588, 703, 720],
    before: {
      label: "AGENTS.md until #1259 — 16,737 bytes, every rule imported up front",
      lang: "markdown",
      code: "@CONSTITUTION.md\n@.claude/rules/ai-agent-workflow.md\n@.claude/skills/ai-agent-workflow/SKILL.md\n…\n@.claude/rules/general.md\n@.claude/rules/programming.md\n@.claude/rules/code-quality.md\n@.claude/rules/refactoring.md\n@.claude/rules/ui-ux-design.md",
      source: { label: "git show 6e817569^:AGENTS.md", href: `${REPO}/commit/6e817569414ad5b5967d64e630268159eb6d7b28` },
    },
    after: {
      label: "3,385 bytes, a 3 KB card, rules on demand, budget checked in CI",
      lang: "markdown",
      code: "**CONSTITUTION.md is highest process authority.**\nAlways-loaded digest: CONSTITUTION-AGENT-CARD.md.\n\n| When touching | Load |\n| Any Java/backend | .claude/rules/programming.md, … |\n| Flyway / schema  | .claude/rules/database-migrations.md |\n\n# guard: agent-context-budget.py --max-tokens 8000",
      source: { label: "AGENTS.md", href: src("AGENTS.md") },
    },
    sources: [{ label: "agent-context-budget.py", href: src("workspace/ci/agent-context-budget.py") }],
  },
  {
    id: "constitution",
    period: "Aug 2026",
    title: "One Constitution, and a Gate 1 that cannot be fooled",
    problem:
      "Specifications were optional and Gate 1 only checked that a proposal cited something shaped like an issue number. An agent could cite #999999 and pass.",
    move: "An agent-agnostic Engineering Constitution became the single authority, wired into OpenSpec so every agent receives it, and Gate 1 began resolving the issue live on GitHub.",
    result: "Bug issues per merged PR fell from 0.78 to 0.33 the next month, while the process itself took a third of all PRs.",
    refs: [781, 783, 793, 856],
    before: {
      label: "Gate 1 until #793 — a format check",
      lang: "bash",
      code: "# A real issue number, not the template placeholder.\nif grep -qE '^\\|[[:space:]]*GitHub Issue[[:space:]]*\\|[[:space:]]*#[0-9]+' \"$proposal\"; then\n  ok \"proposal: GitHub Issue referenced\"",
      source: { label: "validate-sdlc-plan.sh @ 8c339d17", href: `${REPO}/blob/8c339d17/scripts/validate-sdlc-plan.sh` },
    },
    after: {
      label: "the issue must exist on GitHub",
      lang: "bash",
      code: "check_issue_live() {\n  local number=\"$1\"\n  state=\"$(gh issue view \"$number\" --json state --jq '.state')\"\n  if [ -z \"$state\" ]; then\n    bad \"proposal: Issue #$number was not found on GitHub\"\n    return 1",
      source: { label: "validate-sdlc-plan.sh", href: src("workspace/sdlc/validate-sdlc-plan.sh") },
    },
    sources: [{ label: "CONSTITUTION.md", href: src("CONSTITUTION.md") }, { label: "NOTAIRE-ADAPTATIONS.md", href: src("docs/openspec/NOTAIRE-ADAPTATIONS.md") }],
  },
  {
    id: "mechanize",
    period: "Sep 2026",
    title: "Mechanize every rule that can fail",
    problem: "Agents skipped steps the Constitution required, and security skills were used aspirationally rather than applied.",
    move: "Harness hooks block pushes to main, CI checks the process itself (commit subjects, tests with code, spec or exception), and a dashboarded pipeline runs the security case catalog before every PR.",
    result: "Each rule moved from a sentence into a command with an exit code, and therefore into every agent's path.",
    refs: [1027, 1028, 1209, 1500],
    sources: [{ label: "sdlc-process.yml", href: workflow("sdlc-process.yml") }, { label: "block-push-to-main.sh", href: src(".claude/hooks/block-push-to-main.sh") }, { label: "run_pipeline.sh", href: src("workspace/sdlc/run_pipeline.sh") }],
  },
  {
    id: "harness",
    period: "Sep 2026",
    title: "Models that claim success they have not earned",
    problem:
      "A local 9B-to-20B model wrote \"1,940 tests pass\" and \"PR created\" into its task list, copied commit SHAs from main and reported changes its commits never made. One issue took 16 worker runs, 17 retries and 3 foreman stops.",
    move: "A foreman/worker harness took the truth away from the worker: one phase per run, frozen test commands, scope and ref guards, a harness-owned ledger, harness-owned pushes, and review notes with CHECK/EXPECTED lines.",
    result: "Gates caught every observed failure, but not every cheap pass: a duplicate test and an absolute path still needed a reviewer. Review cannot be removed; it can be made checkable.",
    refs: [1049, 1063, 1074, 1102],
    sources: [{ label: "local-ai/sdlc/AI-SDLC.md", href: src("local-ai/sdlc/AI-SDLC.md") }, { label: "local-ai/AUDIT.md", href: src("local-ai/AUDIT.md") }],
  },
  {
    id: "fleet",
    period: "Oct 2026",
    title: "A fleet, and the discipline to serialize it",
    problem:
      "Cloud agents worked in parallel and merged 435 PRs in a month. One merged on \"all 18 checks success\" while integration and E2E suites were still running; parallel heavy PRs queued behind each other; closed issues left specs that blocked every other PR.",
    move: "A heavy-CI merge gate, one written Foreman merge rule, an atomic claim protocol, archive-in-the-PR, continuous publication, and one heavy PR at a time.",
    result: "Throughput rose six-fold over July while bug issues per merged PR fell to 0.09. The process share of PRs dropped from 35% to 16% as its toil was automated.",
    refs: [1114, 1133, 1257, 1623, 1624, 1646, 1681],
    sources: [{ label: "CI-MERGE-GATE.md", href: src("docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md") }, { label: "check-heavy-ci.sh", href: src("workspace/sdlc/check-heavy-ci.sh") }, { label: "FLEET-LESSONS-HEX-1693.md", href: src("docs/300-development/304-ai-sdlc-cloud/FLEET-LESSONS-HEX-1693.md") }],
  },
  {
    id: "ratchets",
    period: "14 → 17 Oct 2026",
    title: "Ratchets: 512 architecture violations to zero in four days",
    problem:
      "The top-priority goal was a full Ports & Adapters system, but the code had 512 known violations. A big-bang rewrite by parallel agents would have been unreviewable.",
    move: "ArchUnit and ESLint rules landed on day one with a shrink-only baseline: a new violation fails the build, a fixed one must be removed from the file. One domain per PR.",
    result: "The hexagon baseline went from 512 entries to 0, and frontend boundary suppressions from 66 to 0.",
    refs: [1693, 1695, 1696, 1914],
    before: {
      label: "hexagon-baseline.txt at #1695 — 512 entries (133 R1, 377 R2, 2 R6)",
      lang: "text",
      code: "R1 | …adapter.in.web.budget.BudgetController -> …business.Budget\nR1 | …adapter.in.web.budget.BudgetController -> …business.BudgetStatus\nR1 | …adapter.in.web.budget.BudgetController -> …business.Person\nR1 | …adapter.in.web.budget.BudgetController -> …repository.PersonRepository\n… 508 more",
      source: { label: "hexagon-baseline.txt @ 217f83cd", href: `${REPO}/blob/217f83cd/backend-api/src/test/resources/architecture/hexagon-baseline.txt` },
    },
    after: {
      label: "#1914 — empty, and now it can only stay empty",
      lang: "text",
      code: "$ aisdlc ratchet --base origin/main\n  PASS  ArchUnit hexagon baseline (R1–R6) — 0 entries (base 0)\n  PASS  frontend hexagon ESLint suppressions — 0 entries (base 0)\nratchets: PASSED",
      source: { label: "hexagon-baseline.txt", href: src("backend-api/src/test/resources/architecture/hexagon-baseline.txt") },
    },
    sources: [{ label: "hexagonal-architecture.md", href: src(".claude/rules/hexagonal-architecture.md") }],
  },
  {
    id: "extract",
    period: "28 Oct 2026",
    title: "Extracting a framework any repository can adopt",
    problem:
      "The process worked, but it was welded to Notaire: the Constitution named Maven and Playwright, and three agent runtimes kept three configurations. A rule like \"never add a line to a baseline by hand\" was still only prose.",
    move: "One policy, one TOML profile, one zero-dependency kit. Notaire became its first adopter: the kit runs Notaire's existing scripts and adds the checks that were missing.",
    result: "On its first run the kit found four real defects in the skills catalog, and it now fails any hand-added baseline line in preflight and CI.",
    refs: [1958],
    sources: [{ label: "framework/ai-sdlc/", href: src("framework/ai-sdlc/") }, { label: ".aisdlc/aisdlc.toml", href: src(".aisdlc/aisdlc.toml") }],
  },
];

export const CASE_TERMINALS: { title: string; caption: string; lines: string[] }[] = [
  {
    title: "A hand-added baseline line, caught",
    caption: "Real output: a violation appended to Notaire's hexagon baseline, then checked against main.",
    lines: [
      "$ aisdlc --profile .aisdlc/aisdlc.toml ratchet --base origin/main",
      "  FAIL  ArchUnit hexagon baseline (R1–R6) — 1 entry added to",
      "        backend-api/src/test/resources/architecture/hexagon-baseline.txt since origin/main;",
      "        fix the violation instead of listing it:",
      "        + R1 | adapter.in.web.FooController -> jpa.FooJpaController (+1)",
      "ratchets: FAILED (ArchUnit hexagon baseline (R1–R6))",
    ],
  },
  {
    title: "Code without a test, in a brand-new repository",
    caption: "Real output: `aisdlc init` in an empty repository, then a commit that adds code and no test.",
    lines: [
      "$ aisdlc gate pre-push --base HEAD~1",
      "stage pre-push · G3 verified",
      "  PASS  commit subjects (exit 0, 0.1s)",
      "  FAIL  tests come with code (exit 1, 0.1s)",
      "        |   FAIL  production code changed without any test change:",
      "        |         src/app.py",
      "  PASS  agent context budget (exit 0, 0.0s)",
      "  PASS  ratchets (exit 0, 0.1s)",
      "  TODO  test suite — placeholder; configure its command in the profile",
      "stage pre-push: FAILED",
    ],
  },
];

export const CASE_LESSONS: { title: string; body: string; tradeoff: string }[] = [
  { title: "A rule without an exit code is a wish", body: "Every rule that held in Notaire became a script or a test. The ones that stayed prose were the ones agents broke.", tradeoff: "Writing and maintaining checks is real work; budget for it." },
  { title: "Agents are not witnesses", body: "A model's report of its own success is not evidence. The harness, not the worker, owns ledgers, pushes and results.", tradeoff: "Harness code grows; it needs its own tests and its own review." },
  { title: "Gates tell pass from fail, not right from cheap", body: "Duplicate tests, absolute paths and invented scope pass gates. An independent reviewer with checkable notes catches them.", tradeoff: "Human review time stays in the loop for anything high-risk." },
  { title: "Serialize what is scarce", body: "Parallel agents scale only when heavy CI, shared files and merges are serialized and work is published continuously.", tradeoff: "Some agents wait; that is cheaper than conflict storms." },
  { title: "Ratchet, do not big-bang", body: "Freeze today's violations, forbid new ones, and let every PR shrink the list. 512 became 0 in four days.", tradeoff: "Baselines need a guard against hand edits — now added." },
  { title: "Size the process to the project", body: "The process took a third of all PRs while it was being built. Adoption levels keep a small project from paying for a fleet.", tradeoff: "Lower levels accept more risk; raise the level on evidence." },
];

export const FRAMEWORK_PRINCIPLES = [
  { k: "Policy first", v: "One document prevails over every agent file, rule and prompt — and names no tool." },
  { k: "Spec before code, red before green", v: "No code without a change record; no implementation before a failing test." },
  { k: "Mechanical over aspirational", v: "A rule that can be a command is a gate. Prose is for what cannot be checked." },
  { k: "Machines check machines, humans check meaning", v: "Agents' claims are never evidence; reviewers check what gates cannot." },
  { k: "Gate integrity", v: "Final commit, full suite, live references. Light green is not green." },
  { k: "Ratchet, do not big-bang", v: "Debt shrinks monotonically; quality floors only rise." },
  { k: "Serialize scarce, publish in-flight", v: "Heavy CI, shared files and merges go one at a time; work lives on the remote." },
  { k: "Least context, least privilege", v: "A small always-loaded card; rules and skills on demand; narrow tokens." },
  { k: "Traceable end to end", v: "Issue → change record → tests → commits → PR → release evidence." },
  { k: "Size the process, measure its tax", v: "Pick an adoption level; track the share of work spent on the process itself." },
];

export const FRAMEWORK_STAGES: { stage: string; gate: string; done: string; notaire: string }[] = [
  { stage: "intake", gate: "G0 ready", done: "Issue exists, states the problem, links its requirement", notaire: "Issue template + Use Case" },
  { stage: "plan", gate: "G1 specified", done: "Change record complete; the issue resolves live", notaire: "validate-sdlc-plan.sh" },
  { stage: "red", gate: "G2 tests fail first", done: "New tests exist and fail (expect = \"fail\")", notaire: "mvn / vitest single test, path-scoped" },
  { stage: "pre-push", gate: "G3 verified", done: "Suite, lint, ratchets, process checks", notaire: "preflight.sh (+ ratchet, skills lint)" },
  { stage: "pre-pr", gate: "G3 verified", done: "Full local pipeline, docs updated", notaire: "run_pipeline.sh" },
  { stage: "merge", gate: "G4 reviewed", done: "Full CI on the final commit, review resolved", notaire: "check-heavy-ci.sh, archive hygiene" },
  { stage: "release", gate: "G5 shipped", done: "Deployed, smoke-tested, issue closed", notaire: "version coherence, health smoke" },
];

export const FRAMEWORK_LEVELS: { level: string; name: string; fits: string; adds: string }[] = [
  { level: "1", name: "Guided", fits: "One developer with an assistant", adds: "Policy, card, profile, change records, plan and pre-push gates" },
  { level: "2", name: "Gated", fits: "Small team; agents write most code", adds: "Red gates, ratchets, CI mirror, evidence report" },
  { level: "3", name: "Fleet", fits: "Parallel agents, one human Owner", adds: "Roles with a merge rule, merge gate, serialization, claims, continuous publication" },
  { level: "4", name: "Autonomous", fits: "Bounded autonomous runs", adds: "Release gate, cost prices, harness-owned ledgers, measured autonomy" },
];

export const FRAMEWORK_COMMANDS: { cmd: string; does: string }[] = [
  { cmd: "init --name N --level L", does: "Scaffold policy, agent card, AGENTS.md, profile, role agents" },
  { cmd: "doctor", does: "Validate the profile; warn on placeholders and what the level lacks" },
  { cmd: "gate <stage>", does: "Run the stage's gates by exit code; path-scoped; one ledger record" },
  { cmd: "ratchet", does: "Baselines may not grow against the base; floors must hold" },
  { cmd: "change new | check", does: "Built-in change records with a live issue check" },
  { cmd: "skills · budget · commits · tdd", does: "Catalog lint, context budget, commit subjects, tests with code" },
  { cmd: "report · cost", does: "Gate pass rates and durations; token cost per model and agent" },
];

export const NOTAIRE_PROFILE_EXCERPT = `[project]
name = "notaire"
policy = "CONSTITUTION.md"
level = 3                     # fleet: Owner + Foreman + Workers

[[gates]]
name = "OpenSpec plans conform (live issue check)"
stage = "plan"
run = "bash workspace/sdlc/validate-sdlc-plan.sh"

[[gates]]
name = "backend test fails"
stage = "red"
run = "mvn -q -B test -pl backend-api -Dtest={test}"
expect = "fail"
paths = ["backend-api/src/test/**"]

[[gates]]
name = "heavy CI green on the PR head"
stage = "merge"
run = "bash workspace/sdlc/check-heavy-ci.sh {pr}"

[[ratchets]]
name = "ArchUnit hexagon baseline (R1–R6)"
kind = "baseline"
file = "backend-api/src/test/resources/architecture/hexagon-baseline.txt"`;
