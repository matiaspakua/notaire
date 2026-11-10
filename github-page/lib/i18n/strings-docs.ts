/** Spanish for the remaining technical-docs pages (#2202). */
import { pair } from "./es";

pair("Technical docs · Glossary", "Documentación técnica · Glosario");
pair("Every term, in plain words", "Cada término, en palabras simples");
pair(
  "Notarial terms first, engineering and AI SDLC terms second. The pictures behind them are on How it works, the engineering anatomy and the AI SDLC anatomy.",
  "Primero los términos notariales, después los de ingeniería y del AI SDLC. Las figuras están en Cómo funciona, la anatomía de ingeniería y la anatomía del AI SDLC.",
);
pair("The notary office", "La escribanía");
pair("Engineering and the AI SDLC", "Ingeniería y el AI SDLC");
pair("Term", "Término");
pair("Meaning", "Significado");
pair("Use case (CU)", "Caso de uso (CU)");
pair("A written business scenario, numbered CU01 onward. Every change cites one.", "Un escenario de negocio escrito, numerado desde CU01. Cada cambio cita uno.");
pair("Requirement (RF / RNF)", "Requisito (RF / RNF)");
pair("A functional (RF) or non-functional (RNF) requirement. A traceability matrix links them to use cases.", "Un requisito funcional (RF) o no funcional (RNF). Una matriz de trazabilidad los une a los casos de uso.");
pair("Person, client", "Persona, cliente");
pair("Anyone the office deals with; a client is a person who buys services.", "Cualquiera con quien trata la oficina. Un cliente es una persona que compra servicios.");
pair("Budget (presupuesto)", "Presupuesto (budget)");
pair("The priced list of a procedure for a client. Payments are collected against it.", "La lista con precio de un trámite para un cliente. Los pagos se cobran contra ella.");
pair("Management (gestión)", "Gestión (management)");
pair("The case file for one procedure, with a status, a history and linked documents.", "El expediente de un trámite, con un estado, una historia y documentos vinculados.");
pair("Procedure type", "Tipo de trámite");
pair("The kind of procedure, such as a sale, a donation or a mortgage. Some have a workflow.", "La clase de trámite, como una compraventa, una donación o una hipoteca. Algunos tienen un workflow.");
pair("Workflow", "Workflow");
pair("A data-driven graph of statuses and the allowed transitions between them.", "Un grafo, definido por datos, de estados y de las transiciones permitidas entre ellos.");
pair("Deed (escritura)", "Escritura (deed)");
pair("The notarial instrument for the management. It needs a folio to be signed.", "El instrumento notarial de la gestión. Necesita un folio para firmarse.");
pair("Folio", "Folio");
pair("A numbered page of the protocol book.", "Una hoja numerada del libro de protocolo.");
pair("Testimony (testimonio)", "Testimonio (testimony)");
pair("The authorised copy of a signed deed. It goes to the registry and may be re-entered.", "La copia autorizada de una escritura firmada. Va al registro y puede volver a ingresar.");
pair("Registry (registro)", "Registro (registry)");
pair("The public office where the deed is recorded.", "La oficina pública donde se inscribe la escritura.");
pair("Substitution (suplencia)", "Suplencia (substitution)");
pair("A period in which one user covers another, so work is redirected.", "Un período en el que un usuario cubre a otro, y el trabajo se redirige.");
pair("Audit trail", "Auditoría (audit trail)");
pair("The record of who created, changed, deleted or logged in, taken from the security context.", "El registro de quién creó, cambió, borró o inició sesión, tomado del contexto de seguridad.");
pair("ADR", "ADR");
pair("Architecture Decision Record: one decision, its context, options and consequences.", "Registro de decisión de arquitectura: una decisión, su contexto, las opciones y las consecuencias.");
pair("OpenSpec", "OpenSpec");
pair("The specification method and CLI. A change has a proposal, traceability, specs, design and tasks.", "El método y el CLI de especificación. Un cambio tiene proposal, trazabilidad, specs, design y tasks.");
pair("Constitution", "Constitution");
pair("CONSTITUTION.md: the mandatory process and the five quality gates for people and agents.", "CONSTITUTION.md: el proceso obligatorio y las cinco quality gates, para personas y agentes.");
pair("Gate", "Gate");
pair("A hard stop. Gate 1 specification, 2 tests first, 3 before a pull request, 4 before merge, 5 before close.", "Una parada dura. Gate 1 especificación, 2 tests primero, 3 antes del pull request, 4 antes del merge, 5 antes de cerrar.");
pair("TDD", "TDD");
pair("Test-driven development: write a failing test, then the code that passes it.", "Desarrollo dirigido por tests: escribir un test que falla, y después el código que lo hace pasar.");
pair("Atomic commit", "Commit atómico");
pair("One logical change per commit, in Conventional Commits form.", "Un cambio lógico por commit, en la forma de Conventional Commits.");
pair("Flyway", "Flyway");
pair("The tool that versions the database schema. A migration that ran is never edited.", "La herramienta que versiona el esquema de la base. Una migración que ya corrió no se edita.");
pair("Hexagonal architecture", "Arquitectura hexagonal");
pair("Ports and Adapters: rules in the middle, controllers and persistence at the edges.", "Puertos y adaptadores: las reglas en el medio, los controllers y la persistencia en los bordes.");
pair("JWT", "JWT");
pair("JSON Web Token, carried in an HttpOnly cookie, that proves who is calling.", "JSON Web Token, en una cookie HttpOnly, que prueba quién llama.");
pair("Ratchet", "Ratchet");
pair("A threshold that may rise but never fall, used for coverage floors.", "Un umbral que puede subir y no bajar, usado como piso de cobertura.");
pair("Preflight", "Preflight");
pair("A local script that mirrors the gates so a push cannot fail for a known reason.", "Un script local que refleja las gates, para que un push no falle por una razón ya conocida.");
pair("Version tag", "Tag de versión");
pair("A release tag; the product workflows run on it, not on every pull request.", "Un tag de release. Los workflows del producto corren ahí, no en cada pull request.");
pair("Testcontainers", "Testcontainers");
pair("Starts a real PostgreSQL in a container for integration tests.", "Levanta un PostgreSQL real en un contenedor para los tests de integración.");
pair("Bruno", "Bruno");
pair("The API test tool; the collection lives with the backend.", "La herramienta de tests de la API. La colección vive con el backend.");
pair("Playwright", "Playwright");
pair("The browser tool for end-to-end tests of the real UI.", "La herramienta de navegador para tests end-to-end de la UI real.");
pair("SAST, SCA, DAST", "SAST, SCA, DAST");
pair("Static code analysis, dependency and image scanning, and scanning of a running app.", "Análisis estático del código, escaneo de dependencias e imágenes, y escaneo de una app en marcha.");
pair("Owner, Foreman, Worker", "Owner, Foreman, Worker");
pair("The human code owner; the supervising agent; the implementing agent.", "El dueño humano del código, el agente que supervisa, y el agente que implementa.");
pair("Agent queue, lane", "Cola de agentes, carril");
pair("The shared list of claimable work and the derived label that keeps agents from colliding.", "La lista compartida de trabajo que se puede tomar, y la etiqueta derivada que evita que los agentes choquen.");
pair("Observability", "Observabilidad");
pair("Metrics, logs and traces that show what the running system is doing.", "Métricas, logs y trazas que muestran qué hace el sistema en marcha.");
pair("Prometheus, Grafana, Loki, Tempo", "Prometheus, Grafana, Loki, Tempo");
pair("Metrics store, dashboards, log store and trace store.", "Almacén de métricas, tableros, almacén de logs y almacén de trazas.");
pair("SonarQube", "SonarQube");
pair("Code quality and static analysis with a gate on new code.", "Calidad de código y análisis estático, con una gate sobre el código nuevo.");
pair("Smoke test", "Smoke test");
pair("A short check that a freshly deployed system answers correctly.", "Un chequeo corto de que un sistema recién desplegado responde bien.");

pair("Business documentation", "Documentación de negocio");
pair(
  "Domain requirements, use cases and manuals. Markdown under docs/100-business/ stays the source of truth; this page deep-links into GitHub for the full trees.",
  "Requisitos del dominio, casos de uso y manuales. El Markdown en docs/100-business/ sigue siendo la fuente de verdad. Esta página enlaza a GitHub para los árboles completos.",
);
pair("Requirements", "Requisitos");
pair("System survey (RS), SRS and the requirements CSV used for traceability.", "Relevamiento del sistema (RS), SRS y el CSV de requisitos usado para la trazabilidad.");
pair("Use cases", "Casos de uso");
pair("Business use-case catalog (CUxx). Every change must cite one of these.", "Catálogo de casos de uso de negocio (CUxx). Cada cambio tiene que citar uno.");
pair("Actors", "Actores");
pair("Actors and hierarchy for the notarial domain.", "Actores y jerarquía del dominio notarial.");
pair("Traceability", "Trazabilidad");
pair("Requirements ↔ use-case matrix — the bridge to GitHub issues and code.", "Matriz de requisitos y casos de uso: el puente hacia los Issues de GitHub y el código.");
pair("Manuals", "Manuales");
pair("Installation, system and user manuals (user PDF via Release — ADR-022).", "Manuales de instalación, de sistema y de usuario (el PDF de usuario sale por Release, ADR-022).");
pair("docs/100-business/ on GitHub →", "docs/100-business/ en GitHub →");

pair("Module ownership", "Dueño de cada módulo");
pair(
  "Phase 0 of repository topology: keep one monorepo, give every folder a clear responsibility (ADR-026). Live Owner umbrella",
  "Fase 0 de la topología del repositorio: un solo monorepo, y cada carpeta con una responsabilidad clara (ADR-026). El paraguas abierto del Owner es",
);
pair("(prior #1197 closed by keyword parsing). Extraction happens later, only behind evidence gates.", "(el #1197 anterior se cerró por un parseo de palabras clave). La extracción viene después, y solo detrás de gates de evidencia.");
pair("Dependencies", "Dependencias");
pair("Module", "Módulo");
pair("Fleet", "Flota");
pair("Responsibility", "Responsabilidad");
pair("Verify", "Verificar");
pair("Scrollable table", "Tabla con desplazamiento");
pair("Module commands", "Comandos de módulos");
pair("REST API, business rules, persistence and Flyway schema", "API REST, reglas de negocio, persistencia y esquema Flyway");
pair("Next.js web client; talks to the API over HTTP only", "Cliente web Next.js. Habla con la API solo por HTTP");
pair("Observability, quality stack, Kubernetes and load-test assets", "Observabilidad, stack de calidad, Kubernetes y activos de prueba de carga");
pair("Black-box QA suites (E2E, integration smoke, database V&V)", "Suites de QA de caja negra (E2E, smoke de integración, V&V de base de datos)");
pair("Local AI SDLC engine; Cursor Cloud fleet does not use it", "Motor local del AI SDLC. La flota de Cursor Cloud no lo usa");
pair("Business and architecture knowledge base; OpenSpec root", "Base de conocimiento de negocio y arquitectura. Raíz de OpenSpec");
pair("Repository protection as code and the map of every security control", "La protección del repositorio como código, y el mapa de cada control de seguridad");
pair("Seams between modules and the guards that keep both sides in step", "Las costuras entre módulos y los guardas que mantienen los dos lados alineados");
pair("Unifier, manifest and cross-module guards (includes .github)", "Unificador, manifiesto y guardas entre módulos (incluye .github)");

pair("Security docs", "Docs de seguridad");
pair(
  "The interactive explanation — trust boundaries, STRIDE, the toolchain cadence, and the public snapshot — lives in the Security section. This page keeps the old route and points at the architecture notes on GitHub.",
  "La explicación interactiva — límites de confianza, STRIDE, la cadencia de la cadena y la foto pública — vive en la sección Seguridad. Esta página conserva la ruta vieja y apunta a las notas de arquitectura en GitHub.",
);
pair("Open the Security section", "Abrir la sección Seguridad");
pair("Assessment 2026-11-05", "Evaluación 2026-11-05");
pair("Security architecture docs", "Docs de arquitectura de seguridad");
pair("security/ module", "módulo security/");

pair("Architecture internals", "Interior de la arquitectura");
pair("API contract & Bruno", "Contrato de API y Bruno");
pair(
  "The REST API is code-first: controllers are the source, springdoc turns them into an OpenAPI document, and that document is committed. CI checks it is current, refuses unannounced breaking changes, generates the front end's types from it, and a Bruno suite calls the running API.",
  "La API REST nace del código: los controllers son la fuente, springdoc los vuelve un documento OpenAPI, y ese documento se commitea. El CI comprueba que esté al día, rechaza cambios incompatibles no anunciados, genera los tipos del frontend a partir de él, y una suite Bruno llama a la API en marcha.",
);
pair("API paths", "Rutas de la API");
pair("Operations", "Operaciones");
pair("Defects found", "Defectos encontrados");
pair("by the Bruno suite, all fixed", "por la suite Bruno, todos corregidos");
pair("One contract, four checks", "Un contrato, cuatro chequeos");
pair("The OpenAPI contract and its checks", "El contrato OpenAPI y sus chequeos");
pair(
  "Spring controllers export openapi.yaml; oasdiff, generated front-end types and a reachability test check it, and the Bruno suite calls the running API.",
  "Los controllers de Spring exportan openapi.yaml. oasdiff, los tipos generados del frontend y un test de alcance lo chequean, y la suite Bruno llama a la API en marcha.",
);
pair("The committed OpenAPI document sits between the backend and everything that depends on it.", "El documento OpenAPI commiteado queda entre el backend y todo lo que depende de él.");
pair("Check", "Chequeo");
pair("Fails when", "Falla cuando");
pair("Where", "Dónde");
pair("Export matches", "La exportación coincide");
pair("A controller changed but openapi.yaml was not regenerated.", "Un controller cambió y openapi.yaml no se regeneró.");
pair("Breaking changes", "Cambios incompatibles");
pair("oasdiff finds an error-level break against the base branch that no accepted-breaking-changes.d file lists.", "oasdiff encuentra una ruptura de nivel error contra la rama base que ningún archivo de accepted-breaking-changes.d lista.");
pair("Front-end types", "Tipos del frontend");
pair("api.generated.ts differs from what openapi-typescript generates.", "api.generated.ts difiere de lo que genera openapi-typescript.");
pair("Reachability", "Alcance");
pair("An endpoint has no caller in the front end and no reason in the allowlist.", "Un endpoint no tiene quien lo llame en el frontend y no tiene una razón en la lista de permitidos.");
pair("Why code-first", "Por qué el código es la fuente");
pair(
  "The controllers already existed when the contract was introduced, so the spec is generated from them rather than written by hand. Committing the generated file turns every API change into a visible diff in the pull request, which is what the breaking-change and type checks compare.",
  "Los controllers ya existían cuando se introdujo el contrato, así que la spec se genera desde ellos y no se escribe a mano. Commitear el archivo generado convierte cada cambio de API en un diff visible en el pull request, que es lo que comparan los chequeos de ruptura y de tipos.",
);
pair("The Bruno API suite", "La suite Bruno de la API");
pair(
  "Bruno keeps API tests as plain files in the repository, so they are reviewed like code. The collection uses the YAML OpenCollection format (Bruno CLI 4 or later): one file per request, with its assertions, grouped by resource. 00-auth runs first: it logs in and stores the JWT that the other folders send. This is that first request, read from the repository:",
  "Bruno guarda los tests de la API como archivos comunes en el repositorio, así se revisan como código. La colección usa el formato YAML OpenCollection (Bruno CLI 4 o posterior): un archivo por request, con sus aserciones, agrupado por recurso. 00-auth corre primero: inicia sesión y guarda el JWT que envían las otras carpetas. Este es ese primer request, leído del repositorio:",
);
pair("Locally, backend on :8080", "En local, con el backend en :8080");
pair("The E2E workflow starts the backend, then runs the suite with JUnit, JSON and HTML reporters and uploads the reports.", "El workflow de E2E levanta el backend, corre la suite con reporters JUnit, JSON y HTML, y sube los informes.");
pair("Resource folders:", "Carpetas de recursos:");
pair("Code is the source", "El código es la fuente");
pair("Controllers carry springdoc annotations. export-openapi.sh boots the backend on H2 and writes the document; CI fails if the committed copy differs.", "Los controllers llevan anotaciones de springdoc. export-openapi.sh levanta el backend en H2 y escribe el documento. El CI falla si la copia commiteada difiere.");
pair("Breaking changes need a ticket", "Un cambio incompatible necesita un ticket");
pair("oasdiff compares the document with the base branch. An error-level break fails the PR unless a file in accepted-breaking-changes.d names it and its issue.", "oasdiff compara el documento con la rama base. Una ruptura de nivel error hace fallar el pull request, salvo que un archivo en accepted-breaking-changes.d la nombre junto con su Issue.");
pair("The front end's types are generated", "Los tipos del frontend se generan");
pair("openapi-typescript writes api.generated.ts; frontend-ci runs openapi:types:check so the HTTP adapters cannot drift from the backend.", "openapi-typescript escribe api.generated.ts. frontend-ci corre openapi:types:check para que los adaptadores HTTP no se aparten del backend.");
pair("Every endpoint has a caller", "Cada endpoint tiene quien lo llama");
pair("A contract test lists each operation and fails when one has no caller in the front end and no reason in the allowlist.", "Un test de contrato lista cada operación y falla cuando una no tiene quien la llame en el frontend y no tiene una razón en la lista de permitidos.");
pair("Bruno calls the running API", "Bruno llama a la API en marcha");
pair("Bruno requests hit the real backend with real tokens and assert the responses, in CI and on a laptop.", "Los requests de Bruno pegan al backend real con tokens reales y afirman las respuestas, en CI y en una laptop.");
pair("Spring controllers", "Controllers de Spring");
pair("springdoc annotations", "anotaciones de springdoc");
pair("vs the base branch", "contra la rama base");
pair("PR fails", "El pull request falla");
pair("unless accepted", "salvo que esté aceptado");
pair("Front-end adapters", "Adaptadores del frontend");

pair(
  "The backend and the front end are both built as ports and adapters: business rules sit in the middle, and HTTP, JPA, PDF and the browser are plugged in from the outside. The Owner made it the rule for all code in ADR-030, after a pilot on the payment slice (ADR-021), and tests keep the boundaries from eroding.",
  "El backend y el frontend están armados como puertos y adaptadores: las reglas de negocio en el medio, y HTTP, JPA, PDF y el navegador enchufados desde afuera. El Owner lo hizo la regla para todo el código en ADR-030, después de un piloto en la tajada de pagos (ADR-021), y los tests impiden que los límites se erosionen.",
);
pair("Inbound port files", "Archivos de puertos de entrada");
pair("Use-case files", "Archivos de casos de uso");
pair("Outbound port files", "Archivos de puertos de salida");
pair("Known violations", "Violaciones conocidas");
pair("ArchUnit baseline", "Línea base de ArchUnit");
pair("The object", "El objeto");
pair(
  "Ports and adapters are not a stack of boxes. They are a sealed core with keyed sockets: only a matching plug reaches the domain, and ArchUnit fails any cable that tries to skip the socket. Hover a plug to seat it; the readout names the real package.",
  "Los puertos y adaptadores no son una pila de cajas. Son un núcleo sellado con enchufes con llave: solo un plug que coincide llega al dominio, y ArchUnit falla cualquier cable que intente saltear el enchufe. Pasá el cursor por un plug para asentarlo. La lectura nombra el paquete real.",
);
pair("The map", "El mapa");
pair("The package detail, as a static chart:", "El detalle de paquetes, como un diagrama estático:");
pair("Backend packages under com.licensis.notaire and the direction of their dependencies (ADR-030).", "Paquetes del backend bajo com.licensis.notaire y la dirección de sus dependencias (ADR-030).");
pair("Package", "Paquete");
pair("Holds", "Contiene");
pair("May depend on", "Puede depender de");
pair("Entities, value objects, domain services and exceptions. Plain Java.", "Entidades, value objects, servicios de dominio y excepciones. Java puro.");
pair("JDK only", "Solo el JDK");
pair("One interface per use case, with command and result records.", "Una interfaz por caso de uso, con records de comando y de resultado.");
pair("What use cases need from the outside, in domain terms.", "Lo que los casos de uso necesitan de afuera, en términos del dominio.");
pair("Classes implementing inbound ports; they own the transaction.", "Clases que implementan los puertos de entrada. Ellas son dueñas de la transacción.");
pair("domain, ports", "dominio, puertos");
pair("Controllers, web DTOs, mappers, request validation.", "Controllers, DTOs web, mappers y validación del request.");
pair("inbound ports", "puertos de entrada");
pair("JPA entities, Spring Data repositories, mappers, persistence adapters.", "Entidades JPA, repositorios de Spring Data, mappers y adaptadores de persistencia.");
pair("outbound ports, domain", "puertos de salida, dominio");
pair("Spring wiring, security, OpenAPI.", "Cableado de Spring, seguridad, OpenAPI.");
pair("everything", "todo");
pair("One request through a slice", "Un pedido a través de una tajada");
pair("The payment slice, the first one migrated, shows the whole path of a request:", "La tajada de pagos, la primera que se migró, muestra el camino completo de un pedido:");
pair("One payment request through the hexagon", "Un pedido de pago a través del hexágono");
pair(
  "POST /api/v1/payments goes from the Next.js app through PaymentController, the ProcessPaymentUseCase in-port and ProcessPaymentService, which uses two out-ports implemented by persistence adapters over PostgreSQL.",
  "POST /api/v1/payments sale de la app Next.js, pasa por PaymentController, el puerto de entrada ProcessPaymentUseCase y ProcessPaymentService, que usa dos puertos de salida implementados por adaptadores de persistencia sobre PostgreSQL.",
);
pair("Every box is a real class in the payment slice; the step panel links its file.", "Cada caja es una clase real de la tajada de pagos. El panel de pasos enlaza su archivo.");
pair("How the boundaries are enforced", "Cómo se sostienen los límites");
pair("The same shape in the front end", "La misma forma en el frontend");
pair("Where it is not finished", "Dónde no está terminado");
pair("An honest status", "Un estado honesto");
pair("Legacy package", "Paquete legado");
pair("Java files left", "Archivos Java que quedan");
pair("Inbound adapters do not reach repositories, legacy JPA or entities.", "Los adaptadores de entrada no llegan a repositorios, al JPA legado ni a las entidades.");
pair("The application layer depends on domain and ports only, plus @Service and @Transactional.", "La capa de aplicación depende solo del dominio y de los puertos, más @Service y @Transactional.");
pair("The domain depends on nothing but the JDK and itself.", "El dominio no depende de nada más que del JDK y de sí mismo.");
pair("Outbound adapters do not depend on inbound adapters.", "Los adaptadores de salida no dependen de los de entrada.");
pair("Inbound adapters do not depend on outbound adapters.", "Los adaptadores de entrada no dependen de los de salida.");
pair("Nothing new depends on the legacy jpa and service packages.", "Nada nuevo depende de los paquetes legados jpa y service.");
pair("Use-case slices do not form dependency cycles beyond the baseline.", "Las tajadas de casos de uso no forman ciclos de dependencia más allá de la línea base.");
pair("The UI posts a payment", "La UI publica un pago");
pair("The browser sends the budget id, amount, date and method as JSON with the user's JWT. Spring Security checks the token before any Notaire code runs.", "El navegador envía el id del presupuesto, el monto, la fecha y el medio como JSON, con el JWT del usuario. Spring Security chequea el token antes de que corra código de Notaire.");
pair("The controller makes a command", "El controller arma un comando");
pair("PaymentController.processPayment (CU15) validates the body and builds a ProcessPaymentCommand. It knows the in-port interface, not the service class.", "PaymentController.processPayment (CU15) valida el cuerpo y arma un ProcessPaymentCommand. Conoce la interfaz del puerto de entrada, no la clase del servicio.");
pair("The in-port is the contract", "El puerto de entrada es el contrato");
pair("ProcessPaymentUseCase is a plain interface in application/port/in. Spring injects whichever class implements it.", "ProcessPaymentUseCase es una interfaz simple en application/port/in. Spring inyecta la clase que la implemente.");
pair("The use case applies the rules", "El caso de uso aplica las reglas");
pair("ProcessPaymentService normalises the amount with Money, then refuses a payment that is zero or larger than the pending balance BudgetCharges computes. The rules live in domain/payment, with no Spring or JPA.", "ProcessPaymentService normaliza el monto con Money, y rechaza un pago que es cero o mayor que el saldo pendiente que calcula BudgetCharges. Las reglas viven en domain/payment, sin Spring ni JPA.");
pair("It asks for the charges through an out-port", "Pide los cargos a través de un puerto de salida");
pair("budgets.findCharges(budgetId) goes through BudgetLookupPort. BudgetLookupAdapter answers from the database and maps the JPA entities into domain types.", "budgets.findCharges(budgetId) pasa por BudgetLookupPort. BudgetLookupAdapter responde desde la base y mapea las entidades JPA a tipos de dominio.");
pair("It stores the payment through another", "Guarda el pago a través de otro");
pair("payments.register(new NewPayment(…)) goes through PaymentRepositoryPort, inside one transaction. PaymentPersistenceAdapter writes the row and returns PaymentDetails.", "payments.register(new NewPayment(…)) pasa por PaymentRepositoryPort, dentro de una transacción. PaymentPersistenceAdapter escribe la fila y devuelve PaymentDetails.");
pair("The answer goes back the same way", "La respuesta vuelve por el mismo camino");
pair("The service returns PaymentDetails; PaymentWebMapper turns it into the JSON response. Swapping the database or the web framework would touch only the outer ring.", "El servicio devuelve PaymentDetails. PaymentWebMapper lo vuelve la respuesta JSON. Cambiar la base o el framework web tocaría solo el anillo de afuera.");
pair("The domain is the centre", "El dominio es el centro");
pair("Use cases orchestrate", "Los casos de uso orquestan");
pair("In-ports: what the app can do", "Puertos de entrada: lo que la app puede hacer");
pair("Out-ports: what the app needs", "Puertos de salida: lo que la app necesita");
pair("Driving adapters call in", "Los adaptadores que manejan llaman hacia adentro");
pair("Driven adapters reach out", "Los adaptadores manejados salen hacia afuera");
pair("Rules hold the shape", "Las reglas sostienen la forma");
pair("The Notaire backend as a hexagon", "El backend de Notaire como un hexágono");
pair(
  "Domain in the centre, use cases and ports around it, driving adapters on the left and driven adapters on the right, with the browser and the database outside.",
  "El dominio en el centro, los casos de uso y los puertos alrededor, los adaptadores que manejan a la izquierda y los manejados a la derecha, con el navegador y la base afuera.",
);
pair("Domain", "Dominio");
pair("In-ports", "Puertos de entrada");
pair("Out-ports", "Puertos de salida");
pair("Driving adapters", "Adaptadores que manejan");
pair("Driven adapters", "Adaptadores manejados");
pair("REST controllers, mappers", "Controllers REST, mappers");
pair("JPA persistence, PDFBox reports", "Persistencia JPA, informes PDFBox");
pair("Next.js app", "App Next.js");
pair("Flyway schema", "Esquema Flyway");
pair("Every dependency points inwards. Click a ring or use the steps below.", "Toda dependencia apunta hacia adentro. Hacé clic en un anillo o usá los pasos de abajo.");
pair("The backend hexagon", "El hexágono del backend");
pair("Counts are read from the source tree when the site is built.", "Los conteos se leen del árbol de fuentes cuando se construye el sitio.");
pair("{n} contexts", "{n} contextos");
pair("{n} files", "{n} archivos");
pair(
  "{n} bounded contexts keep their entities and value objects in domain/ — plain Java with no Spring, JPA or HTTP. Money, BudgetCharges and PaymentStatus in domain/payment are examples.",
  "{n} contextos acotados guardan sus entidades y value objects en domain/: Java puro, sin Spring, JPA ni HTTP. Money, BudgetCharges y PaymentStatus en domain/payment son ejemplos.",
);
pair(
  "{n} files in application/usecase, across {contexts} contexts. Each service implements an in-port and reaches the outside only through out-ports.",
  "{n} archivos en application/usecase, en {contexts} contextos. Cada servicio implementa un puerto de entrada y sale al exterior solo por puertos de salida.",
);
pair(
  "{n} files in application/port/in: use-case interfaces and their commands (for example ProcessPaymentUseCase). Controllers depend on these, never on a service class.",
  "{n} archivos en application/port/in: interfaces de casos de uso y sus comandos (por ejemplo ProcessPaymentUseCase). Los controllers dependen de estas, nunca de una clase de servicio.",
);
pair(
  "{n} files in application/port/out: interfaces and the records they carry (for example PaymentRepositoryPort, BudgetLookupPort). The use case states the need; an adapter decides how.",
  "{n} archivos en application/port/out: interfaces y los records que llevan (por ejemplo PaymentRepositoryPort, BudgetLookupPort). El caso de uso dice la necesidad. Un adaptador decide el cómo.",
);
pair("{n} web adapter files: REST controllers and their mappers translate HTTP into commands and back.", "{n} archivos de adaptador web: los controllers REST y sus mappers traducen HTTP a comandos y de vuelta.");
pair("{n} outbound adapter files implement the out-ports: JPA persistence per context, plus PDFBox report rendering.", "{n} archivos de adaptador de salida implementan los puertos de salida: persistencia JPA por contexto, más el render de informes con PDFBox.");
pair(
  "Two ArchUnit tests check seven rules in every backend build: R1–R6 on dependencies, R7 on use-case cycles. The dependency baseline has {n} entries, so any new arrow pointing the wrong way fails the build.",
  "Dos tests de ArchUnit chequean siete reglas en cada build del backend: R1–R6 sobre dependencias, R7 sobre ciclos de casos de uso. La línea base de dependencias tiene {n} entradas, así que cualquier flecha nueva en la dirección equivocada hace fallar el build.",
);
pair("The port manifold", "El múltiple de puertos");
pair("Hover a plug, or use the arrow keys", "Pasá el cursor por un plug, o usá las flechas");
pair("Domain core: sealed, depends on the JDK only", "Núcleo de dominio: sellado, depende solo del JDK");
pair("Keyed plug: an adapter or port that seats into one socket", "Plug con llave: un adaptador o un puerto que calza en un solo enchufe");
pair("Six faces: inbound, ports, outbound — never a shortcut through the core", "Seis caras: entrada, puertos, salida. Nunca un atajo a través del núcleo");
pair("Fig 1", "Fig 1");

pair("Database & Flyway", "Base de datos y Flyway");
pair(
  "Notaire stores everything in PostgreSQL. The schema has exactly one source: the versioned SQL migrations that Flyway applies when the backend starts. Hibernate never creates or alters tables in production, and a test checks that the entities match the migrated schema (ADR-004, ADR-007).",
  "Notaire guarda todo en PostgreSQL. El esquema tiene una sola fuente: las migraciones SQL versionadas que Flyway aplica cuando arranca el backend. Hibernate nunca crea ni altera tablas en producción, y un test comprueba que las entidades coinciden con el esquema migrado (ADR-004, ADR-007).",
);
pair("Migrations", "Migraciones");
pair("Schema version", "Versión del esquema");
pair("Rollback scripts", "Scripts de rollback");
pair("applied by hand only", "se aplican solo a mano");
pair("Flyway settings", "Ajustes de Flyway");
pair("How a schema change ships", "Cómo sale un cambio de esquema");
pair("A migration is tested in CI against a real PostgreSQL, recorded in the release manifest, and applied at start-up.", "Una migración se prueba en CI contra un PostgreSQL real, se anota en el manifiesto de release y se aplica al arrancar.");
pair("The Flyway settings", "Los ajustes de Flyway");
pair("Setting", "Ajuste");
pair("Value", "Valor");
pair("Why", "Por qué");
pair("Flyway runs at every backend start-up.", "Flyway corre en cada arranque del backend.");
pair("An existing database without history is baselined instead of rejected.", "Una base que ya existe y no tiene historia se toma como baseline, en lugar de rechazarla.");
pair("The baseline sits below V1, so every migration still runs on a fresh database.", "El baseline queda debajo de V1, así que todas las migraciones igual corren en una base nueva.");
pair("Migrations are packaged inside the backend jar.", "Las migraciones viajan dentro del jar del backend.");
pair("A migration numbered below the latest applied one is refused.", "Una migración con un número menor que la última aplicada se rechaza.");
pair("Start-up fails if an applied migration's checksum changed.", "El arranque falla si cambió el checksum de una migración ya aplicada.");
pair("Nobody can wipe a database with flyway clean.", "Nadie puede vaciar una base con flyway clean.");
pair("Start-up waits for PostgreSQL to accept connections.", "El arranque espera a que PostgreSQL acepte conexiones.");
pair("The schema history", "La historia del esquema");
pair("Every migration on main, grouped by what it did. The list is read from the migration folder when the site is built.", "Cada migración en main, agrupada por lo que hizo. La lista se lee de la carpeta de migraciones cuando se construye el sitio.");
pair("What keeps the schema honest", "Qué mantiene el esquema honesto");
pair("What it proves", "Qué prueba");
pair("Schema validation test", "Test de validación del esquema");
pair("The migrated schema matches every JPA entity (no drift).", "El esquema migrado coincide con cada entidad JPA (sin deriva).");
pair("Integration tests", "Tests de integración");
pair("Run on a real PostgreSQL through Testcontainers, migrations included.", "Corren sobre un PostgreSQL real con Testcontainers, migraciones incluidas.");
pair("Database V&V", "V&V de la base de datos");
pair("The database suite on its own workflow.", "La suite de base de datos en su propio workflow.");
pair("Backup and restore (not yet active)", "Backup y restore (todavía no activo)");
pair("Skips itself until the backup tooling of #256 exists, rather than reporting a false green.", "Se saltea solo hasta que exista el tooling de backup de #256, en lugar de informar un verde falso.");
pair("Known gap", "Hueco conocido");
pair("Baseline", "Línea de base");
pair("The schema carried over from the original system, and its reference data.", "El esquema heredado del sistema original, y sus datos de referencia.");
pair("Fixes and features", "Correcciones y funciones");
pair("Missing columns, workflow tables, roles and permissions, notebooks, registration drafts.", "Columnas que faltaban, tablas de workflow, roles y permisos, cuadernos, borradores de inscripción.");
pair("English names", "Nombres en inglés");
pair("Tables renamed from Spanish to English, one bounded group per migration.", "Tablas renombradas del español al inglés, un grupo acotado por migración.");
pair("Hardening", "Endurecimiento");
pair("Numeric money, unique identities, revoked tokens, login attempts, append-only audit, bcrypt, role templates.", "Dinero numérico, identidades únicas, tokens revocados, intentos de login, auditoría de solo agregado, bcrypt, plantillas de roles.");
pair("Flyway migration timeline", "Línea de tiempo de migraciones Flyway");
pair("Hover a dot to read a migration; click a phase to step to it.", "Pasá el cursor por un punto para leer una migración. Hacé clic en una fase para ir a ese paso.");
pair("One dot per V<n>__*.sql file, read from the migration folder at build time.", "Un punto por archivo V<n>__*.sql, leído de la carpeta de migraciones al construir el sitio.");

pair("Start here", "Empieza aquí");
pair("Technology stack", "Stack tecnológico");
pair(
  "Every technology Notaire uses and the job it does. Versions are read from pom.xml, package.json and the Compose files when this site is built, so they cannot drift from the code.",
  "Cada tecnología que usa Notaire y el trabajo que hace. Las versiones se leen de pom.xml, package.json y los archivos de Compose cuando se construye este sitio, así no se apartan del código.",
);
pair("ADRs", "ADR");
pair("the decisions behind this list", "las decisiones detrás de esta lista");
pair("Patterns", "Patrones");
pair("Pattern", "Patrón");
pair("Where it shows", "Dónde se ve");
pair("Ports and adapters (hexagonal)", "Puertos y adaptadores (hexagonal)");
pair("Database as code", "La base como código");
pair("Contract-first checks on a code-first API", "Chequeos de contrato sobre una API que nace del código");
pair("Architecture decision records", "Registros de decisiones de arquitectura");
pair("Spec-driven, gated SDLC for people and AI agents", "SDLC dirigido por specs y con gates, para personas y agentes de IA");
pair("Technology", "Tecnología");
pair("Version", "Versión");
pair("What it does here", "Qué hace acá");
pair("Language of the backend; the build pins the release.", "Lenguaje del backend. El build fija el release.");
pair("Web, validation, JPA, security, actuator and Flyway starters.", "Starters de web, validación, JPA, seguridad, actuator y Flyway.");
pair("Stateless JWT sessions, bcrypt passwords, revoked-token and login-attempt tables.", "Sesiones JWT sin estado, contraseñas bcrypt, y tablas de tokens revocados e intentos de login.");
pair("Persistence behind the out-ports; it never changes the schema in production.", "Persistencia detrás de los puertos de salida. En producción no cambia el esquema.");
pair("Generates the OpenAPI document from the controllers.", "Genera el documento OpenAPI a partir de los controllers.");
pair("Renders PDF reports behind an out-port adapter.", "Renderiza informes PDF detrás de un adaptador de puerto de salida.");
pair("Removes boilerplate in entities and DTOs.", "Saca el boilerplate de entidades y DTOs.");
pair("App Router front end for the office staff.", "Frontend con App Router para el personal de la oficina.");
pair("UI library.", "Biblioteca de UI.");
pair("Strict types, including types generated from the API contract.", "Tipos estrictos, incluidos los generados desde el contrato de la API.");
pair("Styling and accessible primitives (dialogs, menus, tabs, selects).", "Estilos y primitivas accesibles (diálogos, menús, pestañas, selects).");
pair("Server state: fetching, caching, invalidation.", "Estado del servidor: pedidos, caché e invalidación.");
pair("Client-side auth session store.", "Almacén de la sesión de autenticación en el cliente.");
pair("Spanish and English messages.", "Mensajes en español y en inglés.");
pair("The workflow editor and progress graphs.", "El editor de workflows y los gráficos de avance.");
pair("Turns openapi.yaml into api.generated.ts.", "Convierte openapi.yaml en api.generated.ts.");
pair("Data", "Datos");
pair("The only production database.", "La única base de producción.");
pair("Versioned SQL migrations; the single owner of the schema.", "Migraciones SQL versionadas. El único dueño del esquema.");
pair("In-memory database used only to export the OpenAPI document in CI.", "Base en memoria, usada solo para exportar el documento OpenAPI en CI.");
pair("Testing", "Pruebas");
pair("{n} backend test classes.", "{n} clases de test del backend.");
pair("Integration tests against a real PostgreSQL.", "Tests de integración contra un PostgreSQL real.");
pair("Enforces the hexagonal rules R1–R7 in the build.", "Exige las reglas hexagonales R1–R7 en el build.");
pair("{n} frontend test files.", "{n} archivos de test del frontend.");
pair("{n} end-to-end specs against the full stack.", "{n} specs end-to-end contra el stack completo.");
pair("API requests with assertions, run by CI against the live backend.", "Requests de API con aserciones, que el CI corre contra el backend en vivo.");
pair("Performance tests in their own workflow.", "Tests de performance en su propio workflow.");
pair("Backend coverage reports.", "Informes de cobertura del backend.");
pair("Quality and security", "Calidad y seguridad");
pair("Formatting and static analysis in the Maven build.", "Formato y análisis estático en el build de Maven.");
pair("Front-end lint, including the import rules that keep the hexagon closed.", "Lint del frontend, incluidas las reglas de import que mantienen cerrado el hexágono.");
pair("Code scanning for Java, TypeScript and the workflows themselves.", "Escaneo de código para Java, TypeScript y los workflows mismos.");
pair("Baseline DAST scan against the running app.", "Escaneo DAST de línea base contra la app en marcha.");
pair("Local quality dashboard in the observability stack.", "Tablero local de calidad en el stack de observabilidad.");
pair("Dependency update pull requests.", "Pull requests de actualización de dependencias.");
pair("Traces from backend and browser through the collector.", "Trazas del backend y del navegador a través del collector.");
pair("Metrics scraped from the actuator.", "Métricas que se scrapean del actuator.");
pair("Dashboards over metrics, traces and logs.", "Tableros sobre métricas, trazas y logs.");
pair("Trace storage.", "Almacén de trazas.");
pair("Log storage; the backend writes structured JSON logs.", "Almacén de logs. El backend escribe logs JSON estructurados.");
pair("Delivery and AI tooling", "Entrega y herramientas de IA");
pair("{n} workflows (listed below).", "{n} workflows (listados abajo).");
pair("{app} application services and {obs} observability services.", "{app} servicios de aplicación y {obs} servicios de observabilidad.");
pair("Versions and changelog for product releases.", "Versiones y changelog de los releases del producto.");
pair("Every change starts as a proposal, design, spec and tasks.", "Cada cambio empieza como proposal, design, spec y tasks.");
pair("AI agents that work through the same SDLC gates as people.", "Agentes de IA que pasan por las mismas gates del SDLC que las personas.");
pair("CI/CD workflows", "Workflows de CI/CD");
pair("What starts each workflow, read from its on: block. Product workflows sit on the tag and on manual dispatch, not on the pull request.", "Qué arranca cada workflow, leído de su bloque on:. Los workflows del producto están en el tag y en el disparo manual, no en el pull request.");
pair("Full write-up", "El texto completo");
pair("Pull request", "Pull request");
pair("Push", "Push");
pair("After CI on main", "Después del CI en main");
pair("Scheduled", "Programado");
pair("Comment", "Comentario");
pair("A pull_request trigger is rare. Product builds, tests and security scans do not start here. The local preflight is the proof before merge.", "Un disparo pull_request es raro. Los builds, tests y escaneos de seguridad del producto no arrancan acá. El preflight local es la prueba antes del merge.");
pair("A version tag starts the product suite and image publish. A push to main starts Release Please and the portfolio. It does not publish images.", "Un tag de versión arranca la suite del producto y la publicación de imágenes. Un push a main arranca Release Please y el portafolio. No publica imágenes.");
pair("A workflow_run chain would start after another workflow. Product publish is a tag trigger, not a chain from main.", "Una cadena workflow_run arrancaría después de otro workflow. La publicación del producto es un disparo de tag, no una cadena desde main.");
pair("Scheduled jobs cover what a tag does not: the Monday ZAP baseline, k6, the Sunday backup smoke, and artifact cleanup. CodeQL also has a Monday run.", "Los trabajos programados cubren lo que un tag no hace: la línea base de ZAP del lunes, k6, el smoke de backup del domingo y la limpieza de artefactos. CodeQL también corre los lunes.");
pair("An AI agent workflow wakes up when someone mentions it in an issue or review comment.", "Un workflow de agente de IA se despierta cuando alguien lo menciona en un Issue o en un comentario de review.");
pair("CI/CD pipeline", "Pipeline de CI/CD");
pair("Project chart", "Tablero del proyecto");
pair(
  "Live milestone snapshot for {total} issues ({open} open, {closed} closed). Every issue has a history or delivery milestone. {claimable} open issues are delivery work; {catalog} are catalog anchors.",
  "Foto viva de los hitos para {total} issues ({open} abiertos, {closed} cerrados). Cada issue tiene un hito de historia o de entrega. {claimable} issues abiertos son trabajo de entrega; {catalog} son anclas del catálogo.",
);
pair(
  "{catalog} RF, CU, and RNF anchors remain open for traceability. They have dedicated Project views and are never picked as implementation work.",
  "{catalog} anclas RF, CU y RNF siguen abiertas para la trazabilidad. Tienen vistas propias en el Project y nunca se toman como trabajo de implementación.",
);
pair("Roadmap and progress", "Roadmap y avance");
pair("Project management docs →", "Docs de gestión del proyecto →");
pair("History", "Historia");
pair("closed issues grouped by era", "issues cerrados, agrupados por época");
pair("open delivery work", "trabajo de entrega abierto");
pair("Catalog", "Catálogo");
pair("requirement anchors, not agent work", "anclas de requisitos, no trabajo de agentes");
pair("Delivery sequence", "Secuencia de entrega");
pair("Train", "Tren");
pair("Open", "Abiertos");
pair("How the project was organized", "Cómo se organizó el proyecto");
pair("Closed cohorts preserve the path from the first issue to the current delivery plan. Curated phase buckets remain distinct from date cohorts.", "Las cohortes cerradas conservan el camino desde el primer Issue hasta el plan de entrega actual. Los grupos de fase curados siguen distintos de las cohortes por fecha.");
pair("Requirements catalog", "Catálogo de requisitos");
pair("Parallel lanes", "Carriles en paralelo");
pair("Lanes are derived from area and type labels. Cross work blocks backend and frontend; security also blocks backend. Catalog and epic lanes are not claimable.", "Los carriles se derivan de las etiquetas de área y de tipo. El trabajo cruzado bloquea backend y frontend. Seguridad también bloquea backend. Los carriles de catálogo y de epic no se pueden tomar.");
pair("H0–H8 history (2014–2026-10)", "Historia H0–H8 (2014–2026-10)");
pair("v0.1 First release cuts", "v0.1 Primeros cortes de release");
pair("v0.2 Release identity & product forms", "v0.2 Identidad de release y formularios del producto");
pair("v0.3 Hexagonal core & security (2026-11)", "v0.3 Núcleo hexagonal y seguridad (2026-11)");
pair("v0.4 Product completeness (2026-12)", "v0.4 Completitud del producto (2026-12)");
pair("v0.5 Quality & traceability (2027-01)", "v0.5 Calidad y trazabilidad (2027-01)");
pair("v0.6 Platform & operability (2027-02)", "v0.6 Plataforma y operación (2027-02)");
pair("v1.0 GA: legacy-free (2027-03)", "v1.0 GA: sin legado (2027-03)");
pair("Backlog · Unscheduled", "Backlog · Sin fecha");
pair("Merged pull requests each ISO week", "Pull requests fusionados por semana ISO");
pair("Median {median} per week. A week with no merge is zero. Bot pull requests are excluded.", "Mediana {median} por semana. Una semana sin merge es cero. Los pull requests de bots no entran.");
pair("{week}: {count} merged pull requests", "{week}: {count} pull requests fusionados");
pair("Weeks", "Semanas");
pair("Weekly chart", "Gráfico semanal");
pair("Week", "Semana");
pair("Weekly counts from the delivery snapshot", "Conteos semanales de la foto de entrega");
pair("This snapshot has no weekly counts.", "Esta foto no tiene conteos semanales.");
pair("Diagram key", "Clave del diagrama");
pair("Person or role", "Persona o rol");
pair("The work", "El trabajo");
pair("A command", "Un comando");
pair("A refusal", "Un rechazo");
pair("Done", "Hecho");
pair("It starts with a real issue", "Empieza con un Issue real");
pair("Every change starts from one GitHub Issue linked to a use case or a requirement. A prompt is not a specification.", "Cada cambio empieza en un Issue de GitHub enlazado a un caso de uso o a un requisito. Un prompt no es una especificación.");
pair("Gate 1: plan before code", "Gate 1: el plan antes del código");
pair("An OpenSpec change on the notaire-sdlc schema describes only this change. validate-sdlc-plan.sh checks that the Issue exists and is open. The kit does not write the spec.", "Un cambio OpenSpec con el esquema notaire-sdlc describe solo este cambio. validate-sdlc-plan.sh comprueba que el Issue existe y está abierto. El kit no escribe la spec.");
pair("Gate 2: watch the tests fail", "Gate 2: ver fallar los tests");
pair("Tests are written first and seen failing. A test that was never red proves nothing.", "Los tests se escriben primero y se ven fallar. Un test que nunca estuvo en rojo no prueba nada.");
pair("The minimum code that passes", "El mínimo código que pasa");
pair("The implementation is the minimum that turns those tests green, using the project skills. It does not skip a gate.", "La implementación es el mínimo que pone esos tests en verde, usando las skills del proyecto. No saltea una gate.");
pair("Gate 3: the local proof", "Gate 3: la prueba local");
pair("The pull-request proof is preflight.sh --full on the agent machine. GitHub does not re-run the product suite on a pull request. Product CI runs on a version tag.", "La prueba del pull request es preflight.sh --full en la máquina del agente. GitHub no vuelve a correr la suite del producto en un pull request. El CI del producto corre en un tag de versión.");
pair("Gate 4: who may merge", "Gate 4: quién puede fusionar");
pair("The Foreman may merge a Worker pull request when its merge rule is met. The Owner merges Constitution, sdlc-exception, and harness changes. Workers never merge.", "El Foreman puede fusionar el pull request de un Worker cuando se cumple su regla de merge. El Owner fusiona la Constitution, sdlc-exception y los cambios del harness. Los Workers nunca fusionan.");
pair("Gate 5: shipped and closed", "Gate 5: entregado y cerrado");
pair("A version tag runs product CI and image publish. Smoke passes, then the Issue is closed. Pages deploy on the push to main, not as a substitute for that tag.", "Un tag de versión corre el CI del producto y la publicación de imágenes. Pasa el smoke, y después se cierra el Issue. Pages se publica en el push a main, y no reemplaza ese tag.");
pair("What the kit on main does", "Qué hace el kit que está en main");
pair("The aisdlc kit runs gates by exit code. aisdlc foreman plan only prints a route; it does not run agents. The plugin allow list is openspec and example-method. Spec Kit and AWS AI-DLC are declared and not loaded.", "El kit aisdlc corre las gates por código de salida. aisdlc foreman plan solo imprime una ruta. No corre agentes. La lista permitida de plugins es openspec y example-method. Spec Kit y AWS AI-DLC están declarados y no están cargados.");
pair("A refusal is a loop, not a crash", "Un rechazo es un ciclo, no un choque");
pair("A gate whose command exits non-zero sends the change back with the reason. Agents cannot skip a gate; they can only satisfy it.", "Una gate cuyo comando sale distinto de cero devuelve el cambio con el motivo. Los agentes no pueden saltear una gate. Solo pueden cumplirla.");
pair("Gate 1", "Gate 1");
pair("Gate 2", "Gate 2");
pair("Gate 3", "Gate 3");
pair("Gate 4", "Gate 4");
pair("Gate 5", "Gate 5");
pair("linked to a use case", "enlazado a un caso de uso");
pair("OpenSpec, then validate", "OpenSpec, y después validar");
pair("tests seen failing", "tests vistos fallar");
pair("minimum code", "código mínimo");
pair("Foreman or Owner", "Foreman u Owner");
pair("tag, smoke, close", "tag, smoke, cierre");
pair("Closed", "Cerrado");
pair("issue done", "issue cerrado");
pair("aisdlc kit", "kit aisdlc");
pair("exit codes only", "solo códigos de salida");
pair("Refusal", "Rechazo");
pair("non-zero, back to work", "distinto de cero, de vuelta al trabajo");
pair("Where each gate runs", "Dónde corre cada gate");
pair("The pull-request proof is on the agent machine. Product CI runs on a version tag. Pages and Release Please run on main. ZAP, CodeQL, k6 and backup-restore run on a schedule.", "La prueba del pull request está en la máquina del agente. El CI del producto corre en un tag de versión. Pages y Release Please corren en main. ZAP, CodeQL, k6 y backup-restore corren en un horario.");
pair("Step from the agent machine to the tag, then to main. The schedule is beside them, not on the pull request.", "Del agente al tag, y después a main. El horario está al lado, no en el pull request.");
pair("The same story as one map: local preflight, the merge rule, the version tag, main, and the schedule.", "La misma historia en un mapa: preflight local, la regla de merge, el tag de versión, main y el horario.");
pair("On the agent machine", "En la máquina del agente");
pair("The pull-request proof is preflight.sh --full, including the security case catalog. GitHub does not re-run that suite on the pull request. Workers never merge.", "La prueba del pull request es preflight.sh --full, incluido el catálogo de casos de seguridad. GitHub no vuelve a correr esa suite en el pull request. Los Workers nunca fusionan.");
pair("On a version tag", "En un tag de versión");
pair("Product CI, Bruno, Playwright, and image publish run when a version tag is pushed, or when someone dispatches the workflow. A quiet pull request is the design.", "El CI del producto, Bruno, Playwright y la publicación de imágenes corren cuando se pushea un tag de versión, o cuando alguien dispara el workflow. Un pull request en silencio es el diseño.");
pair("On a push to main", "En un push a main");
pair("Release Please and the portfolio publish on main. A merge to main does not publish the product images.", "Release Please y el portafolio se publican en main. Un merge a main no publica las imágenes del producto.");
pair("On a schedule", "En un horario");
pair("Monday runs the ZAP baseline and CodeQL. Sunday runs backup-restore. k6 is on its own Monday schedule. These do not start on a pull request.", "El lunes corre la línea base de ZAP y CodeQL. El domingo corre backup-restore. k6 tiene su propio horario del lunes. Nada de esto arranca en un pull request.");
pair("Agent machine", "Máquina del agente");
pair("Version tag", "Tag de versión");
pair("product CI and images", "CI del producto e imágenes");
pair("Pages, Release Please", "Pages, Release Please");
pair("Schedule", "Horario");
pair("ZAP, k6, backup", "ZAP, k6, backup");
pair("Lead time from opened to merged.", "Lead time de abierto a fusionado.");
pair("The snapshot stores the median and the p90, not a histogram of every pull request.", "La foto guarda la mediana y el p90, no un histograma de cada pull request.");
pair("Lead time was not measured in this snapshot.", "El lead time no se midió en esta foto.");
pair("Lead time summary from the delivery snapshot", "Resumen de lead time de la foto de entrega");
pair("Median", "Mediana");
pair("Pull requests in the window", "Pull requests en la ventana");
pair("The browser is outside the trust boundary", "El navegador está fuera del límite de confianza");
pair("Staff send the session cookie. Headers, the body, and the query are data. A client header is not an identity.", "El personal envía la cookie de sesión. Los headers, el cuerpo y la query son datos. Un header del cliente no es una identidad.");
pair("The BFF proxies on the server", "El BFF hace de proxy en el servidor");
pair("Next.js calls the API with the runtime backend address. The public login page does not reveal that host.", "Next.js llama a la API con la dirección de backend del runtime. La página pública de login no revela ese host.");
pair("The JWT filter authenticates", "El filtro JWT autentica");
pair("JwtAuthenticationFilter checks the cookie or the bearer token before a use case runs.", "JwtAuthenticationFilter chequea la cookie o el bearer token antes de que corra un caso de uso.");
pair("The module grant authorizes the path", "El permiso del módulo autoriza la ruta");
pair("RoleModuleAuthorizationFilter returns 403 when the role does not hold the module for that path. A notary is not an administrator.", "RoleModuleAuthorizationFilter devuelve 403 cuando el rol no tiene el módulo de esa ruta. Un escribano no es un administrador.");
pair("The use case writes through parameters", "El caso de uso escribe con parámetros");
pair("The use case reaches PostgreSQL with parameterized JPA. Personal data and notarial records stay inside this last boundary.", "El caso de uso llega a PostgreSQL con JPA parametrizado. Los datos personales y los expedientes notariales se quedan dentro de este último límite.");
