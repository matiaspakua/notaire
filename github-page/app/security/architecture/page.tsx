import { FlowDiagram } from "@/components/diagrams/FlowDiagram";
import { T } from "@/components/site/T";
import { trustBoundaryFlow } from "@/lib/diagram-flows";
import { pageMetadata } from "@/lib/site";

export const metadata = pageMetadata("/security/architecture/");

export default function SecurityArchitecturePage() {
  const flow = trustBoundaryFlow();
  return (
    <div>
      <header className="mb-8 space-y-3">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-brand-700"><T text="Security · architecture" /></p>
        <h1 className="font-display text-4xl font-semibold tracking-tight text-slate-900"><T text="Four trust boundaries" /></h1>
        <p className="max-w-3xl text-lg leading-relaxed text-slate-600">
          <T text="The browser is untrusted. The BFF proxies. The API authenticates, then checks the module grant. PostgreSQL holds the personal data and the deeds. Step through one request." />
        </p>
      </header>
      <FlowDiagram
        id="trust"
        title="Notaire trust boundaries"
        desc="A request moves from the browser through the BFF and the API filters into PostgreSQL."
        caption="Same boundaries as the PlantUML trust-boundary diagram in the architecture docs. Demo labels only."
        {...flow}
      />
    </div>
  );
}
