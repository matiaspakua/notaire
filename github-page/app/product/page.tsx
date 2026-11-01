import Link from "next/link";
import { Card, PageHeader, SectionTitle, Sources } from "@/components/FactoryUI";
import { StatGrid } from "@/components/DocsUI";
import { src } from "@/lib/ai-sdlc-content";
import { pageMetadata } from "@/lib/site";
import { openapi, productModules, testCounts, useCaseCount, versions } from "@/lib/tech-facts";

export const metadata = pageMetadata("/product/");

/** Staff roles from docs/100-business/103-actors (Spanish names kept in brackets). */
const ROLES = [
  { role: "Notary", es: "Escribano", does: "Runs the cases, drafts and signs deeds and certificates, manages the folios." },
  { role: "Receptionist", es: "Recepcionista", does: "Answers questions about cases, prepares quotes and registers people and clients." },
  { role: "Case manager", es: "Gestor", does: "Starts each case and follows it through documents, registrations and payments." },
  { role: "Administrator", es: "Administrador", does: "Configures catalogs, templates, users, roles, substitutions and reads the audit trail." },
];

/** The main path of a case, as written in the use cases (docs/100-business/102-use-cases). */
const CASE_FLOW = [
  ["CU01", "Prepare a quote"],
  ["CU02", "Start the case"],
  ["CU03", "List the documents and certificates it needs"],
  ["CU04", "Register the client's documents"],
  ["CU05", "Prepare and sign the deed"],
  ["CU07", "Issue the testimony"],
  ["CU11", "Submit it for registration"],
  ["CU15", "Process the payment"],
  ["CU16", "Archive the case"],
];

export default function ProductOverviewPage() {
  const modules = productModules();
  const screens = modules.reduce((n, g) => n + g.items.length, 0);
  const api = openapi();
  const tests = testCounts();
  const v = versions();

  return (
    <>
      <PageHeader
        eyebrow="Product"
        title="Case management for a notary office"
        lead={
          <>
            Notaire runs the daily work of an Argentine notary office (<em>escribanía</em>): the people and properties
            involved, each case from quote to archive, the deeds and their testimonies, payments, deadlines and an
            audit trail. It is a web app — a Next.js front end over a Spring Boot API and PostgreSQL — in English and
            Spanish.
          </>
        }
      />

      <StatGrid
        items={[
          { value: useCaseCount(), label: "Use cases", hint: "documented in docs/100-business" },
          { value: screens, label: "App areas", hint: `in ${modules.length} menu sections` },
          { value: api?.paths ?? "—", label: "API paths", hint: "in the OpenAPI contract" },
          { value: v.product ?? "—", label: "Product version", hint: "from pom.xml" },
        ]}
      />

      <SectionTitle id="who">Who uses it</SectionTitle>
      <div className="grid gap-3 sm:grid-cols-2">
        {ROLES.map((r) => (
          <Card key={r.role}>
            <p className="font-semibold text-slate-900">
              {r.role} <span className="text-sm font-normal text-slate-500">({r.es})</span>
            </p>
            <p className="mt-1 text-sm text-slate-600">{r.does}</p>
          </Card>
        ))}
      </div>
      <Sources items={[{ label: "docs/100-business/103-actors/", href: src("docs/100-business/103-actors/") }]} />

      <SectionTitle id="case">How a case moves</SectionTitle>
      <p>
        Every case follows a status workflow that the office can redesign in the app (ADR-014); the default path is
        the one the use cases describe:
      </p>
      <ol className="my-6 grid gap-2 sm:grid-cols-3">
        {CASE_FLOW.map(([cu, step], i) => (
          <li key={cu} className="flex items-start gap-3 rounded-xl border border-slate-200 bg-white p-3">
            <span aria-hidden="true" className="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-[var(--accent-500)]/10 font-mono text-xs font-semibold text-[var(--accent-700)]">
              {i + 1}
            </span>
            <span className="text-sm text-slate-700">
              {step} <span className="font-mono text-xs text-slate-500">{cu}</span>
            </span>
          </li>
        ))}
      </ol>
      <Sources
        items={[
          { label: "docs/100-business/102-use-cases/", href: src("docs/100-business/102-use-cases/") },
          { label: "ADR-014 workflow engine", href: src("docs/200-architecture/202-ADR/ADR-014-workflow-engine.md") },
        ]}
      />

      <SectionTitle id="areas">What is in the app</SectionTitle>
      <p>The app&apos;s menu, read from the front end at build time — each entry is a screen with its own tests.</p>
      <div className="my-6 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
        {modules.map((g) => (
          <Card key={g.group}>
            <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">{g.group}</p>
            <ul className="mt-2 space-y-1 text-sm text-slate-700">
              {g.items.map((it) => (
                <li key={it.href}>
                  {it.label}
                  {it.adminOnly && <span className="ml-2 rounded bg-slate-100 px-1.5 py-0.5 text-[11px] text-slate-700">admin</span>}
                </li>
              ))}
            </ul>
          </Card>
        ))}
      </div>
      <Sources items={[{ label: "frontend/src/lib/dashboard-nav.ts", href: src("frontend/src/lib/dashboard-nav.ts") }, { label: "frontend/messages/en.json", href: src("frontend/messages/en.json") }]} />

      <SectionTitle id="quality">How it is kept working</SectionTitle>
      <p>
        {tests.backend} backend test classes, {tests.frontend} front-end test files, {tests.e2e} Playwright specs and{" "}
        {tests.bruno} Bruno API requests run in CI before anything reaches <code>main</code>. The{" "}
        <Link href="/docs/testing/">testing docs</Link> explain the pyramid; the{" "}
        <Link href="/ai-sdlc/">AI Factory</Link> explains the process that writes and checks them.
      </p>
    </>
  );
}
