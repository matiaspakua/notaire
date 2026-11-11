/** Spanish for Diagram captions and Mermaid sentence labels (#2202). */
import { pair } from "./es";

/* Diagram caption= under github-page/app page.tsx files */
pair(
  "The standard management workflow for a sale. Payment and the testimony movement are separate state machines.",
  "El workflow estándar de gestión para una venta. El pago y el movimiento del testimonio son máquinas de estados aparte.",
);
pair(
  "Same boundaries as the PlantUML trust-boundary diagram in the architecture docs. Demo labels only.",
  "Los mismos límites que el diagrama PlantUML de la documentación de arquitectura. Solo etiquetas de demo.",
);
pair(
  "The Foreman briefs specialists and reviews their evidence. The Owner holds the decisions.",
  "El Foreman instruye a los especialistas y revisa su evidencia. El Owner tiene las decisiones.",
);
pair(
  "One rulebook feeds the Foreman and the Workers. Evidence from the five gates, not a claim, decides what merges.",
  "Un solo reglamento alimenta al Foreman y a los Workers. La evidencia de las cinco gates, no una afirmación, decide qué se fusiona.",
);
pair("Step through the gates; each one is a command with an exit code.", "Recorré las gates; cada una es un comando con un código de salida.");
pair(
  "Purple: governance. Green: the kit. Your toolchain stays as it is; the ledger feeds the learning loop.",
  "Violeta: gobierno. Verde: el kit. Tu toolchain se queda como está; el ledger alimenta el ciclo de aprendizaje.",
);
pair(
  "Pick the level from how many agents work at once and how much runs without a human.",
  "Elegí el nivel según cuántos agentes trabajan a la vez y cuánto corre sin una persona.",
);
pair(
  "Every incident is asked one question: can a command detect it? If yes, it becomes a gate.",
  "A cada incidente se le hace una pregunta: ¿puede detectarlo un comando? Si sí, se vuelve una gate.",
);
pair(
  "docs/300-development/305-project-management/LABELS.md — status lifecycle for the agent fleet.",
  "docs/300-development/305-project-management/LABELS.md — ciclo de vida de status para la flota de agentes.",
);
pair(
  "From first push to closed Issue. Every stage is visible on GitHub, so another agent can resume.",
  "Del primer push al Issue cerrado. Cada etapa se ve en GitHub, así otro agente puede retomar.",
);
pair(
  "Purple: policy and context. Blue: mechanical enforcement. Red: the human Owner.",
  "Violeta: política y contexto. Azul: enforcement mecánico. Rojo: el Owner humano.",
);
pair(
  "CONSTITUTION.md §5 Official SDLC Workflow. The yellow hexagons are the hard gates of §6.",
  "CONSTITUTION.md §5 Official SDLC Workflow. Los hexágonos amarillos son las gates duras del §6.",
);
pair(
  "Explore → triage into real Issues → propose → apply with TDD → archive in the same PR → living specs.",
  "Explore → triage en Issues reales → propose → apply con TDD → archive en el mismo pull request → specs vivas.",
);
pair(
  "Step from the agent machine to the tag, then to main. The schedule is beside them, not on the pull request.",
  "Del agente al tag, y después a main. El schedule está al lado, no en el pull request.",
);
pair(
  "The same story as one map: local preflight, the merge rule, the version tag, main, and the schedule.",
  "La misma historia en un solo mapa: preflight local, la regla de merge, el tag de versión, main y el schedule.",
);
pair(
  "The committed OpenAPI document sits between the backend and everything that depends on it.",
  "El documento OpenAPI commiteado está entre el backend y todo lo que depende de él.",
);
pair(
  "A migration is tested in CI against a real PostgreSQL, recorded in the release manifest, and applied at start-up.",
  "Una migración se prueba en CI contra un PostgreSQL real, se registra en el manifiesto de release y se aplica al arrancar.",
);
pair(
  "A change goes from issue to a version tag, and what operations learn comes back as a new issue.",
  "Un cambio va del Issue a un tag de versión, y lo que aprende operaciones vuelve como un Issue nuevo.",
);
pair(
  "Backend packages under com.licensis.notaire and the direction of their dependencies (ADR-030).",
  "Paquetes del backend bajo com.licensis.notaire y la dirección de sus dependencias (ADR-030).",
);
pair(
  "Every box is a real class in the payment slice; the step panel links its file.",
  "Cada caja es una clase real de la tajada de pago; el panel de pasos enlaza su archivo.",
);

/* Mermaid sentence labels in ai-sdlc-content.ts (exact substrings, incl. <br/>) */

// FACTORY_VS_PRODUCT
pair("Spec engine<br/>OpenSpec notaire-sdlc", "Motor de specs<br/>OpenSpec notaire-sdlc");
pair("Agent crew<br/>.claude/agents + skills", "Tripulación de agentes<br/>.claude/agents + skills");
pair("Gates<br/>preflight · CI · ruleset", "Gates<br/>preflight · CI · ruleset");
pair("frontend<br/>Next.js", "frontend<br/>Next.js");
pair("backend-api<br/>Spring Boot", "backend-api<br/>Spring Boot");
pair("PostgreSQL 16<br/>Flyway", "PostgreSQL 16<br/>Flyway");
pair("Policy<br/>CONSTITUTION.md", "Política<br/>CONSTITUTION.md");

// LIFECYCLE_FLOW
pair("1 · Issue + Use Case", "1 · Issue + caso de uso");
pair("2 · Refine requirement", "2 · Afinar el requisito");
pair("3 · OpenSpec specification", "3 · Especificación OpenSpec");
pair("GATE 1<br/>Issue · Spec · AC", "GATE 1<br/>Issue · Spec · AC");
pair("4 · Impact analysis<br/>5 · Architecture review / ADR", "4 · Análisis de impacto<br/>5 · Revisión de arquitectura / ADR");
pair("6 · Branch type/N_desc<br/>7 · Acceptance criteria", "6 · Rama type/N_desc<br/>7 · Criterios de aceptación");
pair("8 · Design test cases", "8 · Diseñar casos de prueba");
pair("GATE 2<br/>tests written and seen failing", "GATE 2<br/>tests escritos y vistos fallar");
pair("9–10 · Failing unit + integration tests", "9–10 · Tests unit e integración que fallan");
pair("11 · Implement · 12 · Update tests", "11 · Implementar · 12 · Actualizar tests");
pair("13–15 · Suite · regression · Playwright", "13–15 · Suite · regresión · Playwright");
pair("16 · Permanent documentation", "16 · Documentación permanente");
pair("GATE 3<br/>green suite · coverage · docs", "GATE 3<br/>suite en verde · cobertura · docs");
pair("17 · Atomic commits<br/>17.5 · run_pipeline.sh", "17 · Commits atómicos<br/>17.5 · run_pipeline.sh");
pair("18 · Pull request<br/>19 · preflight.sh --full", "18 · Pull request<br/>19 · preflight.sh --full");
pair("GATE 4<br/>review · local evidence<br/>product CI is the tag", "GATE 4<br/>review · evidencia local<br/>el CI del producto es el tag");
pair("21 · Merge via PR", "21 · Merge por pull request");
pair("22 · Pages on main<br/>product CI + GHCR on the tag", "22 · Pages en main<br/>CI del producto + GHCR en el tag");
pair("GATE 5<br/>smoke test · issue closed", "GATE 5<br/>smoke test · Issue cerrado");
pair("24 · Close issue · archive spec", "24 · Cerrar Issue · archivar spec");

// GUARDRAIL_LAYERS — full node labels (must include <br/> as in the chart)
pair(
  "9 · Human Owner<br/>approves specs · grants exceptions · merges policy",
  "9 · Owner humano<br/>aprueba specs · otorga excepciones · fusiona política",
);
pair(
  "8 · Repository rules<br/>protect-main: PR only · no force-push · live checks are Owner-owned",
  "8 · Reglas del repositorio<br/>protect-main: solo pull request · sin force-push · los chequeos en vivo los posee el Owner",
);
pair(
  "7 · Release-tag quality<br/>tests · Trivy · CodeQL · Monday ZAP",
  "7 · Calidad del tag de release<br/>tests · Trivy · CodeQL · ZAP los lunes",
);
pair(
  "6 · Process checks<br/>preflight locally · the workflow on the version tag",
  "6 · Chequeos de proceso<br/>preflight en local · el workflow en el tag de versión",
);
pair(
  "5 · Local gates<br/>preflight.sh pre-push · run_pipeline.sh · validate-sdlc-plan.sh",
  "5 · Gates locales<br/>preflight.sh pre-push · run_pipeline.sh · validate-sdlc-plan.sh",
);
pair(
  "4 · Agent-side hooks & harness<br/>block push to main · scope/ref guards · frozen TEST_CMD",
  "4 · Hooks y harness del agente<br/>bloquear push a main · guards de scope/ref · TEST_CMD congelado",
);
pair(
  "3 · Skills & agents catalog<br/>.claude/skills · .claude/agents · fleet manifest",
  "3 · Catálogo de skills y agentes<br/>.claude/skills · .claude/agents · fleet manifest",
);
pair(
  "2 · Context injection<br/>openspec config.yaml · schema notaire-sdlc · 8k-token card",
  "2 · Inyección de contexto<br/>openspec config.yaml · schema notaire-sdlc · tarjeta de 8k tokens",
);
pair(
  "1 · Policy<br/>CONSTITUTION.md — highest authority",
  "1 · Política<br/>CONSTITUTION.md — máxima autoridad",
);

// QUALITY_GATE_PIPELINE
pair("On the agent's machine — the pull-request proof", "En la máquina del agente — la prueba del pull request");
pair("tests, red then green", "tests, rojo y después verde");
pair("preflight.sh --full<br/>includes security cases", "preflight.sh --full<br/>incluye los casos de seguridad");
pair("run_pipeline.sh<br/>when the stack is up", "run_pipeline.sh<br/>cuando el stack está arriba");
pair(
  "Merge rule<br/>Foreman or Owner<br/>Workers never merge",
  "Regla de merge<br/>Foreman o Owner<br/>los Workers nunca fusionan",
);
pair("Version tag or manual dispatch", "Tag de versión o dispatch manual");
pair("CI: tests, Trivy,<br/>secrets, Semgrep", "CI: tests, Trivy,<br/>secretos, Semgrep");
pair("Playwright + Bruno", "Playwright + Bruno");
pair("CD: sign + GHCR", "CD: firmar + GHCR");
pair("ZAP · CodeQL Monday", "ZAP · CodeQL los lunes");
pair("k6 · backup-restore", "k6 · backup-restore");

// ROLES_CHART
pair(
  "👤 Owner (human)<br/>approves specs · grants sdlc-exception<br/>merges Constitution & harness PRs",
  "👤 Owner (humano)<br/>aprueba specs · otorga sdlc-exception<br/>fusiona pull requests de Constitution y harness",
);
pair(
  "🧭 Foreman agent<br/>picks issue · runs gates · reviews<br/>merges Worker PRs under #1624",
  "🧭 Agente Foreman<br/>elige Issue · corre gates · revisa<br/>fusiona pull requests de Workers bajo #1624",
);
pair("Specialist Workers", "Workers especialistas");
pair("code-reviewer<br/>Gate 4", "code-reviewer<br/>Gate 4");
pair("sync_issues_and_code<br/>issue/board hygiene", "sync_issues_and_code<br/>higiene de Issue/tablero");
pair("Issues, priorities, decisions", "Issues, prioridades, decisiones");
pair("brief: issue · UC · surface · AC", "brief: Issue · CU · superficie · AC");
pair("result + exit-code evidence", "resultado + evidencia de exit-code");
pair("blocked / Owner-only step", "bloqueado / paso solo del Owner");

// OPENSPEC_FLOW
pair("/opsx:explore<br/>findings report", "/opsx:explore<br/>informe de hallazgos");
pair("openspec-triage<br/>real, estimated Issues + CU", "openspec-triage<br/>Issues reales y estimados + CU");
pair("validate-sdlc-plan.sh<br/>Issue exists & open?", "validate-sdlc-plan.sh<br/>¿el Issue existe y está abierto?");
pair("Gate 1 fails", "Gate 1 falla");
pair("proposal → traceability → specs → design → tasks", "proposal → trazabilidad → specs → design → tasks");
pair("/opsx:apply<br/>TDD per tasks.md groups 3–8", "/opsx:apply<br/>TDD por grupos 3–8 de tasks.md");
pair("openspec archive --yes<br/>in the same PR (#1623)", "openspec archive --yes<br/>en el mismo pull request (#1623)");
pair("docs/openspec/specs<br/>living capability specs", "docs/openspec/specs<br/>specs de capacidad vivas");
pair("next change reads current behaviour", "el próximo cambio lee el comportamiento actual");
pair("/opsx:propose<br/>seed-openspec-change.sh", "/opsx:propose<br/>seed-openspec-change.sh");

// ISSUE_STATE_MACHINE (transition / note sentences)
pair("type + priority + area", "type + priority + area");
pair("claim-issue.py<br/>status in-progress", "claim-issue.py<br/>status in-progress");
pair("PR opened<br/>status in-review", "pull request abierto<br/>status in-review");
pair("review notes / red CI", "notas de review / CI en rojo");
pair("status blocked or needs-info", "status blocked o needs-info");
pair("merged + smoke<br/>issue CLOSED", "merge + smoke<br/>Issue CLOSED");
pair("CLOSED means board Done", "CLOSED significa tablero Done");
pair("and no WIP labels (#1550)", "y sin etiquetas WIP (#1550)");

// FRAMEWORK_ARCHITECTURE
pair("Agent runtimes — interchangeable", "Runtimes de agentes — intercambiables");
pair("Agent card<br/>≤ 3 KB", "Tarjeta de agente<br/>≤ 3 KB");
pair("Roles<br/>Owner · Foreman · Workers · Reviewer", "Roles<br/>Owner · Foreman · Workers · Reviewer");
pair("aisdlc kit — stdlib only", "kit aisdlc — solo stdlib");
pair("skills · budget<br/>commits · tdd", "skills · budget<br/>commits · tdd");
pair("Your toolchain — unchanged", "Tu toolchain — sin cambios");
pair("tests · linters", "tests · linters");
pair("CI · tracker · deploy", "CI · tracker · deploy");
pair("learning loop", "ciclo de aprendizaje");
pair("Evidence ledger", "Ledger de evidencia");

// LEARNING_LOOP
pair("Incident · review note<br/>gate failure · metric", "Incidente · nota de review<br/>fallo de gate · métrica");
pair("Lesson<br/>what happened → why", "Lección<br/>qué pasó → por qué");
pair("Can a command<br/>detect it?", "¿Puede un comando<br/>detectarlo?");
pair("New gate or ratchet<br/>in the profile", "Nueva gate o ratchet<br/>en el perfil");
pair("Policy line or<br/>role instruction", "Línea de política o<br/>instrucción de rol");
pair("Test the check<br/>it must be able to fail", "Probar el chequeo<br/>tiene que poder fallar");
pair("Each release:<br/>can it be mechanized now?", "Cada release:<br/>¿se puede mecanizar ahora?");
pair("Measure in the ledger", "Medir en el ledger");

// ADOPTION_LEVELS
pair("More than one agent<br/>working at once?", "¿Más de un agente<br/>trabajando a la vez?");
pair("Legacy debt or<br/>regulated domain?", "¿Deuda legacy o<br/>dominio regulado?");
pair("Level 1 · Guided", "Nivel 1 · Guiado");
pair("Level 2 · Gated", "Nivel 2 · Con gates");
pair("Runs without a human<br/>between stages?", "¿Corre sin una persona<br/>entre etapas?");
pair("Level 3 · Fleet", "Nivel 3 · Flota");
pair("Pass-without-notes<br/>≥ 80% per stage?", "¿Pass-without-notes<br/>≥ 80% por etapa?");
pair("Level 4 · Autonomous", "Nivel 4 · Autónomo");

/* Sentence-like labels in anatomy-content.ts charts */
pair("Issue and use case", "Issue y caso de uso");
pair("OpenSpec change", "Cambio OpenSpec");
pair("Failing tests", "Tests que fallan");
pair("preflight and pipeline", "preflight y pipeline");
pair("Review and merge", "Review y merge");
pair("Version tag: product CI and images", "Tag de versión: CI del producto e imágenes");
pair("Operate and observe", "Operar y observar");
pair("finding or alert", "hallazgo o alerta");
pair("One rulebook", "Un solo reglamento");
pair("CONSTITUTION.md and the agent card", "CONSTITUTION.md y la tarjeta de agente");
pair("Skills and agents catalog", "Catálogo de skills y agentes");
pair("Roadmap, board and agent queue", "Roadmap, tablero y cola de agentes");
pair("Foreman agent", "Agente Foreman");
pair("Worker agents", "Agentes Worker");
pair("Five gates", "Cinco gates");
pair("Owner, human", "Owner, humano");
pair("specs, exceptions, harness PRs", "specs, excepciones, pull requests del harness");
pair("archive without a deed", "archivar sin escritura");
pair("entered for registration", "ingresado para inscripción");
pair("procedures assigned", "trámites asignados");
pair("documents delivered", "documentos entregados");
pair("deed drafted", "escritura redactada");
pair("parties sign", "las partes firman");
pair("testimony generated", "testimonio generado");
