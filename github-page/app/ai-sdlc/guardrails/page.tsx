import { GUARDRAILS, GUARDRAIL_LAYERS, NEVER_LIST, src } from "@/lib/ai-sdlc-content";
import { Card, Diagram, PageHeader, SectionTitle, Sources } from "@/components/FactoryUI";

export default function GuardrailsPage() {
  return (
    <div>
      <PageHeader
        eyebrow="AI Factory · Guardrails"
        title="Nine layers between an agent and main"
        lead={
          <>
            No single control is trusted. Policy reaches the agent through its context. Skills narrow what it does. Hooks
            and harness guards stop it locally. CI tests both the code and the process, the repository refuses what CI did
            not pass, and a human owns the decisions that matter. Every layer is a file you can read.
          </>
        }
      />
      <Diagram chart={GUARDRAIL_LAYERS} caption="Purple: policy and context. Blue: mechanical enforcement. Red: the human Owner." />

      <SectionTitle>Layer by layer</SectionTitle>
      <div className="space-y-4">
        {GUARDRAILS.map((g) => (
          <Card key={g.layer}>
            <h3 className="font-semibold text-slate-900">{g.layer}</h3>
            <p className="mt-2 text-sm text-slate-600 leading-relaxed">{g.what}</p>
            <Sources items={g.sources} />
          </Card>
        ))}
      </div>

      <SectionTitle>What an agent may never do</SectionTitle>
      <Card className="border-red-200 bg-red-50/40">
        <ul className="grid md:grid-cols-2 gap-2 text-sm text-slate-800">
          {NEVER_LIST.map((n) => (
            <li key={n} className="flex gap-2">
              <span className="text-red-600 font-bold">✕</span>
              {n}
            </li>
          ))}
        </ul>
        <Sources items={[{ label: "CONSTITUTION.md §10 Rules for AI Agents", href: src("CONSTITUTION.md") }, { label: "CI-MERGE-GATE.md", href: src("docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md") }]} />
      </Card>

      <SectionTitle>Exceptions are rare and human</SectionTitle>
      <p className="text-sm text-slate-600 max-w-3xl leading-relaxed">
        Only emergency security hotfixes, one-time migration scripts and trivial doc typos qualify. Only the Owner grants
        one, with the <code className="font-mono text-xs">sdlc-exception</code> label. The label is valid only on a PR that
        carries no OpenSpec change, and a check enforces that. Dependency bots and the release-please PR are the only
        automatic exemptions.
      </p>
      <Sources items={[{ label: "CONSTITUTION.md §12", href: src("CONSTITUTION.md") }, { label: "check-sdlc-exception.sh", href: src("workspace/sdlc/check-sdlc-exception.sh") }]} />
    </div>
  );
}
