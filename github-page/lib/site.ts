/**
 * Site map (#1970): the single registry behind the header, the mobile drawer, the sidebars,
 * breadcrumbs, previous/next links, the static search and the link tests.
 * Add a page here and it appears everywhere; a href with no page file fails the tests.
 */

export type SectionKey = "factory" | "product" | "docs" | "releases";

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

export const PRIMARY_NAV: { key: SectionKey; label: string; href: string; blurb: string }[] = [
  { key: "factory", label: "AI Factory", href: "/ai-sdlc/", blurb: "The AI SDLC that builds Notaire" },
  { key: "product", label: "Product", href: "/", blurb: "The Notaire system and its story" },
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
      { href: "/ai-sdlc/quality-gates/", title: "Quality gates", summary: "From preflight to heavy CI: every check is an exit code.", keywords: "ci pipeline preflight heavy coverage" },
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
    pages: [{ href: "/docs/", title: "Overview", summary: "Map of the technical documentation.", keywords: "docs index start" }],
  },
  {
    label: "The system",
    pages: [
      { href: "/docs/business/", title: "Business", summary: "Requirements, use cases, actors and manuals.", keywords: "use cases cu rf rnf requirements" },
      { href: "/docs/modules/", title: "Modules", summary: "Module ownership and boundaries.", keywords: "frontend backend modules ownership" },
      { href: "/docs/architecture/", title: "Architecture", summary: "System context, ADRs and diagrams.", keywords: "adr hexagonal sad c4 diagrams" },
    ],
  },
  {
    label: "Quality & operations",
    pages: [
      { href: "/docs/testing/", title: "Testing", summary: "Test plan, pyramid and suites.", keywords: "tests playwright junit vitest bruno" },
      { href: "/docs/security/", title: "Security", summary: "Authentication, audit, SAST/SCA/DAST, repository protection.", keywords: "devsecops codeql zap trivy" },
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

/** The product story on the home page: anchors into one long page. */
export const PRODUCT_CHAPTERS: SitePage[] = [
  { href: "/#origin", title: "Origin", summary: "2014: a Java Swing university project.", keywords: "swing history" },
  { href: "/#versions", title: "Versions", summary: "Product cut history.", keywords: "releases" },
  { href: "/#evolution", title: "Evolution", summary: "How the stack changed.", keywords: "stack technology" },
  { href: "/#ai", title: "AI era", summary: "When AI agents joined the work.", keywords: "ai agents" },
  { href: "/#architecture", title: "Architecture", summary: "The three tiers today.", keywords: "spring next postgres" },
  { href: "/#today", title: "Today", summary: "The numbers, read from the repository.", keywords: "stats metrics" },
];

/** Static reports copied into the export by deploy-github-page.yml (not Next routes). */
export const REPORTS: { href: string; title: string; summary: string }[] = [
  { href: "/coverage/", title: "Backend coverage (JaCoCo)", summary: "Latest coverage report from main." },
  { href: "/playwright/", title: "Playwright E2E report", summary: "Latest end-to-end run." },
  { href: "/cicd-reports/", title: "CI/CD reports", summary: "Latest pipeline reports." },
];

export const FACTORY_PAGES: SitePage[] = FACTORY_ACTS.flatMap((g) => g.pages);
export const DOCS_PAGES: SitePage[] = DOCS_GROUPS.flatMap((g) => g.pages);

function norm(p: string): string {
  const path = (p || "/").split("#")[0].split("?")[0];
  return path.endsWith("/") ? path : `${path}/`;
}

export function sectionOf(pathname: string): SectionKey {
  const p = norm(pathname);
  if (p.startsWith("/ai-sdlc/")) return "factory";
  if (p.startsWith("/docs/releases/")) return "releases";
  if (p.startsWith("/docs/")) return "docs";
  return "product";
}

/** Sidebar groups for the section a path belongs to (releases live inside the docs). */
export function groupsFor(section: SectionKey): PageGroup[] {
  if (section === "factory") return FACTORY_ACTS;
  if (section === "docs" || section === "releases") return DOCS_GROUPS;
  return [{ label: "The story", pages: PRODUCT_CHAPTERS }];
}

export function findPage(pathname: string): SitePage | undefined {
  const p = norm(pathname);
  return [...FACTORY_PAGES, ...DOCS_PAGES].find((x) => x.href === p);
}

export function prevNext(pathname: string): { prev?: SitePage; next?: SitePage } {
  const p = norm(pathname);
  const list = sectionOf(p) === "factory" ? FACTORY_PAGES : DOCS_PAGES;
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
  if (!root || section === "product") return [];
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
  ...DOCS_PAGES.map((p) => ({ ...p, section: "Technical docs" })),
  ...PRODUCT_CHAPTERS.map((p) => ({ ...p, section: "Product story" })),
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
  const section = sectionOf(href) === "factory" ? "AI Factory" : "Technical docs";
  const isRoot = page.href === "/ai-sdlc/" || page.href === "/docs/";
  return { title: isRoot ? section : `${page.title} — ${section}`, description: page.summary };
}
