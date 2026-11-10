import type { Locale } from "./preferences";

const TRANSLATED = new Set(["/", "/ai-sdlc/"]);

export function pageIsTranslated(pathname: string): boolean {
  const path = pathname.endsWith("/") ? pathname : `${pathname}/`;
  return TRANSLATED.has(path);
}

export function chrome(locale: Locale) {
  const es = locale === "es";
  return {
    skip: es ? "Saltar al contenido" : "Skip to content",
    home: es ? "Inicio" : "Home",
    nav: {
      factory: "AI Factory",
      product: es ? "Producto" : "Product",
      security: es ? "Seguridad" : "Security",
      docs: es ? "Documentación técnica" : "Technical docs",
      releases: es ? "Versiones" : "Releases",
    },
    blurb: {
      factory: es ? "El AI SDLC que construye Notaire" : "The AI SDLC that builds Notaire",
      product: es ? "Qué hace Notaire, cómo funciona, su historia y sus números" : "What Notaire does, how it works, its story and its numbers",
      security: es ? "Límites de confianza, amenazas, herramientas y cuándo corren" : "Trust boundaries, threats, tools and when they run",
      docs: es ? "Arquitectura, módulos, pruebas, seguridad" : "Architecture, modules, testing, security",
      releases: es ? "Cada corte del producto y sus partes" : "Every product cut and its parts",
    },
    search: es ? "Buscar" : "Search",
    searchLabel: es ? "Buscar en el sitio" : "Search the site",
    searchPlaceholder: es ? "Buscar en la documentación y en la AI Factory…" : "Search the docs and the AI Factory…",
    searchStart: es ? "Empieza aquí" : "Start here",
    searchEmpty: (query: string) =>
      es
        ? `Ninguna página coincide con “${query}”. Prueba “architecture”, “guardrails” o “releases”.`
        : `No page matches “${query}”. Try “architecture”, “guardrails” or “releases”.`,
    results: es ? "Resultados" : "Results",
    menu: es ? "Menú" : "Menu",
    openMenu: es ? "Abrir menú" : "Open menu",
    closeMenu: es ? "Cerrar menú" : "Close menu",
    siteMenu: es ? "Menú del sitio" : "Site menu",
    primary: es ? "Principal" : "Primary",
    sections: es ? "Secciones" : "Sections",
    pages: es ? "páginas" : "pages",
    previous: es ? "Anterior" : "Previous",
    next: es ? "Siguiente" : "Next",
    pager: es ? "Anterior y siguiente" : "Previous and next",
    breadcrumb: es ? "Ruta de navegación" : "Breadcrumb",
    footerBlurb: es
      ? "Un sistema de gestión notarial, y la fábrica de software asistida por IA que lo construye."
      : "A notarial management system, and the AI-assisted software factory that builds it.",
    footerNote: es
      ? "Cada afirmación de este sitio enlaza su fuente en el repositorio. Los números se leen de él al construir el sitio."
      : "Every claim on this site links its source in the repository. Numbers are read from it when the site is built.",
    about: es ? "Sobre el autor" : "About the author",
    demo: es ? "Demo en vivo" : "Live demo",
    reports: es ? "Informes" : "Reports",
    liveReports: es ? "Informes en vivo" : "Live reports",
    untranslated: "Esta página sigue en inglés. La navegación está en español.",
    github: "Notaire on GitHub",
  };
}

const FACTORY_PAGES: Record<string, { en: string; es: string; summaryEs: string }> = {
  "/ai-sdlc/": { en: "Overview", es: "Panorama", summaryEs: "Por qué los agentes necesitan una fábrica, y qué es esa fábrica." },
  "/ai-sdlc/case-study/": { en: "Case study", es: "Caso", summaryEs: "Cómo Notaire aprendió a entregar código escrito por IA, con los datos." },
  "/ai-sdlc/anatomy/": { en: "Anatomy", es: "Anatomía", summaryEs: "Qué es el AI SDLC, por qué existe y cómo funciona, como una cámara de esclusas." },
  "/ai-sdlc/lifecycle/": { en: "Lifecycle", es: "Ciclo de vida", summaryEs: "24 pasos obligatorios, del Issue al Issue cerrado, con cinco gates duras." },
  "/ai-sdlc/openspec/": { en: "OpenSpec", es: "OpenSpec", summaryEs: "El esquema dirigido por especificación y las adaptaciones de Notaire." },
  "/ai-sdlc/agents/": { en: "Agents & skills", es: "Agentes y skills", summaryEs: "El equipo, sus skills, y quién hace qué junto al Owner humano." },
  "/ai-sdlc/guardrails/": { en: "Guardrails", es: "Guardrails", summaryEs: "Nueve capas entre un agente y main." },
  "/ai-sdlc/quality-gates/": { en: "Quality gates", es: "Quality gates", summaryEs: "El preflight local es la prueba del pull request. El CI del producto corre en un tag." },
  "/ai-sdlc/security-gates/": { en: "Security gates", es: "Security gates", summaryEs: "Cómo los agentes corren el catálogo de seguridad, actualizan el modelo de amenazas y abren Issues." },
  "/ai-sdlc/governance/": { en: "Governance", es: "Gobierno", summaryEs: "Etiquetas, hitos, el tablero y el flujo del pull request." },
  "/ai-sdlc/lessons/": { en: "Lessons", es: "Lecciones", summaryEs: "Incidentes reales, cada uno convertido en una regla o un chequeo." },
  "/ai-sdlc/results/": { en: "Results", es: "Resultados", summaryEs: "Lo que produjo la fábrica, contado desde el repositorio." },
  "/ai-sdlc/framework/": { en: "Framework", es: "Framework", summaryEs: "Una política, un perfil, un kit que cualquier repositorio puede adoptar." },
  "/ai-sdlc/playbook/": { en: "Playbook", es: "Playbook", summaryEs: "Ocho pasos para aplicar un AI SDLC a tu propio proyecto." },
};

const ACTS: Record<string, string> = {
  "I · The problem": "I · El problema",
  "II · How a change flows": "II · Cómo avanza un cambio",
  "III · What keeps it safe": "III · Qué lo mantiene a salvo",
  "IV · What we learned": "IV · Qué aprendimos",
  "V · Apply it": "V · Cómo aplicarlo",
};

export function factoryPageTitle(href: string, locale: Locale, fallback: string): string {
  const row = FACTORY_PAGES[href];
  if (!row || locale === "en") return fallback;
  return row.es;
}

export function factoryPageSummary(href: string, locale: Locale, fallback: string): string {
  const row = FACTORY_PAGES[href];
  if (!row || locale === "en") return fallback;
  return row.summaryEs;
}

export function actLabel(label: string, locale: Locale): string {
  if (locale === "en") return label;
  return ACTS[label] ?? label;
}

export function navLabel(key: "factory" | "product" | "security" | "docs" | "releases", locale: Locale): string {
  return chrome(locale).nav[key];
}

export function homeCopy(locale: Locale) {
  const es = locale === "es";
  return {
    kicker: es ? "Proyecto de portafolio · Matias Miguez" : "Portfolio project · Matias Miguez",
    titleBefore: es
      ? "Un proyecto universitario de 2014, rehecho con agentes de IA que "
      : "A 2014 university project, rebuilt with AI agents that ",
    titleEm: es ? "no pueden saltarse un paso" : "cannot skip a step",
    lead: es
      ? "Notaire gestiona una escribanía: personas, inmuebles, expedientes, escrituras, presupuestos y pagos. Una persona lo llevó de un monolito Java Swing a una plataforma Spring Boot y Next.js. Los agentes de IA escriben la mayor parte del código. Cada cambio pasa por un Issue, una especificación OpenSpec y un pull request que solo entra a main si las gates locales quedan en verde. El esquema de la base se versiona con Flyway y cada commit queda en el historial."
      : "Notaire manages a notary office: people, properties, cases, deeds, quotes and payments. One engineer moved it from a Java Swing monolith to a Spring Boot and Next.js platform, using AI agents for most of the code and a gated SDLC that checks every change with tests, specs and CI.",
    ctaFactory: es ? "Cómo funciona la AI Factory" : "How the AI factory works",
    ctaCase: es ? "Leer el caso" : "Read the case study",
    ctaDemo: es ? "Probar la demo en vivo" : "Try the live demo",
    ctaRepo: es ? "Código en GitHub" : "Code on GitHub",
    countedLabel: es ? "Contado desde el repositorio" : "Counted from the repository",
    countedNote: es ? "Contado desde el repositorio cuando se construyó este sitio." : "Counted from the repository when this site was built.",
    proof: {
      openspec: es ? "cambios entregados por OpenSpec" : "changes shipped through OpenSpec",
      tests: es ? "archivos de test de backend y frontend" : "backend and frontend test files",
      bruno: es ? "requests de Bruno" : "Bruno API requests",
      e2e: es ? "specs E2E de Playwright" : "Playwright E2E specs",
      adrs: es ? "registros de decisión de arquitectura" : "architecture decision records",
      skills: es ? "skills de agentes en el repo" : "agent skills in the repo",
    },
    takeawaysTitle: es ? "Qué muestra este proyecto" : "What this project shows",
    takeaways: es
      ? [
          { title: "Modernización de un legado", body: "Un monolito de escritorio acoplado pasó a una API REST hexagonal y una web tipada, corte a corte, cada corte con sus propios tests.", link: "El hexágono" },
          { title: "IA con guardrails", body: "Los agentes escriben la mayor parte del código, pero cada cambio pasa las mismas gates comprobadas por máquina: un Issue real, una spec, tests que fallan primero, un pipeline local en verde y un merge revisado.", link: "Las guardrails" },
          { title: "Ingeniería de producción", body: "Un esquema versionado, un contrato de API comprobado en CI, {workflows} workflows de GitHub Actions, escaneo de seguridad y un stack de observabilidad y calidad de código de {services} servicios.", link: "El stack" },
        ]
      : [
          { title: "Legacy modernisation", body: "A coupled desktop monolith became a hexagonal REST API and a typed web app, slice by slice, each slice behind its own tests.", link: "The hexagon" },
          { title: "AI with guardrails", body: "Agents write most of the code, but every change passes the same machine-checked gates: a real issue, a spec, failing tests first, a green local pipeline, and a reviewed merge.", link: "The guardrails" },
          { title: "Production engineering", body: "A versioned schema, a CI-checked API contract, {workflows} CI workflows, security scanning and a {services}-service observability and code-quality stack.", link: "The stack" },
        ],
    factoryTitle: es ? "Cómo sale cada cambio" : "How every change ships",
    factoryLead: es
      ? "Las personas y los agentes toman el mismo camino. Una gate se abre solo cuando su comando termina como se espera. Si se niega, el cambio vuelve atrás. Avanza paso a paso."
      : "Humans and agents take the same path. A gate opens only when its command exits as expected; a refusal sends the change back. Step through it.",
    beforeTitle: es ? "Antes y ahora" : "Before and after",
    beforeHeads: es ? (["Área", "2014", "Hoy"] as const) : (["Area", "2014", "Today"] as const),
    beforeNote: es ? "La columna de 2014 sale del README del proyecto. Los valores de hoy se leen del repositorio. La historia completa está en Producto · Historia." : "The 2014 column is from the project README; today's values are read from the repository. The full story is in Product · Story.",
    beforeAreas: es
      ? ["Aplicación", "Interfaz", "Base de datos", "API", "Contraseñas", "Tests", "Entrega"]
      : ["Application", "User interface", "Database", "API", "Passwords", "Tests", "Delivery"],
    machinesTitle: es ? "Cómo funciona, dibujado como máquinas" : "How it works, drawn as machines",
    machinesLead: es
      ? "Tres máquinas ilustradas explican el sistema desde lo básico: qué hace la escribanía y cómo viaja un pedido, cómo desarrollo, QA, seguridad y operaciones corren en un solo ciclo, y cómo el AI SDLC deja que los agentes construyan una función sin saltarse una gate."
      : "Three illustrated, step-through machines explain the system from the basics: what the office does and how a request travels, how development, QA, security and operations run as one loop, and how the AI SDLC lets agents build a feature without skipping a gate.",
    machines: es
      ? [
          { title: "El mostrador del escribano", body: "Un pedido, de punta a punta: navegador, API, reglas, base de datos, auditoría y monitoreo." },
          { title: "La línea de release", body: "Desarrollo, QA, seguridad, gates, entornos y operaciones en una sola cinta." },
          { title: "La cámara de esclusas", body: "Cinco gates, tres roles y un reglamento entre un agente y main." },
        ]
      : [
          { title: "The notary's counter", body: "One request, end to end: browser, API, rules, database, audit and monitoring." },
          { title: "The release line", body: "Dev, QA, security, gates, environments and operations on one conveyor." },
          { title: "The lock chamber", body: "Five gates, three roles and one rulebook between an agent and main." },
        ],
    stepThrough: es ? "Avanzar paso a paso" : "Step through it",
    shotsTitle: es ? "El producto, en marcha" : "The product, running",
    shotsLeadBefore: es ? "Pantallas de la app Next.js en el stack local de Docker, con datos de demo. O " : "Screens of the Next.js app on the local Docker stack with demo data. Or ",
    shotsLeadLink: es ? "abre la demo en vivo" : "open the live demo",
    shotsLeadAfter: es ? ": la misma app, de solo lectura, con datos de ejemplo." : ": the same app, read-only, with sample data.",
    shots: es
      ? [
          { title: "Tablero", caption: "La actividad de hoy y en qué punto del flujo está cada expediente." },
          { title: "Expedientes", caption: "Expedientes notariales con sus trámites y su estado." },
          { title: "Administración", caption: "Catálogos, plantillas, roles y permisos." },
          { title: "Auditoría", caption: "Las lecturas de datos personales quedan registradas." },
        ]
      : [
          { title: "Dashboard", caption: "Today's activity and where each case is in its workflow." },
          { title: "Cases", caption: "Notarial cases with their procedures and status." },
          { title: "Administration", caption: "Catalogues, templates, roles and permissions." },
          { title: "Audit log", caption: "Reads of personal data are recorded." },
        ],
    outcomesTitle: es ? "Resultados de entrega" : "Delivery outcomes",
    outcomesLead: es ? "Desde GitHub, de" : "From GitHub,",
    outcomesTo: es ? "a" : "to",
    outcomesExclude: es ? "pull requests de Dependabot excluidos." : "Dependabot PRs excluded.",
    outcomesHow: es ? "Cómo se calcula cada número" : "How each number is computed",
    outcomes: {
      median: es ? "mediana del lead time del pull request, de abierto a fusionado" : "median PR lead time, opened to merged",
      p90: es ? "lead time p90 del pull request" : "p90 PR lead time",
      week: es ? "pull requests fusionados en una semana mediana" : "merged PRs in a median week",
      failure: es ? "de los pull requests fusionados fueron reversiones o hotfixes" : "of merged PRs were reverts or hotfixes",
      issues: es ? "Issues cerrados" : "issues closed",
      releases: es ? "releases" : "releases",
    },
    sectionsTitle: es ? "Explorar" : "Explore",
    doors: {
      product: { eyebrow: es ? "Producto" : "Product", title: es ? "Qué hace Notaire" : "What Notaire does", open: es ? "Abrir Producto" : "Open Product" },
      docs: { eyebrow: es ? "Documentación técnica" : "Technical docs", title: es ? "Cómo está construido" : "How it is built", open: es ? "Abrir la documentación" : "Open Technical docs" },
      factory: { eyebrow: "AI Factory", title: es ? "Cómo lo entregan los agentes" : "How AI agents ship it", open: es ? "Abrir la AI Factory" : "Open AI Factory" },
      security: { eyebrow: es ? "Seguridad" : "Security", title: es ? "Cómo se protege el registro" : "How the record is protected", open: es ? "Abrir Seguridad" : "Open Security" },
    },
    aboutTitle: es ? "Sobre el autor" : "About the author",
    aboutMore: es ? "Más sobre él, con las fuentes" : "More about him, with sources",
    storyLink: es ? "Producto · Historia" : "Product · Story",
    readOrderTitle: es ? "Léelo en orden" : "Read this in order",
    readOrderLead: es
      ? "Si es la primera vez, sigue este camino: de qué es Notaire a cómo aplicar el kit en otro proyecto. Si ya conoces las ideas, usa los cinco actos de la AI Factory."
      : "If this is your first visit, follow this path: from what Notaire is to how to apply the kit on another project. If you already know the ideas, use the five acts in the AI Factory.",
  };
}

export function readingCopy(locale: Locale) {
  const es = locale === "es";
  return {
    title: es ? "Léelo en orden" : "Read this in order",
    lead: es
      ? "De lo más simple a lo más específico. Cada paso es una página. No hace falta leerlas todas de una vez."
      : "From the simplest idea to the most specific one. Each step is a page. You do not have to read them all at once.",
    steps: {
      "/ai-sdlc/": { title: es ? "Qué estás leyendo" : "What you are reading", body: es ? "La fábrica no es el producto. El producto es la escribanía. La fábrica es cómo se cambia el producto." : "The factory is not the product. The product is the notary office. The factory is how the product changes." },
      "/ai-sdlc/case-study/": { title: es ? "La historia, en corto" : "The story, briefly", body: es ? "Qué se rompió cuando los agentes escribían rápido, y qué regla salió de cada rotura." : "What broke when agents wrote quickly, and which rule came out of each break." },
      "/ai-sdlc/lifecycle/": { title: es ? "Un cambio, paso a paso" : "One change, step by step", body: es ? "Del Issue al merge. Cada gate es un comando con un código de salida." : "From the Issue to the merge. Each gate is a command with an exit code." },
      "/ai-sdlc/agents/": { title: es ? "Quién hace qué" : "Who does what", body: es ? "El Owner decide. El Foreman ordena el trabajo. Los Workers implementan. Nadie se aprueba a sí mismo." : "The Owner decides. The Foreman orders the work. Workers implement. Nobody approves their own change." },
      "/ai-sdlc/guardrails/": { title: es ? "Qué frena un mal cambio" : "What stops a bad change", body: es ? "Guardrails, quality gates y security gates. Una afirmación del modelo no es evidencia." : "Guardrails, quality gates and security gates. A model's claim is not evidence." },
      "/ai-sdlc/results/": { title: es ? "Qué muestra el repositorio" : "What the repository shows", body: es ? "Conteos y lead time leídos de GitHub, con la fecha y el método al lado del número." : "Counts and lead time read from GitHub, with the date and the method next to the number." },
      "/ai-sdlc/framework/": { title: es ? "El kit, y lo que sigue planificado" : "The kit, and what is still planned", body: es ? "Qué comandos están en main y qué sigue marcado como planificado." : "Which commands are on main, and what is still marked planned." },
      "/ai-sdlc/playbook/": { title: es ? "Probarlo en otro proyecto" : "Try it on another project", body: es ? "Ocho pasos. El kit no exige el stack de Notaire." : "Eight steps. The kit does not require Notaire's stack." },
    } as Record<string, { title: string; body: string }>,
  };
}

export function factoryCopy(locale: Locale) {
  const es = locale === "es";
  return {
    kicker: es ? "AI SDLC · la fábrica, no el producto" : "AI SDLC · the factory, not the product",
    title: es ? "La AI Factory que construye Notaire" : "The AI Factory that builds Notaire",
    lead: es
      ? "Notaire es un sistema de gestión notarial: Next.js, Spring Boot y PostgreSQL. Esta sección es la fábrica de software que lo construye: una Constitución escrita, un flujo dirigido por especificaciones, un equipo de agentes especializados y gates mecánicas. El Owner humano está en cada decisión que importa."
      : "Notaire is a notarial management system: Next.js, Spring Boot and PostgreSQL. This section is about the software factory that builds it: a written Constitution, a spec-driven workflow, a crew of specialised AI agents and mechanical quality gates take every change from a GitHub Issue to a deployed, smoke-tested release, with a human Owner on every decision that matters.",
    start: es ? "Empezar la historia" : "Start the story",
    flow: es ? "Cómo avanza un cambio" : "How a change flows",
    apply: es ? "Aplicarlo a tu proyecto" : "Apply it to your project",
    problemTitle: es ? "El problema: velocidad sin prueba" : "The problem: speed without proof",
    problemLead: es
      ? "Los agentes de IA escriben código rápido. Solos, también se saltan pasos, citan Issues que no existen e informan corridas en verde que nunca ocurrieron. Notaire chocó con cada uno de estos casos. Cada incidente pasó a ser una regla y después un comando con código de salida."
      : "AI agents write code fast. Left to themselves they also skip steps, cite issues that do not exist and report green runs that never happened. Notaire hit every one of these. Each incident below became a rule, and then a command with an exit code.",
    now: es ? "Ahora: " : "Now: ",
    allLessons: es ? "Todos, con los Issues que los corrigieron, están en Lecciones." : "All of them, with the issues that fixed them, are in Lessons.",
    answerTitle: es ? "La respuesta: una fábrica con gates" : "The answer: a factory with gates",
    answerLead: es
      ? "Cada cambio, de una persona o de un agente, toma el mismo camino. Una gate se abre solo cuando su comando termina como se espera. El banco de abajo es ese camino como máquina: una tarjeta de Issue sobre un carro, cinco barreras que siguen bajas hasta que su chequeo sale en verde."
      : "Every change, by a human or by an agent, takes the same path. A gate opens only when its command exits as expected. The bench below is that path as a machine: one Issue card on a carriage, five booms that stay down until their check exits green.",
    lifecycle: es ? "ciclo de vida completo" : "full lifecycle",
    anatomy: es ? "anatomía" : "anatomy",
    storyTitle: es ? "La historia en cinco actos" : "The story in five acts",
    numbersTitle: es ? "La fábrica, contada" : "The factory, counted",
    numbersNote: es ? "Contado desde el repositorio cuando se construyó este sitio. Más en Resultados." : "Counted from the repository when this site was built. More in Results.",
    versusTitle: es ? "Fábrica y producto" : "Factory vs. product",
    versus: es
      ? "La fábrica y el producto viven en un solo monorepo y responden preguntas distintas. La documentación técnica describe qué hace Notaire. La fábrica describe cómo se hace cualquier cambio, por una persona o por un agente, y es la misma para cada módulo y cada herramienta."
      : "The factory and the product live in one monorepo but answer different questions. The Technical docs describe what Notaire does: use cases, architecture, modules. The factory describes how any change to Notaire is made, by a human or by an agent, and it is the same for every module and every tool.",
    principlesTitle: es ? "Seis principios" : "Six principles",
    sourcesLead: es ? "Estas páginas resumen. El repositorio decide. Si difieren, ganan los archivos de abajo." : "These pages summarise; the repository decides. When they differ, the files below win.",
    sourcesTitle: es ? "Fuentes de verdad" : "Sources of truth",
    lessonsLink: es ? "Lecciones" : "Lessons",
    resultsLink: es ? "Resultados" : "Results",
    docsLink: es ? "Documentación técnica" : "Technical docs",
    stat: {
      skills: es ? "skills en el catálogo" : "skills in the catalog",
      agents: es ? "definiciones de agentes" : "agent definitions",
      workflows: es ? "workflows de CI/CD" : "CI/CD workflows",
      archived: es ? "cambios OpenSpec archivados" : "OpenSpec changes archived",
      specs: es ? "specs de capacidad vivas" : "living capability specs",
      scripts: es ? "scripts de proceso (workspace/sdlc)" : "process scripts (workspace/sdlc)",
      e2e: es ? "suites E2E de Playwright" : "Playwright E2E suites",
      adrs: es ? "ADRs" : "ADRs",
    },
    principles: es
      ? [
          { k: "La política primero", v: "Una Constitución, agnóstica del agente, por encima de cualquier otro documento. Las herramientas la implementan. No la redefinen." },
          { k: "Spec antes que código", v: "No hay implementación sin un Issue, un caso de uso y una especificación OpenSpec (Gate 1)." },
          { k: "Rojo antes que verde", v: "Los tests se escriben y se ven fallar antes de implementar (Gate 2). Un test que nunca viste fallar no prueba nada." },
          { k: "Las máquinas comprueban a las máquinas", v: "Las gates son comandos con código de salida. Que un modelo diga que está todo en verde no cuenta." },
          { k: "Trazable de punta a punta", v: "Issue, caso de uso, spec, tests, código, docs, pull request, deploy. Queda en el libro de trazabilidad del cambio." },
          { k: "Las personas deciden", v: "El Owner aprueba especificaciones, concede excepciones y fusiona la política. Los agentes anotan los pasos que solo puede hacer el Owner." },
        ]
      : [
          { k: "Policy first", v: "One Constitution, agent-agnostic, above every other document. Tools implement it; they never redefine it." },
          { k: "Spec before code", v: "No implementation without an Issue, a Use Case and an OpenSpec specification (Gate 1)." },
          { k: "Red before green", v: "Tests are written and observed failing before implementation (Gate 2). A test you never saw fail proves nothing." },
          { k: "Machines check machines", v: "Gates are commands with exit codes. A model's own claim of \"all green\" is never trusted." },
          { k: "Traceable end to end", v: "Issue → Use Case → spec → tests → code → docs → PR → deploy, recorded in each change's traceability ledger." },
          { k: "Humans own decisions", v: "The Owner approves specifications, grants exceptions and merges policy. Agents write down Owner-only steps." },
        ],
  };
}

export function lessonCopy(locale: Locale, title: string, happened: string, rule: string): { happened: string; rule: string } {
  if (locale === "en") return { happened, rule };
  const es: Record<string, { happened: string; rule: string }> = {
    "Light green is not mergeable": {
      happened: "Una suscripción de CI informó que los 18 chequeos habían pasado mientras la suite de integración del backend y Playwright seguían pendientes.",
      rule: "check-heavy-ci.sh falla si una corrida pesada está pendiente o en rojo. Una corrida ausente cuenta como éxito: el CI del producto no arranca en el pull request. La prueba local es preflight.sh --full.",
    },
    "An invented Issue number passed Gate 1": {
      happened: "La Gate 1 solo comprobaba el formato #[0-9]+, así que una propuesta podía citar #999999.",
      rule: "validate-sdlc-plan.sh resuelve el Issue en vivo con gh. Los hallazgos de exploración pasan por openspec-triage y se vuelven Issues reales antes.",
    },
    "Models report success they have not earned": {
      happened: "Workers locales escribieron «1.940 tests pasan» y «PR creado» en tasks.md, copiaron SHAs de main y afirmaron cambios que sus commits no tenían.",
      rule: "El harness es dueño del libro y de los push. Las gates corren comandos reales. La Gate 4 compara cada línea del mensaje de commit con git show --stat.",
    },
    "Too much always-loaded context": {
      happened: "Importar reglas enteras y skills con alwaysApply llenaba la ventana de contexto de cada sesión.",
      rule: "Lo que se carga siempre se queda en 8.000 tokens o menos: un AGENTS.md breve, una tarjeta de la Constitución de 3 KB, y reglas bajo demanda.",
    },
  };
  return es[title] ?? { happened, rule };
}
