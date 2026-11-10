import { T } from "@/components/site/T";
import { pageMetadata } from "@/lib/site";

export const metadata = pageMetadata("/security/posture/");

const SURFACES = [
  {
    title: "On the agent machine",
    body: "run_gates.py writes an HTML report under reports/security/latest. The local observability stack can serve it on port 8890. That file is not published.",
  },
  {
    title: "On a release run",
    body: "The security job appends a case table to the Actions summary and uploads the HTML plus Trivy JSON as artifacts. People with access to the workflow can read them.",
  },
  {
    title: "CodeQL",
    body: "SARIF goes to the GitHub Security tab. The job fails if analysis fails. A finding by itself does not fail the job.",
  },
  {
    title: "This site",
    body: "The posture and the cadence are checked-in explanations. They use no production credential and no scan output. A later change can feed a redacted summary. Until then, treat every status here as a snapshot.",
  },
];

export default function SecurityReportingPage() {
  return (
    <div>
      <header className="mb-8 space-y-3">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-brand-700"><T text="Security · reporting" /></p>
        <h1 className="font-display text-4xl font-semibold tracking-tight text-slate-900"><T text="Where a finding goes" /></h1>
        <p className="max-w-3xl text-lg leading-relaxed text-slate-600">
          <T text="Four surfaces. Only the last one is public, and it is not a dashboard of live results." />
        </p>
      </header>
      <ol className="space-y-4">
        {SURFACES.map((item, index) => (
          <li key={item.title} className="rounded-2xl border border-slate-200 bg-white p-5">
            <p className="text-xs font-semibold uppercase tracking-[0.16em] text-slate-500">0{index + 1}</p>
            <h2 className="mt-1 font-semibold text-slate-900"><T text={item.title} /></h2>
            <p className="mt-2 text-sm leading-relaxed text-slate-700"><T text={item.body} /></p>
          </li>
        ))}
      </ol>
    </div>
  );
}
