import Link from "next/link";
import { AnatomyFigure } from "@/components/anatomy/AnatomyFigure";
import { systemFigure } from "@/components/anatomy/system-figure";
import { Card, Code, Diagram, PageHeader, SectionTitle, Sources, Table } from "@/components/FactoryUI";
import { src } from "@/lib/ai-sdlc-content";
import { MANAGEMENT_STATES_CHART } from "@/lib/anatomy-content";
import { pageMetadata } from "@/lib/site";
import { useCaseCount, v, versions } from "@/lib/tech-facts";

export const metadata = pageMetadata("/product/anatomy/");

const WORDS: [string, string][] = [
  ["Management (gestión)", "The case file for one procedure, such as a sale. It has a status, a history and linked documents."],
  ["Budget (presupuesto)", "The priced list of procedures offered to a client. Payments are collected against it."],
  ["Deed (escritura)", "The notarial instrument drafted for the management. It starts unsigned and needs a folio to be signed."],
  ["Testimony (testimonio)", "The authorised copy of a signed deed. It goes to the registry and can come back for correction."],
  ["Folio", "A numbered page of the notary's protocol book."],
];

export default function ProductAnatomyPage() {
  const ver = versions();
  return (
    <>
      <PageHeader
        eyebrow="Product · How it works"
        title="Notaire as a machine: one request, end to end"
        lead={
          <>
            Notaire runs a notary office. This page opens it up: first what the office does, then a drawn machine where a
            request rides a rail past the seven stations that make up the real system.
          </>
        }
      />

      <SectionTitle id="office">Start with the office</SectionTitle>
      <p>
        A client asks the office for a procedure, for example the sale of a property. The office prices it (a <strong>budget</strong>), opens a{" "}
        <strong>management</strong> to follow it, collects the required documents, drafts and signs the <strong>deed</strong>, issues the{" "}
        <strong>testimony</strong> that goes to the registry, collects the payments and archives the case. Notaire keeps each of those steps, who did
        it and when. The {useCaseCount()} written use cases describe them in detail; the{" "}
        <Link href="/product/">product overview</Link> lists who does what.
      </p>
      <Table head={["Word", "Meaning in Notaire"]} rows={WORDS} />
      <p>
        Every term used on the site is in the <Link href="/docs/glossary/">glossary</Link>.
      </p>

      <SectionTitle id="machine">The machine</SectionTitle>
      <p>
        The ticket on the rail is one request: say, a clerk saving a form. Step through it, or click a station. Each station
        is one layer of the real system, and the text under the drawing says where to find it in the repository.
      </p>
      <AnatomyFigure data={systemFigure} />

      <SectionTitle id="layers">The layers, and what each must never do</SectionTitle>
      <Table
        head={["Layer", "Built with", "Its one job", "It must never"]}
        rows={[
          ["Browser and front end", `Next.js ${v(ver.next)}, React ${v(ver.react)}, TypeScript ${v(ver.typescript)}, Tailwind CSS ${v(ver.tailwind)}`, "Show screens and send requests to /api/v1", "Hold business rules or reach the database"],
          ["API", `Spring Boot ${v(ver.springBoot)} on Java ${v(ver.java)}, springdoc OpenAPI`, "Authenticate, validate, apply rules, return JSON", "Trust a client header for identity"],
          ["Business rules", "Use cases and the domain model in the backend", "Own the notarial rules and the transactions", "Depend on HTTP types"],
          ["Persistence", `PostgreSQL ${v(ver.postgres)}, Spring Data JPA`, "Store and read records", "Contain business rules"],
          ["Schema", "Flyway migrations", "Be the only way the schema changes", "Edit a migration that already ran"],
          ["Audit", "A Spring AOP aspect", "Record who changed what, from the security context", "Read the user from the request"],
        ]}
      />
      <p>
        The backend is organised as Ports and Adapters: controllers are thin inbound adapters, use cases and domain rules sit in the
        middle, persistence is an outbound adapter. The <Link href="/docs/hexagonal/">hexagonal architecture page</Link> shows it
        with the ArchUnit rules that hold it.
      </p>
      <Sources
        items={[
          { label: "ADR-030 hexagonal", href: src("docs/200-architecture/202-ADR/ADR-030-hexagonal-architecture-full-adoption.md") },
          { label: "ADR-013 audit trail", href: src("docs/200-architecture/202-ADR/ADR-013-audit-trail.md") },
          { label: "ADR-008 authentication", href: src("docs/200-architecture/202-ADR/ADR-008-security-authentication.md") },
        ]}
      />

      <SectionTitle id="states">How a case moves</SectionTitle>
      <p>
        A management does not jump around. It follows a workflow the office can redesign in the app: a status changes only through a
        transition request or through archiving, and every change writes the history. The standard workflow for a sale is:
      </p>
      <Diagram chart={MANAGEMENT_STATES_CHART} caption="The standard management workflow for a sale. Payment and the testimony movement are separate state machines." />
      <Sources
        items={[
          { label: "CICLO-DE-VIDA.md", href: src("docs/200-architecture/203-design/CICLO-DE-VIDA.md") },
          { label: "GESTION.md", href: src("docs/200-architecture/203-design/GESTION.md") },
          { label: "ADR-014 workflow engine", href: src("docs/200-architecture/202-ADR/ADR-014-workflow-engine.md") },
        ]}
      />

      <SectionTitle id="next">Where to go next</SectionTitle>
      <div className="grid gap-3 sm:grid-cols-3">
        <Card>
          <h3 className="font-semibold text-slate-900">How it is built and run</h3>
          <p className="mt-2 text-sm text-slate-600">Development, QA, security and operations as one loop, with the environments under it.</p>
          <p className="mt-3 text-sm"><Link href="/docs/engineering/">Engineering anatomy →</Link></p>
        </Card>
        <Card>
          <h3 className="font-semibold text-slate-900">How it is built by agents</h3>
          <p className="mt-2 text-sm text-slate-600">The rulebook, the five gates and the three roles behind every change.</p>
          <p className="mt-3 text-sm"><Link href="/ai-sdlc/anatomy/">AI SDLC anatomy →</Link></p>
        </Card>
        <Card>
          <h3 className="font-semibold text-slate-900">Every structure in detail</h3>
          <p className="mt-2 text-sm text-slate-600">Modules, decisions and the data model, with their sources.</p>
          <p className="mt-3 text-sm"><Link href="/docs/architecture/">Architecture →</Link> <Code>docs/</Code></p>
        </Card>
      </div>
    </>
  );
}
