/** Spanish for the LikeC4 architecture model page and views (#2483). Keys come from lib/architecture-model.json. */
import { pair } from "./es";

// Page
pair("Architecture model", "Modelo de arquitectura");
pair(
  "One LikeC4 model, written from the code, draws every architecture view on this site and in the repository docs. A gap found while writing it is a red finding with its GitHub Issue.",
  "Un solo modelo LikeC4, escrito a partir del código, dibuja todas las vistas de arquitectura de este sitio y de la documentación del repositorio. Cada brecha encontrada al escribirlo es un hallazgo en rojo con su Issue de GitHub.",
);
pair("LikeC4 model of the system, every view and the findings.", "Modelo LikeC4 del sistema, todas sus vistas y los hallazgos.");
pair("Open the interactive model", "Abrir el modelo interactivo");
pair("Open this view in the interactive model", "Abrir esta vista en el modelo interactivo");
pair("Findings", "Hallazgos");
pair(
  "What the modelling pass found that the code or the deployment still gets wrong.",
  "Lo que el modelado encontró que el código o el despliegue todavía hacen mal.",
);
pair("Finding", "Hallazgo");
pair("Detail", "Detalle");
pair("How the views are made", "Cómo se generan las vistas");
pair("Context and containers", "Contexto y contenedores");
pair("Inside the hexagons", "Dentro de los hexágonos");
pair("Testing and delivery", "Pruebas y entrega");
pair("Infrastructure", "Infraestructura");
pair("Views from the architecture model", "Vistas del modelo de arquitectura");

// View titles and descriptions
pair("System context", "Contexto del sistema");
pair("Who uses Notaire and which systems it depends on.", "Quién usa Notaire y de qué sistemas depende.");
pair("Containers", "Contenedores");
pair(
  "Runtime building blocks of Notaire and the observability stack around them.",
  "Los bloques de ejecución de Notaire y el stack de observabilidad que los rodea.",
);
pair("Backend hexagon", "Hexágono del backend");
pair(
  "Ports and Adapters in backend-api (ADR-030). Amber and red boxes are measured debt with their Issues.",
  "Puertos y adaptadores en backend-api (ADR-030). Las cajas ámbar y rojas son deuda medida con sus Issues.",
);
pair("Frontend hexagon", "Hexágono del frontend");
pair(
  "Next.js UI, core, HTTP adapters and the server-side edge (ADR-030).",
  "UI de Next.js, núcleo, adaptadores HTTP y el borde del lado del servidor (ADR-030).",
);
pair("Observability", "Observabilidad");
pair(
  "Metrics, logs, traces and alerts around the application (infra/observability).",
  "Métricas, logs, trazas y alertas alrededor de la aplicación (infra/observability).",
);
pair("Security controls", "Controles de seguridad");
pair(
  "Controls along the request path and on stored data (CU78). Red dashed arrows are open findings.",
  "Controles a lo largo del camino de la petición y sobre los datos guardados (CU78). Las flechas rojas son hallazgos abiertos.",
);
pair("Security scanning", "Escaneo de seguridad");
pair(
  "Local security gate (security/run_gates.py) and the release workflows that repeat it.",
  "La gate de seguridad local (security/run_gates.py) y los workflows de release que la repiten.",
);
pair("Test pyramid", "Pirámide de pruebas");
pair("Which suite verifies which part of the system (CU76).", "Qué suite verifica qué parte del sistema (CU76).");
pair("Architecture findings", "Hallazgos de arquitectura");
pair(
  "Debt and gaps found while building this model (#2483), each with its Issue.",
  "Deuda y brechas encontradas al construir este modelo (#2483), cada una con su Issue.",
);
pair("Request through both hexagons", "Una petición a través de los dos hexágonos");
pair(
  "A clerk saves a payment: UI, core, BFF, security chain, use case, persistence.",
  "Un empleado guarda un pago: UI, núcleo, BFF, cadena de seguridad, caso de uso, persistencia.",
);
pair("Login with MFA", "Inicio de sesión con MFA");
pair(
  "POST /api/v1/users/login, then /login/mfa when the role requires TOTP (ADR-032, CU84).",
  "POST /api/v1/users/login y luego /login/mfa cuando el rol exige TOTP (ADR-032, CU84).",
);
pair("Deployment: development", "Despliegue: desarrollo");
pair("docker-compose.yml plus the observability compose.", "docker-compose.yml más el compose de observabilidad.");
pair("Deployment: production", "Despliegue: producción");
pair(
  "docker-compose.prod.yml: the proxy is the only published port.",
  "docker-compose.prod.yml: el proxy es el único puerto publicado.",
);
pair("Deployment: staging (Kubernetes)", "Despliegue: staging (Kubernetes)");
pair("Kustomize base and staging overlay.", "Base de Kustomize y overlay de staging.");
pair("CI/CD and quality gates", "CI/CD y gates de calidad");
pair(
  "From Gate 1 to the release: the local preflight is the PR gate, the workflows run on release tags.",
  "De la Gate 1 al release: el preflight local es la gate del PR, los workflows corren en los tags de release.",
);

// Findings
pair("Ports speaking legacy web DTOs", "Puertos que hablan DTO web heredados");
pair(
  "40 files in 8 contexts import com.licensis.notaire.dto (testimony, procedure, management, history, concept, audit, registrationdraft, item). #2485; R2 does not guard it yet (#2484).",
  "40 archivos en 8 contextos importan com.licensis.notaire.dto (testimony, procedure, management, history, concept, audit, registrationdraft, item). #2485; R2 todavía no lo vigila (#2484).",
);
pair("Contexts without a domain model", "Contextos sin modelo de dominio");
pair(
  "audit, budget, item, report, catalog, documentcosttemplate: ports carry view records and the rules sit in adapters. The report inbound port (GenerateReportsUseCase) is implemented by adapter.out.report.ReportService, a use case inside an outbound adapter. #2486.",
  "audit, budget, item, report, catalog, documentcosttemplate: los puertos llevan records de vista y las reglas están en los adaptadores. El puerto de entrada de reportes (GenerateReportsUseCase) lo implementa adapter.out.report.ReportService, un caso de uso dentro de un adaptador de salida. #2486.",
);
pair("Legacy JPA model", "Modelo JPA heredado");
pair(
  "36 @Entity classes (47 legacy model classes) and 36 Spring Data repositories still outside adapter.out.persistence. #2486.",
  "36 clases @Entity (47 clases de modelo heredado) y 36 repositorios de Spring Data siguen fuera de adapter.out.persistence. #2486.",
);
pair("Legacy-package guards missing", "Faltan las guardas de los paquetes heredados");
pair(
  "R6 only watches the already-empty jpa and service packages, and R2 does not forbid the legacy dto package in the application layer. #2484.",
  "R6 solo vigila los paquetes jpa y service, que ya están vacíos, y R2 no prohíbe el paquete dto heredado en la capa de aplicación. #2484.",
);
pair("protect-main ruleset", "Ruleset protect-main");
pair(
  "Desired: PR-only merges, no deletion, no force-push, no required checks (the local preflight is the PR gate, #2001). Live: still requires the five retired product-CI checks. #2490.",
  "Deseado: merges solo por PR, sin borrado, sin force-push, sin checks requeridos (el preflight local es la gate del PR, #2001). En vivo: todavía exige los cinco checks retirados del CI de producto. #2490.",
);
pair(
  "PDF reports → Inbound ports: Implements GenerateReportsUseCase (finding)",
  "Reportes PDF → Puertos de entrada: implementa GenerateReportsUseCase (hallazgo)",
);
pair(
  "adapter.out.report.ReportService is the report use case: an inbound port implemented inside an outbound adapter. #2486.",
  "adapter.out.report.ReportService es el caso de uso de reportes: un puerto de entrada implementado dentro de un adaptador de salida. #2486.",
);
pair(
  "Reverse proxy → Actuator chain: Forwards every /actuator/* path to the internet-facing ingress",
  "Proxy inverso → Cadena de actuator: reenvía cada ruta /actuator/* al ingress expuesto a internet",
);
pair(
  "Only /actuator/health is needed outside the private network. #2487.",
  "Fuera de la red privada solo hace falta /actuator/health. #2487.",
);
pair(
  "Reverse proxy → MFA login gate: Production compose and Kustomize omit the MFA key, policy and COOKIE_SECURE",
  "Proxy inverso → Gate de login MFA: el compose de producción y Kustomize omiten la clave MFA, la política y COOKIE_SECURE",
);
pair(
  "MFA enrolment fails and the JWT cookie lacks Secure in production. #2488.",
  "En producción el alta de MFA falla y la cookie JWT no lleva Secure. #2488.",
);
pair("postgres (ClusterIP :5432)", "postgres (ClusterIP :5432)");
pair("Data on emptyDir: lost on every pod restart. #2489.", "Datos en emptyDir: se pierden en cada reinicio del pod. #2489.");
pair("Every view of the architecture model, and its findings", "Todas las vistas del modelo de arquitectura y sus hallazgos");

// How the views are made
pair(
  "The model lives in docs/200-architecture/210-architecture-model (ADR-033), one file per concern.",
  "El modelo vive en docs/200-architecture/210-architecture-model (ADR-033), un archivo por tema.",
);
pair(
  "npm run generate turns each computed view into Mermaid for the Markdown docs and for this page. npm run check and a docs test fail when a copy drifts.",
  "npm run generate convierte cada vista calculada en Mermaid para los docs en Markdown y para esta página. npm run check y un test de docs fallan cuando una copia se desvía.",
);
pair(
  "The Pages build publishes the interactive LikeC4 site next to this page.",
  "El build de Pages publica el sitio interactivo de LikeC4 junto a esta página.",
);

// Diagram labels long enough to read as English prose (MermaidDiagram swaps them in Spanish)
pair("Maps to the entity", "Mapea a la entidad");
pair("Forwards with the httpOnly cookie", "Reenvía con la cookie httpOnly");
pair("Forwards every /actuator/* path to the internet-facing ingress", "Reenvía cada ruta /actuator/* al ingress expuesto a internet");
pair("Forwards the request and the httpOnly cookie", "Reenvía la petición y la cookie httpOnly");
pair("POST /api/v1/users/login/mfa with the TOTP code", "POST /api/v1/users/login/mfa con el código TOTP");
pair(
  "Production compose and Kustomize omit the MFA key, policy and COOKIE_SECURE",
  "El compose de producción y Kustomize omiten la clave MFA, la política y COOKIE_SECURE",
);
pair("Proxies /api/v1 with the httpOnly JWT cookie", "Hace de proxy de /api/v1 con la cookie JWT httpOnly");
pair("Reads and writes, Flyway migrates on start", "Lee y escribe, Flyway migra al arrancar");
pair("Runs the suites the branch touches", "Corre las suites que toca la rama");
pair("Still uses entities and repositories of", "Todavía usa entidades y repositorios de");
