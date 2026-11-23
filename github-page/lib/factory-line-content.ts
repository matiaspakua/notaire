/**
 * Factory Production Line Dashboard — data model.
 *
 * Describes every station on the AI SDLC conveyor: phase, agent, skills,
 * tools, inputs, outputs, gates, and integration points (API, CLI, MCP).
 * Used by /ai-sdlc/factory-line/ to render the interactive production line.
 */

export type { Source } from "@/lib/ai-sdlc-content";

/* ───────────────────── Station types ───────────────────── */

export type StationKind = "intake" | "plan" | "prove" | "publish" | "ship" | "gate" | "human";

export interface Station {
  id: string;
  kind: StationKind;
  title: string;
  subtitle: string;
  phase: "Plan" | "Prove" | "Publish" | "Ship" | "Intake";
  agent?: string;
  skills: string[];
  tools: string[];
  inputs: string[];
  outputs: string[];
  gate?: {
    number: number;
    name: string;
    check: string;
    items: string[];
  };
  human?: {
    role: string;
    action: string;
    when: string;
  };
  integration?: ("api" | "cli" | "mcp" | "github" | "ci")[];
  mermaid?: string;
  repolens?: {
    query: string;
    description: string;
  };
}

/* ───────────────────── Production line stations ───────────────────── */

export const STATIONS: Station[] = [
  // ── INTAKE ──
  {
    id: "intake",
    kind: "intake",
    title: "Issue Intake",
    subtitle: "A GitHub Issue arrives with a Use Case and acceptance criteria",
    phase: "Intake",
    agent: "sync_issues_and_code",
    skills: ["devsecops-traceability", "product-owner"],
    tools: ["gh issue", "aisdlc intake check"],
    inputs: ["GitHub Issue", "Use Case (CU-XX)", "Acceptance criteria"],
    outputs: ["Validated issue", "Labels applied", "Board status: ready"],
    integration: ["github", "cli"],
    repolens: {
      query: "issue intake workflow",
      description: "Find the issue templates and intake lint scripts",
    },
  },
  {
    id: "foreman-claim",
    kind: "human",
    title: "Foreman Claim",
    subtitle: "The Foreman picks an issue, runs Gate 1, and dispatches a Worker",
    phase: "Intake",
    agent: "cloud-foreman",
    skills: ["ai-agent-workflow", "devsecops-traceability"],
    tools: ["aisdlc foreman claim", "validate-sdlc-plan.sh"],
    inputs: ["Ready issue", "OpenSpec config"],
    outputs: ["Claimed issue", "Brief for Worker", "status: in-progress"],
    human: {
      role: "Owner",
      action: "Approves the specification before Gate 1 passes",
      when: "When the spec touches Constitution, harness, or owner-only paths",
    },
    integration: ["cli", "github"],
  },

  // ── PLAN ──
  {
    id: "spec",
    kind: "plan",
    title: "Specification",
    subtitle: "OpenSpec change: proposal, traceability, delta specs, design, tasks",
    phase: "Plan",
    agent: "openspec-planner",
    skills: ["openspec-propose", "openspec-triage", "analyst", "product-owner"],
    tools: ["openspec propose", "validate-sdlc-plan.sh", "python3 workspace/modules.py affected"],
    inputs: ["Issue + Use Case", "Impact analysis"],
    outputs: ["OpenSpec change record", "Delta specs", "Tasks.md"],
    gate: {
      number: 1,
      name: "Do not start implementation without",
      check: "validate-sdlc-plan.sh resolves the Issue live with gh",
      items: ["Issue linked to a Use Case", "Specification written", "Acceptance criteria defined"],
    },
    integration: ["cli", "github"],
  },
  {
    id: "architecture",
    kind: "plan",
    title: "Architecture & Design",
    subtitle: "Hexagonal slice design, ADR if architectural, Mermaid diagrams",
    phase: "Plan",
    agent: "java-architect",
    skills: ["architecture-decision-design", "hexagonal-arch", "design-doc-mermaid"],
    tools: ["repolens search", "ArchUnit", "Mermaid"],
    inputs: ["OpenSpec design section", "Existing architecture"],
    outputs: ["ADR (if needed)", "Design docs", "Mermaid/C4 views"],
    integration: ["mcp", "cli"],
  },
  {
    id: "branch",
    kind: "plan",
    title: "Branch & Setup",
    subtitle: "Create branch from updated main, push at once (continuous publication)",
    phase: "Plan",
    agent: "cloud-foreman",
    skills: ["ai-agent-workflow"],
    tools: ["git checkout -b", "git push", "publish-progress.sh"],
    inputs: ["Approved spec", "Updated main"],
    outputs: ["Branch type/N_desc", "Issue comment with stage"],
    integration: ["github", "cli"],
  },

  // ── PROVE ──
  {
    id: "test-design",
    kind: "prove",
    title: "Test Design",
    subtitle: "Happy path, edge cases, error paths at unit, integration, and E2E level",
    phase: "Prove",
    agent: "testing-qa",
    skills: ["qa-automation-strategy", "testing"],
    tools: ["Bruno", "Playwright", "JUnit 5"],
    inputs: ["Acceptance criteria", "Delta specs"],
    outputs: ["Test plan", "Test cases", "Fixtures"],
    gate: {
      number: 2,
      name: "Do not implement without",
      check: "aisdlc tdd: production code needs test changes",
      items: ["Test cases designed", "Unit tests written and observed failing", "Integration tests where applicable"],
    },
    integration: ["cli"],
  },
  {
    id: "failing-tests",
    kind: "prove",
    title: "Failing Tests",
    subtitle: "Write tests first and watch them fail. A test you never saw fail proves nothing",
    phase: "Prove",
    agent: "backend-implementer",
    skills: ["backend", "java", "programming", "maven-build"],
    tools: ["mvn test", "vitest", "aisdlc tdd"],
    inputs: ["Test cases", "Empty implementation"],
    outputs: ["Failing test output", "Red proof in ledger"],
    integration: ["cli"],
  },
  {
    id: "implement",
    kind: "prove",
    title: "Implementation",
    subtitle: "Minimum code to make tests pass, following conventions and hexagonal patterns",
    phase: "Prove",
    agent: "backend-implementer",
    skills: ["openspec-apply-change", "programming", "java", "frontend-design"],
    tools: ["mvn verify", "vitest", "repolens search", "RepoLens MCP"],
    inputs: ["Failing tests", "Architecture design"],
    outputs: ["Working code", "Green tests", "Updated ledger"],
    integration: ["mcp", "cli"],
  },
  {
    id: "full-suite",
    kind: "prove",
    title: "Full Suite & E2E",
    subtitle: "Unit, integration, coverage ratchet, Bruno API tests, Playwright E2E",
    phase: "Prove",
    agent: "testing-qa",
    skills: ["testing", "qa-automation-strategy", "api-contract-testing"],
    tools: ["mvn verify", "vitest", "Bruno", "Playwright"],
    inputs: ["Implementation", "Existing test suite"],
    outputs: ["Full green suite", "Coverage report", "E2E evidence"],
    integration: ["ci", "cli"],
  },

  // ── PUBLISH ──
  {
    id: "docs",
    kind: "publish",
    title: "Permanent Documentation",
    subtitle: "Update docs in the same diff. Never duplicate; point at the permanent document",
    phase: "Publish",
    agent: "backend-implementer",
    skills: ["openspec-apply-change", "programming"],
    tools: ["check-doc-traceability.py"],
    inputs: ["Implementation", "Affected docs"],
    outputs: ["Updated docs", "CHANGELOG entry"],
    gate: {
      number: 3,
      name: "Do not create a PR if",
      check: "preflight.sh pre-push hook + run_pipeline.sh dashboard",
      items: ["Docs not updated", "Any test failing", "Coverage below the JaCoCo ratchet", "Lint/format failing", "run_pipeline.sh not run or red"],
    },
    integration: ["cli"],
  },
  {
    id: "commits",
    kind: "publish",
    title: "Atomic Commits",
    subtitle: "Conventional Commits; Refs #N intermediate, Closes #N on the closing commit",
    phase: "Publish",
    agent: "backend-implementer",
    skills: ["openspec-apply-change"],
    tools: ["git commit", "aisdlc commits"],
    inputs: ["Working code", "Updated docs"],
    outputs: ["Atomic commits", "Conventional commit messages"],
    integration: ["cli", "github"],
  },
  {
    id: "pipeline",
    kind: "publish",
    title: "Full Local Pipeline",
    subtitle: "Dashboarded pre-PR gate: plan check, security cases, preflight --full, markdown lint",
    phase: "Publish",
    agent: "devops-engineer",
    skills: ["ci-cd-quality-gates", "devops"],
    tools: ["run_pipeline.sh", "preflight.sh --full", "security/run_gates.py"],
    inputs: ["Commits", "Full suite green"],
    outputs: ["Pipeline dashboard", "Gate log", "Security report"],
    integration: ["cli", "ci"],
  },
  {
    id: "pr",
    kind: "publish",
    title: "Pull Request",
    subtitle: "PR titled [#N] type(scope): description, with template. preflight --full is the PR proof",
    phase: "Publish",
    agent: "backend-implementer",
    skills: ["openspec-apply-change"],
    tools: ["gh pr create", "preflight.sh --full"],
    inputs: ["Green pipeline", "Conventional commits"],
    outputs: ["Open PR", "Issue comment with stage"],
    integration: ["github", "cli"],
  },

  // ── SHIP ──
  {
    id: "review",
    kind: "ship",
    title: "Code Review",
    subtitle: "Gate 4: Code owner or Foreman review with no open findings",
    phase: "Ship",
    agent: "code-reviewer",
    skills: ["ai-agent-workflow", "programming"],
    tools: ["gh pr review", "check-heavy-ci.sh"],
    inputs: ["Open PR", "Green preflight"],
    outputs: ["Review notes", "Approved PR or findings"],
    gate: {
      number: 4,
      name: "Do not merge if",
      check: "check-heavy-ci.sh; aisdlc protected; protect-main ruleset",
      items: ["CI red or pending", "Review unresolved", "Merge conflicts", "Docs pending"],
    },
    human: {
      role: "Owner",
      action: "Merges Constitution, harness, and sdlc-exception PRs",
      when: "When aisdlc protected flags owner-only paths",
    },
    integration: ["github", "cli"],
  },
  {
    id: "merge",
    kind: "ship",
    title: "Merge & Archive",
    subtitle: "Via PR only. OpenSpec change archived in the same PR (#1623)",
    phase: "Ship",
    agent: "cloud-foreman",
    skills: ["ai-agent-workflow"],
    tools: ["gh pr merge", "openspec archive --yes"],
    inputs: ["Approved PR", "Green CI"],
    outputs: ["Merged branch", "Archived OpenSpec change"],
    integration: ["github", "cli"],
  },
  {
    id: "deploy",
    kind: "ship",
    title: "Deploy & Sign",
    subtitle: "CI → CD builds, signs (cosign), and publishes images to GHCR",
    phase: "Ship",
    agent: "devops-engineer",
    skills: ["ci-cd-quality-gates", "devops", "operations-observability-readiness"],
    tools: ["cd.yml", "cosign", "GHCR"],
    inputs: ["Merged PR on main", "Version tag"],
    outputs: ["Signed Docker images", "SBOMs", "GitHub Pages deploy"],
    integration: ["ci", "github"],
  },
  {
    id: "smoke",
    kind: "ship",
    title: "Smoke Test & Close",
    subtitle: "Health endpoint and key flow on target environment. Close the issue only after deploy",
    phase: "Ship",
    agent: "cloud-foreman",
    skills: ["ai-agent-workflow", "devsecops-traceability"],
    tools: ["gh issue close", "check-board-status-label-coherence.py"],
    inputs: ["Deployed images", "Health endpoint UP"],
    outputs: ["Smoke evidence", "Closed issue", "Board Status: Done"],
    gate: {
      number: 5,
      name: "Not finished until",
      check: "check-board-status-label-coherence.py; archive hygiene on main",
      items: ["Deploy succeeded", "Smoke test passed", "Issue closed", "Board Status and labels coherent"],
    },
    integration: ["github", "api"],
  },
];

/* ───────────────────── Agents on the line ───────────────────── */

export interface AgentOnLine {
  name: string;
  emoji: string;
  role: string;
  stations: string[];
  color: string;
}

export const AGENTS_ON_LINE: AgentOnLine[] = [
  { name: "cloud-foreman", emoji: "🧭", role: "Orchestrator", stations: ["foreman-claim", "branch", "merge"], color: "#6366f1" },
  { name: "openspec-planner", emoji: "📋", role: "Spec writer", stations: ["spec"], color: "#8b5cf6" },
  { name: "java-architect", emoji: "🏗️", role: "Architecture", stations: ["architecture"], color: "#a855f7" },
  { name: "testing-qa", emoji: "🧪", role: "Test design", stations: ["test-design", "full-suite"], color: "#06b6d4" },
  { name: "backend-implementer", emoji: "⚙️", role: "Implementation", stations: ["failing-tests", "implement", "docs", "commits", "pr"], color: "#10b981" },
  { name: "frontend-design", emoji: "🎨", role: "UI design", stations: ["implement"], color: "#f59e0b" },
  { name: "devops-engineer", emoji: "🔧", role: "CI/CD", stations: ["pipeline", "deploy"], color: "#f97316" },
  { name: "code-reviewer", emoji: "🔍", role: "Gate 4 review", stations: ["review"], color: "#ef4444" },
  { name: "security-auditor", emoji: "🔒", role: "Security", stations: ["pipeline"], color: "#dc2626" },
  { name: "sync_issues_and_code", emoji: "🔄", role: "Issue hygiene", stations: ["intake"], color: "#64748b" },
];

/* ───────────────────── Integration points ───────────────────── */

export interface IntegrationPoint {
  id: string;
  name: string;
  icon: string;
  description: string;
  stations: string[];
  protocol: string;
}

export const INTEGRATION_POINTS: IntegrationPoint[] = [
  {
    id: "github",
    name: "GitHub API",
    icon: "🐙",
    description: "Issues, PRs, labels, comments, board status, rulesets",
    stations: ["intake", "foreman-claim", "branch", "pr", "review", "merge", "smoke"],
    protocol: "REST + GraphQL via gh CLI",
  },
  {
    id: "cli",
    name: "CLI Tools",
    icon: "💻",
    description: "aisdlc, openspec, git, mvn, vitest, Bruno, Playwright",
    stations: ["foreman-claim", "spec", "test-design", "failing-tests", "implement", "docs", "commits", "pipeline", "pr", "merge"],
    protocol: "Exit codes + JSON output",
  },
  {
    id: "mcp",
    name: "MCP Server",
    icon: "🔌",
    description: "RepoLens knowledge graph for code navigation and impact analysis",
    stations: ["architecture", "implement"],
    protocol: "JSON-RPC over stdio",
  },
  {
    id: "ci",
    name: "GitHub Actions",
    icon: "⚡",
    description: "CI/CD workflows: build, test, security, deploy, sign",
    stations: ["full-suite", "pipeline", "deploy"],
    protocol: "YAML workflows + artifacts",
  },
  {
    id: "api",
    name: "Health API",
    icon: "🏥",
    description: "Smoke test endpoints on deployed environments",
    stations: ["smoke"],
    protocol: "HTTP GET /actuator/health",
  },
];

/* ───────────────────── Event stream (example run) ───────────────────── */

export interface Event {
  time: string;
  station: string;
  agent: string;
  action: string;
  result: "pass" | "fail" | "pending" | "human";
  detail?: string;
}

export const EXAMPLE_EVENTS: Event[] = [
  { time: "00:00", station: "intake", agent: "sync_issues_and_code", action: "Issue #2380 created", result: "pass", detail: "type:feature, area:devops, priority:medium" },
  { time: "00:01", station: "foreman-claim", agent: "cloud-foreman", action: "Claim #2380", result: "pass", detail: "Intake lint: pass, dispatch allowed" },
  { time: "00:02", station: "spec", agent: "openspec-planner", action: "OpenSpec propose", result: "pass", detail: "Gate 1: Issue resolves live, AC present" },
  { time: "00:03", station: "architecture", agent: "java-architect", action: "Impact analysis", result: "pass", detail: "Modules affected: framework/ai-sdlc, github-page" },
  { time: "00:04", station: "branch", agent: "cloud-foreman", action: "Create branch feat/2380_factory_line", result: "pass", detail: "Pushed, Issue commented" },
  { time: "00:05", station: "test-design", agent: "testing-qa", action: "Design test cases", result: "pass", detail: "Happy path + edge cases for dashboard rendering" },
  { time: "00:06", station: "failing-tests", agent: "backend-implementer", action: "Write failing tests", result: "pass", detail: "Gate 2: Tests committed, TEST_CMD fails" },
  { time: "00:07", station: "implement", agent: "backend-implementer", action: "Implement dashboard", result: "pass", detail: "factory-line-content.ts + page.tsx created" },
  { time: "00:08", station: "full-suite", agent: "testing-qa", action: "Run full suite", result: "pass", detail: "mvn verify + vitest + Playwright green" },
  { time: "00:09", station: "docs", agent: "backend-implementer", action: "Update docs", result: "pass", detail: "CHANGELOG + permanent docs updated" },
  { time: "00:10", station: "commits", agent: "backend-implementer", action: "Atomic commits", result: "pass", detail: "Conventional commits, Closes #2380" },
  { time: "00:11", station: "pipeline", agent: "devops-engineer", action: "run_pipeline.sh", result: "pass", detail: "preflight --full + security gates green" },
  { time: "00:12", station: "pr", agent: "backend-implementer", action: "Open PR", result: "pass", detail: "[#2380] feat(pages): add factory production line dashboard" },
  { time: "00:13", station: "review", agent: "code-reviewer", action: "Gate 4 review", result: "human", detail: "Owner review needed for pages changes" },
  { time: "00:14", station: "merge", agent: "cloud-foreman", action: "Merge PR", result: "pass", detail: "OpenSpec archived, branch merged" },
  { time: "00:15", station: "deploy", agent: "devops-engineer", action: "Deploy to GitHub Pages", result: "pass", detail: "Pages auto-deployed on main" },
  { time: "00:16", station: "smoke", agent: "cloud-foreman", action: "Smoke test + close", result: "pass", detail: "Health UP, issue closed, board Done" },
];

/* ───────────────────── Mermaid diagrams ───────────────────── */

export const FACTORY_LINE_DIAGRAM = `
flowchart LR
  subgraph intake["📥 INTAKE"]
    direction TB
    I1["Issue arrives"] --> I2["Intake lint"] --> I3["Foreman claim"]
  end
  subgraph plan["📋 PLAN"]
    direction TB
    P1["OpenSpec spec"] --> P2["Architecture"] --> P3["Branch"]
  end
  subgraph prove["🧪 PROVE"]
    direction TB
    T1["Test design"] --> T2["Failing tests"] --> T3["Implement"] --> T4["Full suite"]
  end
  subgraph publish["📦 PUBLISH"]
    direction TB
    D1["Docs"] --> D2["Commits"] --> D3["Pipeline"] --> D4["PR"]
  end
  subgraph ship["🚀 SHIP"]
    direction TB
    S1["Review"] --> S2["Merge"] --> S3["Deploy"] --> S4["Smoke"]
  end
  intake ==> plan ==> prove ==> publish ==> ship
  classDef intake fill:#f0f9ff,stroke:#0284c7,color:#082f49
  classDef plan fill:#ede9fe,stroke:#6d28d9,color:#2e1065
  classDef prove fill:#ecfdf5,stroke:#059669,color:#064e3b
  classDef publish fill:#fefce8,stroke:#ca8a04,color:#713f12
  classDef ship fill:#fef2f2,stroke:#dc2626,color:#7f1d1d
  class I1,I2,I3 intake
  class P1,P2,P3 plan
  class T1,T2,T3,T4 prove
  class D1,D2,D3,D4 publish
  class S1,S2,S3,S4 ship
`;

export const AGENT_FLOW_DIAGRAM = `
sequenceDiagram
  autonumber
  participant O as Owner (human)
  participant F as Foreman
  participant W as Worker agent
  participant G as Gates (scripts)
  participant CI as GitHub Actions
  participant GH as GitHub

  O->>GH: Create Issue + Use Case
  F->>GH: Claim issue
  F->>G: Gate 1 — validate-sdlc-plan.sh
  G-->>F: PASS
  F->>W: Brief: issue + UC + AC
  W->>W: OpenSpec propose
  W->>W: Architecture + design
  W->>GH: Branch + push
  W->>W: Test design + failing tests
  W->>G: Gate 2 — aisdlc tdd
  G-->>W: PASS
  W->>W: Implement
  W->>W: Full suite + E2E
  W->>W: Docs + commits
  W->>G: Gate 3 — preflight.sh --full
  G-->>W: PASS
  W->>GH: Open PR
  F->>G: Gate 4 — check-heavy-ci.sh
  G-->>F: PASS
  F->>GH: Review + merge
  GH->>CI: Pages deploy on main
  CI-->>F: Deployed
  F->>G: Gate 5 — smoke test
  G-->>F: PASS
  F->>GH: Close issue + board Done
`;

/* ───────────────────── Phase colors ───────────────────── */

export const PHASE_COLORS: Record<string, { bg: string; border: string; text: string; accent: string }> = {
  Intake: { bg: "bg-sky-50", border: "border-sky-200", text: "text-sky-800", accent: "bg-sky-500" },
  Plan: { bg: "bg-violet-50", border: "border-violet-200", text: "text-violet-800", accent: "bg-violet-500" },
  Prove: { bg: "bg-emerald-50", border: "border-emerald-200", text: "text-emerald-800", accent: "bg-emerald-500" },
  Publish: { bg: "bg-amber-50", border: "border-amber-200", text: "text-amber-800", accent: "bg-amber-500" },
  Ship: { bg: "bg-rose-50", border: "border-rose-200", text: "text-rose-800", accent: "bg-rose-500" },
};

/* ───────────────────── Station kind icons ───────────────────── */

export const STATION_ICONS: Record<string, string> = {
  intake: "📥",
  plan: "📋",
  prove: "🧪",
  publish: "📦",
  ship: "🚀",
  gate: "🚧",
  human: "👤",
};
