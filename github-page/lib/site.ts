/**
 * Site map (#1970): the single registry behind the header, the mobile drawer, the sidebars,
 * breadcrumbs, previous/next links, the static search and the link tests.
 * Add a page here and it appears everywhere; a href with no page file fails the tests.
 */

/** `home` is the landing page at `/`; it belongs to no section and shows no sidebar. */
export type SectionKey = "home" | "factory" | "product" | "security" | "docs" | "releases";

export type SitePage = {
  href: string;
  title: string;
  /** One line, shown in search results, sidebars and previous/next cards. */
  summary: string;
  /** Extra words people may type into search. */
  keywords?: string;
};

export type PageGroup = { label: string; pages: SitePage[] };

export const REPO_URL = "https://github.com/matiaspakua/notaire";

/** Prefix for files in public/: the export is served under /notaire (next.config.ts). */
export const ASSET_BASE = process.env.NODE_ENV === "production" ? "/notaire" : "";

export const PRIMARY_NAV: { key: Exclude<SectionKey, "home">; label: string; href: string; blurb: string }[] = [
  { key: "factory", label: "AI Factory", href: "/ai-sdlc/", blurb: "The AI SDLC that builds Notaire" },
  { key: "product", label: "Product", href: "/product/", blurb: "What Notaire does, its story and its numbers" },
  { key: "security", label: "Security", href: "/security/", blurb: "Trust boundaries, threats, tools and when they run" },
  { key: "docs", label: "Technical docs", href: "/docs/", blurb: "Architecture, modules, testing, security" },
  { key: "releases", label: "Releases", href: "/docs/releases/", blurb: "Every product cut and its parts" },
];

/** The AI Factory, told as a story in five acts: why, how, safety, evidence, adoption. */
export const FACTORY_ACTS: PageGroup[] = [
  {
    label: "I · The problem",
    pages: [
      { href: "/ai-sdlc/", title: "Overview", summary: "Why AI agents need a factory, and what the factory is.", keywords: "ai sdlc factory problem principles" },
      { href: "/ai-sdlc/case-study/", title: "Case study", summary: "How Notaire learned to ship AI-written code, with the data.", keywords: "history story chapters timeline" },
    ],
  },
  {
    label: "II · How a change flows",
    pages: [
      { href: "/ai-sdlc/lifecycle/", title: "Lifecycle", summary: "24 mandatory steps from Issue to closed Issue, five hard gates.", keywords: "workflow steps constitution issue pr" },
      { href: "/ai-sdlc/openspec/", title: "OpenSpec", summary: "The spec-driven schema and Notaire's adaptations.", keywords: "spec proposal tasks traceability archive" },
      { href: "/ai-sdlc/agents/", title: "Agents & skills", summary: "The crew, its skills, and who does what next to the human Owner.", keywords: "foreman worker owner swimlane crew" },
    ],
  },
  {
    label: "III · What keeps it safe",
    pages: [
      { href: "/ai-sdlc/guardrails/", title: "Guardrails", summary: "Nine layers between an agent and main.", keywords: "hooks policy rules never" },
      { href: "/ai-sdlc/quality-gates/", title: "Quality gates", summary: "Local preflight is the pull-request proof. Product CI runs on a version tag.", keywords: "ci pipeline preflight release tag coverage" },
      { href: "/ai-sdlc/security-gates/", title: "Security gates", summary: "How agents run the security catalog, update the threat model, and file issues.", keywords: "semgrep trivy codeql zap run_gates threat model" },
      { href: "/ai-sdlc/governance/", title: "Governance", summary: "Labels, milestones, the board and the pull-request flow.", keywords: "labels milestones board roles merge" },
    ],
  },
  {
    label: "IV · What we learned",
    pages: [
      { href: "/ai-sdlc/lessons/", title: "Lessons", summary: "Real incidents, each turned into a rule or a check.", keywords: "incidents mistakes decisions" },
      { href: "/ai-sdlc/results/", title: "Results", summary: "What the factory produced, counted from the repository.", keywords: "metrics numbers coverage throughput" },
    ],
  },
  {
    label: "V · Apply it",
    pages: [
      { href: "/ai-sdlc/framework/", title: "Framework", summary: "One policy, one profile, one kit any repository can adopt.", keywords: "aisdlc kit adopt levels" },
      { href: "/ai-sdlc/playbook/", title: "Playbook", summary: "Eight steps to apply an AI SDLC to your own project.", keywords: "how to apply guide start" },
    ],
  },
];

export const DOCS_GROUPS: PageGroup[] = [
  {
    label: "Start here",
    pages: [
      { href: "/docs/", title: "Overview", summary: "Map of the technical documentation.", keywords: "docs index start" },
      { href: "/docs/stack/", title: "Technology stack", summary: "Every major technology and pattern, with versions read from the repo.", keywords: "stack technologies versions java spring next react postgres patterns assessment" },
    ],
  },
  {
    label: "The system",
    pages: [
      { href: "/docs/business/", title: "Business", summary: "Requirements, use cases, actors and manuals.", keywords: "use cases cu rf rnf requirements" },
      { href: "/docs/modules/", title: "Modules", summary: "Module ownership and boundaries.", keywords: "frontend backend modules ownership" },
      { href: "/docs/architecture/", title: "Architecture", summary: "System context, every ADR and the SAD.", keywords: "adr decisions sad c4 diagrams" },
    ],
  },
  {
    label: "Architecture internals",
    pages: [
      { href: "/docs/hexagonal/", title: "Hexagonal architecture", summary: "Ports, adapters and use cases, enforced by ArchUnit and ESLint.", keywords: "hexagonal ports adapters archunit use cases domain adr-030" },
      { href: "/docs/database/", title: "Database & Flyway", summary: "PostgreSQL schema versioned by Flyway migrations.", keywords: "flyway migrations postgresql schema sql versioning" },
      { href: "/docs/api/", title: "API contract & Bruno", summary: "The OpenAPI contract, generated types and the Bruno API suite.", keywords: "openapi swagger springdoc bruno api rest contract oasdiff types" },
    ],
  },
  {
    label: "Quality & operations",
    pages: [
      { href: "/docs/testing/", title: "Testing", summary: "Test plan, pyramid and suites.", keywords: "tests playwright junit vitest" },
      { href: "/docs/security/", title: "Security docs", summary: "Bridge to the interactive Security section and the architecture notes.", keywords: "devsecops codeql zap trivy assessment" },
    ],
  },
  {
    label: "Planning & delivery",
    pages: [
      { href: "/docs/roadmap/", title: "Roadmap", summary: "Release trains, milestones and history.", keywords: "milestones plan train" },
      { href: "/docs/releases/", title: "Releases", summary: "Every product cut: backend, frontend and database.", keywords: "versions changelog tags" },
    ],
  },
];

/** The Product section (#1976): four pages in the shared document shell. */
export const PRODUCT_GROUPS: PageGroup[] = [
  {
    label: "The product",
    pages: [
      { href: "/product/", title: "Overview", summary: "What Notaire does for a notary office, and who uses it.", keywords: "product features screenshots notary cases quotes deeds" },
      { href: "/product/story/", title: "Story", summary: "From a 2014 Swing project to an AI-built platform.", keywords: "history origin swing evolution ai era timeline" },
      { href: "/product/platform/", title: "Platform", summary: "The three tiers and the infrastructure around them.", keywords: "architecture tiers infrastructure observability docker" },
      { href: "/product/numbers/", title: "Numbers", summary: "Versions, coverage and statistics read from the repository.", keywords: "stats metrics coverage versions" },
    ],
  },
];

/** Static reports copied into the export by deploy-github-page.yml (not Next routes). */
export const REPORTS: { href: string; title: string; summary: string }[] = [
  { href: "/coverage/", title: "Backend coverage (JaCoCo)", summary: "Latest coverage report from main." },
  { href: "/playwright/", title: "Playwright E2E report", summary: "Latest end-to-end run." },
  { href: "/cicd-reports/", title: "CI/CD reports", summary: "Latest pipeline reports." },
];

/** Pages outside the three sections (#1985): no sidebar, but searchable and titled. */
export const OTHER_PAGES: SitePage[] = [
  { href: "/about/", title: "About", summary: "Who built Notaire, from sourced facts only.", keywords: "author matias miguez contact linkedin github about me" },
];

/**
 * The read-only demo (#1985): the real frontend exported under /demo/ by demo/build-demo.mjs.
 * Not a Next route of this app, so link it with a plain <a href={ASSET_BASE + DEMO.href}>.
 */
export const DEMO = { href: "/demo/", title: "Live demo", summary: "The real Notaire app with sample data, read-only. Sign in with any name." };

export const FACTORY_PAGES: SitePage[] = FACTORY_ACTS.flatMap((g) => g.pages);
export const DOCS_PAGES: SitePage[] = DOCS_GROUPS.flatMap((g) => g.pages);
/** Public security story (#2003). Demo data. Not a live scan. */
export const SECURITY_GROUPS: PageGroup[] = [
  {
    label: "The posture",
    pages: [
      { href: "/security/", title: "Overview", summary: "What the security program protects, and the public snapshot.", keywords: "security posture stride devsecops" },
      { href: "/security/architecture/", title: "Trust boundaries", summary: "Step through the browser, the BFF, the API and the database.", keywords: "trust boundary dfd jwt bff" },
      { href: "/security/threats/", title: "Threats", summary: "Explore STRIDE and the privacy notes for notarial personal data.", keywords: "stride linddun threat model sr" },
      { href: "/security/toolchain/", title: "Toolchain", summary: "Which scanner runs locally, on a version tag, or on a weekly schedule.", keywords: "semgrep trivy codeql zap cadence" },
      { href: "/security/posture/", title: "Reporting", summary: "Where findings go today, and why this page is not a live dashboard.", keywords: "sarif report dashboard artifact" },
    ],
  },
];

export const PRODUCT_PAGES: SitePage[] = PRODUCT_GROUPS.flatMap((g) => g.pages);
export const SECURITY_PAGES: SitePage[] = SECURITY_GROUPS.flatMap((g) => g.pages);

function norm(p: string): string {
  const path = (p || "/").split("#")[0].split("?")[0];
  return path.endsWith("/") ? path : `${path}/`;
}

export function sectionOf(pathname: string): SectionKey {
  const p = norm(pathname);
  if (p.startsWith("/ai-sdlc/")) return "factory";
  if (p.startsWith("/security/")) return "security";
  if (p.startsWith("/docs/releases/")) return "releases";
  if (p.startsWith("/docs/")) return "docs";
  if (p.startsWith("/product/")) return "product";
  return "home";
}

/** Sidebar groups for the section a path belongs to (releases live inside the docs). */
export function groupsFor(section: SectionKey): PageGroup[] {
  if (section === "factory") return FACTORY_ACTS;
  if (section === "docs" || section === "releases") return DOCS_GROUPS;
  if (section === "product") return PRODUCT_GROUPS;
  if (section === "security") return SECURITY_GROUPS;
  return [];
}

export function findPage(pathname: string): SitePage | undefined {
  const p = norm(pathname);
  return [...FACTORY_PAGES, ...DOCS_PAGES, ...PRODUCT_PAGES, ...SECURITY_PAGES, ...OTHER_PAGES].find((x) => x.href === p);
}

export function prevNext(pathname: string): { prev?: SitePage; next?: SitePage } {
  const p = norm(pathname);
  const section = sectionOf(p);
  const list =
    section === "factory" ? FACTORY_PAGES : section === "product" ? PRODUCT_PAGES : section === "security" ? SECURITY_PAGES : DOCS_PAGES;
  const i = list.findIndex((x) => x.href === p);
  if (i < 0) return {};
  return { prev: list[i - 1], next: list[i + 1] };
}

export function actOf(pathname: string): string | undefined {
  const p = norm(pathname);
  return FACTORY_ACTS.find((g) => g.pages.some((x) => x.href === p))?.label;
}

export function breadcrumbs(pathname: string): { href: string; label: string }[] {
  const p = norm(pathname);
  const section = sectionOf(p);
  const root = PRIMARY_NAV.find((n) => n.key === (section === "releases" ? "docs" : section));
  if (!root) return [];
  const crumbs = [{ href: "/", label: "Home" }, { href: root.href, label: root.label }];
  const page = findPage(p);
  if (page && page.href !== root.href) crumbs.push({ href: page.href, label: page.title });
  return crumbs;
}

export function isActive(key: SectionKey, pathname: string): boolean {
  return sectionOf(pathname) === key;
}

export type SearchEntry = SitePage & { section: string };

export const SEARCH_INDEX: SearchEntry[] = [
  ...FACTORY_PAGES.map((p) => ({ ...p, section: "AI Factory" })),
  ...SECURITY_PAGES.map((p) => ({ ...p, section: "Security" })),
  ...DOCS_PAGES.map((p) => ({ ...p, section: "Technical docs" })),
  ...PRODUCT_PAGES.map((p) => ({ ...p, section: "Product" })),
  ...OTHER_PAGES.map((p) => ({ ...p, section: "Project" })),
  { ...DEMO, keywords: "demo try app live sample", section: "Project" },
  ...REPORTS.map((r) => ({ ...r, section: "Reports" })),
];

/** Case- and accent-insensitive search: every word must appear in the title, summary, keywords or section. */
export function search(query: string, limit = 8): SearchEntry[] {
  const fold = (s: string) => s.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "");
  const words = fold(query).split(/\s+/).filter(Boolean);
  if (words.length === 0) return [];
  const scored = SEARCH_INDEX.map((e) => {
    const title = fold(e.title);
    const hay = `${title} ${fold(e.summary)} ${fold(e.keywords ?? "")} ${fold(e.section)}`;
    if (!words.every((w) => hay.includes(w))) return null;
    const score = words.reduce((s, w) => s + (title.startsWith(w) ? 3 : title.includes(w) ? 2 : 1), 0);
    return { e, score };
  }).filter((x): x is { e: SearchEntry; score: number } => x !== null);
  return scored.sort((a, b) => b.score - a.score).slice(0, limit).map((x) => x.e);
}

/** Per-page `<title>` and description from the site map (#1970): every page gets its own. */
export function pageMetadata(href: string): { title: string; description: string } {
  const page = findPage(href);
  if (!page) throw new Error(`pageMetadata: ${href} is not in lib/site.ts`);
  const key = sectionOf(href);
  if (OTHER_PAGES.includes(page)) return { title: page.title, description: page.summary };
  const section = key === "factory" ? "AI Factory" : key === "product" ? "Product" : key === "security" ? "Security" : "Technical docs";
  const isRoot = page.href === "/ai-sdlc/" || page.href === "/docs/" || page.href === "/product/" || page.href === "/security/";
  return { title: isRoot ? section : `${page.title} — ${section}`, description: page.summary };
}
