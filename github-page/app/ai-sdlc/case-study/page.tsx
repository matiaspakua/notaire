import Link from "next/link";
import { src } from "@/lib/ai-sdlc-content";
import { fmt, loadFactoryFacts } from "@/lib/ai-sdlc-facts";
import { Card, Diagram, PageHeader, SectionTitle, Sources } from "@/components/FactoryUI";

const BEFORE_AFTER = [
  {
    before: "Agents invent Issue numbers or skip OpenSpec Gate 1.",
    after: "validate-sdlc-plan.sh and archive hygiene fail the PR until the ledger is real.",
  },
  {
    before: "Scaffolded OpenSpec Markdown fails markdownlint every branch.",
    after: "Templates + scaffold emit lint-clean files; archive gets a documented autofix (#1689).",
  },
  {
    before: "Light CI (~12 checks) looks green; heavy suites still pending.",
    after: "check-heavy-ci.sh + CI-MERGE-GATE: Foreman merges only when heavy CI is terminal success.",
  },
  {
    before: "Process welded to Maven/Next paths — cannot lift to another product.",
    after: "framework/ai-sdlc kit with profiles; Notaire is the first adopter profile (#1958).",
  },
];

const EXAMPLES = [
  { title: "UX uplift epic #1941", body: "Seven waves, each with OpenSpec → TDD → run_pipeline → PR, then post-UX AI SDLC iteration." },
  { title: "Hexagonal fleet #1693", body: "Fleet lessons encode steward cadence, one-domain slices, and Mac≠CI — written back into permanent docs." },
  { title: "Scaffold lint #1689", body: "Gate 1 generators stopped inventing MD022/MD032/MD040 debt; workspace tests lock it." },
];

const NARRATIVE = `
flowchart LR
  P[Problem: AI agents ship without a factory] --> S[Solution: Constitution + specs + mechanical gates]
  S --> K[Kit: framework/ai-sdlc profiles]
  K --> C[Case study + metrics on GitHub Pages]
`;

export default function CaseStudyPage() {
  const f = loadFactoryFacts();
  const metrics = [
    { label: "skills", value: fmt(f.skills) },
    { label: "agents", value: fmt(f.agents) },
    { label: "archived OpenSpec changes", value: fmt(f.archivedChanges) },
    { label: "living specs", value: fmt(f.livingSpecs) },
    { label: "sdlc scripts", value: fmt(f.sdlcScripts) },
    { label: "CI workflows", value: fmt(f.workflows) },
  ];

  return (
    <div>
      <PageHeader
        eyebrow="AI Factory · Case study"
        title="From Notaire evidence to a reusable AI SDLC"
        lead={
          <>
            Portfolio narrative for issue{" "}
            <a className="text-violet-700 hover:underline" href="https://github.com/matiaspakua/notaire/issues/1958">
              #1958
            </a>
            : the problem, the factory that grew inside this monorepo, the stack-agnostic kit, and what another product
            should copy first.
          </>
        }
      />

      <SectionTitle>Problem → solution</SectionTitle>
      <p className="text-slate-600 max-w-3xl leading-relaxed">
        Coding agents can edit a repository quickly and still leave undocumentable, untested, unmerged work. Notaire&apos;s
        answer was not a bigger prompt: it was a <strong>written Constitution</strong>, a spec-driven Gate 1, red-before-green
        tests, layered local/CI gates, and a human Owner on irreversible decisions. That factory is now extractable as{" "}
        <code className="text-sm bg-slate-100 px-1 rounded">framework/ai-sdlc</code> with a Notaire profile, so another product
        can adopt the process without inheriting Spring Boot or Next.js paths.
      </p>
      <Diagram caption="Case study arc" chart={NARRATIVE} />

      <SectionTitle>Workflow (what ships)</SectionTitle>
      <p className="text-slate-600 max-w-3xl leading-relaxed">
        Issue + Use Case → OpenSpec Gate 1 → failing tests → implement → permanent docs →{" "}
        <code className="text-sm bg-slate-100 px-1 rounded">run_pipeline.sh</code> → PR → heavy CI → merge → smoke → close.
        Details live on the{" "}
        <Link className="text-violet-700 hover:underline" href="/ai-sdlc/lifecycle/">
          Lifecycle
        </Link>{" "}
        and{" "}
        <Link className="text-violet-700 hover:underline" href="/ai-sdlc/quality-gates/">
          Quality gates
        </Link>{" "}
        pages.
      </p>

      <SectionTitle>Agents, skills, rules, guardrails</SectionTitle>
      <p className="text-slate-600 max-w-3xl leading-relaxed">
        Owner / Foreman / Workers, specialist agents under <code className="text-sm bg-slate-100 px-1 rounded">.claude/agents</code>,
        skills indexed in <code className="text-sm bg-slate-100 px-1 rounded">.claude/skills/README.md</code> (including{" "}
        <code className="text-sm bg-slate-100 px-1 rounded">ui-design</code>), and nine guardrail layers from Constitution to Owner
        approval. See{" "}
        <Link className="text-violet-700 hover:underline" href="/ai-sdlc/agents/">
          Agents &amp; skills
        </Link>{" "}
        and{" "}
        <Link className="text-violet-700 hover:underline" href="/ai-sdlc/guardrails/">
          Guardrails
        </Link>
        .
      </p>

      <SectionTitle>Real examples</SectionTitle>
      <div className="grid md:grid-cols-3 gap-3">
        {EXAMPLES.map((e) => (
          <Card key={e.title}>
            <h3 className="font-semibold text-slate-900">{e.title}</h3>
            <p className="mt-2 text-sm text-slate-600 leading-relaxed">{e.body}</p>
          </Card>
        ))}
      </div>

      <SectionTitle>Before / after</SectionTitle>
      <div className="space-y-3">
        {BEFORE_AFTER.map((row) => (
          <Card key={row.before} className="grid md:grid-cols-2 gap-4">
            <div>
              <p className="text-xs uppercase tracking-wide text-slate-400 font-semibold">Before</p>
              <p className="mt-1 text-sm text-slate-700">{row.before}</p>
            </div>
            <div>
              <p className="text-xs uppercase tracking-wide text-emerald-700 font-semibold">After</p>
              <p className="mt-1 text-sm text-slate-700">{row.after}</p>
            </div>
          </Card>
        ))}
      </div>

      <SectionTitle>Lessons</SectionTitle>
      <ul className="list-disc pl-5 space-y-2 text-sm text-slate-700 max-w-3xl">
        <li>Mechanical exit codes beat aspirational rules.</li>
        <li>Serialize expensive CI; Mac green ≠ CI green.</li>
        <li>Write incidents back into permanent docs (fleet lessons), not chat history.</li>
        <li>Keep the kit stack-agnostic; put product paths only in an adopter profile.</li>
      </ul>
      <Sources
        items={[
          { label: "FLEET-LESSONS-HEX-1693.md", href: src("docs/300-development/304-ai-sdlc-cloud/FLEET-LESSONS-HEX-1693.md") },
          { label: "ai_sdlc_framework.md", href: src("ai_sdlc_framework.md") },
          { label: "framework/ai-sdlc/README.md", href: src("framework/ai-sdlc/README.md") },
        ]}
      />

      <SectionTitle>Metrics (build-time)</SectionTitle>
      <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
        {metrics.map((m) => (
          <Card key={m.label} className="text-center">
            <div className="text-3xl font-semibold text-slate-900 tabular-nums">{m.value}</div>
            <div className="mt-1 text-xs text-slate-500">{m.label}</div>
          </Card>
        ))}
      </div>
      <p className="mt-2 text-xs text-slate-400">Counted from the repository when this site was built.</p>

      <div className="mt-10 flex flex-wrap gap-3">
        <Link href="/ai-sdlc/playbook/" className="rounded-full bg-slate-900 px-5 py-2 text-sm font-semibold text-white hover:bg-violet-800">
          Apply the playbook →
        </Link>
        <Link href="/ai-sdlc/" className="rounded-full border border-slate-300 px-5 py-2 text-sm font-semibold text-slate-800 hover:bg-slate-50">
          Back to overview
        </Link>
      </div>
    </div>
  );
}
