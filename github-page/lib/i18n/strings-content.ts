import {
  CASE_CHAPTERS,
  CASE_LESSONS,
  FRAMEWORK_COMMANDS,
  FRAMEWORK_LEVELS,
  FRAMEWORK_PRINCIPLES,
  FRAMEWORK_STAGES,
  LESSONS,
  PLAYBOOK,
  PRODUCTION_READY,
} from "../ai-sdlc-content";
import { pair } from "./es";

const lesson: Record<string, [string, string, string]> = {
  "Light green is not mergeable": [
    "Verde claro no se puede fusionar",
    "Una suscripción de CI informó que los 18 chequeos habían pasado mientras la suite de integración del backend y Playwright seguían pendientes.",
    "check-heavy-ci.sh falla si una corrida pesada está pendiente o en rojo. Una corrida ausente cuenta como éxito: el CI del producto no arranca en el pull request. La prueba local es preflight.sh --full.",
  ],
  "Product CI does not run on the pull request": [
    "El CI del producto no corre en el pull request",
    "Cada pull request y cada push a main corrían la suite completa y publicaban imágenes, y la cuota de Actions se llenaba.",
    "preflight.sh --full es la prueba del pull request. Los workflows del producto corren en un tag de versión o por despacho manual. Pages y Release Please siguen corriendo en main.",
  ],
  "`Issue: #N` does not close anything": [
    "`Issue: #N` no cierra nada",
    "Los pull requests de la flota se fusionaban y sus Issues seguían abiertos, porque los commits decían Issue: #N.",
    "Los commits y el cuerpo del pull request que cierran usan Closes #N. Los cortes intermedios usan Refs #N, y un guard lo comprueba.",
  ],
  "Bot commits with [skip ci] stall the merge": [
    "Los commits de un bot con [skip ci] traban el merge",
    "PR Validation commiteaba informes wiki en la punta del pull request con [skip ci]. El SHA tenía una suite de chequeos vacía y merge-when-green esperaba para siempre.",
    "Los workflows de informes nunca commitean sobre la punta de un pull request. El harness juzga el CI en el último commit sin [skip ci].",
  ],
  "A stale tip is not a product bug": [
    "Una punta vieja no es un bug del producto",
    "Fallos de integración y de Playwright (Budget/person, undefined, undefined) en ramas detrás de main ya estaban arreglados en main.",
    "Primero se fusiona origin/main y se vuelve a correr preflight.sh --full, antes de tocar el producto. Nunca se hace rebase de una rama ya publicada.",
  ],
  "An invented Issue number passed Gate 1": [
    "Un número de Issue inventado pasó la Gate 1",
    "La Gate 1 solo comprobaba el formato #[0-9]+, así que una propuesta podía citar #999999.",
    "validate-sdlc-plan.sh resuelve el Issue en vivo con gh. Los hallazgos de exploración pasan por openspec-triage y se vuelven Issues reales antes.",
  ],
  "Closed issues left active changes that blocked every PR": [
    "Issues cerrados dejaron cambios activos que bloquearon cada pull request",
    "Después de cerrar un Issue, su cambio OpenSpec sin archivar hacía fallar la validación del plan en cada pull request abierto, y Main Health quedaba en rojo.",
    "Se archiva el cambio en el pull request que lo implementa. Los cambios multi_pr los archiva su último pull request. Main Health falla si queda un resto.",
  ],
  "Models report success they have not earned": [
    "Los modelos informan un éxito que no ganaron",
    "Workers locales escribieron «1.940 tests pasan» y «PR creado» en tasks.md, copiaron SHAs de main y afirmaron cambios que sus commits no tenían.",
    "El harness es dueño del libro y de los push. Las gates corren comandos reales. La Gate 4 compara cada línea del mensaje de commit con git show --stat.",
  ],
  "Too much always-loaded context": [
    "Demasiado contexto siempre cargado",
    "Importar reglas enteras y skills con alwaysApply llenaba la ventana de contexto de cada sesión.",
    "Lo que se carga siempre se queda en 8.000 tokens o menos: un AGENTS.md breve, una tarjeta de la Constitución de 3 KB, y reglas bajo demanda.",
  ],
  "Heavy suites on docs-only PRs": [
    "Suites pesadas en pull requests que solo tocan docs",
    "Los pull requests de docs y de OpenSpec encolaban corridas completas de Java y Playwright y dejaban sin runners compartidos.",
    "CI por rutas: los paths de docs saltan los jobs hoja, y los agregadores mantienen en verde sus nombres requeridos. main siempre corre todo.",
  ],
  "Parallel heavy PRs are slower": [
    "Varios pull requests pesados a la vez son más lentos",
    "Varios pull requests pesados a la vez encolaban Playwright uno detrás de otro. Cada pull request esperaba más y costaba más.",
    "Se serializan los pull requests pesados en Playwright. Un corte del tamaño de un dominio por pull request. Los pull requests de Dependabot se dejan en borrador si dejan sin turno a la cola.",
  ],
  "Board, labels and state drift apart": [
    "El tablero, las etiquetas y el estado se separan",
    "Issues cerrados seguían In Progress en el tablero. Los abiertos llevaban etiquetas WIP viejas.",
    "Un solo estado coherente: CLOSED implica Done y sin etiquetas WIP. OPEN con WIP implica In Progress y status:in-progress. Lo comprueba un script.",
  ],
  "Work that lives only on one disk": [
    "Trabajo que vive en un solo disco",
    "Los agentes se detenían a mitad de tarea con commits que nunca se publicaban, y nadie podía retomar.",
    "Publicación continua: se hace push de la rama enseguida y después de cada grupo de commits, con un comentario en el Issue sobre la etapa y lo que falta.",
  ],
  "CodeQL default setup beside the advanced workflow": [
    "El setup por defecto de CodeQL junto al workflow avanzado",
    "Con los dos activos, la subida de SARIF se rechazaba y Analyze fallaba.",
    "Avanzado o por defecto, no los dos: se queda codeql.yml y se apaga el setup por defecto con security/enable-gh-secure.sh --apply.",
  ],
  "Toolchain majors from a bot": [
    "Majors de la cadena de herramientas propuestos por un bot",
    "Dependabot propuso TypeScript 7. openapi-typescript necesita ^5 y TS7 no tiene la API del compilador, así que el pull request no podía quedar en verde.",
    "Un major que rompe el build es un cambio planificado, no un pull request de un bot: una regla de ignore con la condición para quitarla.",
  ],
  "Anonymous image pulls hit rate limits": [
    "Bajar imágenes sin autenticar choca con el límite",
    "Docker Hub respondió 429 en runners compartidos y los contenedores de servicio no arrancaban.",
    "Las imágenes de CI se bajan por mirror.gcr.io y la instalación de navegadores se reintenta.",
  ],
  "Evidence uploads must not fail a passing scan": [
    "Subir evidencia no debe hacer fallar un escaneo que pasó",
    "La corrida semanal de k6 pasó, pero el job quedó en rojo porque se agotó la cuota de artefactos. ZAP falló por una alerta informativa sobre respuestas 404 que no se pueden guardar.",
    "Las subidas de informes son continue-on-error. La lista permitida de ZAP es un conjunto revisado, con razones, fijado por un test.",
  ],
  "A deploy cannot fix a setting": [
    "Un deploy no arregla un ajuste",
    "Los deploys de Pages fallaban con 404 porque GitHub Pages estaba apagado en los ajustes del repositorio. Ningún cambio de workflow podía arreglarlo.",
    "Los pasos que solo puede hacer el Owner (ajustes, cuotas, tokens) se escriben como pasos exactos para el Owner. Un agente no los rodea.",
  ],
  "Bots without write access still need issue hygiene": [
    "Los bots sin permiso de escritura igual necesitan higiene de Issues",
    "Los tokens de los agentes podían abrir pull requests y recibían 403 al etiquetar o comentar un Issue, así que el estado se desviaba en silencio.",
    "El estado vive en el cuerpo del pull request con Closes/Refs como canal de sincronización, y se le deja al Owner una plantilla lista para pegar. No se inventa otro tablero.",
  ],
};

for (const item of LESSONS) {
  const row = lesson[item.title];
  if (!row) continue;
  pair(item.title, row[0]);
  pair(item.happened, row[1]);
  pair(item.rule, row[2]);
}

const playbook: Record<string, [string, string]> = {
  "Write one constitution, for humans and every agent": [
    "Escribir una constitución, para personas y para cada agente",
    "Principios, definición de hecho, convenciones, un flujo en orden, gates duras, roles, y cómo se concede una excepción. Que sea la autoridad más alta, y que haya una tarjeta breve siempre cargada.",
  ],
  "Fork a spec-driven schema, don't invent one": [
    "Bifurcar un esquema dirigido por specs, no inventar uno",
    "Extiende el esquema spec-driven de OpenSpec con tus secciones obligatorias y un artefacto de trazabilidad, e inyecta la constitución por config.yaml para que cada herramienta la reciba por el mismo canal.",
  ],
  "Turn every rule into an exit code": [
    "Volver cada regla un código de salida",
    "Cada gate que un agente debe pasar es un script que falla: plan completo, Issue en vivo, evidencia de TDD, formato de commit, trazabilidad de docs. Los mismos scripts corren en local (pre-push) y en CI.",
  ],
  "Split the work into phases a model can hold": [
    "Partir el trabajo en fases que un modelo pueda sostener",
    "Una fase por corrida, con contexto nuevo, un brief corto y una gate determinista después de cada fase. Si la gate falla, su propia salida vuelve a un reintento, hasta un límite, y después entra una persona.",
  ],
  "Specialize the crew and match cost to the task": [
    "Especializar el equipo y ajustar el costo a la tarea",
    "Un Foreman orquesta. Los Workers especialistas reciben el skill más estrecho y un alcance explícito. Modelos baratos para trabajo mecánico, un nivel medio para tests y arreglos de rutina, el nivel alto para arquitectura y seguridad.",
  ],
  "Make the platform enforce the last mile": [
    "Que la plataforma exija el último tramo",
    "Reglas de rama que exigen un pull request y chequeos con nombre, sin force-push, CODEOWNERS, y un ruleset de protección guardado como código y afirmado por un script.",
  ],
  "Keep humans on decisions, not keystrokes": [
    "Dejar a las personas las decisiones, no las teclas",
    "El Owner aprueba especificaciones, concede excepciones y fusiona los cambios de política. Los agentes anotan los pasos que solo puede hacer el Owner, en vez de rodearlos.",
  ],
  "Learn in writing": [
    "Aprender por escrito",
    "Cada incidente se vuelve una regla en un documento permanente y, cuando se puede, un test, para que el siguiente agente no lo repita.",
  ],
};

for (const step of PLAYBOOK) {
  const row = playbook[step.title];
  if (!row) continue;
  pair(step.title, row[0]);
  pair(step.body, row[1]);
}

const chapters: Record<string, [string, string, string, string]> = {
  legacy: [
    "Una app de escritorio universitaria que nadie podía cambiar con seguridad",
    "Notaire empezó en 2014 como un monolito Java Swing con acceso directo a la base: 24 commits, sin tests, sin pipeline. Reconstruirlo en Spring Boot, Next.js y PostgreSQL era un trabajo de años para una sola persona.",
    "Diciembre de 2025: llega el primer archivo de entrada de un agente, y los asistentes de IA empiezan a escribir la mayor parte del código nuevo.",
    "La apuesta: los agentes traen la velocidad. La pregunta abierta: quién prueba que el trabajo está bien.",
  ],
  speed: [
    "Velocidad sin prueba",
    "Los pull requests fusionados pasaron de 15 a 94 por mes con archivos de reglas y prompts, pero las reglas se contradecían y nada las exigía. En julio se abrieron 54 Issues de bug contra 69 pull requests fusionados.",
    "Un preflight local empezó a espejar el CI, para que un push que pasa en local pase en el servidor.",
    "Lección: una regla en prosa es un deseo. El contexto siempre cargado también pasó de 16.000 tokens de reglas importadas.",
  ],
  constitution: [
    "Una Constitución, y una Gate 1 que no se deja engañar",
    "Las especificaciones eran opcionales y la Gate 1 solo comprobaba que la propuesta citara algo con forma de número de Issue. Un agente podía citar #999999 y pasar.",
    "Una Constitución de ingeniería, agnóstica del agente, pasó a ser la única autoridad, conectada a OpenSpec para que cada agente la reciba, y la Gate 1 empezó a resolver el Issue en vivo en GitHub.",
    "Los Issues de bug por pull request fusionado bajaron de 0,78 a 0,33 al mes siguiente, mientras el proceso en sí ocupaba un tercio de todos los pull requests.",
  ],
  mechanize: [
    "Mecanizar cada regla que puede fallar",
    "Los agentes se saltaban pasos que la Constitución exigía, y los skills de seguridad se usaban como aspiración, no se aplicaban.",
    "Los hooks del harness bloquean los push a main, el CI comprueba el proceso (asuntos de commit, tests con código, spec o excepción) y un pipeline con tablero corre el catálogo de casos de seguridad antes de cada pull request.",
    "Cada regla pasó de una frase a un comando con código de salida, y por eso al camino de cada agente.",
  ],
  harness: [
    "Modelos que afirman un éxito que no ganaron",
    "Un modelo local de 9B a 20B escribió «1.940 tests pasan» y «PR creado» en su lista de tareas, copió SHAs de main e informó cambios que sus commits no tenían. Un Issue llevó 16 corridas de worker, 17 reintentos y 3 paradas del foreman.",
    "Un harness de foreman y worker le quitó la verdad al worker: una fase por corrida, comandos de test congelados, guards de alcance y de ref, un libro del harness, push del harness, y notas de review con líneas CHECK/EXPECTED.",
    "Las gates atraparon cada fallo observado, pero no cada pase barato: un test duplicado y una ruta absoluta todavía necesitaban un revisor. La review no se quita. Se puede hacer comprobable.",
  ],
  fleet: [
    "Una flota, y la disciplina de serializarla",
    "Los agentes en la nube trabajaron en paralelo y fusionaron 435 pull requests en un mes. Uno se fusionó con «los 18 chequeos en éxito» mientras integración y E2E todavía corrían. Los pull requests pesados se encolaban. Los Issues cerrados dejaban specs que bloqueaban a los demás.",
    "Una gate de merge de CI pesado, una regla escrita de merge del Foreman, un protocolo de reclamo atómico, archivar en el pull request, publicación continua, y un pull request pesado a la vez.",
    "El caudal subió seis veces respecto de julio, y los Issues de bug por pull request fusionado bajaron a 0,09. La parte de pull requests dedicada al proceso bajó del 35 % al 16 % cuando se automatizó el trabajo repetido.",
  ],
  ratchets: [
    "Ratchets: 512 violaciones de arquitectura a cero en cuatro días",
    "La meta de mayor prioridad era un sistema completo de puertos y adaptadores, pero el código tenía 512 violaciones conocidas. Una reescritura de golpe con agentes en paralelo no se habría podido revisar.",
    "Las reglas de ArchUnit y de ESLint llegaron el primer día con una línea base que solo puede achicarse: una violación nueva hace fallar el build, y una arreglada hay que sacarla del archivo. Un dominio por pull request.",
    "La línea base del hexágono pasó de 512 entradas a 0, y las supresiones de frontera del frontend de 66 a 0.",
  ],
  extract: [
    "Extraer un framework que cualquier repositorio pueda adoptar",
    "El proceso funcionaba, pero estaba soldado a Notaire: la Constitución nombraba Maven y Playwright, y tres runtimes de agentes guardaban tres configuraciones. Una regla como «nunca agregar una línea a una línea base a mano» seguía siendo solo prosa.",
    "Una política, un perfil TOML, un kit sin dependencias. Notaire fue el primer adoptante: el kit corre los scripts que ya tenía Notaire y suma los chequeos que faltaban.",
    "En su primera corrida el kit encontró cuatro defectos reales en el catálogo de skills, y ahora hace fallar cualquier línea de línea base agregada a mano, en preflight y en CI.",
  ],
};

for (const chapter of CASE_CHAPTERS) {
  const row = chapters[chapter.id];
  if (!row) continue;
  pair(chapter.title, row[0]);
  pair(chapter.problem, row[1]);
  pair(chapter.move, row[2]);
  pair(chapter.result, row[3]);
}

for (const item of CASE_LESSONS) {
  pair(item.title, {
    "A rule without an exit code is a wish": "Una regla sin código de salida es un deseo",
    "Agents are not witnesses": "Los agentes no son testigos",
    "Gates tell pass from fail, not right from cheap": "Las gates distinguen el pase del fallo, no lo correcto de lo barato",
    "Serialize what is scarce": "Serializar lo que es escaso",
    "Ratchet, do not big-bang": "Ratchet, no un cambio de golpe",
    "Size the process to the project": "Ajustar el proceso al tamaño del proyecto",
  }[item.title] ?? item.title);
  pair(item.body, {
    "Every rule that held in Notaire became a script or a test. The ones that stayed prose were the ones agents broke.": "Cada regla que se sostuvo en Notaire se volvió un script o un test. Las que siguieron en prosa son las que los agentes rompieron.",
    "A model's report of its own success is not evidence. The harness, not the worker, owns ledgers, pushes and results.": "El informe de un modelo sobre su propio éxito no es evidencia. El harness, no el worker, es dueño de los libros, los push y los resultados.",
    "Duplicate tests, absolute paths and invented scope pass gates. An independent reviewer with checkable notes catches them.": "Tests duplicados, rutas absolutas y un alcance inventado pasan las gates. Un revisor independiente, con notas comprobables, los encuentra.",
    "Parallel agents scale only when heavy CI, shared files and merges are serialized and work is published continuously.": "Los agentes en paralelo escalan solo cuando el CI pesado, los archivos compartidos y los merges se serializan y el trabajo se publica de continuo.",
    "Freeze today's violations, forbid new ones, and let every PR shrink the list. 512 became 0 in four days.": "Se congelan las violaciones de hoy, se prohíben las nuevas, y cada pull request achica la lista. 512 pasó a 0 en cuatro días.",
    "The process took a third of all PRs while it was being built. Adoption levels keep a small project from paying for a fleet.": "El proceso ocupó un tercio de todos los pull requests mientras se construía. Los niveles de adopción evitan que un proyecto chico pague una flota.",
  }[item.body] ?? item.body);
  pair(item.tradeoff, {
    "Writing and maintaining checks is real work; budget for it.": "Escribir y mantener los chequeos es trabajo real. Hay que presupuestarlo.",
    "Harness code grows; it needs its own tests and its own review.": "El código del harness crece. Necesita sus propios tests y su propia review.",
    "Human review time stays in the loop for anything high-risk.": "El tiempo de review humana se queda en el ciclo para lo que tiene riesgo alto.",
    "Some agents wait; that is cheaper than conflict storms.": "Algunos agentes esperan. Sale más barato que una tormenta de conflictos.",
    "Baselines need a guard against hand edits — now added.": "Las líneas base necesitan un guard contra ediciones a mano. Ya está.",
    "Lower levels accept more risk; raise the level on evidence.": "Los niveles bajos aceptan más riesgo. Se sube el nivel con evidencia.",
  }[item.tradeoff] ?? item.tradeoff);
}

for (const item of FRAMEWORK_PRINCIPLES) {
  pair(item.k, {
    "Policy first": "La política primero",
    "Spec before code, red before green": "Spec antes que código, rojo antes que verde",
    "Mechanical over aspirational": "Mecánico antes que aspiracional",
    "Machines check machines, humans check meaning": "Las máquinas comprueban a las máquinas, las personas el sentido",
    "Gate integrity": "Integridad de la gate",
    "Ratchet, do not big-bang": "Ratchet, no un cambio de golpe",
    "Serialize scarce, publish in-flight": "Serializar lo escaso, publicar lo que está en vuelo",
    "Least context, least privilege": "El menor contexto, el menor privilegio",
    "Traceable end to end": "Trazable de punta a punta",
    "Size the process, measure its tax": "Ajustar el proceso y medir su costo",
  }[item.k] ?? item.k);
  pair(item.v, {
    "One document prevails over every agent file, rule and prompt — and names no tool.": "Un documento prevalece sobre cada archivo de agente, regla y prompt, y no nombra ninguna herramienta.",
    "No code without a change record; no implementation before a failing test.": "No hay código sin un registro del cambio. No hay implementación antes de un test que falla.",
    "A rule that can be a command is a gate. Prose is for what cannot be checked.": "Una regla que puede ser un comando es una gate. La prosa es para lo que no se puede comprobar.",
    "Agents' claims are never evidence; reviewers check what gates cannot.": "Lo que afirma un agente nunca es evidencia. Quien revisa comprueba lo que las gates no pueden.",
    "Final commit, full suite, live references. Light green is not green.": "Commit final, suite completa, referencias en vivo. Verde claro no es verde.",
    "Debt shrinks monotonically; quality floors only rise.": "La deuda se achica de forma monótona. Los pisos de calidad solo suben.",
    "Heavy CI, shared files and merges go one at a time; work lives on the remote.": "El CI pesado, los archivos compartidos y los merges van de a uno. El trabajo vive en el remoto.",
    "A small always-loaded card; rules and skills on demand; narrow tokens.": "Una tarjeta breve siempre cargada. Reglas y skills bajo demanda. Tokens estrechos.",
    "Issue → change record → tests → commits → PR → release evidence.": "Issue, registro del cambio, tests, commits, pull request, evidencia del release.",
    "Pick an adoption level; track the share of work spent on the process itself.": "Se elige un nivel de adopción. Se mide qué parte del trabajo se gasta en el proceso.",
  }[item.v] ?? item.v);
}

for (const item of PRODUCTION_READY) pair(item.need, {
  "Test plan and pyramid with a generated inventory": "Plan de pruebas y pirámide, con un inventario generado",
  "E2E suites traced to Use Cases": "Suites E2E trazadas a casos de uso",
  "Security case catalog run locally and in CI": "Catálogo de casos de seguridad, corrido en local y en CI",
  "Contract stability for API clients": "Estabilidad del contrato para los clientes de la API",
  "Signed images and SBOMs": "Imágenes firmadas y SBOM",
  "One product version everywhere": "Una sola versión del producto en todos lados",
  "Restorable backups": "Copias de seguridad que se pueden restaurar",
  "Observability stack and quality dashboards": "Stack de observabilidad y tableros de calidad",
}[item.need] ?? item.need);

for (const row of FRAMEWORK_STAGES) {
  pair(row.done, {
    "Issue exists, states the problem, links its requirement": "El Issue existe, dice el problema y enlaza su requisito",
    "Change record complete; the issue resolves live": "El registro del cambio está completo y el Issue se resuelve en vivo",
    "New tests exist and fail (expect = \"fail\")": "Hay tests nuevos y fallan (expect = \"fail\")",
    "Suite, lint, ratchets, process checks": "Suite, lint, ratchets y chequeos del proceso",
    "Full local pipeline, docs updated": "Pipeline local completo y docs actualizados",
    "Full CI on the final commit, review resolved": "CI completo en el commit final y la review resuelta",
    "Deployed, smoke-tested, issue closed": "Desplegado, con smoke test, y el Issue cerrado",
  }[row.done] ?? row.done);
  pair(row.notaire, {
    "Issue template + Use Case": "Plantilla de Issue + caso de uso",
    "validate-sdlc-plan.sh": "validate-sdlc-plan.sh",
    "mvn / vitest single test, path-scoped": "un test de mvn o vitest, acotado por ruta",
    "preflight.sh (+ ratchet, skills lint)": "preflight.sh (más ratchet y lint de skills)",
    "run_pipeline.sh": "run_pipeline.sh",
    "check-heavy-ci.sh, archive hygiene": "check-heavy-ci.sh e higiene de archivo",
    "version coherence, health smoke": "coherencia de versión y smoke de salud",
  }[row.notaire] ?? row.notaire);
}

for (const row of FRAMEWORK_LEVELS) {
  pair(row.name, { Guided: "Guiado", Gated: "Con gates", Fleet: "Flota", Autonomous: "Autónomo" }[row.name] ?? row.name);
  pair(row.fits, {
    "One developer with an assistant": "Una persona con un asistente",
    "Small team; agents write most code": "Equipo chico; los agentes escriben la mayor parte del código",
    "Parallel agents, one human Owner": "Agentes en paralelo, un Owner humano",
    "Bounded autonomous runs": "Corridas autónomas acotadas",
  }[row.fits] ?? row.fits);
  pair(row.adds, {
    "Policy, card, profile, change records, plan and pre-push gates": "Política, tarjeta, perfil, registros de cambio, y gates de plan y de pre-push",
    "Red gates, ratchets, CI mirror, evidence report": "Gates rojas, ratchets, espejo de CI e informe de evidencia",
    "Roles with a merge rule, merge gate, serialization, claims, continuous publication": "Roles con regla de merge, gate de merge, serialización, reclamos y publicación continua",
    "Release gate, cost prices, harness-owned ledgers, measured autonomy": "Gate de release, precios de costo, libros del harness y autonomía medida",
  }[row.adds] ?? row.adds);
}

for (const row of FRAMEWORK_COMMANDS) {
  pair(row.does, {
    "Copy the kit into a repository and scaffold a profile. Absolute paths are refused.": "Copia el kit a un repositorio y arma un perfil. Las rutas absolutas se rechazan.",
    "Scaffold policy, agent card, AGENTS.md, profile, role agents": "Arma la política, la tarjeta del agente, AGENTS.md, el perfil y los agentes de rol",
    "Validate the profile; warn on placeholders and what the level lacks": "Valida el perfil. Avisa de placeholders y de lo que al nivel le falta",
    "Check factory knobs, trusted plugins and the migration inventory. config show requires --redacted.": "Comprueba los ajustes de la fábrica, los plugins de confianza y el inventario de migración. config show exige --redacted.",
    "List trusted plugins. plugin plan prints argv and does not run it.": "Lista los plugins de confianza. plugin plan imprime un argv y no lo ejecuta.",
    "Run the stage's gates by exit code; path-scoped; one ledger record": "Corre las gates de la etapa por código de salida, acotadas por ruta, y deja un registro en el libro",
    "Baselines may not grow against the base; floors must hold": "Las líneas base no pueden crecer respecto de la base. Los pisos deben cumplirse",
    "Compare changed paths with a Grant write set. An expiry fails closed. This is policy, not a sandbox.": "Compara las rutas cambiadas con el write set de un Grant. Si venció, falla cerrado. Es política, no un sandbox.",
    "Probe bubblewrap. sandbox is true only after a successful probe.": "Hace probe de bubblewrap. sandbox es true solo después de un probe exitoso.",
    "Print a dispatch plan from stage knobs. It does not run agents.": "Imprime un plan de despacho a partir de los knobs de la etapa. No ejecuta agentes.",
    "Lead time from a fixture or, read-only, from GitHub. It does not write to the forge.": "Lead time desde un fixture o, en solo lectura, desde GitHub. No escribe en el forge.",
    "Built-in change records with a live issue check": "Registros de cambio del kit, con un chequeo de Issue en vivo",
    "Catalog lint. budget, commits and tdd are separate checks.": "Lint del catálogo. budget, commits y tdd son chequeos aparte.",
    "Gate pass rates and durations. cost records tokens when something calls it.": "Tasas de pase y duraciones de las gates. cost anota tokens cuando algo lo llama.",
  }[row.does] ?? row.does);
}

