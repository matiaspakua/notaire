import Link from "next/link";
import { AnatomyFigure } from "@/components/anatomy/AnatomyFigure";
import { sdlcFigure } from "@/components/anatomy/sdlc-figure";
import { Card, Code, Diagram, PageHeader, SectionTitle, Sources, Table } from "@/components/FactoryUI";
import { GATES, NEVER_LIST, src } from "@/lib/ai-sdlc-content";
import { fmt, loadFactoryFacts } from "@/lib/ai-sdlc-facts";
import { AI_SDLC_ANATOMY_CHART } from "@/lib/anatomy-content";
import { pageMetadata } from "@/lib/site";

export const metadata = pageMetadata("/ai-sdlc/anatomy/");

const WHY: [string, string][] = [
  ["Build from a vague prompt and drift from the requirement", "An issue tied to a use case and a validated OpenSpec change before any code (Gate 1)."],
  ["Write code first and tests that merely pass", "Tests written first and seen failing (Gate 2)."],
  ["Say done while checks are red or docs are stale", "Docs, coverage, lint and the full local pipeline before a pull request (Gate 3)."],
  ["Merge on a partial green", "A review with no open finding, and a merge rule: only the Foreman merges Worker pull requests (Gate 4)."],
  ["Close the issue when the pull request opens", "Deploy, smoke test and a coherent board before closing (Gate 5)."],
  ["Carry one rulebook per tool", "One Constitution and one catalog of skills and agents, read by every tool."],
];

export default function AiSdlcAnatomyPage() {
  const f = loadFactoryFacts();
  return (
    <>
      <PageHeader
        eyebrow="AI Factory · Anatomy"
        title="The AI SDLC at a glance: a lock chamber"
        lead="A software development lifecycle is the path a change takes from request to running system. In Notaire that path is written once and followed by people and AI agents alike. This page explains what it is, why it exists and how it works, with the machine first."
      />

      <SectionTitle id="what">What it is</SectionTitle>
      <p>
        Four parts: a <strong>rulebook</strong> (<Code>CONSTITUTION.md</Code> and its short digest), a <strong>specification method</strong> (OpenSpec),
        a <strong>catalog</strong> of {fmt(f.skills)} skills and {fmt(f.agents)} agent definitions, and <strong>roles</strong> with a clear
        boundary on who may merge what. Agents are workers inside the process, not a shortcut around it.
      </p>

      <SectionTitle id="machine">The machine</SectionTitle>
      <p>
        A lock is the right machine. A boat cannot pass a gate until the water on the far side has risen, and the water only rises when the
        earlier work is done. The five gates are the Constitution&apos;s five hard stops, the tower holds the three roles, and the lectern holds the
        rulebook. Step through it, or click a part.
      </p>
      <AnatomyFigure data={sdlcFigure} />

      <SectionTitle id="why">Why it exists</SectionTitle>
      <p>The factory answers the failures an unguided agent tends to produce. Each one is a real incident in the <Link href="/ai-sdlc/lessons/">lessons</Link>.</p>
      <Table head={["Without it, an agent tends to…", "The factory answers with…"]} rows={WHY} />

      <SectionTitle id="how">How a change flows</SectionTitle>
      <Diagram chart={AI_SDLC_ANATOMY_CHART} caption="One rulebook feeds the Foreman and the Workers. Evidence from the five gates, not a claim, decides what merges." />
      <Table
        head={["Gate", "Do not proceed without", "How a machine checks it"]}
        rows={GATES.map((g) => [`Gate ${g.n}`, g.items.join("; "), g.machine])}
      />
      <p>
        The full 24 steps, with the commands, are on the <Link href="/ai-sdlc/lifecycle/">lifecycle page</Link>; the specification method is on{" "}
        <Link href="/ai-sdlc/openspec/">OpenSpec</Link>, and who does what is on <Link href="/ai-sdlc/agents/">Agents &amp; skills</Link>.
      </p>

      <SectionTitle id="roles">Roles</SectionTitle>
      <div className="grid gap-3 sm:grid-cols-3">
        <Card>
          <h3 className="font-semibold text-slate-900">Owner</h3>
          <p className="mt-2 text-sm text-slate-600">The human code owner. Approves specifications, grants exceptions, merges the Constitution and harness changes.</p>
        </Card>
        <Card>
          <h3 className="font-semibold text-slate-900">Foreman</h3>
          <p className="mt-2 text-sm text-slate-600">A supervising agent. Picks the issue, runs the gates, reviews, and merges Worker pull requests under its merge rule.</p>
        </Card>
        <Card>
          <h3 className="font-semibold text-slate-900">Worker</h3>
          <p className="mt-2 text-sm text-slate-600">An implementing agent. Implements phase by phase and answers review notes. Never merges or pushes to main.</p>
        </Card>
      </div>

      <SectionTitle id="never">What an agent may never do</SectionTitle>
      <ul className="list-disc space-y-1 pl-5">
        {NEVER_LIST.map((n) => (
          <li key={n}>{n}</li>
        ))}
      </ul>

      <SectionTitle id="limits">Honest limits</SectionTitle>
      <p>
        Autonomy has edges. Decisions that belong to the Owner (a deployment model, which second factor to use for multi-factor authentication,
        repository topology) are written up and waited for, not guessed. Gates that need Docker, Testcontainers or other services run on a developer
        machine or on a version tag, not in every sandbox; an agent records what it could not run instead of claiming it passed.
      </p>
      <Sources
        items={[
          { label: "CONSTITUTION.md", href: src("CONSTITUTION.md") },
          { label: "CONSTITUTION-AGENT-CARD.md", href: src("CONSTITUTION-AGENT-CARD.md") },
          { label: "AGENTS.md", href: src("AGENTS.md") },
          { label: "304-ai-sdlc-cloud", href: src("docs/300-development/304-ai-sdlc-cloud/README.md") },
        ]}
      />
    </>
  );
}
