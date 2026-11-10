/**
 * Node, edge and step data for the interactive FlowDiagrams (#1977). Names and paths are the real
 * ones in the repository; the counts come from lib/tech-facts.ts and are passed in by the pages.
 */
import type { FlowEdge, FlowNode, FlowStep } from "@/components/diagrams/FlowDiagram";
import { src } from "@/lib/ai-sdlc-content";

type Flow = { nodes: FlowNode[]; edges: FlowEdge[]; steps: FlowStep[]; width: number; height: number };

const JAVA = "backend-api/src/main/java/com/licensis/notaire";

/** One request through the payment slice: POST /api/v1/payments (CU15). */
export function requestFlow(): Flow {
  const y = 150;
  const nodes: FlowNode[] = [
    { id: "ui", label: "Next.js app", sub: "POST /api/v1/payments", x: 95, y, w: 170, kind: "actor" },
    { id: "ctl", label: "PaymentController", sub: "adapter/in/web", x: 290, y, w: 170, kind: "adapter" },
    { id: "in", label: "ProcessPaymentUseCase", sub: "port/in", x: 495, y, w: 190, kind: "port" },
    { id: "svc", label: "ProcessPaymentService", sub: "application/usecase", x: 715, y, w: 190, kind: "core" },
    { id: "dom", label: "Money · BudgetCharges", sub: "domain/payment", x: 715, y: 40, w: 190, kind: "core" },
    { id: "bp", label: "BudgetLookupPort", sub: "port/out", x: 930, y: 80, w: 170, kind: "port" },
    { id: "rp", label: "PaymentRepositoryPort", sub: "port/out", x: 930, y: 230, w: 180, kind: "port" },
    { id: "ba", label: "BudgetLookupAdapter", sub: "adapter/out/persistence", x: 1140, y: 80, w: 180, kind: "adapter" },
    { id: "pa", label: "PaymentPersistenceAdapter", sub: "adapter/out/persistence", x: 1140, y: 230, w: 200, kind: "adapter" },
    { id: "db", label: "PostgreSQL", sub: "Flyway schema", x: 1340, y, w: 140, kind: "store" },
  ];
  const edges: FlowEdge[] = [
    { from: "ui", to: "ctl", label: "JSON + JWT" }, // 0
    { from: "ctl", to: "in", label: "command" }, // 1
    { from: "in", to: "svc", label: "implements" }, // 2
    { from: "svc", to: "dom", label: "rules" }, // 3
    { from: "svc", to: "bp", label: "findCharges" }, // 4
    { from: "bp", to: "ba" }, // 5
    { from: "ba", to: "db" }, // 6
    { from: "svc", to: "rp", label: "register" }, // 7
    { from: "rp", to: "pa" }, // 8
    { from: "pa", to: "db" }, // 9
  ];
  const steps: FlowStep[] = [
    {
      title: "The UI posts a payment",
      body: "The browser sends the budget id, amount, date and method as JSON with the user's JWT. Spring Security checks the token before any Notaire code runs.",
      nodes: ["ui"],
      edges: [0],
    },
    {
      title: "The controller makes a command",
      body: "PaymentController.processPayment (CU15) validates the body and builds a ProcessPaymentCommand. It knows the in-port interface, not the service class.",
      nodes: ["ctl"],
      edges: [1],
      source: { label: "PaymentController.java", href: src(`${JAVA}/adapter/in/web/payment/PaymentController.java`) },
    },
    {
      title: "The in-port is the contract",
      body: "ProcessPaymentUseCase is a plain interface in application/port/in. Spring injects whichever class implements it.",
      nodes: ["in"],
      edges: [2],
      source: { label: "ProcessPaymentUseCase.java", href: src(`${JAVA}/application/port/in/payment/ProcessPaymentUseCase.java`) },
    },
    {
      title: "The use case applies the rules",
      body: "ProcessPaymentService normalises the amount with Money, then refuses a payment that is zero or larger than the pending balance BudgetCharges computes. The rules live in domain/payment, with no Spring or JPA.",
      nodes: ["svc", "dom"],
      edges: [3],
      source: { label: "ProcessPaymentService.java", href: src(`${JAVA}/application/usecase/payment/ProcessPaymentService.java`) },
    },
    {
      title: "It asks for the charges through an out-port",
      body: "budgets.findCharges(budgetId) goes through BudgetLookupPort. BudgetLookupAdapter answers from the database and maps the JPA entities into domain types.",
      nodes: ["bp", "ba", "db"],
      edges: [4, 5, 6],
      source: { label: "BudgetLookupAdapter.java", href: src(`${JAVA}/adapter/out/persistence/payment/BudgetLookupAdapter.java`) },
    },
    {
      title: "It stores the payment through another",
      body: "payments.register(new NewPayment(…)) goes through PaymentRepositoryPort, inside one transaction. PaymentPersistenceAdapter writes the row and returns PaymentDetails.",
      nodes: ["rp", "pa", "db"],
      edges: [7, 8, 9],
      source: { label: "PaymentPersistenceAdapter.java", href: src(`${JAVA}/adapter/out/persistence/payment/PaymentPersistenceAdapter.java`) },
    },
    {
      title: "The answer goes back the same way",
      body: "The service returns PaymentDetails; PaymentWebMapper turns it into the JSON response. Swapping the database or the web framework would touch only the outer ring.",
      nodes: ["ctl", "ui"],
      edges: [1, 0],
      source: { label: "PaymentWebMapper.java", href: src(`${JAVA}/adapter/in/web/payment/PaymentWebMapper.java`) },
    },
  ];
  return { nodes, edges, steps, width: 1420, height: 290 };
}

/** The committed OpenAPI document and the checks that hang off it. */
export function contractFlow(ops: { paths: number; operations: number } | null, brunoRequests: number): Flow {
  const nodes: FlowNode[] = [
    { id: "code", label: "Spring controllers", sub: "springdoc annotations", x: 110, y: 150, w: 180, kind: "adapter" },
    { id: "spec", label: "openapi.yaml", sub: ops ? `${ops.paths} paths · ${ops.operations} ops` : "committed", x: 360, y: 150, w: 180, kind: "core" },
    { id: "diff", label: "oasdiff", sub: "vs the base branch", x: 620, y: 45, w: 170, kind: "check" },
    { id: "types", label: "api.generated.ts", sub: "openapi-typescript", x: 620, y: 150, w: 180, kind: "check" },
    { id: "reach", label: "Reachability", sub: "contracts/tests", x: 620, y: 255, w: 170, kind: "check" },
    { id: "fail", label: "PR fails", sub: "unless accepted", x: 870, y: 45, w: 150, kind: "fail" },
    { id: "http", label: "Front-end adapters", sub: "adapters/http", x: 870, y: 150, w: 180, kind: "adapter" },
    { id: "bruno", label: "Bruno suite", sub: `${brunoRequests} requests`, x: 870, y: 255, w: 160, kind: "ok" },
  ];
  const edges: FlowEdge[] = [
    { from: "code", to: "spec", label: "export-openapi.sh" }, // 0
    { from: "spec", to: "diff" }, // 1
    { from: "diff", to: "fail", label: "breaking", dashed: true }, // 2
    { from: "spec", to: "types" }, // 3
    { from: "types", to: "http" }, // 4
    { from: "spec", to: "reach" }, // 5
    { from: "reach", to: "bruno", dashed: true }, // 6
  ];
  const steps: FlowStep[] = [
    {
      title: "Code is the source",
      body: "Controllers carry springdoc annotations. export-openapi.sh boots the backend on H2 and writes the document; CI fails if the committed copy differs.",
      nodes: ["code", "spec"],
      edges: [0],
      source: { label: "export-openapi.sh", href: src("backend-api/tools/export-openapi.sh") },
    },
    {
      title: "Breaking changes need a ticket",
      body: "oasdiff compares the document with the base branch. An error-level break fails the PR unless a file in accepted-breaking-changes.d names it and its issue.",
      nodes: ["diff", "fail"],
      edges: [1, 2],
      source: { label: "openapi-contract.yml", href: src(".github/workflows/openapi-contract.yml") },
    },
    {
      title: "The front end's types are generated",
      body: "openapi-typescript writes api.generated.ts; frontend-ci runs openapi:types:check so the HTTP adapters cannot drift from the backend.",
      nodes: ["types", "http"],
      edges: [3, 4],
      source: { label: "FRONTEND-OPENAPI-TYPES.md", href: src("docs/200-architecture/203-design/FRONTEND-OPENAPI-TYPES.md") },
    },
    {
      title: "Every endpoint has a caller",
      body: "A contract test lists each operation and fails when one has no caller in the front end and no reason in the allowlist.",
      nodes: ["reach"],
      edges: [5],
      source: { label: "test_api_reachability.py", href: src("contracts/tests/test_api_reachability.py") },
    },
    {
      title: "Bruno calls the running API",
      body: "Bruno requests hit the real backend with real tokens and assert the responses, in CI and on a laptop.",
      nodes: ["bruno"],
      edges: [6],
      source: { label: "backend-api/api-test", href: src("backend-api/api-test") },
    },
  ];
  return { nodes, edges, steps, width: 980, height: 300 };
}

/**
 * One change through the factory, as it is on main.
 * Gate 4 is not "only the Owner": the Foreman may merge a Worker pull request;
 * the Owner merges protected paths; Workers never merge (#1624, #2001, #2052).
 * `aisdlc foreman plan` prints a route and does not execute. Plugin allow list
 * is openspec and example-method.
 */
export function factoryFlow(): Flow {
  const nodes: FlowNode[] = [
    { id: "issue", label: "Issue", sub: "linked to a use case", x: 140, y: 70, w: 160, kind: "actor" },
    { id: "g1", label: "Gate 1", sub: "OpenSpec, then validate", x: 340, y: 70, w: 180, kind: "port" },
    { id: "g2", label: "Gate 2", sub: "tests seen failing", x: 540, y: 70, w: 170, kind: "fail" },
    { id: "impl", label: "Implement", sub: "minimum code", x: 740, y: 70, w: 160, kind: "core" },
    { id: "g3", label: "Gate 3", sub: "preflight.sh --full", x: 140, y: 190, w: 180, kind: "check" },
    { id: "g4", label: "Gate 4", sub: "Foreman or Owner", x: 360, y: 190, w: 180, kind: "port" },
    { id: "g5", label: "Gate 5", sub: "tag, smoke, close", x: 570, y: 190, w: 170, kind: "ok" },
    { id: "done", label: "Closed", sub: "issue done", x: 760, y: 190, w: 140, kind: "ok" },
    { id: "kit", label: "aisdlc kit", sub: "exit codes only", x: 250, y: 320, w: 180, kind: "check" },
    { id: "back", label: "Refusal", sub: "non-zero, back to work", x: 560, y: 320, w: 220, kind: "fail" },
  ];
  const edges: FlowEdge[] = [
    { from: "issue", to: "g1" }, // 0
    { from: "g1", to: "g2" }, // 1
    { from: "g2", to: "impl" }, // 2
    { from: "impl", to: "g3" }, // 3
    { from: "g3", to: "g4" }, // 4
    { from: "g4", to: "g5" }, // 5
    { from: "g5", to: "done" }, // 6
    { from: "g1", to: "back", dashed: true }, // 7
    { from: "g3", to: "back", dashed: true }, // 8
    { from: "g4", to: "back", dashed: true }, // 9
    { from: "g3", to: "kit", dashed: true }, // 10
  ];
  const steps: FlowStep[] = [
    {
      title: "It starts with a real issue",
      body: "Every change starts from one GitHub Issue linked to a use case or a requirement. A prompt is not a specification.",
      nodes: ["issue"],
      edges: [0],
    },
    {
      title: "Gate 1: plan before code",
      body: "An OpenSpec change on the notaire-sdlc schema describes only this change. validate-sdlc-plan.sh checks that the Issue exists and is open. The kit does not write the spec.",
      nodes: ["g1"],
      edges: [1],
    },
    {
      title: "Gate 2: watch the tests fail",
      body: "Tests are written first and seen failing. A test that was never red proves nothing.",
      nodes: ["g2"],
      edges: [2],
    },
    {
      title: "The minimum code that passes",
      body: "The implementation is the minimum that turns those tests green, using the project skills. It does not skip a gate.",
      nodes: ["impl"],
      edges: [3],
    },
    {
      title: "Gate 3: the local proof",
      body: "The pull-request proof is preflight.sh --full on the agent machine. GitHub does not re-run the product suite on a pull request. Product CI runs on a version tag.",
      nodes: ["g3"],
      edges: [4],
    },
    {
      title: "Gate 4: who may merge",
      body: "The Foreman may merge a Worker pull request when its merge rule is met. The Owner merges Constitution, sdlc-exception, and harness changes. Workers never merge.",
      nodes: ["g4"],
      edges: [5],
    },
    {
      title: "Gate 5: shipped and closed",
      body: "A version tag runs product CI and image publish. Smoke passes, then the Issue is closed. Pages deploy on the push to main, not as a substitute for that tag.",
      nodes: ["g5", "done"],
      edges: [6],
    },
    {
      title: "What the kit on main does",
      body: "The aisdlc kit runs gates by exit code. aisdlc foreman plan only prints a route; it does not run agents. The plugin allow list is openspec and example-method. Spec Kit and AWS AI-DLC are declared and not loaded.",
      nodes: ["kit", "g3"],
      edges: [10],
    },
    {
      title: "A refusal is a loop, not a crash",
      body: "A gate whose command exits non-zero sends the change back with the reason. Agents cannot skip a gate; they can only satisfy it.",
      nodes: ["back", "g1", "g3", "g4"],
      edges: [7, 8, 9],
    },
  ];
  return { nodes, edges, steps, width: 900, height: 390 };
}

/** Where a change is proved: the agent machine, a version tag, main, and the schedule. */
export function qualityGateFlow(): Flow {
  const nodes: FlowNode[] = [
    { id: "local", label: "Agent machine", sub: "preflight.sh --full", x: 220, y: 70, w: 220, kind: "check" },
    { id: "tag", label: "Version tag", sub: "product CI and images", x: 220, y: 190, w: 220, kind: "ok" },
    { id: "main", label: "Push to main", sub: "Pages, Release Please", x: 520, y: 190, w: 230, kind: "port" },
    { id: "week", label: "Schedule", sub: "ZAP, k6, backup", x: 520, y: 70, w: 200, kind: "store" },
  ];
  const edges: FlowEdge[] = [
    { from: "local", to: "tag" }, // 0
    { from: "tag", to: "main" }, // 1
    { from: "local", to: "week", dashed: true }, // 2
  ];
  const steps: FlowStep[] = [
    {
      title: "On the agent machine",
      body: "The pull-request proof is preflight.sh --full, including the security case catalog. GitHub does not re-run that suite on the pull request. Workers never merge.",
      nodes: ["local"],
      edges: [0],
    },
    {
      title: "On a version tag",
      body: "Product CI, Bruno, Playwright, and image publish run when a version tag is pushed, or when someone dispatches the workflow. A quiet pull request is the design.",
      nodes: ["tag"],
      edges: [1],
    },
    {
      title: "On a push to main",
      body: "Release Please and the portfolio publish on main. A merge to main does not publish the product images.",
      nodes: ["main"],
      edges: [],
    },
    {
      title: "On a schedule",
      body: "Monday runs the ZAP baseline and CodeQL. Sunday runs backup-restore. k6 is on its own Monday schedule. These do not start on a pull request.",
      nodes: ["week"],
      edges: [2],
    },
  ];
  return { nodes, edges, steps, width: 760, height: 280 };
}

/** Browser to database, the same boundaries as docs/200-architecture/206-security/diagrams/trust-boundaries.puml. */
export function trustBoundaryFlow(): Flow {
  const y = 150;
  const nodes: FlowNode[] = [
    { id: "staff", label: "Notary browser", sub: "HttpOnly cookie", x: 110, y, w: 160, kind: "actor" },
    { id: "bff", label: "Next.js BFF", sub: "/api/v1 proxy", x: 320, y, w: 160, kind: "adapter" },
    { id: "jwt", label: "JWT filter", sub: "authenticates", x: 530, y, w: 160, kind: "check" },
    { id: "grant", label: "Module grant", sub: "403 if missing", x: 750, y, w: 170, kind: "check" },
    { id: "uc", label: "Use cases", sub: "Person, deed, budget", x: 970, y, w: 180, kind: "core" },
    { id: "db", label: "PostgreSQL", sub: "PII and deeds", x: 1180, y, w: 150, kind: "store" },
  ];
  const edges: FlowEdge[] = [
    { from: "staff", to: "bff", label: "HTTPS" },
    { from: "bff", to: "jwt" },
    { from: "jwt", to: "grant" },
    { from: "grant", to: "uc" },
    { from: "uc", to: "db", label: "parameterized SQL" },
  ];
  const steps: FlowStep[] = [
    {
      title: "The browser is outside the trust boundary",
      body: "Staff send the session cookie. Headers, the body, and the query are data. A client header is not an identity.",
      nodes: ["staff"],
      edges: [0],
    },
    {
      title: "The BFF proxies on the server",
      body: "Next.js calls the API with the runtime backend address. The public login page does not reveal that host.",
      nodes: ["bff"],
      edges: [1],
    },
    {
      title: "The JWT filter authenticates",
      body: "JwtAuthenticationFilter checks the cookie or the bearer token before a use case runs.",
      nodes: ["jwt"],
      edges: [2],
    },
    {
      title: "The module grant authorizes the path",
      body: "RoleModuleAuthorizationFilter returns 403 when the role does not hold the module for that path. A notary is not an administrator.",
      nodes: ["grant"],
      edges: [3],
    },
    {
      title: "The use case writes through parameters",
      body: "The use case reaches PostgreSQL with parameterized JPA. Personal data and notarial records stay inside this last boundary.",
      nodes: ["uc", "db"],
      edges: [4],
    },
  ];
  return { nodes, edges, steps, width: 1320, height: 260 };
}
