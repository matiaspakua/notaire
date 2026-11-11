"use client";
import { Es } from "@/components/site/Es";
import { T } from "@/components/site/T";

const features = [
  {
    title: "AI-Driven TDD",
    desc: "Claude AI writes failing tests first, implements to pass them, then refactors. Enforced by mandatory workflow rules.",
  },
  {
    title: "Playwright E2E",
    desc: "Playwright end-to-end suites cover the critical use cases, with Bruno API tests beside them. The Product numbers page counts them from the repository.",
  },
  {
    title: "Full Observability",
    desc: "Prometheus, Grafana, Loki and SonarQube are in the local observability stack. The 2014 Swing project did not have them.",
  },
  {
    title: "Security-First",
    desc: "OWASP Top 10 guides, JWT authentication, SQL injection prevention, Trivy vulnerability scanning in CI/CD.",
  },
  {
    title: "Living Documentation",
    desc: "Use cases, functional and non-functional requirements, and ADRs live in docs/ and change in the same PR as the code. The AI Factory section counts them live.",
  },
  {
    title: "Accelerated Delivery",
    desc: "AI pairing turned the long-dormant project into a stream of small, atomic commits. What took years of manual work now happens in sprints; the Product numbers page counts the commits.",
  },
];

const PATH = ["GitHub Issue", "OpenSpec Gate 1", "TDD", "preflight.sh --full", "Merge rule", "Version tag"];

export function AIEra() {
  return (
    <Es>
      <section id="ai" className="bg-white px-6 py-24">
        <div className="mx-auto max-w-6xl">
          <div className="mb-10 max-w-3xl">
            <h2 className="font-display text-4xl font-semibold tracking-tight text-slate-900 sm:text-5xl">
              <T text="Built with Artificial Intelligence" />
            </h2>
            <p className="mt-4 leading-relaxed text-slate-600">
              <T text="In 2026, Claude AI became the primary co-developer, then Cursor Cloud agents closed the loop on remote VMs: OpenSpec, TDD, documentation, Bruno, Playwright, and preflight.sh --full before a merge." />
            </p>
          </div>
          <ol className="mb-12 flex flex-wrap gap-2">
            {PATH.map((step, index) => (
              <li key={step} className="rounded-full border border-slate-200 bg-slate-50 px-3 py-1 text-xs text-slate-700">
                <span className="font-mono text-slate-500">{index + 1}</span> {step}
              </li>
            ))}
          </ol>
          <ul className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {features.map((feature) => (
              <li key={feature.title} className="rounded-2xl border border-slate-200 bg-white p-6">
                <h3 className="font-semibold text-slate-900">{feature.title}</h3>
                <p className="mt-2 text-sm leading-relaxed text-slate-700">{feature.desc}</p>
              </li>
            ))}
          </ul>
        </div>
      </section>
    </Es>
  );
}
