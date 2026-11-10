import Link from "next/link";
import { Card, Code, PageHeader, SectionTitle, Sources } from "@/components/FactoryUI";
import { src } from "@/lib/ai-sdlc-content";
import { pageMetadata } from "@/lib/site";

export const metadata = pageMetadata("/ai-sdlc/security-gates/");

export default function SecurityGatesPage() {
  return (
    <div>
      <PageHeader
        eyebrow="AI Factory · Security gates"
        title="The pull request is not the security scan"
        lead="Agents prove the security catalog on their machine with preflight.sh --full. GitHub runs the product scanners on a version tag, on a Monday schedule, or when someone dispatches the workflow. A quiet pull request is the design, not a skipped job."
      />
      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <h3 className="font-semibold">Run it</h3>
          <p className="mt-2 text-sm text-slate-600">
            <Code>python3 security/run_gates.py --profile local --skip-dast</Code> is inside preflight. The DAST baseline
            runs only after <Code>run_pipeline.sh</Code> has a stack.
          </p>
        </Card>
        <Card>
          <h3 className="font-semibold">Update the model</h3>
          <p className="mt-2 text-sm text-slate-600">
            A change that touches auth, personal data, secrets, dependencies, or a public endpoint updates the markdown
            threat model and <Code>system.stride.yml</Code> together.
          </p>
        </Card>
        <Card>
          <h3 className="font-semibold">File the rest</h3>
          <p className="mt-2 text-sm text-slate-600">
            A gap that is not this change becomes a CU78 issue with acceptance criteria. Do not hide it in the pull
            request body, and do not weaken a gate to make the branch green.
          </p>
        </Card>
      </div>
      <SectionTitle>Keep the factory and the product apart</SectionTitle>
      <p className="max-w-3xl text-sm leading-relaxed text-slate-600">
        This page is the agent procedure. The threat register, the STRIDE status, and the control evidence stay in the
        product security docs and in the{" "}
        <Link href="/security/" className="font-semibold text-violet-700 hover:underline">
          Security section
        </Link>
        . Do not paste scan output into either place.
      </p>
      <Sources
        items={[
          { label: "notaire-cybersec skill", href: src(".claude/skills/notaire-cybersec/SKILL.md") },
          { label: "SECURITY-GATES.md", href: src("docs/300-development/304-ai-sdlc-cloud/SECURITY-GATES.md") },
          { label: "ASSESSMENT-2026-11-05.md", href: src("docs/200-architecture/206-security/ASSESSMENT-2026-11-05.md") },
        ]}
      />
    </div>
  );
}
