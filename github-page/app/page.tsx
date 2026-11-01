import Link from "next/link";
import { SmoothScroll } from "@/components/SmoothScroll";
import { Hero } from "@/components/Hero";
import { PRIMARY_NAV } from "@/lib/site";
import { loadFactoryFacts } from "@/lib/ai-sdlc-facts";
import { migrations, openapi, testCounts, versions } from "@/lib/tech-facts";

/** The landing page (#1976): the hero, then one door per section. */
export default function Home() {
  const v = versions();
  const api = openapi();
  const tests = testCounts();
  const factory = loadFactoryFacts();
  const blurb = (key: string) => PRIMARY_NAV.find((n) => n.key === key)?.blurb ?? "";

  const doors = [
    {
      href: "/product/",
      eyebrow: "Product",
      title: "What Notaire does",
      body: blurb("product"),
      facts: [`v${v.product ?? "—"}`, `${api?.paths ?? "—"} API paths`, `${migrations().length} migrations`],
    },
    {
      href: "/docs/",
      eyebrow: "Technical docs",
      title: "How it is built",
      body: blurb("docs"),
      facts: [`Java ${v.java ?? "—"}`, `Spring Boot ${v.springBoot ?? "—"}`, `Next.js ${v.next ?? "—"}`],
    },
    {
      href: "/ai-sdlc/",
      eyebrow: "AI Factory",
      title: "How AI agents ship it",
      body: blurb("factory"),
      facts: [`${factory.skills ?? "—"} skills`, `${factory.agents ?? "—"} agents`, `${tests.e2e} E2E specs`],
    },
  ];

  return (
    <SmoothScroll>
      <Hero />
      <section aria-labelledby="sections-title" className="mx-auto max-w-6xl px-4 pb-24 sm:px-6">
        <h2 id="sections-title" className="sr-only">
          Sections
        </h2>
        <div className="grid gap-4 md:grid-cols-3">
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
                {d.facts.map((f) => (
                  <span key={f} className="rounded-full bg-slate-100 px-2.5 py-0.5 font-mono text-xs text-slate-700">
                    {f}
                  </span>
                ))}
              </span>
              <span className="mt-5 text-sm font-semibold text-brand-700 group-hover:underline">Open {d.eyebrow} →</span>
            </Link>
          ))}
        </div>
      </section>
    </SmoothScroll>
  );
}
