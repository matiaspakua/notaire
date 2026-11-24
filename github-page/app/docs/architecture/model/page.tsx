import Link from "next/link";

import { Code, PageHeader, SectionTitle, Sources, Table } from "@/components/FactoryUI";
import { ModelView } from "@/components/docs/ModelView";
import { src } from "@/lib/ai-sdlc-content";
import { LIKEC4_VERSION, MODEL_FINDINGS, MODEL_GROUPS, MODEL_SITE_HREF } from "@/lib/architecture-model";
import { pageMetadata } from "@/lib/site";
import { HowList } from "./HowList";

export const metadata = pageMetadata("/docs/architecture/model/");

const HOW = [
  "The model lives in docs/200-architecture/210-architecture-model (ADR-033), one file per concern.",
  "npm run generate turns each computed view into Mermaid for the Markdown docs and for this page. npm run check and a docs test fail when a copy drifts.",
  "The Pages build publishes the interactive LikeC4 site next to this page.",
];

function issueNumber(url: string): string {
  return `#${url.split("/").at(-1)}`;
}

export default function ArchitectureModelPage() {
  return (
    <>
      <PageHeader
        eyebrow="Architecture"
        title="Architecture model"
        lead="One LikeC4 model, written from the code, draws every architecture view on this site and in the repository docs. A gap found while writing it is a red finding with its GitHub Issue."
      />

      <p>
        <Link
          href={MODEL_SITE_HREF}
          prefetch={false}
          className="font-semibold text-brand-700 underline decoration-brand-700/30 underline-offset-2 hover:decoration-current"
        >
          Open the interactive model
        </Link>{" "}
        <span className="text-sm text-slate-600">
          LikeC4 <Code>{LIKEC4_VERSION}</Code>
        </span>
      </p>

      <SectionTitle id="findings">Findings</SectionTitle>
      <p>What the modelling pass found that the code or the deployment still gets wrong.</p>
      <Table
        head={["Finding", "Detail", "Issue"]}
        rows={MODEL_FINDINGS.map((f) => [
          f.title,
          f.description,
          <a key={f.id} href={f.issue} className="text-brand-700 underline">
            {issueNumber(f.issue)}
          </a>,
        ])}
      />

      {MODEL_GROUPS.map((group) => (
        <section key={group.label} className="space-y-6">
          <SectionTitle id={group.label.toLowerCase().replace(/\W+/g, "-")}>{group.label}</SectionTitle>
          {group.views.map((id) => (
            <ModelView key={id} id={id} />
          ))}
        </section>
      ))}

      <SectionTitle id="how">How the views are made</SectionTitle>
      <HowList steps={HOW} />
      <Sources
        items={[
          { label: "Model README", href: src("docs/200-architecture/210-architecture-model/README.md") },
          { label: "ADR-033", href: src("docs/200-architecture/202-ADR/ADR-033-likec4-architecture-model.md") },
          { label: "SAD", href: src("docs/200-architecture/201-SAD/sad.md") },
        ]}
      />
    </>
  );
}
