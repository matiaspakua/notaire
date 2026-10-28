import Link from "next/link";
import {
  ADOPTION_LEVELS,
  FRAMEWORK_ARCHITECTURE,
  FRAMEWORK_COMMANDS,
  FRAMEWORK_LEVELS,
  FRAMEWORK_PRINCIPLES,
  FRAMEWORK_STAGES,
  LEARNING_LOOP,
  NOTAIRE_PROFILE_EXCERPT,
  src,
} from "@/lib/ai-sdlc-content";
import { Card, Code, Diagram, PageHeader, SectionTitle, Sources, Table } from "@/components/FactoryUI";
import { GateFlow } from "@/components/GateFlow";
import { Terminal } from "@/components/CaseStudy";

export const metadata = {
  title: "The AI SDLC framework — one policy, one profile, one kit",
  description: "A stack-agnostic AI SDLC framework extracted from Notaire: gates by exit code, ratchets, evidence and adoption levels.",
};

const QUICK_START = [
  "$ cp -R framework/ai-sdlc your-repo/tools/ai-sdlc",
  "$ python3 tools/ai-sdlc/bin/aisdlc init --name shop --level 2",
  "  created  .aisdlc/agents/foreman.md",
  "  …",
  "  created  aisdlc.toml",
  "$ python3 tools/ai-sdlc/bin/aisdlc doctor",
  "  WARN   gate red/new test fails is a placeholder",
  "  WARN   gate pre-push/test suite is a placeholder",
  "doctor: shop · level 2 · 7 gates in 3 stages · 0 ratchets · 4 roles — OK (0 errors, 4 warnings)",
  "$ python3 tools/ai-sdlc/bin/aisdlc gate pre-push --base HEAD~1",
  "  PASS  commit subjects (exit 0, 0.1s)",
  "  TODO  test suite — placeholder; configure its command in the profile",
  "stage pre-push: PASSED",
];

export default function FrameworkPage() {
  return (
    <div>
      <PageHeader
        eyebrow="AI Factory · Framework"
        title="One policy, one profile, one kit"
        lead={
          <>
            The process that builds Notaire, with Notaire taken out. A stack-agnostic AI SDLC framework: a policy
            document that names no tool, a TOML profile that holds every project-specific command, and a zero-dependency
            kit (<Code>aisdlc</Code>) that runs gates by exit code, enforces ratchets and keeps the evidence.
          </>
        }
      />
      <div className="flex flex-wrap gap-3">
        <a href={src("framework/ai-sdlc/")} className="rounded-full bg-slate-900 px-5 py-2 text-sm font-semibold text-white hover:bg-violet-800 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-violet-600">
          Get the kit ↗
        </a>
        <a href={src("docs/300-development/306-ai-sdlc-framework/ai_sdlc_framework.md")} className="rounded-full border border-slate-300 px-5 py-2 text-sm font-semibold text-slate-800 hover:border-violet-400">
          Read the report ↗
        </a>
        <Link href="/ai-sdlc/case-study/" className="rounded-full border border-slate-300 px-5 py-2 text-sm font-semibold text-slate-800 hover:border-violet-400">
          How it was derived
        </Link>
      </div>

      <GateFlow />

      <SectionTitle id="principles">Ten principles, each earned by an incident</SectionTitle>
      <ol className="grid gap-3 md:grid-cols-2">
        {FRAMEWORK_PRINCIPLES.map((p, i) => (
          <li key={p.k}>
            <Card className="flex h-full gap-4">
              <span className="font-mono text-sm font-semibold text-amber-700" aria-hidden>{String(i + 1).padStart(2, "0")}</span>
              <div>
                <h3 className="font-semibold text-slate-900">{p.k}</h3>
                <p className="mt-1 text-sm leading-relaxed text-slate-600">{p.v}</p>
              </div>
            </Card>
          </li>
        ))}
      </ol>

      <SectionTitle id="components">Components</SectionTitle>
      <p className="max-w-3xl text-slate-600 leading-relaxed">
        Agents are interchangeable and read the policy; they run the stages through the kit; the kit calls your existing
        tools and writes the evidence. Only the profile knows the project.
      </p>
      <Diagram chart={FRAMEWORK_ARCHITECTURE} caption="Purple: governance. Green: the kit. Your toolchain stays as it is; the ledger feeds the learning loop." />

      <SectionTitle id="stages">Stages and hard gates</SectionTitle>
      <Table
        head={["Stage", "Gate", "Done when", "In Notaire"]}
        rows={FRAMEWORK_STAGES.map((s) => [<Code key="s">{s.stage}</Code>, s.gate, s.done, s.notaire])}
      />
      <p className="mt-3 text-sm text-slate-600">
        A gate is <Code>name · stage · run · expect · required · timeout · paths</Code>. <Code>expect = &quot;fail&quot;</Code>{" "}
        makes the red step of TDD mechanical; <Code>paths</Code> skips slow gates when nothing they cover changed.
      </p>

      <SectionTitle id="profile">Notaire&apos;s profile, the first adopter</SectionTitle>
      <div className="grid gap-4 lg:grid-cols-2">
        <figure className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
          <figcaption className="border-b border-slate-200 bg-slate-50 px-4 py-2 font-mono text-xs text-slate-600">.aisdlc/aisdlc.toml (excerpt)</figcaption>
          <pre className="overflow-x-auto px-4 py-3 font-mono text-[12px] leading-relaxed text-slate-800" tabIndex={0}>
            <code>{NOTAIRE_PROFILE_EXCERPT}</code>
          </pre>
        </figure>
        <Terminal title="Getting started in any repository" lines={QUICK_START} caption="Real output, abridged. Python 3.11+ and git are the only requirements; placeholders show as TODO, never as a pass." />
      </div>
      <Sources items={[{ label: ".aisdlc/aisdlc.toml", href: src(".aisdlc/aisdlc.toml") }, { label: "example profiles", href: src("framework/ai-sdlc/profiles/") }, { label: "kit README", href: src("framework/ai-sdlc/README.md") }]} />

      <SectionTitle id="commands">The kit</SectionTitle>
      <Table head={["Command", "Does"]} rows={FRAMEWORK_COMMANDS.map((c) => [<Code key="c">{`aisdlc ${c.cmd}`}</Code>, c.does])} />
      <p className="mt-3 text-sm text-slate-600">Exit 0 passed, 1 a gate or check failed, 2 usage or configuration error. Nothing an agent says counts.</p>

      <SectionTitle id="levels">Adoption levels</SectionTitle>
      <div className="grid gap-6 lg:grid-cols-[1fr_1.1fr]">
        <Diagram chart={ADOPTION_LEVELS} caption="Pick the level from how many agents work at once and how much runs without a human." />
        <div className="space-y-3">
          {FRAMEWORK_LEVELS.map((l) => (
            <Card key={l.level} className="flex gap-4">
              <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-amber-100 font-mono text-sm font-bold text-amber-800">L{l.level}</span>
              <div>
                <h3 className="font-semibold text-slate-900">
                  {l.name} <span className="font-normal text-slate-500">· {l.fits}</span>
                </h3>
                <p className="mt-1 text-sm text-slate-600">{l.adds}</p>
              </div>
            </Card>
          ))}
        </div>
      </div>

      <SectionTitle id="loop">The learning loop</SectionTitle>
      <Diagram chart={LEARNING_LOOP} caption="Every incident is asked one question: can a command detect it? If yes, it becomes a gate." />

      <SectionTitle id="limits">What is not proven yet</SectionTitle>
      <ul className="list-disc space-y-2 pl-5 text-sm text-slate-700 max-w-3xl">
        <li>Adoption by a second real project and team (the kit is validated on Notaire, a fresh repository and four example stacks).</li>
        <li>Whether near-zero human review time is safe for high-risk changes; the merge rule may need a risk threshold.</li>
        <li>Real cost per merged change: <Code>aisdlc cost</Code> exists, but no harness records tokens yet.</li>
      </ul>
      <Sources items={[{ label: "report §10 — evidence, inference, open questions", href: src("docs/300-development/306-ai-sdlc-framework/ai_sdlc_framework.md") }]} />
    </div>
  );
}
