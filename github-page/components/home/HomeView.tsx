"use client";

import Image from "next/image";
import Link from "next/link";
import { FlowDiagram, type FlowEdge, type FlowNode, type FlowStep } from "@/components/diagrams/FlowDiagram";
import { ReadingPath } from "@/components/site/ReadingPath";
import { homeCopy } from "@/lib/i18n";
import { ASSET_BASE, DEMO, REPO_URL } from "@/lib/site";
import { usePreferences } from "@/components/site/Preferences";

export type HomeModel = {
  proof: { key: "openspec" | "tests" | "bruno" | "e2e" | "adrs" | "skills"; value: string; href: string }[];
  workflows: number | null;
  observability: number;
  before: { then: string; now: string }[];
  flow: { nodes: FlowNode[]; edges: FlowEdge[]; steps: FlowStep[]; width: number; height: number };
  shots: { file: string }[];
  outcomes: { key: "median" | "p90" | "week" | "failure" | "issues" | "releases"; value: string }[];
  outcomeWindow: { from: string; to: string };
  doors: { key: "product" | "docs" | "factory" | "security"; href: string; facts: string[] }[];
  name: string;
  role: string;
  contacts: { label: string; href: string }[];
};

const MACHINE_HREFS = ["/product/anatomy/", "/docs/engineering/", "/ai-sdlc/anatomy/"];
const TAKEAWAY_HREFS = ["/docs/hexagonal/", "/ai-sdlc/guardrails/", "/docs/stack/"];

export function HomeView({ model }: { model: HomeModel }) {
  const { locale } = usePreferences();
  const c = homeCopy(locale);
  const fill = (body: string) =>
    body.replaceAll("{workflows}", String(model.workflows ?? "—")).replaceAll("{services}", String(model.observability));

  return (
    <div className="pb-24">
      <section aria-labelledby="hero-title" className="grid-pattern border-b border-slate-200">
        <div className="mx-auto max-w-6xl px-4 py-16 sm:px-6 sm:py-24">
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-brand-700">{c.kicker}</p>
          <h1 id="hero-title" className="mt-4 max-w-4xl font-display text-4xl font-semibold leading-[1.05] tracking-tight text-slate-900 sm:text-6xl">
            {c.titleBefore}
            <span className="text-brand-700">{c.titleEm}</span>.
          </h1>
          <p className="mt-6 max-w-2xl text-lg leading-relaxed text-slate-700">{c.lead}</p>
          <div className="mt-8 flex flex-wrap gap-3">
            <Link href="/ai-sdlc/" className="rounded-full bg-brand-700 px-6 py-3 text-sm font-semibold text-white transition-colors duration-200 hover:bg-brand-800 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-700">
              {c.ctaFactory}
            </Link>
            <Link href="/ai-sdlc/case-study/" className="rounded-full border border-slate-300 bg-white px-6 py-3 text-sm font-semibold text-slate-800 transition-colors duration-200 hover:border-brand-500">
              {c.ctaCase}
            </Link>
            <a href={`${ASSET_BASE}${DEMO.href}`} className="rounded-full border border-slate-300 bg-white px-6 py-3 text-sm font-semibold text-slate-800 transition-colors duration-200 hover:border-brand-500">
              {c.ctaDemo}
            </a>
            <a href={REPO_URL} className="rounded-full border border-slate-300 bg-white px-6 py-3 text-sm font-semibold text-slate-800 transition-colors duration-200 hover:border-brand-500">
              {c.ctaRepo} ↗
            </a>
          </div>
          <ul className="mt-12 grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6" aria-label={c.countedLabel}>
            {model.proof.map((p) => (
              <li key={p.key}>
                <Link href={p.href} className="block h-full rounded-2xl border border-slate-200 bg-white/90 p-4 transition-colors duration-200 hover:border-brand-500">
                  <span className="block font-display text-3xl font-semibold tabular-nums text-slate-900">{p.value}</span>
                  <span className="mt-1 block text-xs leading-snug text-slate-600">{c.proof[p.key]}</span>
                </Link>
              </li>
            ))}
          </ul>
          <p className="mt-3 text-xs text-slate-600">{c.countedNote}</p>
        </div>
      </section>

      <div className="mx-auto max-w-6xl px-4 sm:px-6">
        <ReadingPath compact />

        <section aria-labelledby="takeaways-title" className="pt-16">
          <h2 id="takeaways-title" className="font-display text-3xl font-semibold text-slate-900">{c.takeawaysTitle}</h2>
          <div className="mt-6 grid gap-4 md:grid-cols-3">
            {c.takeaways.map((t, i) => (
              <div key={t.title} className="flex flex-col rounded-3xl border border-slate-200 bg-white p-6 shadow-[var(--shadow-card)]">
                <h3 className="font-semibold text-slate-900">{t.title}</h3>
                <p className="mt-2 flex-1 text-sm leading-relaxed text-slate-700">{i === 2 ? fill(t.body) : t.body}</p>
                <Link href={TAKEAWAY_HREFS[i]} className="mt-4 text-sm font-semibold text-brand-700 hover:underline">
                  {t.link} →
                </Link>
              </div>
            ))}
          </div>
        </section>

        <section aria-labelledby="factory-title" className="pt-16">
          <h2 id="factory-title" className="font-display text-3xl font-semibold text-slate-900">{c.factoryTitle}</h2>
          <p className="mt-3 max-w-3xl text-slate-700">{c.factoryLead}</p>
          <FlowDiagram
            id="factory"
            title={c.factoryTitle}
            desc={c.factoryLead}
            caption={c.countedNote}
            {...model.flow}
          />
        </section>

        <section aria-labelledby="before-title" className="pt-16">
          <h2 id="before-title" className="font-display text-3xl font-semibold text-slate-900">{c.beforeTitle}</h2>
          <div className="mt-6 overflow-x-auto rounded-2xl border border-slate-200 bg-white" tabIndex={0} aria-label={c.beforeTitle}>
            <table className="min-w-full text-sm">
              <thead className="bg-slate-50 text-left text-slate-700">
                <tr>
                  {c.beforeHeads.map((head) => (
                    <th key={head} scope="col" className="px-4 py-3 font-semibold">{head}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {model.before.map((row, i) => (
                  <tr key={c.beforeAreas[i]} className="border-t border-slate-100 align-top">
                    <th scope="row" className="px-4 py-3 text-left font-medium text-slate-900">{c.beforeAreas[i]}</th>
                    <td className="px-4 py-3 text-slate-600">{row.then}</td>
                    <td className="px-4 py-3 text-slate-900">{row.now}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <p className="mt-2 text-xs text-slate-600">
            {c.beforeNote.split(locale === "es" ? "Producto · Historia" : "Product · Story")[0]}
            <Link href="/product/story/" className="underline">{c.storyLink}</Link>.
          </p>
        </section>

        <section aria-labelledby="machines-title" className="pt-16">
          <h2 id="machines-title" className="font-display text-3xl font-semibold text-slate-900">{c.machinesTitle}</h2>
          <p className="mt-3 max-w-3xl text-slate-700">{c.machinesLead}</p>
          <div className="mt-6 grid gap-4 md:grid-cols-3">
            {c.machines.map((m, i) => (
              <Link key={MACHINE_HREFS[i]} href={MACHINE_HREFS[i]} className="flex h-full flex-col rounded-3xl border border-slate-200 bg-white p-6 shadow-[var(--shadow-card)] transition-colors duration-200 hover:border-brand-500">
                <h3 className="font-semibold text-slate-900">{m.title}</h3>
                <p className="mt-2 flex-1 text-sm leading-relaxed text-slate-700">{m.body}</p>
                <span className="mt-4 text-sm font-semibold text-brand-700">{c.stepThrough} →</span>
              </Link>
            ))}
          </div>
        </section>

        <section aria-labelledby="shots-title" className="pt-16">
          <h2 id="shots-title" className="font-display text-3xl font-semibold text-slate-900">{c.shotsTitle}</h2>
          <p className="mt-3 max-w-3xl text-slate-700">
            {c.shotsLeadBefore}
            <a href={`${ASSET_BASE}${DEMO.href}`}>{c.shotsLeadLink}</a>
            {c.shotsLeadAfter}
          </p>
          <ul className="mt-6 grid gap-6 md:grid-cols-2">
            {model.shots.map((s, i) => (
              <li key={s.file}>
                <figure className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-[var(--shadow-card)]">
                  <Image
                    src={`${ASSET_BASE}/screenshots/${s.file}.webp`}
                    alt={`${c.shots[i].title}: ${c.shots[i].caption}`}
                    width={1280}
                    height={800}
                    className="h-auto w-full border-b border-slate-200"
                    sizes="(min-width: 768px) 50vw, 100vw"
                  />
                  <figcaption className="px-4 py-3 text-sm">
                    <span className="font-semibold text-slate-900">{c.shots[i].title}</span>
                    <span className="text-slate-600"> · {c.shots[i].caption}</span>
                  </figcaption>
                </figure>
              </li>
            ))}
          </ul>
        </section>

        <section aria-labelledby="outcomes-title" className="pt-16">
          <h2 id="outcomes-title" className="font-display text-3xl font-semibold text-slate-900">{c.outcomesTitle}</h2>
          <p className="mt-3 max-w-3xl text-slate-700">
            {c.outcomesLead} {model.outcomeWindow.from} {c.outcomesTo} {model.outcomeWindow.to}, {c.outcomesExclude}{" "}
            <Link href="/ai-sdlc/results/#outcomes">{c.outcomesHow}</Link>.
          </p>
          <ul className="mt-6 grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6">
            {model.outcomes.map((o) => (
              <li key={o.key} className="rounded-2xl border border-slate-200 bg-white p-4">
                <span className="block font-display text-3xl font-semibold tabular-nums text-slate-900">{o.value}</span>
                <span className="mt-1 block text-xs leading-snug text-slate-600">{c.outcomes[o.key]}</span>
              </li>
            ))}
          </ul>
        </section>

        <section aria-labelledby="sections-title" className="pt-16">
          <h2 id="sections-title" className="font-display text-3xl font-semibold text-slate-900">{c.sectionsTitle}</h2>
          <div className="mt-6 grid gap-4 md:grid-cols-2 xl:grid-cols-4">
            {model.doors.map((d) => {
              const door = c.doors[d.key];
              return (
                <Link
                  key={d.href}
                  href={d.href}
                  className="group flex flex-col rounded-3xl border border-slate-200 bg-white p-6 shadow-[var(--shadow-card)] transition-[border-color,transform] duration-200 hover:-translate-y-0.5 hover:border-brand-500 motion-reduce:transform-none"
                >
                  <span className="text-xs font-semibold uppercase tracking-[0.16em] text-brand-700">{door.eyebrow}</span>
                  <span className="mt-2 font-display text-2xl font-semibold text-slate-900">{door.title}</span>
                  <span className="mt-5 flex flex-wrap gap-2">
                    {d.facts.map((x) => (
                      <span key={x} className="rounded-full bg-slate-100 px-2.5 py-0.5 font-mono text-xs text-slate-700">
                        {x}
                      </span>
                    ))}
                  </span>
                  <span className="mt-5 text-sm font-semibold text-brand-700 group-hover:underline">{door.open} →</span>
                </Link>
              );
            })}
          </div>
        </section>

        <section aria-labelledby="about-title" className="pt-16">
          <h2 id="about-title" className="font-display text-3xl font-semibold text-slate-900">{c.aboutTitle}</h2>
          <div className="mt-6 rounded-3xl border border-slate-200 bg-white p-6 shadow-[var(--shadow-card)]">
            <p className="font-display text-2xl font-semibold text-slate-900">{model.name}</p>
            {model.role && <p className="mt-2 max-w-3xl text-slate-700">{locale === "es" ? roleEs(model.role) : model.role}</p>}
            <p className="mt-4 flex flex-wrap gap-x-5 gap-y-2 text-sm font-semibold">
              {model.contacts.map((contact) => (
                <a key={contact.href} href={contact.href}>
                  {contact.label} ↗
                </a>
              ))}
              <Link href="/about/">{c.aboutMore}</Link>
            </p>
          </div>
        </section>
      </div>
    </div>
  );
}

/** Same claim as the sourced English sentence. The About page keeps the source. */
function roleEs(english: string): string {
  if (!english.startsWith("Builds and maintains Notaire")) return english;
  return "Construye y mantiene Notaire. En su AI SDLC es el Owner: aprueba las especificaciones y, como dueño del código, fusiona los cambios.";
}
