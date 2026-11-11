/** Spanish for anatomy figures and their plate chrome (#2202). */
import { pair } from "./es";

// Plate chrome shared by AnatomyFigure
pair("Where to look:", "Dónde mirar:");
pair("AI Factory chapters", "Capítulos de la AI Factory");

/* Fig 1 · system-figure.ts */
pair("The notary's counter", "El mostrador de la escribanía");
pair("Pick a station", "Elegí una estación");
pair(
  "An isometric machine on a base plate: a request ticket rides a rail past seven stations. A counter with a monitor is the browser, a desk with a stack of forms is the Next.js frontend, a barrier arch is the security gate, a pigeonhole rack with a calculator dial holds the business rules, a drawer cabinet is the PostgreSQL archive, a stamp press is the audit trail and a console of three dials is the monitoring stack.",
  "Una máquina isométrica sobre una placa base: un ticket de pedido recorre un riel por siete estaciones. Un mostrador con monitor es el navegador, un escritorio con una pila de formularios es el frontend Next.js, un arco barrera es la security gate, un casillero con dial de calculadora guarda las reglas de negocio, un archivador de cajones es el archivo PostgreSQL, una prensa de sellos es la auditoría y una consola de tres diales es el stack de monitoreo.",
);
pair(
  "One request, end to end. The ticket is a call from the browser; it never touches the database directly, only the API does, and every change leaves a stamp in the audit book.",
  "Un pedido, de punta a punta. El ticket es una llamada del navegador; nunca toca la base de datos directamente, solo la API lo hace, y cada cambio deja un sello en el libro de auditoría.",
);
pair("The request ticket and the part doing the work", "El ticket del pedido y la parte que está trabajando");
pair("Seven numbered stations, one per layer", "Siete estaciones numeradas, una por capa");
pair("Dimmed while another station is explained", "Atenuado mientras se explica otra estación");

pair("Browser: the counter", "Navegador: el mostrador");
pair(
  "Notary staff, receptionists and clerks work in a web browser. It shows screens and sends requests; it holds no business rules and never talks to the database.",
  "Escribanos, recepcionistas y empleados trabajan en un navegador web. Muestra pantallas y envía pedidos; no guarda reglas de negocio y nunca habla con la base de datos.",
);
pair("request leaves the browser", "el pedido sale del navegador");

pair("Next.js 16 frontend: the desk", "Frontend Next.js 16: el escritorio");
pair(
  "The desk turns clicks into forms built from one design system (FormContainer, FormSection, FormField) and forwards every data call to the API under /api/v1. React 19 and TanStack Query handle loading, errors and empty states.",
  "El escritorio convierte los clics en formularios armados con un solo design system (FormContainer, FormSection, FormField) y reenvía cada llamada de datos a la API bajo /api/v1. React 19 y TanStack Query manejan carga, errores y estados vacíos.",
);
pair("form submitted to /api/v1", "formulario enviado a /api/v1");

pair("Security gate", "Security gate");
pair(
  "Spring Security checks the JWT (kept in an HttpOnly cookie), the user's role and a rate limit before any rule runs. A request without a valid token never gets past the barrier.",
  "Spring Security comprueba el JWT (en una cookie HttpOnly), el rol del usuario y un rate limit antes de que corra cualquier regla. Un pedido sin un token válido no pasa la barrera.",
);
pair("token valid, role allowed", "token válido, rol permitido");

pair("Business rules: the rack", "Reglas de negocio: el casillero");
pair(
  "Services in the Spring Boot 4 API (Java 26) apply the notarial rules: workflow transitions, budgets, payment status, deed signing. Controllers stay thin; services own transactions and throw domain exceptions.",
  "Los services de la API Spring Boot 4 (Java 26) aplican las reglas notariales: transiciones de workflow, presupuestos, estado de pago, firma de escrituras. Los controllers se mantienen delgados; los services son dueños de las transacciones y lanzan excepciones de dominio.",
);
pair("rule applied", "regla aplicada");

pair("PostgreSQL 16: the archive", "PostgreSQL 16: el archivo");
pair(
  "Repositories file the data in PostgreSQL. Only the backend connects to it, and Flyway migrations are the single source of truth for the schema, so the archive can be rebuilt from scratch.",
  "Los repositories archivan los datos en PostgreSQL. Solo el backend se conecta, y las migraciones Flyway son la única fuente de verdad del esquema, así que el archivo se puede reconstruir desde cero.",
);
pair("record filed in a drawer", "registro archivado en un cajón");

pair("Audit stamp", "Sello de auditoría");
pair(
  "An aspect records every create, update, delete and login with the user taken from the security context, never from a client header. Reading personal data is audited too. The stamp lands in the ledger on every change.",
  "Un aspect registra cada create, update, delete y login con el usuario tomado del security context, nunca de un header del cliente. Leer datos personales también se audita. El sello cae en el ledger en cada cambio.",
);
pair("audit entry written", "entrada de auditoría escrita");

pair("Instrument console", "Consola de instrumentos");
pair(
  "Micrometer metrics go to Prometheus, structured JSON logs to Loki, traces through an OpenTelemetry collector to Tempo, and Grafana draws the dashboards. Operators read the dials; they do not touch the machine.",
  "Las métricas de Micrometer van a Prometheus, los logs JSON estructurados a Loki, las trazas por un collector OpenTelemetry a Tempo, y Grafana dibuja los tableros. Los operadores leen los diales; no tocan la máquina.",
);
pair("dials: metrics, logs, traces", "diales: métricas, logs, trazas");

/* Fig 3 · sdlc-figure.ts */
pair("The lock chamber", "La cámara de esclusas");
pair("Pick a gate", "Elegí una gate");
pair(
  "An isometric canal with five lock gates across it, each in a frame with a numbered disc. A boat, which is the change, is raised from chamber to chamber and passes a gate only when it is open. On the bank stand a control tower on stilts with three levers, the three roles, and a lectern with an open book, the Constitution.",
  "Un canal isométrico con cinco gates de esclusa, cada una en un marco con un disco numerado. Un barco, que es el cambio, sube de cámara en cámara y pasa una gate solo cuando está abierta. En la orilla hay una torre de control sobre pilotes con tres palancas, los tres roles, y un atril con un libro abierto, la Constitution.",
);
pair(
  "The AI SDLC is a lock, not a ladder: a gate cannot be skipped, because the water on the far side only rises when the evidence is in. Humans and agents use the same locks.",
  "El AI SDLC es una esclusa, no una escalera: una gate no se puede saltar, porque el agua del otro lado solo sube cuando está la evidencia. Personas y agentes usan las mismas esclusas.",
);
pair("The change (boat) and the gate it must pass", "El cambio (barco) y la gate que tiene que pasar");
pair("Five gates, a control tower for the roles, a lectern for the rules", "Cinco gates, una torre de control para los roles, un atril para las reglas");
pair("Dimmed while another part is explained", "Atenuado mientras se explica otra parte");

pair("Gate 1: specification", "Gate 1: especificación");
pair(
  "Nothing is built from a prompt alone. The change needs a GitHub issue tied to a use case, then an OpenSpec change (proposal, traceability, specs, design, tasks) that validates. Until it does, gate 1 stays shut.",
  "Nada se construye solo con un prompt. El cambio necesita un Issue de GitHub atado a un caso de uso, y después un cambio OpenSpec (proposal, trazabilidad, specs, design, tasks) que valide. Hasta que lo haga, la gate 1 sigue cerrada.",
);
pair("gate 1 open: spec valid", "gate 1 abierta: spec válida");

pair("Gate 2: tests first", "Gate 2: tests primero");
pair(
  "Failing tests are written and seen to fail before any production code: unit, integration, API and, for UI, Playwright. The agent proves red, then green, and may not weaken a test to pass.",
  "Se escriben tests que fallan y se los ve fallar antes de cualquier código de producción: unit, integración, API y, para UI, Playwright. El agente prueba el rojo, después el verde, y no puede debilitar un test para pasar.",
);
pair("gate 2 open: red, then green", "gate 2 abierta: rojo, después verde");

pair("Gate 3: docs and local pipeline", "Gate 3: docs y pipeline local");
pair(
  "Before a pull request: permanent documentation updated, coverage above the ratchet floor, lint and format clean, the local pipeline and the local security cases passing, and the OpenSpec change archived in the same PR.",
  "Antes de un pull request: documentación permanente actualizada, cobertura sobre el piso del ratchet, lint y formato limpios, el pipeline local y los casos de seguridad locales en verde, y el cambio OpenSpec archivado en el mismo pull request.",
);
pair("gate 3 open: pipeline exit 0", "gate 3 abierta: pipeline exit 0");

pair("Gate 4: review and merge", "Gate 4: review y merge");
pair(
  "A change does not merge with failing checks, unresolved review, merge conflicts or pending docs. GitHub does not re-run the product suite on a pull request, so preflight --full proves it on the agent machine, and check-heavy-ci.sh fails only if a heavy run started on the PR and is red or pending. Workers never merge; the Foreman merges Worker PRs under its rule.",
  "Un cambio no se fusiona con chequeos en rojo, review sin resolver, conflictos de merge o docs pendientes. GitHub no vuelve a correr la suite del producto en un pull request, así que preflight --full lo demuestra en la máquina del agente, y check-heavy-ci.sh falla solo si un run pesado arrancó en el pull request y está en rojo o pendiente. Los Workers nunca fusionan; el Foreman fusiona los pull requests de Workers bajo su regla.",
);
pair("gate 4 open: review clean, checks green", "gate 4 abierta: review limpia, chequeos en verde");

pair("Gate 5: deploy, smoke, close", "Gate 5: deploy, smoke, cerrar");
pair(
  "The issue is not closed when the PR opens. It closes after the deploy succeeds and a smoke test passes on the target environment, and the board status and labels agree with the issue state.",
  "El Issue no se cierra cuando se abre el pull request. Se cierra después de que el deploy sale bien y un smoke test pasa en el entorno de destino, y el status del tablero y las etiquetas coinciden con el estado del Issue.",
);
pair("gate 5 open: released and verified", "gate 5 abierta: liberado y verificado");

pair("Three roles: the control tower", "Tres roles: la torre de control");
pair(
  "The Owner is the human code owner: approves specifications, merges the Constitution and harness changes, grants exceptions. The Foreman is a supervising agent that runs the gates and merges Worker PRs by rule. The Worker is the implementing agent and never merges or pushes to main.",
  "El Owner es el code owner humano: aprueba especificaciones, fusiona cambios de la Constitution y del harness, otorga excepciones. El Foreman es un agente supervisor que corre las gates y fusiona pull requests de Workers por regla. El Worker es el agente que implementa y nunca fusiona ni hace push a main.",
);
pair("who may pull which lever", "quién puede tirar de qué palanca");

pair("The Constitution: the rulebook", "La Constitution: el reglamento");
pair(
  "One document, CONSTITUTION.md, defines the workflow, the five gates and the exceptions. A short digest and the OpenSpec schema carry it to every agent, so Claude Code, Copilot, OpenCode and the cloud fleet follow the same rules without agent-specific files.",
  "Un documento, CONSTITUTION.md, define el workflow, las cinco gates y las excepciones. Un resumen corto y el esquema OpenSpec lo llevan a cada agente, así Claude Code, Copilot, OpenCode y la flota en la nube siguen las mismas reglas sin archivos propios de cada agente.",
);
pair("same rules for every agent", "mismas reglas para cada agente");

/* Fig 2 · engineering-figure.ts */
pair("The release line", "La línea de release");
pair(
  "An isometric conveyor machine on a base plate. A work package rides a belt past six stations: a workbench with a vise and a stack of spec documents (development), a test rig with a pyramid of blocks and a coverage dial (QA), a scanner arch over the belt with three lenses (security), a gate board with six check lamps and a merge lever (the local preflight), three environment crates of growing size (infrastructure) and a cabinet of six dials and a lamp (operations).",
  "Una máquina isométrica de cinta sobre una placa base. Un paquete de trabajo recorre una cinta por seis estaciones: un banco con tornillo de banco y una pila de specs (desarrollo), un banco de ensayo con una pirámide de bloques y un dial de cobertura (QA), un arco escáner sobre la cinta con tres lentes (seguridad), un tablero de gates con seis lámparas de chequeo y una palanca de merge (el preflight local), tres cajones de entorno cada vez más grandes (infraestructura) y un gabinete de seis diales y una lámpara (operaciones).",
);
pair(
  "From a request to a running system. Every package is checked at every station, and what operations learn at the end comes back to the workbench as a new issue.",
  "De un pedido a un sistema en marcha. Cada paquete se chequea en cada estación, y lo que operaciones aprende al final vuelve al banco de trabajo como un Issue nuevo.",
);
pair("The work package and the check that is running", "El paquete de trabajo y el chequeo que está corriendo");
pair("Six numbered stations: dev, QA, security, gates, environments, ops", "Seis estaciones numeradas: dev, QA, seguridad, gates, entornos, ops");

pair("Develop: the workbench", "Desarrollar: el banco de trabajo");
pair(
  "Every change starts as a GitHub issue tied to a use case (CU-XX), then an OpenSpec change: proposal, specs, design and tasks. The branch is named type/issue_description, and tests are written to fail first (TDD).",
  "Cada cambio empieza como un Issue de GitHub atado a un caso de uso (CU-XX), y después un cambio OpenSpec: proposal, specs, design y tasks. La rama se llama type/issue_description, y los tests se escriben para fallar primero (TDD).",
);
pair("issue, spec, failing test", "Issue, spec, test que falla");

pair("QA: the test rig", "QA: el banco de ensayo");
pair(
  "The pyramid has many unit tests (JUnit, Vitest), fewer integration and API tests (Spring, Bruno), a few end-to-end tests (Playwright) and database checks. Coverage is a raise-only ratchet: the floor can go up, never down.",
  "La pirámide tiene muchos tests unitarios (JUnit, Vitest), menos de integración y de API (Spring, Bruno), unos pocos end-to-end (Playwright) y chequeos de base de datos. La cobertura es un ratchet que solo sube: el piso puede subir, nunca bajar.",
);
pair("coverage floor holds", "el piso de cobertura se sostiene");

pair("Security: the scanner arch", "Seguridad: el arco escáner");
pair(
  "Scanners look at the same package everywhere: secret scanning and Semgrep rules in the local catalog, Trivy on files and images, CodeQL on a version tag and every Monday, and an OWASP ZAP baseline every Monday. The local case catalog runs inside the pipeline before a pull request.",
  "Los escáneres miran el mismo paquete en todos lados: escaneo de secretos y reglas Semgrep en el catálogo local, Trivy en archivos e imágenes, CodeQL en un tag de versión y cada lunes, y una línea base de OWASP ZAP cada lunes. El catálogo local de casos corre dentro del pipeline antes de un pull request.",
);
pair("secrets, SAST, SCA clean", "secretos, SAST, SCA limpios");

pair("Gates: the preflight board", "Gates: el tablero de preflight");
pair(
  "A pull request is proved on the agent machine: preflight --full runs lint, unit, API and end-to-end checks, the security cases and the docs checks. GitHub does not re-run the product suite on a pull request; it runs on a version tag. The Foreman merges Worker pull requests by rule, and the Owner merges the Constitution and harness changes.",
  "Un pull request se demuestra en la máquina del agente: preflight --full corre lint, unit, API y end-to-end, los casos de seguridad y los chequeos de docs. GitHub no vuelve a correr la suite del producto en un pull request; corre en un tag de versión. El Foreman fusiona pull requests de Workers por regla, y el Owner fusiona los cambios de la Constitution y del harness.",
);
pair("all lamps green, lever pulled", "todas las lámparas en verde, palanca tirada");

pair("Environments: the crates", "Entornos: los cajones");
pair(
  "Development runs from Docker Compose with published ports. Staging is Kubernetes manifests (Kustomize base plus a staging overlay). Production is a separate Compose file where only the reverse proxy is published on the host. Images are built, signed and published to GHCR on a version tag.",
  "El desarrollo corre con Docker Compose y puertos publicados. Staging son manifests de Kubernetes (base Kustomize más un overlay de staging). Producción es un Compose aparte donde solo el reverse proxy se publica en el host. Las imágenes se construyen, firman y publican en GHCR en un tag de versión.",
);
pair("image promoted", "imagen promovida");

pair("Operate: the observability cabinet", "Operar: el gabinete de observabilidad");
pair(
  "Prometheus scrapes metrics, Loki keeps logs, Tempo keeps traces through an OpenTelemetry collector, Alertmanager pages, Grafana draws it all, and SonarQube tracks code quality. A smoke test proves a release before its issue closes.",
  "Prometheus scrapea métricas, Loki guarda logs, Tempo guarda trazas por un collector OpenTelemetry, Alertmanager pagina, Grafana lo dibuja todo, y SonarQube sigue la calidad del código. Un smoke test demuestra un release antes de que se cierre su Issue.",
);
pair("dials live, alerts armed", "diales en vivo, alertas armadas");

/* Product / engineering anatomy page chrome still missing from earlier passes */
pair("Product · How it works", "Producto · Cómo funciona");
pair("Notaire as a machine: one request, end to end", "Notaire como máquina: un pedido, de punta a punta");
pair("Start with the office", "Empezá por la escribanía");
pair("The layers, and what each must never do", "Las capas, y lo que cada una no debe hacer nunca");
pair("Word", "Palabra");
pair("Meaning in Notaire", "Significado en Notaire");
pair(
  "The case file for one procedure, such as a sale. It has a status, a history and linked documents.",
  "El expediente de un trámite, por ejemplo una venta. Tiene un estado, un historial y documentos vinculados.",
);
pair(
  "The priced list of procedures offered to a client. Payments are collected against it.",
  "La lista con precio de trámites ofrecidos a un cliente. Los pagos se cobran contra ella.",
);
pair(
  "The notarial instrument drafted for the management. It starts unsigned and needs a folio to be signed.",
  "El instrumento notarial redactado para la gestión. Empieza sin firmar y necesita un folio para firmarse.",
);
pair(
  "The authorised copy of a signed deed. It goes to the registry and can come back for correction.",
  "La copia autorizada de una escritura firmada. Va al registro y puede volver para corrección.",
);
pair("A numbered page of the notary's protocol book.", "Una página numerada del protocolo del escribano.");
pair("Folio", "Folio");
