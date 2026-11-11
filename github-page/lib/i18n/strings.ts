/** Side-effect registry: every English sentence the translated routes show, with its Spanish. */
import { pair } from "./es";
import "./strings-content";
import "./strings-rest";
import "./strings-docs";

// Demo shell
pair("Demo – sample data, read-only", "Demo: datos de ejemplo, solo lectura");
pair(
  "Sign in with any name and password; nothing you change is saved.",
  "Entra con cualquier nombre y contraseña. Nada de lo que cambies se guarda.",
);
pair("Back to the project site", "Volver al sitio del proyecto");
pair("Data recorded", "Datos grabados");
pair("Demo notice", "Aviso de la demo");

// About
pair("About", "Sobre el autor");
pair(
  "The engineer behind Notaire. Everything below comes from a public source, linked next to it.",
  "La persona que construye Notaire. Todo lo de abajo sale de una fuente pública, enlazada al lado.",
);
pair("Delivery outcomes", "Resultados de entrega");
pair("The story of the project", "La historia del proyecto");
pair("Source:", "Fuente:");
pair("Sources checked", "Fuentes revisadas");
pair("Name", "Nombre");
pair("Based in", "Dónde vive");
pair(
  "Builds and maintains Notaire. In its AI SDLC he is the Owner: he approves the specifications and, as code owner, merges the changes.",
  "Construye y mantiene Notaire. En su AI SDLC es el Owner: aprueba las especificaciones y, como dueño del código, fusiona los cambios.",
);
pair(
  "A 2012 course project in the Licenciatura en Sistemas at Universidad FASTA, San Carlos de Bariloche, written with Juan Carlos Ramos and Estefanía Klein.",
  "Un trabajo de curso de 2012 en la Licenciatura en Sistemas de la Universidad FASTA, San Carlos de Bariloche, escrito con Juan Carlos Ramos y Estefanía Klein.",
);
pair("On this project", "En este proyecto");
pair("Where Notaire began", "Dónde empezó Notaire");

// Product overview
pair("Product", "Producto");
pair("Case management for a notary office", "Gestión de expedientes para una escribanía");
pair(
  "Notaire runs the daily work of an Argentine notary office (escribanía): the people and properties involved, each case from quote to archive, the deeds and their testimonies, payments, deadlines and an audit trail. It is a web app — a Next.js front end over a Spring Boot API and PostgreSQL — in English and Spanish. Try the live demo: the real app with sample data, read-only.",
  "Notaire lleva el trabajo diario de una escribanía argentina: las personas y los inmuebles, cada expediente del presupuesto al archivo, las escrituras y sus testimonios, los pagos, los vencimientos y la auditoría. Es una aplicación web: un frontend Next.js sobre una API Spring Boot y PostgreSQL, en inglés y en español. Prueba la demo en vivo: la app real, con datos de ejemplo, de solo lectura.",
);
pair("Use cases", "Casos de uso");
pair("documented in docs/100-business", "documentados en docs/100-business");
pair("App areas", "Áreas de la app");
pair("in the front-end menu", "en el menú del frontend");
pair("API paths", "rutas de la API");
pair("in the OpenAPI contract", "en el contrato OpenAPI");
pair("Product version", "Versión del producto");
pair("from pom.xml", "desde pom.xml");
pair("Who uses it", "Quién lo usa");
pair("Runs the cases, drafts and signs deeds and certificates, manages the folios.", "Lleva los expedientes, redacta y firma escrituras y certificados, y administra los folios.");
pair("Answers questions about cases, prepares quotes and registers people and clients.", "Responde consultas sobre expedientes, prepara presupuestos y registra personas y clientes.");
pair("Starts each case and follows it through documents, registrations and payments.", "Abre cada expediente y lo sigue en documentos, inscripciones y pagos.");
pair("Configures catalogs, templates, users, roles, substitutions and reads the audit trail.", "Configura catálogos, plantillas, usuarios, roles y sustituciones, y lee la auditoría.");
pair("How a case moves", "Cómo avanza un expediente");
pair(
  "Every case follows a status workflow that the office can redesign in the app (ADR-014); the default path is the one the use cases describe.",
  "Cada expediente sigue un flujo de estados que la escribanía puede rediseñar en la app (ADR-014). El camino por defecto es el que describen los casos de uso.",
);
pair("How it works", "Cómo funciona");
pair("Prepare a quote", "Preparar un presupuesto");
pair("Start the case", "Abrir el expediente");
pair("List the documents and certificates it needs", "Listar los documentos y certificados que necesita");
pair("Register the client's documents", "Registrar los documentos del cliente");
pair("Prepare and sign the deed", "Preparar y firmar la escritura");
pair("Issue the testimony", "Emitir el testimonio");
pair("Submit it for registration", "Presentarlo para la inscripción");
pair("Process the payment", "Procesar el pago");
pair("Archive the case", "Archivar el expediente");
pair("What is in the app", "Qué hay en la app");
pair("The app's menu, read from the front end at build time — each entry is a screen with its own tests.", "El menú de la app, leído del frontend al construir el sitio. Cada entrada es una pantalla con sus propios tests.");
pair("admin", "admin");
pair("How it is kept working", "Cómo se mantiene en marcha");

// Product story / platform headings
pair("Project History", "Historia del proyecto");
pair("A Decade of Evolution", "Una década de evolución");
pair("From dormant Swing monolith to an AI-accelerated platform — every commit tells a story.", "De un monolito Swing dormido a una plataforma acelerada con IA. Cada commit cuenta una parte.");
pair("The Origin", "El origen");
pair("University Project", "Proyecto universitario");
pair("A Java Swing monolith born as a university notarial management system. Direct DB access, JDBC queries embedded in UI event handlers, tightly coupled business logic.", "Un monolito Java Swing nacido como sistema de gestión notarial universitario. Acceso directo a la base, consultas JDBC dentro de los manejadores de la interfaz y reglas de negocio acopladas.");
pair("Years of Silence", "Años de silencio");
pair("Project Paused", "Proyecto en pausa");
pair("The project entered a long dormancy. Real-world complexity exceeded the scope of an academic exercise. The code slept, but the vision remained.", "El proyecto quedó en una larga pausa. La complejidad real superó el alcance de un ejercicio académico. El código durmió, y la idea siguió.");
pair("The Renaissance", "El renacimiento");
pair("Modern Reboot", "Reinicio moderno");
pair("A decade later, the project was reborn. Java 21, Spring Boot 3.2.9, Docker, PostgreSQL. The monolith began its transformation into a layered REST architecture.", "Una década después, el proyecto volvió a nacer. Java 21, Spring Boot 3.2.9, Docker, PostgreSQL. El monolito empezó a convertirse en una arquitectura REST por capas.");
pair("API & Frontend", "API y frontend");
pair("Next.js Migration", "Paso a Next.js");
pair("REST API layer solidified. The Swing GUI was retired, replaced by a modern Next.js 15 + React 19 + TypeScript frontend with a proper design system.", "La capa REST quedó firme. La interfaz Swing se retiró y la reemplazó un frontend Next.js 15, React 19 y TypeScript, con un sistema de diseño.");
pair("AI Acceleration", "Aceleración con IA");
pair("Claude Code Integration", "Integración de Claude Code");
pair("AI-assisted development became the primary workflow. 220 commits in two months. Tests, E2E coverage, security audits, observability—all accelerated by Claude AI agents.", "El desarrollo asistido por IA pasó a ser el flujo principal. 220 commits en dos meses. Tests, cobertura E2E, auditorías de seguridad y observabilidad, acelerados por agentes de Claude.");
pair("Production Ready", "Listo para producción");
pair("Observability & Security", "Observabilidad y seguridad");
pair("Grafana dashboards, Prometheus alerts, Loki log aggregation, SonarQube quality gates. A 2014 university project became a production-grade platform on Spring Boot 4.1.0 and Next.js 16.", "Tableros de Grafana, alertas de Prometheus, logs en Loki y quality gates de SonarQube. Un proyecto universitario de 2014 se volvió una plataforma de producción sobre Spring Boot 4.1.0 y Next.js 16.");
pair("Cloud AI SDLC", "AI SDLC en la nube");
pair("Autonomous Delivery Fleet", "Flota de entrega");
pair("Cursor Cloud agents took the CONSTITUTION from paper to practice: OpenSpec Gate 1, TDD-first PRs, heavy-CI merge gates (Bruno + Playwright + coverage), Flyway as the sole schema source, and continuous Englishize hygiene. The Delivery Board stayed the single work queue while multi-slice API validation and UI/UX audits shipped in serial heavy-green merges.", "Los agentes de Cursor Cloud pasaron la Constitución del papel a la práctica: OpenSpec en la Gate 1, pull requests con TDD primero, gates de merge con CI pesado (Bruno, Playwright y cobertura), Flyway como única fuente del esquema, y la higiene de pasar el código al inglés. El tablero de entrega siguió siendo la única cola de trabajo.");
pair("March", "marzo");
pair("Dormancy", "pausa");
pair("December", "diciembre");
pair("February", "febrero");
pair("March–April", "marzo–abril");
pair("May–August", "mayo–agosto");
pair("September–October", "septiembre–octubre");
pair("commits", "commits");
pair("Three Generations", "Tres generaciones");
pair("The same business domain, reimagined through each era of software engineering.", "El mismo dominio de negocio, rehecho en cada época de la ingeniería de software.");
pair("Built with Artificial Intelligence", "Construido con inteligencia artificial");
pair("In 2026, Claude AI became the primary co-developer, then Cursor Cloud agents closed the loop on remote VMs: OpenSpec, TDD, documentation, Bruno, Playwright, and preflight.sh --full before a merge.", "En 2026, Claude pasó a ser el coautor principal. Después, los agentes de Cursor Cloud cerraron el ciclo en máquinas remotas: OpenSpec, TDD, documentación, Bruno, Playwright y preflight.sh --full antes de un merge.");
pair("The AI Tools That Built It", "Las herramientas de IA que lo construyeron");
pair("Five AI tools, one codebase. Each played a different role in accelerating from idea to production.", "Cinco herramientas de IA, un solo código. Cada una aceleró una parte distinta del camino a producción.");
pair("Modern Layered Platform", "Plataforma moderna por capas");
pair("From a single Java Swing file to a multi-layer containerized platform.", "De un archivo Java Swing a una plataforma en contenedores, con varias capas.");
pair("DevSecOps Platform", "Plataforma DevSecOps");

// Security
pair("Security · public snapshot", "Seguridad · foto pública");
pair("Protect the notarial record", "Proteger el registro notarial");
pair(
  "Notaire holds identity documents and deeds. The controls are in the repository: a signed session, a module grant on each API path, scanners that fail a release, and a weekly dynamic scan. This section explains them. It does not show a live scan.",
  "Notaire guarda documentos de identidad y escrituras. Los controles están en el repositorio: una sesión firmada, un permiso de módulo en cada ruta de la API, escáneres que frenan un release y un escaneo dinámico semanal. Esta sección los explica. No muestra un escaneo en vivo.",
);
pair("Trust boundaries", "Límites de confianza");
pair("Step from the browser to the database.", "Del navegador a la base de datos.");
pair("STRIDE", "STRIDE");
pair("Six properties, and the privacy notes beside them.", "Seis propiedades, y al lado las notas de privacidad.");
pair("Toolchain", "Cadena de herramientas");
pair("Local gate, version tag, or Monday schedule.", "Gate local, tag de versión, o el lunes.");
pair("Reporting", "Informes");
pair("Where a finding goes, and where it does not.", "A dónde va un hallazgo, y a dónde no.");
pair("Security · architecture", "Seguridad · arquitectura");
pair("Four trust boundaries", "Cuatro límites de confianza");
pair(
  "The browser is untrusted. The BFF proxies. The API authenticates, then checks the module grant. PostgreSQL holds the personal data and the deeds. Step through one request.",
  "El navegador no es de confianza. El BFF hace de proxy. La API autentica y después comprueba el permiso de módulo. PostgreSQL guarda los datos personales y las escrituras. Sigue un pedido paso a paso.",
);
pair("Notaire trust boundaries", "Límites de confianza de Notaire");
pair("A request moves from the browser through the BFF and the API filters into PostgreSQL.", "Un pedido va del navegador al BFF, a los filtros de la API y a PostgreSQL.");
pair("Same boundaries as the PlantUML trust-boundary diagram in the architecture docs. Demo labels only.", "Los mismos límites que el diagrama PlantUML de la documentación de arquitectura. Solo etiquetas de demo.");
pair("Security · threats", "Seguridad · amenazas");
pair("STRIDE, then privacy", "STRIDE, y después la privacidad");
pair(
  "Pick a property. Each card is a requirement id from the system model, with the status recorded on 5 November 2026. Status is a design label, not the result of a penetration test.",
  "Elige una propiedad. Cada tarjeta es un id de requisito del modelo del sistema, con el estado anotado el 5 de noviembre de 2026. El estado es una etiqueta de diseño, no el resultado de una prueba de penetración.",
);
pair("Privacy notes (LINDDUN)", "Notas de privacidad (LINDDUN)");
pair(
  "Personal data in a notarial file is identifiable on purpose. These rows say what the product does about that. They are not a legal opinion.",
  "Los datos personales de un expediente notarial son identificables a propósito. Estas filas dicen qué hace el producto con eso. No son una opinión legal.",
);
pair("Security · toolchain", "Seguridad · cadena de herramientas");
pair("What runs, and when", "Qué corre, y cuándo");
pair(
  "Filter the catalog. A version tag runs the release security job. Monday runs CodeQL and ZAP. A pull request does not start those workflows. The local preflight does.",
  "Filtra el catálogo. Un tag de versión corre el job de seguridad del release. El lunes corren CodeQL y ZAP. Un pull request no arranca esos workflows. El preflight local sí.",
);
pair("Filter tools by when they run", "Filtrar herramientas por cuándo corren");
pair("All", "Todas");
pair("Local", "Local");
pair("Version tag", "Tag de versión");
pair("Tag and weekly", "Tag y semanal");
pair("Weekly", "Semanal");
pair("Main", "Main");
pair("GitHub app", "App de GitHub");
pair("Push to main", "Push a main");
pair("Local only", "Solo local");
pair("Tool", "Herramienta");
pair("When", "Cuándo");
pair("Where", "Dónde");
pair("Security · reporting", "Seguridad · informes");
pair("Where a finding goes", "A dónde va un hallazgo");
pair("Four surfaces. Only the last one is public, and it is not a dashboard of live results.", "Cuatro superficies. Solo la última es pública, y no es un tablero de resultados en vivo.");
pair("On the agent machine", "En la máquina del agente");
pair("run_gates.py writes an HTML report under reports/security/latest. The local observability stack can serve it on port 8890. That file is not published.", "run_gates.py escribe un informe HTML en reports/security/latest. El stack local de observabilidad puede servirlo en el puerto 8890. Ese archivo no se publica.");
pair("On a release run", "En un release");
pair("The security job appends a case table to the Actions summary and uploads the HTML plus Trivy JSON as artifacts. People with access to the workflow can read them.", "El job de seguridad agrega una tabla al resumen de Actions y sube el HTML y el JSON de Trivy como artefactos. Quien tenga acceso al workflow puede leerlos.");
pair("CodeQL", "CodeQL");
pair("SARIF goes to the GitHub Security tab. The job fails if analysis fails. A finding by itself does not fail the job.", "El SARIF va a la pestaña Security de GitHub. El job falla si el análisis falla. Un hallazgo, solo, no hace fallar el job.");
pair("This site", "Este sitio");
pair("The posture and the cadence are checked-in explanations. They use no production credential and no scan output. A later change can feed a redacted summary. Until then, treat every status here as a snapshot.", "La postura y el calendario son explicaciones en el repositorio. No usan credenciales de producción ni salida de un escaneo. Un cambio posterior puede alimentar un resumen redactado. Hasta entonces, cada estado de aquí es una foto.");

// STRIDE questions and summaries are registered from security-content by the test via pair() calls below.
pair("Can someone pretend to be a staff member or an API client?", "¿Alguien puede hacerse pasar por personal de la escribanía o por un cliente de la API?");
pair("The browser session is an HttpOnly cookie. API clients still receive a bearer token in the login JSON.", "La sesión del navegador es una cookie HttpOnly. Los clientes de la API todavía reciben un token bearer en el JSON del login.");
pair("Passwords are BCrypt and login lockout is shared in the database. There is no second factor.", "Las contraseñas son BCrypt y el bloqueo de login vive en la base. No hay un segundo factor.");
pair("Can someone change a record, a query, or a published image?", "¿Alguien puede cambiar un registro, una consulta o una imagen publicada?");
pair("Persistence uses parameterized JPA. The SQL injection note names the query paths.", "La persistencia usa JPA con parámetros. La nota de inyección SQL nombra las rutas de consulta.");
pair("Trivy fails the release job on fixed critical and high findings. That job does not run on a pull request.", "Trivy hace fallar el job de release ante hallazgos críticos y altos fijados. Ese job no corre en un pull request.");
pair("Can someone deny a write, or a read of personal data?", "¿Alguien puede negar una escritura, o una lectura de datos personales?");
pair("Writes are audited from the authenticated identity, not from a client header.", "Las escrituras se auditan desde la identidad autenticada, no desde un header del cliente.");
pair("Reads of Person are audited. Other entities opt in.", "Las lecturas de Person se auditan. Las demás entidades se suman si se les pide.");
pair("Can personal data or a secret leave the boundary?", "¿Pueden salir del límite datos personales o un secreto?");
pair("A high-confidence secret scan fails the local gate and the release job.", "Un escaneo de secretos de alta confianza hace fallar la gate local y el job de release.");
pair("Request logs keep method, path, status, and duration. They omit query, headers, and body.", "Los logs de pedidos guardan método, ruta, estado y duración. Omiten la query, los headers y el cuerpo.");
pair("Can an anonymous caller exhaust the API?", "¿Un llamador anónimo puede agotar la API?");
pair("Only login attempts are throttled. A general quota is still an open requirement.", "Solo se limitan los intentos de login. Una cuota general sigue siendo un requisito abierto.");
pair("Can a signed-in user reach a module or a role they were not granted?", "¿Una persona autenticada puede llegar a un módulo o a un rol que no se le dio?");
pair("A notary (Escribano) receives ROLE_NOTARY, not administrator.", "Un escribano recibe ROLE_NOTARY, no administrador.");
pair("Module grants are enforced on the API. Inside one office, staff share the files. A second notary is a second installation.", "Los permisos de módulo se exigen en la API. Dentro de una escribanía, el personal comparte los expedientes. Un segundo escribano es otra instalación.");
pair("Linkability is not the issue — identifiability is", "El problema no es enlazar registros: es identificar a la persona");
pair("An authenticated caller still receives DNI, email, and name. Field minimization is not built.", "Quien está autenticado sigue recibiendo DNI, email y nombre. Minimizar campos no está construido.");
pair("Linkability", "Enlazabilidad");
pair("The same person key joins management, deed, and budget. The notarial record needs that.", "La misma clave de persona une expediente, escritura y presupuesto. El registro notarial lo necesita.");
pair("Disclosure", "Divulgación");
pair("Logs skip the body. A path can still contain a record id.", "Los logs omiten el cuerpo. Una ruta todavía puede llevar el id de un registro.");
pair("Unawareness", "Falta de aviso");
pair("There is no in-product privacy notice.", "No hay un aviso de privacidad dentro del producto.");
pair("Non-compliance", "Incumplimiento");
pair("Retention of notarial records is indefinite by the architecture note. A statute reading belongs to counsel.", "La nota de arquitectura deja la retención de los registros notariales sin plazo. Leer el estatuto corresponde a un abogado.");
pair("Detectability", "Detectabilidad");
pair("Login failures are a metric. Reads of personal data are an audit row, not a dashboard series.", "Los fallos de login son una métrica. Las lecturas de datos personales son una fila de auditoría, no una serie del tablero.");
pair("Identity", "Identidad");
pair("BCrypt, shared login lockout, signed JWT, HttpOnly cookie. No second factor.", "BCrypt, bloqueo de login compartido, JWT firmado, cookie HttpOnly. Sin segundo factor.");
pair("Module grants", "Permisos de módulo");
pair("After the JWT check, the API denies a path whose module the role does not hold.", "Después de comprobar el JWT, la API niega una ruta cuyo módulo el rol no tiene.");
pair("Inside one office", "Dentro de una escribanía");
pair("Staff in the same installation can open the office files. That is the tenancy decision, not a missing check.", "El personal de la misma instalación puede abrir los expedientes. Es la decisión de tenencia, no un control que falte.");
pair("Dependencies and images", "Dependencias e imágenes");
pair("Trivy fails the release job. GitHub does not run that job on the pull request. The local gate does.", "Trivy hace fallar el job de release. GitHub no corre ese job en el pull request. La gate local sí.");
pair("Dynamic scan", "Escaneo dinámico");
pair("A weekly ZAP baseline fails on alerts outside the allowlist. The version tag does not run it yet.", "Una línea base semanal de ZAP falla si hay alertas fuera de la lista permitida. El tag de versión todavía no la corre.");
pair("API quota", "Cuota de la API");
pair("Expensive endpoints have no general limit. Login attempts do.", "Los endpoints caros no tienen un límite general. Los intentos de login sí.");
pair("Control families", "Familias de controles");
pair("Snapshot", "Foto");
pair("Public cadence for the portfolio. This is not a live scan and it contains no findings.", "Calendario público del portafolio. No es un escaneo en vivo y no contiene hallazgos.");

// Factory page headers (string leads)
pair("AI Factory · Lessons learned", "AI Factory · Lecciones");
pair("Every incident became a rule", "Cada incidente se volvió una regla");
pair(
  "The factory was not designed in one go. Each rule below exists because something went wrong while agents were building Notaire. The fix was written into a permanent document and, where possible, into a test or a script, so the next agent cannot repeat it.",
  "La fábrica no se diseñó de una vez. Cada regla de abajo existe porque algo salió mal mientras los agentes construían Notaire. El arreglo quedó en un documento permanente y, cuando se pudo, en un test o un script, para que el siguiente agente no lo repita.",
);
pair("What happened", "Qué pasó");
pair("Problem", "Problema");
pair("Move", "Movimiento");
pair("Result", "Resultado");
pair("Rule now", "Regla ahora");
pair("AI Factory · Playbook", "AI Factory · Playbook");
pair("Apply an AI SDLC to your own project", "Aplicar un AI SDLC a tu propio proyecto");
pair(
  "What carries over from Notaire to any codebase that wants AI agents to ship production-ready software. Each step links the Notaire file that implements it, so you can copy the shape, not just the idea.",
  "Lo que se puede llevar de Notaire a cualquier código que quiera que agentes de IA entreguen software listo para producción. Cada paso enlaza el archivo de Notaire que lo implementa, para copiar la forma y no solo la idea.",
);
pair(
  "The factory is only as good as the evidence it leaves. These are the artifacts Notaire's gates produce or check before a change counts as shipped.",
  "La fábrica vale lo que vale la evidencia que deja. Estos son los artefactos que las gates de Notaire producen o comprueban antes de contar un cambio como entregado.",
);
pair("Adapt, do not replace. Keep vendor engines and skills; extend them through their own extension points.", "Adaptar, no reemplazar. Conserva los motores y los skills del proveedor. Extiéndelos por sus propios puntos de extensión.");
pair("One source of truth. The governance process lives in one document; tools point at it and never copy it.", "Una sola fuente de verdad. El proceso de gobierno vive en un documento. Las herramientas lo apuntan y no lo copian.");
pair("Agent-agnostic. Deliver the rules through a channel every tool reads (the spec CLI), not a feature of one assistant.", "Agnóstico del agente. Entrega las reglas por un canal que lee cada herramienta (el CLI de specs), no por una función de un solo asistente.");
pair("Mechanical over aspirational. A rule without an exit code is a wish.", "Mecánico antes que aspiracional. Una regla sin código de salida es un deseo.");
pair("What “production-ready” means here", "Qué significa aquí «listo para producción»");
pair("Guiding principles for adapting", "Principios para adaptarlo");
pair("AI Factory · Lifecycle", "AI Factory · Ciclo de vida");
pair("From Issue to closed Issue, step by step", "Del Issue al Issue cerrado, paso a paso");
pair(
  "The Constitution defines one mandatory, sequential workflow of 24 steps for every change: feature, bug fix, refactor, docs, CI or schema. No step may be skipped. Five hard gates stop the work until their conditions hold, whether a human or an agent is doing it.",
  "La Constitución define un flujo obligatorio y en orden, de 24 pasos, para cada cambio: función, bug, refactor, docs, CI o esquema. Ningún paso se salta. Cinco gates duras frenan el trabajo hasta que se cumple su condición, lo haga una persona o un agente.",
);
pair("The five hard gates", "Las cinco gates duras");
pair("Understand and specify", "Entender y especificar");
pair("Tests first, then code", "Primero los tests, después el código");
pair("Document, commit, open the PR", "Documentar, commitear, abrir el pull request");
pair("Review, merge, deploy, verify", "Revisar, fusionar, desplegar, verificar");
pair("Issue", "Issue");
pair("Refine the requirement", "Afinar el requisito");
pair("One GitHub Issue per change, labeled and linked to a Use Case (CU-XX), RF-XX or RNF-XX, with acceptance criteria.", "Un Issue de GitHub por cambio, etiquetado y enlazado a un caso de uso (CU-XX), RF-XX o RNF-XX, con criterios de aceptación.");
pair("Confirm the Use Case is accurate; create or update it first if not.", "Confirmar que el caso de uso es exacto. Crearlo o actualizarlo antes, si no lo es.");
pair("Specification", "Especificación");
pair("An OpenSpec change on the notaire-sdlc schema describes only this change: proposal, traceability, delta specs, design, tasks.", "Un cambio OpenSpec con el esquema notaire-sdlc describe solo este cambio: proposal, trazabilidad, specs delta, design y tasks.");
pair("Impact analysis", "Análisis de impacto");
pair("Affected modules from workspace/modules.yaml, entities, endpoints, schema, tests and docs.", "Módulos afectados según workspace/modules.yaml, entidades, endpoints, esquema, tests y docs.");
pair("Architecture review", "Revisión de arquitectura");
pair("Follow the existing architecture (hexagonal, ADR-030); write an ADR when the change is architectural.", "Seguir la arquitectura existente (hexagonal, ADR-030). Escribir un ADR cuando el cambio es de arquitectura.");
pair("Branch", "Rama");
pair("From an updated main: <type>/<issue>_<description>. Push it at once (continuous publication).", "Desde un main actualizado: <type>/<issue>_<description>. Push enseguida (publicación continua).");
pair("Acceptance criteria", "Criterios de aceptación");
pair("Given-When-Then scenarios in the delta spec are the acceptance criteria.", "Los escenarios Given-When-Then de la spec delta son los criterios de aceptación.");
pair("Design test cases", "Diseñar los casos de prueba");
pair("Happy path, edge cases and error paths at unit, integration and E2E level.", "Camino feliz, bordes y errores, en unit, integración y E2E.");
pair("Write failing tests", "Escribir tests que fallan");
pair("Unit and integration tests first, and watch them fail. A test you never saw fail proves nothing (P1).", "Primero los tests de unit e integración, y verlos fallar. Un test que nunca viste fallar no prueba nada (P1).");
pair("Implement", "Implementar");
pair("The minimum code that makes the tests pass, following the conventions.", "El mínimo código que hace pasar los tests, siguiendo las convenciones.");
pair("Update existing tests", "Actualizar los tests que ya existen");
pair("Never weaken an assertion to force green.", "Nunca debilitar una aserción para forzar el verde.");
pair("Run tests and full regression", "Correr los tests y la regresión completa");
pair("Unit, integration, coverage ratchet, Bruno API tests. No @Disabled without approval.", "Unit, integración, ratchet de cobertura y tests Bruno de la API. Sin @Disabled sin aprobación.");
pair("Playwright E2E", "Playwright E2E");
pair("Every UI change: golden path and edge cases at mobile, tablet and desktop widths.", "Cada cambio de UI: el camino principal y los bordes, en anchos de móvil, tablet y escritorio.");
pair("Permanent documentation", "Documentación permanente");
pair("Update the docs the change affects, in the same diff. Never duplicate; point at the permanent document.", "Actualizar los docs que el cambio toca, en el mismo diff. No duplicar. Apuntar al documento permanente.");
pair("Atomic commits", "Commits atómicos");
pair("Conventional Commits; Refs #N on intermediate commits, Closes #N on the closing one.", "Conventional Commits. Refs #N en los commits intermedios, Closes #N en el que cierra.");
pair("Full local pipeline", "Pipeline local completo");
pair("One dashboarded pre-PR gate: plan check, security cases, preflight --full, markdown lint.", "Una gate previa al pull request, con tablero: chequeo del plan, casos de seguridad, preflight --full y lint de markdown.");
pair("Pull request and local full preflight", "Pull request y preflight local completo");
pair("PR titled [#N] type(scope): description, with the PR template. GitHub does not re-run the product suite on a pull request (#2001): the PR is ready when preflight --full has passed locally and the required process checks are green.", "Pull request titulado [#N] type(scope): description, con la plantilla. GitHub no vuelve a correr la suite del producto en un pull request (#2001): el pull request está listo cuando preflight --full pasó en local y los chequeos de proceso requeridos están en verde.");
pair("Code review", "Code review");
pair("Code owner, or the Foreman's Gate 4 review with no open finding, after preflight --full passed on the head. check-heavy-ci.sh fails only if a heavy workflow started on the PR and is pending or red.", "El dueño del código, o la review de Gate 4 del Foreman sin un hallazgo abierto, después de que preflight --full pasó en la punta. check-heavy-ci.sh falla solo si un workflow pesado arrancó en el pull request y está pendiente o en rojo.");
pair("Merge", "Merge");
pair("Via the PR only, by the Owner or by the Foreman under its merge rule; Workers never merge. The OpenSpec change is archived in that same PR (#1623).", "Solo por el pull request, el Owner o el Foreman bajo su regla de merge. Los Workers nunca fusionan. El cambio OpenSpec se archiva en ese mismo pull request (#1623).");
pair("Deploy", "Deploy");
pair("CI → CD builds, signs (cosign) and publishes images to GHCR; SBOMs on version tags.", "CI y después CD construyen, firman (cosign) y publican imágenes en GHCR. Los SBOM van en los tags de versión.");
pair("Smoke test", "Smoke test");
pair("Health endpoint and the key flow of the change on the target environment.", "El endpoint de salud y el flujo clave del cambio en el entorno de destino.");
pair("Close the issue", "Cerrar el Issue");
pair("Only after deploy and smoke. Board Status Done, WIP labels removed.", "Solo después del deploy y del smoke. Estado del tablero Done, y sin etiquetas WIP.");
pair("Definition of Done", "Definición de hecho");
pair("AI Factory · Agents & skills", "AI Factory · Agentes y skills");
pair("A crew, not a chatbot", "Un equipo, no un chatbot");
pair("AI Factory · Guardrails", "AI Factory · Guardrails");
pair("Nine layers between an agent and main", "Nueve capas entre un agente y main");
pair("AI Factory · Quality gates", "AI Factory · Quality gates");
pair("Gates are commands, not opinions", "Las gates son comandos, no opiniones");
pair("AI Factory · Security gates", "AI Factory · Security gates");
pair("The pull request is not the security scan", "El pull request no es el escaneo de seguridad");
pair("AI Factory · Governance", "AI Factory · Gobierno");
pair("One board, one state, many agents", "Un tablero, un estado, muchos agentes");
pair("AI Factory · OpenSpec", "AI Factory · OpenSpec");
pair("Spec-driven development, adapted", "Desarrollo dirigido por specs, adaptado");
pair("AI Factory · Anatomy", "AI Factory · Anatomía");
pair("The AI SDLC at a glance: a lock chamber", "El AI SDLC de un vistazo: una cámara de esclusas");
pair("AI Factory · Results", "AI Factory · Resultados");
pair("What the factory produced", "Lo que produjo la fábrica");
pair(
  "Claims are cheap; counts are not. Every number on this page is read when the site is built, from the repository itself or from a committed snapshot that records the command that made it.",
  "Afirmar es barato. Contar no. Cada número de esta página se lee al construir el sitio, del repositorio o de una foto commiteada que guarda el comando que la hizo.",
);
pair("Delivery", "Entrega");
pair("The factory itself", "La fábrica en sí");
pair("The process is code too. These are counted from the folders that hold it.", "El proceso también es código. Esto se cuenta desde las carpetas que lo guardan.");
pair("Quality held while speed grew", "La calidad se sostuvo mientras crecía la velocidad");
pair("Backend coverage (JaCoCo)", "Cobertura del backend (JaCoCo)");
pair("instructions", "instrucciones");
pair("Branch coverage was not measured for this build.", "La cobertura de ramas no se midió en esta construcción.");
pair("Reading these numbers honestly", "Leer estos números con honestidad");
pair("Each mechanism, when it arrived", "Cada mecanismo, y cuándo llegó");
pair("The AI SDLC framework — one policy, one profile, one kit", "El framework del AI SDLC: una política, un perfil, un kit");
pair("One policy, one profile, one kit", "Una política, un perfil, un kit");
pair(
  "The process that builds Notaire, with Notaire taken out. A stack-agnostic AI SDLC framework: a policy document that names no tool, a TOML profile that holds every project-specific command, and a zero-dependency kit (aisdlc) that runs gates by exit code, enforces ratchets and keeps the evidence.",
  "El proceso que construye Notaire, sin Notaire adentro. Un framework de AI SDLC agnóstico del stack: un documento de política que no nombra herramientas, un perfil TOML con cada comando del proyecto, y un kit sin dependencias (aisdlc) que corre gates por código de salida, exige ratchets y guarda la evidencia.",
);

// Docs index
pair("Technical documentation", "Documentación técnica");
pair("Notaire knowledge base", "Base de conocimiento de Notaire");
pair(
  "Business and engineering docs rendered for the public site. Full Markdown trees stay in docs/ on GitHub; this tab is the curated entry for business, modules, architecture, testing and DevSecOps.",
  "Documentos de negocio e ingeniería para el sitio público. Los árboles Markdown completos siguen en docs/ en GitHub. Esta sección es la entrada curada: negocio, módulos, arquitectura, pruebas y DevSecOps.",
);
pair("Read it as a story", "Léelo como una historia");
pair("Fast agents, slow trust.", "Agentes rápidos, confianza lenta.");
pair("How Notaire learned to ship AI-written code.", "Cómo Notaire aprendió a entregar código escrito por IA.");
pair("Chapter", "Capítulo");
pair(
  "New to Notaire? Follow these pages in order, from what the product does down to how each change is made, proved, protected and shipped.",
  "¿Primera vez en Notaire? Sigue estas páginas en orden: qué hace el producto, y después cómo se hace, se prueba, se protege y se entrega cada cambio.",
);

// Playbook bodies and lesson bodies are registered by importing the data in the test file
// via registerContent(), called from strings-content.ts so this file stays the headings
// and the content file stays next to the arrays.
