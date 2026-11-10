/** Spanish for the remaining public pages (#2202). English sentence is the key. */
import { pair } from "./es";

pair("step controls", "controles del paso");
pair("Previous step", "Paso anterior");
pair("Prev", "Anterior");
pair("Pause", "Pausa");
pair("Play all steps", "Reproducir todos los pasos");
pair("Play", "Reproducir");
pair("Next step", "Paso siguiente");
pair("Next", "Siguiente");
pair("Step", "Paso");
pair("of", "de");
pair("all steps", "todos los pasos");
pair("Where to look:", "Dónde mirar:");
pair("Diagram", "Diagrama");
pair("Business and engineering docs rendered for the public site. Full Markdown trees stay in", "Documentos de negocio e ingeniería para el sitio público. Los árboles Markdown completos siguen en");
pair("on GitHub; this tab is the curated entry for business, modules, architecture, testing and DevSecOps.", "en GitHub. Esta sección es la entrada curada: negocio, módulos, arquitectura, pruebas y DevSecOps.");
pair("commits", "commits");
pair("merged pull requests", "pull requests fusionados");
pair("PRs about the process itself", "pull requests sobre el proceso");
pair("the factory improving the factory", "la fábrica mejorando la fábrica");
pair("issues", "issues");
pair("from the 2014 desktop app", "de la app de escritorio de 2014");
pair("including Dependabot", "incluyendo Dependabot");
pair("of them labelled type:bug", "de ellos con la etiqueta type:bug");
pair("OpenSpec changes archived", "cambios OpenSpec archivados");
pair("one per shipped change", "uno por cambio entregado");
pair("living capability specs", "specs de capacidad vivas");
pair("agent definitions", "definiciones de agentes");
pair("process scripts", "scripts de proceso");
pair("architecture decisions", "decisiones de arquitectura");
pair("security case families", "familias de casos de seguridad");
pair("use cases", "casos de uso");
pair(
  "The first merged pull request in the snapshot is from {first}. The latest month, {month}, is also the busiest: {count} merged PRs by {date}.",
  "El primer pull request fusionado de la foto es de {first}. El último mes, {month}, es también el más ocupado: {count} pull requests fusionados al {date}.",
);
pair(
  "The first merged pull request in the snapshot is from {first}. The busiest month is {busiest}, with {busyCount} merged PRs; the latest, {month}, has {count}.",
  "El primer pull request fusionado de la foto es de {first}. El mes más ocupado es {busiest}, con {busyCount} pull requests fusionados. El último, {month}, tiene {count}.",
);
pair("How it is computed", "Cómo se calcula");

pair("Product", "Producto");
pair("Security", "Seguridad");
pair("Testing", "Pruebas");
pair("Story", "Historia");
pair("Architecture", "Arquitectura");
pair("How it works", "Cómo funciona");
pair("Engineering artifacts", "Artefactos de ingeniería");
pair("Engineering anatomy", "Anatomía de ingeniería");
pair("AI SDLC anatomy", "Anatomía del AI SDLC");
pair("AI SDLC lifecycle", "Ciclo de vida del AI SDLC");
pair("Releases", "Releases");
pair("What does Notaire do, and for whom?", "¿Qué hace Notaire, y para quién?");
pair("How does one request travel through the system?", "¿Cómo viaja un pedido por el sistema?");
pair("Where did it come from?", "¿De dónde salió?");
pair("Which documents define it, and how do they connect?", "¿Qué documentos lo definen, y cómo se conectan?");
pair("How is it structured, and why?", "¿Cómo está armado, y por qué?");
pair("How do dev, QA, security and ops fit together, and where does it run?", "¿Cómo encajan desarrollo, QA, seguridad y operaciones, y dónde corre?");
pair("What is the AI SDLC, and why does it exist?", "¿Qué es el AI SDLC, y por qué existe?");
pair("How does one change go from Issue to production?", "¿Cómo va un cambio del Issue a producción?");
pair("How is each change proved?", "¿Cómo se prueba cada cambio?");
pair("How is the record protected?", "¿Cómo se protege el expediente?");
pair("What shipped, and when?", "¿Qué se entregó, y cuándo?");

pair(
  "Each gate is a script that exits non-zero. The pull-request proof is preflight.sh --full on the agent machine. GitHub runs the product workflows on a version tag or when someone dispatches them. When a gate is added or changed, preflight.sh changes in the same pull request.",
  "Cada gate es un script que sale con un código distinto de cero. La prueba del pull request es preflight.sh --full en la máquina del agente. GitHub corre los workflows del producto en un tag de versión, o cuando alguien los dispara. Si se agrega o cambia una gate, preflight.sh cambia en el mismo pull request.",
);
pair(
  "Agents prove the security catalog on their machine with preflight.sh --full. GitHub runs the product scanners on a version tag, on a Monday schedule, or when someone dispatches the workflow. A quiet pull request is the design, not a skipped job.",
  "Los agentes demuestran el catálogo de seguridad en su máquina con preflight.sh --full. GitHub corre los escáneres del producto en un tag de versión, los lunes, o cuando alguien dispara el workflow. Un pull request en silencio es el diseño, no un trabajo salteado.",
);
pair(
  "Humans and every agent runtime share one coordination protocol on GitHub: Issues, a single Project board, a label taxonomy, milestone trains and pull requests. No runtime keeps a private backlog, and scripts check that Issue state, board Status and labels agree.",
  "Las personas y cada runtime de agentes comparten un protocolo en GitHub: Issues, un solo tablero de Project, una taxonomía de etiquetas, trenes de hitos y pull requests. Ningún runtime tiene un backlog privado, y hay scripts que comprueban que el estado del Issue, el Status del tablero y las etiquetas coinciden.",
);
pair(
  "No single control is trusted. Policy reaches the agent through its context. Skills narrow what it does. Hooks and harness guards stop it locally. CI tests both the code and the process, the repository refuses what CI did not pass, and a human owns the decisions that matter. Every layer is a file you can read.",
  "Ningún control se cree solo. La política llega al agente por su contexto. Las skills acotan lo que hace. Los hooks y el harness lo frenan en local. El CI prueba el código y el proceso, el repositorio rechaza lo que el CI no dejó pasar, y un humano es dueño de las decisiones que importan. Cada capa es un archivo que se puede leer.",
);
pair(
  "Notaire runs a notary office. This page opens it up: first what the office does, then a drawn machine where a request rides a rail past the seven stations that make up the real system.",
  "Notaire lleva el día a día de una escribanía. Esta página lo abre: primero qué hace la oficina, después una máquina dibujada donde un pedido recorre un riel por las siete estaciones del sistema real.",
);
pair(
  "Tests come first, by rule (CONSTITUTION §6, Gate 2): a change starts with tests that fail. The suites are layered from cheap static checks to full-stack browser journeys. The heavy layers run locally in preflight --full before every pull request (#2001), and again on every version tag.",
  "Los tests van primero, por regla (CONSTITUTION §6, Gate 2): un cambio empieza con tests que fallan. Las suites van de chequeos baratos a recorridos de navegador. Las capas pesadas corren en local con preflight --full antes de cada pull request (#2001), y otra vez en cada tag de versión.",
);
pair(
  "Three roles carry authority: Owner, Foreman and Worker. Specialist agents ({agents} definitions) work from one catalog of {skills} skills. Multi-part work is delegated to specialized sub-agents in parallel by default, each with an explicit scope and a model tier matched to the task.",
  "Tres roles tienen autoridad: Owner, Foreman y Worker. Los agentes especialistas ({agents} definiciones) trabajan desde un catálogo de {skills} skills. El trabajo en varias partes se delega, en paralelo, a subagentes especializados, cada uno con un alcance explícito y un nivel de modelo acorde a la tarea.",
);
pair(
  "OpenSpec is the change record. Notaire adapts it instead of replacing it: a forked schema carries the Constitution's mandatory sections, the CLI injects the rules into every agent, and project scripts turn advisory rules into failing exit codes. So far {archived} changes have completed the flow, and {specs} capability specs describe current behaviour.",
  "OpenSpec es el registro del cambio. Notaire lo adapta en lugar de reemplazarlo: un esquema derivado trae las secciones obligatorias de la Constitution, el CLI inyecta las reglas en cada agente, y los scripts del proyecto convierten las reglas de consejo en códigos de salida que fallan. Hasta ahora {archived} cambios completaron el flujo, y {specs} specs de capacidad describen el comportamiento actual.",
);
pair(
  "A software development lifecycle is the path a change takes from request to running system. In Notaire that path is written once and followed by people and AI agents alike. This page explains what it is, why it exists and how it works, with the machine first.",
  "Un ciclo de vida de software es el camino de un cambio desde el pedido hasta el sistema en marcha. En Notaire ese camino se escribe una vez y lo siguen tanto las personas como los agentes de IA. Esta página explica qué es, por qué existe y cómo funciona, con la máquina primero.",
);
pair(
  "Notaire is built and run as one loop, not four teams handing work over a wall. This page shows the loop as a machine, then the environments it runs on, then the commands that run it.",
  "Notaire se construye y se opera como un solo ciclo, no como cuatro equipos que se pasan el trabajo por encima de una pared. Esta página muestra el ciclo como una máquina, después los entornos donde corre, y después los comandos que lo hacen correr.",
);
pair(
  "A legacy Java desktop app was rebuilt as Spring Boot, Next.js and PostgreSQL, mostly by AI agents and one human Owner. This is the step-by-step story of the process that made that safe: what broke, what each failure taught, and how the lessons became a framework any repository can adopt.",
  "Una aplicación de escritorio en Java se rehizo en Spring Boot, Next.js y PostgreSQL, sobre todo con agentes de IA y un Owner humano. Esta es la historia, paso a paso, del proceso que hizo eso seguro: qué se rompió, qué enseñó cada falla, y cómo las lecciones se volvieron un framework que cualquier repositorio puede adoptar.",
);
pair("Start at chapter 1 ↓", "Empezar en el capítulo 1 ↓");
pair("Skip to the framework", "Saltar al framework");
pair("Case study · Notaire · 2014 → 2026", "Caso · Notaire · 2014 → 2026");
pair(
  "The coverage percentages on this page are not a JaCoCo or Vitest run. Missing measurement artifacts publish as null / n/a — never as a fabricated 0%. Activity counts above the coverage charts are counted from the repository and GitHub.",
  "Los porcentajes de cobertura de esta página no son una corrida de JaCoCo ni de Vitest. Si faltan los artefactos de medición se publica null / n/a — nunca un 0% inventado. Los conteos de actividad, arriba de los gráficos de cobertura, salen del repositorio y de GitHub.",
);

pair("Tech Evolution", "Evolución técnica");
pair("The Monolith", "El monolito");
pair("AI-Powered", "Con IA");
pair("AI agents", "Agentes de IA");
pair("AI-Driven TDD", "TDD con IA");
pair("Claude AI writes failing tests first, implements to pass them, then refactors. Enforced by mandatory workflow rules.", "Claude AI escribe primero los tests que fallan, implementa hasta que pasan y después refactoriza. Lo exigen las reglas obligatorias del flujo.");
pair("Full Observability", "Observabilidad completa");
pair("Prometheus metrics, Grafana dashboards, Loki log aggregation, and SonarQube quality gates. Production-grade from day one.", "Métricas de Prometheus, tableros de Grafana, logs en Loki y quality gates de SonarQube. Nivel de producción desde el primer día.");
pair("Security-First", "Seguridad primero");
pair("OWASP Top 10 guides, JWT authentication, SQL injection prevention, Trivy vulnerability scanning in CI/CD.", "Guías del OWASP Top 10, autenticación JWT, prevención de inyección SQL y escaneo de vulnerabilidades con Trivy en CI/CD.");
pair("Living Documentation", "Documentación viva");
pair("Use cases, functional and non-functional requirements, and ADRs live in docs/ and change in the same PR as the code. The AI Factory section counts them live.", "Los casos de uso, los requisitos funcionales y no funcionales y los ADR viven en docs/ y cambian en el mismo pull request que el código. La sección AI Factory los cuenta en vivo.");
pair("Accelerated Delivery", "Entrega acelerada");
pair("AI pairing turned the long-dormant project into a stream of small, atomic commits. What took years of manual work now happens in sprints; the Product numbers page counts the commits.", "Trabajar en pareja con IA convirtió el proyecto dormido en una corriente de commits chicos y atómicos. Lo que llevaba años de trabajo manual ahora ocurre en sprints. La página de números del producto cuenta los commits.");
pair("Playwright end-to-end suites cover the critical use cases, with Bruno API tests beside them. The Product numbers page counts them from the repository.", "Las suites end-to-end de Playwright cubren los casos de uso críticos, con los tests Bruno de la API al lado. La página de números del producto los cuenta desde el repositorio.");
pair("AI Arsenal", "Arsenal de IA");
pair("Core", "Núcleo");
pair("Fleet", "Flota");
pair("Shell", "Shell");
pair("IDE", "IDE");
pair("Primary AI agent — TDD, code review, E2E tests, docs, architecture decisions", "Agente de IA principal: TDD, code review, tests E2E, docs y decisiones de arquitectura");
pair("Autonomous cloud fleet — foreman + specialists run Issue → OpenSpec → PR → heavy-CI on remote VMs", "Flota autónoma en la nube: el foreman y los especialistas recorren Issue → OpenSpec → pull request → CI pesado en VMs remotas");
pair("Terminal-native AI coding agent, integrated directly into the shell workflow", "Agente de código con IA nativo de la terminal, integrado al flujo del shell");
pair("VS Code inline completions, chat, and PR summaries for rapid in-editor development", "Completados en línea, chat y resúmenes de pull request en VS Code, para desarrollar rápido en el editor");
pair("~80% of AI work", "~80% del trabajo de IA");
pair("Cloud SDLC loop", "Ciclo SDLC en la nube");
pair("RTK token proxy", "Proxy de tokens RTK");
pair("Inline completions", "Completados en línea");
pair("System Architecture", "Arquitectura del sistema");
pair("Frontend", "Frontend");
pair("Application core", "Núcleo de la aplicación");
pair("Persistence", "Persistencia");
pair("Observability", "Observabilidad");
pair("All layers run in Docker containers. One command to start everything.", "Todas las capas corren en contenedores Docker. Un comando levanta todo.");
pair("Infrastructure", "Infraestructura");
pair("Where trust sits", "Dónde está la confianza");
pair("A request crosses three boundaries before it touches a notarial record.", "Un pedido cruza tres límites antes de tocar un expediente notarial.");
pair("Trust boundaries", "Límites de confianza");
pair("Browser", "Navegador");
pair("Staff send an HttpOnly cookie. The page does not keep the JWT in local storage.", "El personal envía una cookie HttpOnly. La página no guarda el JWT en local storage.");
pair("Next.js proxies /api/v1 with a server-side backend address. The public login page does not print that host.", "Next.js hace de proxy de /api/v1 con una dirección de backend del lado del servidor. La página pública de login no imprime ese host.");
pair("The token is checked, the role is resolved, then the module grant. A missing grant is 403.", "Se verifica el token, se resuelve el rol y después el permiso del módulo. Si falta el permiso, es un 403.");
pair("Step through the full boundary diagram →", "Recorrer el diagrama completo de límites →");
pair("Version history", "Historial de versiones");
pair("Product cuts", "Cortes del producto");
pair("Semver releases of Notaire. The timeline above is the narrative. This list is the tag history. A delivery-train milestone is not a git tag.", "Releases semver de Notaire. La línea de tiempo de arriba es el relato. Esta lista es la historia de tags. Un hito de tren de entrega no es un tag de git.");
pair("Current product", "Producto actual");
pair("By The Numbers", "En números");
pair("A Decade of Progress", "Una década de avance");
pair("Every metric tells a story. From zero to production-grade in the AI era.", "Cada métrica cuenta una historia. De cero a nivel de producción en la era de la IA.");
pair("Total Commits", "Commits en total");
pair("Pull Requests", "Pull requests");
pair("Use Cases", "Casos de uso");
pair("Functional Requirements", "Requisitos funcionales");
pair("Test Coverage", "Cobertura de tests");
pair("Docs (Markdown files)", "Docs (archivos Markdown)");
pair("Test Classes", "Clases de test");
pair("Docker Services", "Servicios Docker");
pair("Grafana Dashboards", "Tableros de Grafana");
pair("AI Tools", "Herramientas de IA");
pair("Architecture Decisions", "Decisiones de arquitectura");
pair("Contributors", "Colaboradores");
pair("Opened, from GitHub search", "Abiertos, según la búsqueda de GitHub");
pair("not measured this build", "no medido en esta construcción");
pair("JaCoCo snapshot", "Foto de JaCoCo");
pair("Living documentation", "Documentación viva");
pair("Unit + Integration + E2E", "Unit + integración + E2E");
pair("App + Infra stacks", "Stacks de app e infra");
pair("Custom provisioned", "Provisionados a medida");
pair("Claude, Copilot, Gemini, OpenCode", "Claude, Copilot, Gemini, OpenCode");
pair("Active development", "Desarrollo activo");
pair("Since", "Desde");
pair("Quality Metrics", "Métricas de calidad");
pair("Test Coverage Dashboard", "Tablero de cobertura");
pair("Real-time coverage metrics across backend, frontend, E2E, and API layers. Updated on every merge to main.", "Métricas de cobertura del backend, el frontend, E2E y la API. Se actualizan en cada merge a main.");
pair("Backend (JaCoCo)", "Backend (JaCoCo)");
pair("Instruction Coverage", "Cobertura de instrucciones");
pair("Branch Coverage", "Cobertura de ramas");
pair("Component Coverage", "Cobertura de componentes");
pair("Endpoint Coverage", "Cobertura de endpoints");
pair("Trivia", "Curiosidades");
pair("Fun Facts", "Datos curiosos");
pair("Numbers and moments that make this project unique.", "Números y momentos que hacen único a este proyecto.");
pair("Battle Scars", "Cicatrices");
pair("Major Challenges", "Desafíos grandes");
pair("Real production incidents and architectural hurdles that shaped how the project works today.", "Incidentes reales y obstáculos de arquitectura que dieron forma a cómo funciona el proyecto hoy.");
pair("3 Years of Sleep", "3 años de sueño");
pair("The project was dormant from mid-2015 to late 2018. Not a single commit. Then life happened, then AI happened.", "El proyecto durmió de mediados de 2015 a fines de 2018. Ni un commit. Después pasó la vida, y después pasó la IA.");
pair("The 2014 to 2018 years left fewer than thirty commits; the AI-paired period wrote the rest. Agents wrote tests, reviewed code and updated docs continuously.", "De 2014 a 2018 quedaron menos de treinta commits. El período en pareja con IA escribió el resto. Los agentes escribieron tests, revisaron código y actualizaron docs sin parar.");
pair("A steady, ongoing stream of merged PRs — thanks to mandatory TDD, E2E coverage, and automated quality gates that never let a red build through.", "Una corriente continua de pull requests fusionados, gracias al TDD obligatorio, la cobertura E2E y las quality gates automáticas que no dejan pasar un build en rojo.");
pair("Documentation grew from zero to hundreds of .md files. Use cases, ADRs, guides, runbooks, API references — all kept in sync by AI.", "La documentación creció de cero a cientos de archivos .md. Casos de uso, ADR, guías, runbooks y referencias de API, mantenidos al día con IA.");
pair("The original project had no tests at all. Today, JaCoCo enforces a rising coverage ratchet and CI fails if it drops.", "El proyecto original no tenía tests. Hoy JaCoCo exige un ratchet de cobertura que solo sube, y el CI falla si baja.");
pair("Zero Tests → JaCoCo-Enforced", "De cero tests a JaCoCo");
pair("HIGH", "ALTA");
pair("MED", "MEDIA");
pair("LOW", "BAJA");
pair("critical", "crítica");
pair("warning", "aviso");

pair("PR lead time", "Lead time del pull request");
pair("Throughput", "Throughput");
pair("Change-failure proxy", "Proxy de fallos del cambio");
pair("Time to restore main", "Tiempo para restaurar main");
pair("First-push CI pass rate", "Tasa de CI verde en el primer push");
pair("Issues closed", "Issues cerrados");
pair("not available", "no disponible");
pair("closed as completed", "cerrados como completados");
pair("median", "mediana");
pair(
  "Merged PRs in the window, opened to merged, in hours. Bot PRs (Dependabot) excluded. Median and nearest-rank p90.",
  "Pull requests fusionados en la ventana, de abiertos a fusionados, en horas. Se excluyen los de bots (Dependabot). Mediana y p90 por rango más cercano.",
);
pair("Merged non-bot PRs per ISO week; weeks with no merge count as 0.", "Pull requests fusionados, sin bots, por semana ISO. Las semanas sin merge cuentan como 0.");
pair(
  "Proxy: merged PRs whose title starts with 'Revert' or contains 'hotfix', as a share of merged non-bot PRs.",
  "Proxy: pull requests fusionados cuyo título empieza con 'Revert' o contiene 'hotfix', como parte de los fusionados sin bots.",
);
pair(
  "Runs of ci.yml on pushes to main. A red episode starts when a run fails while main is green and ends when the next run succeeds; time between the two run ends, in hours. Cancelled runs are ignored.",
  "Corridas de ci.yml en pushes a main. Un episodio en rojo empieza cuando una corrida falla con main en verde y termina cuando la siguiente tiene éxito. El tiempo entre los dos finales, en horas. Las corridas canceladas no cuentan.",
);
pair(
  "For each merged PR, the earliest ci.yml run triggered by the PR; it passes if that run succeeded. Cancelled or skipped first runs are not judged.",
  "Para cada pull request fusionado, la corrida más temprana de ci.yml disparada por ese pull request. Pasa si esa corrida tuvo éxito. Las primeras corridas canceladas o salteadas no se juzgan.",
);
pair("Issues closed as completed in the window (GitHub search).", "Issues cerrados como completados en la ventana (búsqueda de GitHub).");
pair("Published, non-draft GitHub releases in the window.", "Releases de GitHub publicadas, no borrador, en la ventana.");

pair("Do not start implementation without", "No empezar a implementar sin");
pair("Do not implement without", "No implementar sin");
pair("Do not create a PR if", "No crear un pull request si");
pair("Do not merge if", "No fusionar si");
pair("Not finished until", "No está terminado hasta");
pair("Issue linked to a Use Case", "Issue enlazado a un caso de uso");
pair("Specification written", "Especificación escrita");
pair("Acceptance criteria defined", "Criterios de aceptación definidos");
pair("Test cases designed", "Casos de prueba diseñados");
pair("Unit tests written and observed failing", "Tests de unit escritos y vistos fallar");
pair("Integration tests where applicable", "Tests de integración cuando corresponde");
pair("Docs not updated", "Docs sin actualizar");
pair("Any test failing", "Cualquier test en rojo");
pair("Coverage below the JaCoCo ratchet", "Cobertura bajo el ratchet de JaCoCo");
pair("Lint/format failing", "Lint o formato en rojo");
pair("run_pipeline.sh not run or red", "run_pipeline.sh sin correr o en rojo");
pair("CI red or pending", "CI en rojo o pendiente");
pair("Review unresolved", "Review sin resolver");
pair("Merge conflicts", "Conflictos de merge");
pair("Docs pending", "Docs pendientes");
pair("Deploy succeeded", "Deploy exitoso");
pair("Smoke test passed", "Smoke test en verde");
pair("Issue closed", "Issue cerrado");
pair("Board Status and labels coherent", "Status del tablero y etiquetas coherentes");
pair("Skip or reorder steps of the workflow", "Saltear o reordenar pasos del flujo");
pair("Implement from a prompt without an Issue and a Specification", "Implementar desde un prompt sin un Issue y una especificación");
pair("Write code before its failing tests", "Escribir código antes de los tests que fallan");
pair("Open a PR with failing CI, missing docs or reduced coverage", "Abrir un pull request con CI en rojo, docs faltantes o cobertura más baja");
pair("Commit to main, or merge on light-only green", "Commitear a main, o fusionar con un verde que es solo el CI liviano");
pair("Fabricate an Issue number, a Use Case, or a test result", "Inventar un número de Issue, un caso de uso o un resultado de test");
pair("Close an Issue before deploy and smoke test", "Cerrar un Issue antes del deploy y del smoke test");
pair("Force-push or rebase a pushed branch (merge main in instead)", "Hacer force-push o rebase de una rama ya pusheada (en su lugar, fusionar main)");
pair("What an agent may never do", "Lo que un agente no puede hacer");
pair("Layer by layer", "Capa por capa");
pair("How the layers connect", "Cómo se conectan las capas");
pair("1 · Policy", "1 · Política");
pair("2 · Context injection", "2 · Inyección de contexto");
pair("3 · Skills & agents catalog", "3 · Catálogo de skills y agentes");
pair("4 · Agent-side hooks & harness", "4 · Hooks y harness del lado del agente");
pair("5 · Local gates", "5 · Gates locales");
pair("6 · Process checks", "6 · Chequeos de proceso");
pair("7 · CI quality & security", "7 · Calidad y seguridad en CI");
pair("8 · Repository rules", "8 · Reglas del repositorio");
pair("9 · Human Owner", "9 · Owner humano");

pair("One change, four lanes", "Un cambio, cuatro carriles");
pair("Follow a single Issue from the Owner's backlog to a closed Issue. Each lane can only do what its role allows.", "Seguí un solo Issue desde el backlog del Owner hasta el Issue cerrado. Cada carril solo puede hacer lo que su rol permite.");
pair("Roles and authority", "Roles y autoridad");
pair("The specialist crew", "El equipo especialista");
pair("Cost-matched delegation", "Delegación según el costo");
pair("Two runtimes, one process", "Dos runtimes, un proceso");
pair("Entry points by tool", "Puntos de entrada por herramienta");
pair("Before the push", "Antes del push");
pair("A quiet pull request is expected", "Un pull request en silencio es lo esperado");
pair("Release and deploy", "Release y deploy");
pair("Issue lifecycle", "Ciclo de vida del Issue");
pair("Six adaptations", "Seis adaptaciones");
pair("What it is", "Qué es");
pair("The machine", "La máquina");
pair("The layers", "Las capas");
pair("Gates agents must run", "Gates que los agentes tienen que correr");
pair("One loop, four concerns", "Un ciclo, cuatro preocupaciones");
pair("The machine: a release line", "La máquina: una línea de release");
pair("Development", "Desarrollo");
pair("Quality (QA)", "Calidad (QA)");
pair("Operations", "Operaciones");
pair("System context", "Contexto del sistema");
pair("Owner decisions pending", "Decisiones del Owner pendientes");
pair("Release history", "Historial de releases");
pair("Versions", "Versiones");
pair("Backend", "Backend");
pair("Database", "Base de datos");
pair("Tag", "Tag");
pair("Date", "Fecha");
pair("Train", "Tren");
pair("Quality", "Calidad");
pair("Testing process", "Proceso de pruebas");
pair("Backend test classes", "Clases de test del backend");
pair("Frontend test files", "Archivos de test del frontend");
pair("Bruno requests", "Requests de Bruno");
pair("Playwright specs", "Specs de Playwright");
pair("Layer", "Capa");
pair("Owner module", "Módulo dueño");
pair("Command", "Comando");
pair("Source of truth:", "Fuente de verdad:");
pair("Documentation questions", "Preguntas de la documentación");
pair("Where does a request go?", "¿Adónde va un pedido?");
pair("From the browser, through the BFF, into a port and a use case, then PostgreSQL. Step through the payment slice.", "Del navegador, por el BFF, a un puerto y un caso de uso, y después PostgreSQL. Recorré la tajada del pago.");
pair("Open the hexagon", "Abrir el hexágono");
pair("Who is allowed to call the API?", "¿Quién puede llamar a la API?");
pair("A JWT, then a role, then a module grant. The interactive boundaries are in the Security section.", "Un JWT, después un rol, después un permiso de módulo. Los límites interactivos están en la sección Seguridad.");
pair("Open the trust boundaries", "Abrir los límites de confianza");
pair("What has to pass before a pull request?", "¿Qué tiene que pasar antes de un pull request?");
pair("The local preflight, including the security case catalog. GitHub does not re-run the product suite on the pull request.", "El preflight local, incluido el catálogo de casos de seguridad. GitHub no vuelve a correr la suite del producto en el pull request.");
pair("Open the quality gates", "Abrir las quality gates");
pair("How does the database change?", "¿Cómo cambia la base de datos?");
pair("A Flyway migration. Hibernate does not create the schema.", "Una migración de Flyway. Hibernate no crea el esquema.");
pair("Open Flyway", "Abrir Flyway");
pair("version tag · manual", "tag de versión · manual");
pair("push to main · manual", "push a main · manual");
pair("only a reviewed, green PR", "solo un pull request revisado y en verde");
pair("incidents · findings · metrics become Issues", "incidentes, hallazgos y métricas se vuelven Issues");
pair("AI FACTORY — how Notaire is built", "AI FACTORY — cómo se construye Notaire");
pair("NOTAIRE — the product", "NOTAIRE — el producto");
pair("GitHub Issue + Use Case", "Issue de GitHub + caso de uso");

pair(
  "Development, QA, security and operations as one loop, drawn as a release line, with the environments and infrastructure under it.",
  "Desarrollo, QA, seguridad y operaciones como un solo ciclo, dibujado como una línea de release, con los entornos y la infraestructura debajo.",
);
pair("Every notarial and engineering term the site uses, in plain words.", "Cada término notarial y de ingeniería que usa el sitio, en palabras simples.");
pair("Glossary", "Glosario");
pair(
  "The chain from requirement to runbook: requirements, use cases, design models, SAD and ADRs, OpenSpec, tests, threat models, pipelines and manuals, with counts read from the repository.",
  "La cadena del requisito al runbook: requisitos, casos de uso, modelos de diseño, SAD y ADR, OpenSpec, tests, modelos de amenazas, pipelines y manuales, con conteos leídos del repositorio.",
);
pair("Technology stack", "Stack tecnológico");
pair("Every technology and the job it does, with versions read from pom.xml, package.json and Compose at build time.", "Cada tecnología y el trabajo que hace, con versiones leídas de pom.xml, package.json y Compose al construir el sitio.");
pair("Business", "Negocio");
pair("Requirements, use cases (CUxx), actors, traceability matrix and manuals under docs/100-business/.", "Requisitos, casos de uso (CUxx), actores, matriz de trazabilidad y manuales en docs/100-business/.");
pair("Module ownership", "Dueño de cada módulo");
pair("Nine in-repo modules (ADR-026). One folder, one fleet, one verify — Owner tracker #1445; no repo split yet.", "Nueve módulos en el repo (ADR-026). Una carpeta, una flota, un verify. El seguimiento del Owner es #1445. Todavía no hay split del repo.");
pair("SAD overview, every ADR with its status, Mermaid as the active diagram language (ADR-027).", "Panorama del SAD, cada ADR con su estado, y Mermaid como lenguaje de diagramas activo (ADR-027).");
pair("Hexagonal architecture", "Arquitectura hexagonal");
pair("Ports and adapters in the backend and front end, one request traced through a slice, and the ArchUnit rules that hold it.", "Puertos y adaptadores en el backend y el frontend, un pedido seguido por una tajada, y las reglas de ArchUnit que lo sostienen.");
pair("Database & Flyway", "Base de datos y Flyway");
pair("How a schema change ships, the Flyway settings and why, and the full migration history.", "Cómo sale un cambio de esquema, la configuración de Flyway y por qué, y la historia completa de migraciones.");
pair("API contract & Bruno", "Contrato de API y Bruno");
pair("Code-first OpenAPI with breaking-change, type and reachability checks, and the Bruno suite that calls the live API.", "OpenAPI a partir del código, con chequeos de cambios incompatibles, tipos y alcance, y la suite Bruno que llama a la API en vivo.");
pair("Pyramid, local preflight, and the release-tag suites. Bruno and Playwright stay with the testing module.", "Pirámide, preflight local y las suites del tag de release. Bruno y Playwright se quedan en el módulo de testing.");
pair("Security docs", "Docs de seguridad");
pair("Bridge from this tab to the interactive Security section and the architecture notes.", "Puente desde esta sección a la sección interactiva de Seguridad y a las notas de arquitectura.");
pair("Roadmap", "Roadmap");
pair("Milestone history, delivery trains, requirements catalog, and parallel-agent lanes.", "Historia de hitos, trenes de entrega, catálogo de requisitos y carriles de agentes en paralelo.");
pair("Product semver cuts: backend, frontend, and the Flyway schema recorded with each tag.", "Cortes semver del producto: backend, frontend y el esquema Flyway registrado con cada tag.");
pair(
  "Software Architecture Document (arc42), Architecture Decision Records, and the active diagram standard. Deep-link into the repository for the full SAD body.",
  "Documento de arquitectura de software (arc42), registros de decisiones de arquitectura, y el estándar de diagramas activo. El cuerpo completo del SAD está en el repositorio.",
);
pair("Step through the pyramid from the base. The counts are read from the repository when the site is built.", "Recorré la pirámide desde la base. Los conteos se leen del repositorio cuando se construye el sitio.");
pair(
  "An issue tied to a use case, an OpenSpec change, a branch named type/issue_description, tests first, atomic Conventional Commits, and docs updated in the same pull request.",
  "Un Issue atado a un caso de uso, un cambio OpenSpec, una rama llamada type/issue_description, tests primero, Conventional Commits atómicos, y docs actualizados en el mismo pull request.",
);
pair("A test pyramid from unit to end-to-end, database checks, an OpenAPI contract check, and coverage floors that can only go up.", "Una pirámide de tests de unit a end-to-end, chequeos de base de datos, un chequeo del contrato OpenAPI, y pisos de cobertura que solo pueden subir.");
pair("A gate, not a phase: secrets, static analysis, dependency and image scanning, a DAST baseline, and rules for who may merge what.", "Una gate, no una fase: secretos, análisis estático, escaneo de dependencias e imágenes, una línea base de DAST, y reglas de quién puede fusionar qué.");
pair("Metrics, logs and traces from the running system, alerts, dashboards, a smoke test after a deploy, and findings that return to development as issues.", "Métricas, logs y trazas del sistema en marcha, alertas, tableros, un smoke test después de un deploy, y hallazgos que vuelven a desarrollo como Issues.");
pair(
  "The package on the belt is one change. It is built at the workbench, proved on the test rig, scanned under the security arch, held by the gate board until every lamp is green, promoted through three environment crates, and watched from the observability cabinet. An alarm lamp on the workbench closes the loop.",
  "El paquete en la cinta es un cambio. Se arma en el banco de trabajo, se prueba en el banco de ensayo, se escanea bajo el arco de seguridad, lo retiene el tablero de gates hasta que todas las luces están verdes, avanza por tres cajones de entorno y se mira desde el gabinete de observabilidad. Una luz de alarma en el banco cierra el ciclo.",
);
pair("A change goes from issue to a version tag, and what operations learn comes back as a new issue.", "Un cambio va del Issue a un tag de versión, y lo que aprende operaciones vuelve como un Issue nuevo.");
pair(
  "Orchestrates the loop: pick, Gate 1, dispatch, PR, CI, review, merge",
  "Orquesta el ciclo: elegir, Gate 1, despachar, pull request, CI, review, merge",
);
pair("Writes the specification that Gate 1 checks", "Escribe la especificación que chequea la Gate 1");
pair("Package, port/adapter and migration decisions", "Decisiones de paquetes, puertos, adaptadores y migraciones");
pair("TDD implementation in backend-api", "Implementación con TDD en backend-api");
pair("Next.js UI with the design-system tokens", "UI de Next.js con los tokens del sistema de diseño");
pair("Test design and red proof; Bruno and Playwright", "Diseño de tests y prueba en rojo; Bruno y Playwright");
pair("Compose, CI and observability", "Compose, CI y observabilidad");
pair("Threat model, authz and secrets review", "Modelo de amenazas, autorización y revisión de secretos");
pair("Gate 4 diff review", "Review del diff en la Gate 4");
pair("Milestones, priority and lanes; no product code", "Hitos, prioridad y carriles. Sin código de producto");
pair("Issue, label and board hygiene", "Higiene de Issues, etiquetas y tablero");
pair(
  "The Engineering Constitution is the single, agent-agnostic process for every change, by a human or any AI tool. Where a lower document disagrees, the Constitution wins. A ≤3 KB agent card carries the non-negotiables into every session.",
  "La Engineering Constitution es el único proceso, independiente del agente, para cada cambio, lo haga una persona o cualquier herramienta de IA. Si un documento de menor rango no coincide, gana la Constitution. Una tarjeta de agente de hasta 3 KB lleva lo no negociable a cada sesión.",
);
pair("The Worker never merges. Product CI runs on a version tag. The pull-request proof is preflight.sh --full.", "El Worker nunca fusiona. El CI del producto corre en un tag de versión. La prueba del pull request es preflight.sh --full.");
pair("One change across the Owner, the Foreman agent, a Worker agent and CI", "Un cambio entre el Owner, el agente Foreman, un agente Worker y el CI");
