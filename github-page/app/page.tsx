import Image from "next/image";
import Link from "next/link";
import { FlowDiagram } from "@/components/diagrams/FlowDiagram";
import { loadFactoryFacts } from "@/lib/ai-sdlc-facts";
import { factoryFlow } from "@/lib/diagram-flows";
import { ASSET_BASE, DEMO, PRIMARY_NAV, REPO_URL } from "@/lib/site";
import { fact, loadAbout } from "@/lib/about";
import { hoursText, loadOutcomes, pctText } from "@/lib/outcomes";
import { migrations, openapi, services, testCounts, versions } from "@/lib/tech-facts";

const SCREENSHOTS = [
  { file: "dashboard", title: "Dashboard", caption: "Today's activity and where each case is in its workflow." },
  { file: "cases", title: "Cases", caption: "Notarial cases with their procedures and status." },
  { file: "administration", title: "Administration", caption: "Catalogues, templates, roles and permissions." },
  { file: "audit", title: "Audit log", caption: "Reads of personal data are recorded." },
];

const n = (x: number | null | undefined) => (x === null || x === undefined ? "—" : x.toLocaleString("en-US"));

/**
 * The landing page (#1976, #1977): who built this and what it proves, in one screen; then the
 * factory, the before/after, the product running, and one door per section. Every number is read
 * from the repository when the site is built.
 */
export default function Home() {
  const v = versions();
  const api = openapi();
  const tests = testCounts();
  const factory = loadFactoryFacts();
  const svc = services();
  const list = migrations();
  const blurb = (key: string) => PRIMARY_NAV.find((x) => x.key === key)?.blurb ?? "";

  const proof = [
    { value: n(factory.archivedChanges), label: "changes shipped through OpenSpec", href: "/ai-sdlc/openspec/" },
    { value: n(tests.backend + tests.frontend), label: "backend and frontend test files", href: "/docs/testing/" },
    { value: n(tests.bruno), label: "Bruno API requests", href: "/docs/api/" },
    { value: n(tests.e2e), label: "Playwright E2E specs", href: "/docs/testing/" },
    { value: n(factory.adrs), label: "architecture decision records", href: "/docs/architecture/" },
    { value: n(factory.skills), label: "agent skills in the repo", href: "/ai-sdlc/agents/" },
  ];

  const takeaways = [
    {
      title: "Legacy modernisation",
      body: "A coupled desktop monolith became a hexagonal REST API and a typed web app, slice by slice, each slice behind its own tests.",
      href: "/docs/hexagonal/",
      link: "The hexagon",
    },
    {
      title: "AI with guardrails",
      body: "Agents write most of the code, but every change passes the same machine-checked gates: a real issue, a spec, failing tests first, a green local pipeline, and a reviewed merge.",
      href: "/ai-sdlc/guardrails/",
      link: "The guardrails",
    },
    {
      title: "Production engineering",
      body: `A versioned schema, a CI-checked API contract, ${n(factory.workflows)} CI workflows, security scanning and a ${svc.observability.length}-service observability and code-quality stack.`,
      href: "/docs/stack/",
      link: "The stack",
    },
  ];

  const before_after: [string, string, string][] = [
    ["Application", "Java 1.6 Swing desktop monolith: UI, rules and SQL in one class", `Spring Boot ${v.springBoot ?? "—"} on Java ${v.java ?? "—"}, ports and adapters`],
    ["User interface", "Swing forms", `Next.js ${v.next ?? "—"}, React ${v.react ?? "—"}, TypeScript`],
    ["Database", "MySQL 5, schema not versioned", `PostgreSQL ${v.postgres ?? "—"}, ${list.length} Flyway migrations`],
    ["API", "none", `${n(api?.paths)} paths in a committed, CI-checked OpenAPI contract`],
    ["Passwords", "MD5", "BCrypt (V52)"],
    ["Tests", "none", `${n(tests.backend)} JUnit classes, ${n(tests.frontend)} Vitest files, ${n(tests.bruno)} Bruno requests, ${n(tests.e2e)} E2E specs`],
    ["Delivery", "no containers, manual builds", `Docker Compose, ${n(factory.workflows)} GitHub Actions workflows, signed images`],
  ];

  const doors = [
    { href: "/product/", eyebrow: "Product", title: "What Notaire does", body: blurb("product"), facts: [`v${v.product ?? "—"}`, `${n(api?.paths)} API paths`, `${list.length} migrations`] },
    { href: "/docs/", eyebrow: "Technical docs", title: "How it is built", body: blurb("docs"), facts: [`Java ${v.java ?? "—"}`, `Spring Boot ${v.springBoot ?? "—"}`, `Next.js ${v.next ?? "—"}`] },
    { href: "/ai-sdlc/", eyebrow: "AI Factory", title: "How AI agents ship it", body: blurb("factory"), facts: [`${n(factory.skills)} skills`, `${n(factory.agents)} agents`, `${n(factory.livingSpecs)} living specs`] },
    { href: "/security/", eyebrow: "Security", title: "How the record is protected", body: blurb("security"), facts: ["STRIDE", "local gates", "release scans"] },
  ];

  const f = factoryFlow();
  const out = loadOutcomes();
  const outcomes = [
    { value: hoursText(out.lead_time_hours.median), label: "median PR lead time, opened to merged" },
    { value: hoursText(out.lead_time_hours.p90), label: "p90 PR lead time" },
    { value: String(out.throughput.median_per_week ?? "n/a"), label: "merged PRs in a median week" },
    { value: pctText(out.change_failure.rate_pct), label: "of merged PRs were reverts or hotfixes" },
    { value: n(out.issues_closed), label: "issues closed" },
    { value: n(out.releases.count), label: "releases" },
  ];
  const about = loadAbout();
  const role = fact("On this project");
  const contacts = about.facts.filter((x) => x.href);

  return (
    <div className="pb-24">
      <section aria-labelledby="hero-title" className="grid-pattern border-b border-slate-200">
        <div className="mx-auto max-w-6xl px-4 py-16 sm:px-6 sm:py-24">
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-brand-700">Portfolio project · Matías Miguez</p>
          <h1 id="hero-title" className="mt-4 max-w-4xl font-display text-4xl font-semibold leading-[1.05] tracking-tight text-slate-900 sm:text-6xl">
            A 2014 university project, rebuilt with AI agents that <span className="grad-cyan">cannot skip a step</span>.
          </h1>
          <p className="mt-6 max-w-2xl text-lg leading-relaxed text-slate-700">
            Notaire manages a notary office: people, properties, cases, deeds, quotes and payments. One engineer
            moved it from a Java Swing monolith to a Spring Boot and Next.js platform, using AI agents for most of the
            code and a gated SDLC that checks every change with tests, specs and CI.
          </p>
          <div className="mt-8 flex flex-wrap gap-3">
            <Link href="/ai-sdlc/" className="rounded-full bg-brand-700 px-6 py-3 text-sm font-semibold text-white transition-colors duration-200 hover:bg-brand-800 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-700">
              How the AI factory works
            </Link>
            <Link href="/ai-sdlc/case-study/" className="rounded-full border border-slate-300 bg-white px-6 py-3 text-sm font-semibold text-slate-800 transition-colors duration-200 hover:border-brand-500">
              Read the case study
            </Link>
            <a href={`${ASSET_BASE}${DEMO.href}`} className="rounded-full border border-slate-300 bg-white px-6 py-3 text-sm font-semibold text-slate-800 transition-colors duration-200 hover:border-brand-500">
              Try the live demo
            </a>
            <a href={REPO_URL} className="rounded-full border border-slate-300 bg-white px-6 py-3 text-sm font-semibold text-slate-800 transition-colors duration-200 hover:border-brand-500">
              Code on GitHub ↗
            </a>
          </div>
          <ul className="mt-12 grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6" aria-label="Counted from the repository">
            {proof.map((p) => (
              <li key={p.label}>
                <Link href={p.href} className="block h-full rounded-2xl border border-slate-200 bg-white/90 p-4 transition-colors duration-200 hover:border-brand-500">
                  <span className="block font-display text-3xl font-semibold tabular-nums text-slate-900">{p.value}</span>
                  <span className="mt-1 block text-xs leading-snug text-slate-600">{p.label}</span>
                </Link>
              </li>
            ))}
          </ul>
          <p className="mt-3 text-xs text-slate-600">Counted from the repository when this site was built.</p>
        </div>
      </section>

      <div className="mx-auto max-w-6xl px-4 sm:px-6">
        <section aria-labelledby="takeaways-title" className="pt-16">
          <h2 id="takeaways-title" className="font-display text-3xl font-semibold text-slate-900">What this project shows</h2>
          <div className="mt-6 grid gap-4 md:grid-cols-3">
            {takeaways.map((t) => (
              <div key={t.title} className="flex flex-col rounded-3xl border border-slate-200 bg-white p-6 shadow-[var(--shadow-card)]">
                <h3 className="font-semibold text-slate-900">{t.title}</h3>
                <p className="mt-2 flex-1 text-sm leading-relaxed text-slate-700">{t.body}</p>
                <Link href={t.href} className="mt-4 text-sm font-semibold text-brand-700 hover:underline">
                  {t.link} →
                </Link>
              </div>
            ))}
          </div>
        </section>

        <section aria-labelledby="factory-title" className="pt-16">
          <h2 id="factory-title" className="font-display text-3xl font-semibold text-slate-900">How every change ships</h2>
          <p className="mt-3 max-w-3xl text-slate-700">
            Humans and agents take the same path. A gate opens only when its command exits as expected; a refusal sends
            the change back. Step through it.
          </p>
          <FlowDiagram
            id="factory"
            title="The AI SDLC factory"
            desc="A change goes from a real issue through five gates: plan, failing tests, the local pipeline, CI and review, then release and smoke test. A failed gate sends it back."
            caption="Gate checks are quoted from the AI SDLC content of this site; each has its own page under AI Factory."
            {...f}
          />
        </section>

        <section aria-labelledby="before-title" className="pt-16">
          <h2 id="before-title" className="font-display text-3xl font-semibold text-slate-900">Before and after</h2>
          <div className="mt-6 overflow-x-auto rounded-2xl border border-slate-200 bg-white" tabIndex={0} aria-label="Before and after, scrollable">
            <table className="min-w-full text-sm">
              <thead className="bg-slate-50 text-left text-slate-700">
                <tr>
                  <th scope="col" className="px-4 py-3 font-semibold">Area</th>
                  <th scope="col" className="px-4 py-3 font-semibold">2014</th>
                  <th scope="col" className="px-4 py-3 font-semibold">Today</th>
                </tr>
              </thead>
              <tbody>
                {before_after.map(([area, then, now]) => (
                  <tr key={area} className="border-t border-slate-100 align-top">
                    <th scope="row" className="px-4 py-3 text-left font-medium text-slate-900">{area}</th>
                    <td className="px-4 py-3 text-slate-600">{then}</td>
                    <td className="px-4 py-3 text-slate-900">{now}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <p className="mt-2 text-xs text-slate-600">
            The 2014 column is from the project README; today&apos;s values are read from the repository. The full
            story is in <Link href="/product/story/" className="underline">Product · Story</Link>.
          </p>
        </section>

        <section aria-labelledby="machines-title" className="pt-16">
          <h2 id="machines-title" className="font-display text-3xl font-semibold text-slate-900">How it works, drawn as machines</h2>
          <p className="mt-3 max-w-3xl text-slate-700">
            Three illustrated, step-through machines explain the system from the basics: what the office does and how a request travels, how
            development, QA, security and operations run as one loop, and how the AI SDLC lets agents build a feature without skipping a gate.
          </p>
          <div className="mt-6 grid gap-4 md:grid-cols-3">
            {[
              { href: "/product/anatomy/", title: "The notary's counter", body: "One request, end to end: browser, API, rules, database, audit and monitoring." },
              { href: "/docs/engineering/", title: "The release line", body: "Dev, QA, security, gates, environments and operations on one conveyor." },
              { href: "/ai-sdlc/anatomy/", title: "The lock chamber", body: "Five gates, three roles and one rulebook between an agent and main." },
            ].map((m) => (
              <Link key={m.href} href={m.href} className="flex h-full flex-col rounded-3xl border border-slate-200 bg-white p-6 shadow-[var(--shadow-card)] transition-colors duration-200 hover:border-brand-500">
                <h3 className="font-semibold text-slate-900">{m.title}</h3>
                <p className="mt-2 flex-1 text-sm leading-relaxed text-slate-700">{m.body}</p>
                <span className="mt-4 text-sm font-semibold text-brand-700">Step through it →</span>
              </Link>
            ))}
          </div>
        </section>

        <section aria-labelledby="shots-title" className="pt-16">
          <h2 id="shots-title" className="font-display text-3xl font-semibold text-slate-900">The product, running</h2>
          <p className="mt-3 max-w-3xl text-slate-700">
            Screens of the Next.js app on the local Docker stack with demo data. Or{" "}
            <a href={`${ASSET_BASE}${DEMO.href}`}>open the live demo</a>: the same app, read-only, with sample data.
          </p>
          <ul className="mt-6 grid gap-6 md:grid-cols-2">
            {SCREENSHOTS.map((s) => (
              <li key={s.file}>
                <figure className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-[var(--shadow-card)]">
                  <Image
                    src={`${ASSET_BASE}/screenshots/${s.file}.webp`}
                    alt={`Notaire ${s.title} screen: ${s.caption}`}
                    width={1280}
                    height={800}
                    className="h-auto w-full border-b border-slate-200"
                    sizes="(min-width: 768px) 50vw, 100vw"
                  />
                  <figcaption className="px-4 py-3 text-sm">
                    <span className="font-semibold text-slate-900">{s.title}</span>
                    <span className="text-slate-600"> · {s.caption}</span>
                  </figcaption>
                </figure>
              </li>
            ))}
          </ul>
        </section>

        <section aria-labelledby="outcomes-title" className="pt-16">
          <h2 id="outcomes-title" className="font-display text-3xl font-semibold text-slate-900">Delivery outcomes</h2>
          <p className="mt-3 max-w-3xl text-slate-700">
            From GitHub, {out.window.from} to {out.window.to}, Dependabot PRs excluded.{" "}
            <Link href="/ai-sdlc/results/#outcomes">How each number is computed</Link>.
          </p>
          <ul className="mt-6 grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6">
            {outcomes.map((o) => (
              <li key={o.label} className="rounded-2xl border border-slate-200 bg-white p-4">
                <span className="block font-display text-3xl font-semibold tabular-nums text-slate-900">{o.value}</span>
                <span className="mt-1 block text-xs leading-snug text-slate-600">{o.label}</span>
              </li>
            ))}
          </ul>
        </section>

        <section aria-labelledby="sections-title" className="pt-16">
          <h2 id="sections-title" className="font-display text-3xl font-semibold text-slate-900">Explore</h2>
          <div className="mt-6 grid gap-4 md:grid-cols-2 xl:grid-cols-4">
            {doors.map((d) => (
              <Link
                key={d.href}
                href={d.href}
                className="group flex flex-col rounded-3xl border border-slate-200 bg-white p-6 shadow-[var(--shadow-card)] transition-[border-color,transform] duration-200 hover:-translate-y-0.5 hover:border-brand-500 motion-reduce:transform-none"
              >
                <span className="text-xs font-semibold uppercase tracking-[0.2em] text-brand-700">{d.eyebrow}</span>
                <span className="mt-2 font-display text-2xl font-semibold text-slate-900">{d.title}</span>
                <span className="mt-2 flex-1 text-slate-600">{d.body}</span>
                <span className="mt-5 flex flex-wrap gap-2">
                  {d.facts.map((x) => (
                    <span key={x} className="rounded-full bg-slate-100 px-2.5 py-0.5 font-mono text-xs text-slate-700">
                      {x}
                    </span>
                  ))}
                </span>
                <span className="mt-5 text-sm font-semibold text-brand-700 group-hover:underline">Open {d.eyebrow} →</span>
              </Link>
            ))}
          </div>
        </section>

        <section aria-labelledby="about-title" className="pt-16">
          <h2 id="about-title" className="font-display text-3xl font-semibold text-slate-900">About the author</h2>
          <div className="mt-6 rounded-3xl border border-slate-200 bg-white p-6 shadow-[var(--shadow-card)]">
            <p className="font-display text-2xl font-semibold text-slate-900">{about.name}</p>
            {role && <p className="mt-2 max-w-3xl text-slate-700">{role.value}</p>}
            <p className="mt-4 flex flex-wrap gap-x-5 gap-y-2 text-sm font-semibold">
              {contacts.map((c) => (
                <a key={c.label} href={c.href}>
                  {c.label} ↗
                </a>
              ))}
              <Link href="/about/">More about him, with sources</Link>
            </p>
          </div>
        </section>
      </div>
    </div>
  );
}
