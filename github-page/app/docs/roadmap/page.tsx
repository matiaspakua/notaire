import { MermaidDiagram } from "@/components/MermaidDiagram";
import { DOCS } from "@/lib/docs-content";
import snapshot from "@/lib/milestone-snapshot.json";

const SEQUENCE = `
flowchart LR
  RB["2026-11 Release blockers"] --> FE["2026-12 Frontend product"]
  FE --> QA["2027-01 Quality & traceability"]
  QA --> OP["2027-02 Platform & operability"]
  OP --> LR["2027-03 Legacy retirement"]
  LR --> BL["2027-06 Backlog"]
`;

type Milestone = {
  title: string;
  count: number;
};

const data = snapshot as {
  generated: string;
  open: number;
  closed: number;
  openByMilestone: Record<string, number>;
  closedByMilestone: Record<string, number>;
  openByLane: Record<string, number>;
};

function rows(values: Record<string, number>): Milestone[] {
  return Object.entries(values).map(([title, count]) => ({ title, count }));
}

export default function DocsRoadmapPage() {
  const delivery = rows(data.openByMilestone);
  const history = rows(data.closedByMilestone);
  const lanes = Object.entries(data.openByLane).sort((a, b) => b[1] - a[1]);
  const catalog = data.openByLane.catalog ?? 0;
  const claimable = data.open - catalog;

  return (
    <div className="space-y-10">
      <header className="space-y-3">
        <p className="text-xs uppercase tracking-[0.2em] text-[#0A84FF] font-semibold">
          Project chart · {data.generated}
        </p>
        <h1
          className="text-3xl font-semibold text-neutral-900"
          style={{ fontFamily: "var(--font-display)" }}
        >
          Roadmap and progress
        </h1>
        <p className="text-neutral-600 max-w-3xl leading-relaxed">
          Live milestone snapshot for {data.open + data.closed} issues ({data.open} open,{" "}
          {data.closed} closed). Every issue has a history or delivery milestone.{" "}
          {claimable} open issues are delivery work; {catalog} are catalog anchors.
        </p>
        <a
          className="text-sm text-[#0A84FF] hover:underline"
          href={`${DOCS}/300-development/305-project-management/README.md`}
        >
          Project management docs →
        </a>
      </header>

      <section className="grid sm:grid-cols-3 gap-4">
        {[
          ["History", data.closed, "closed issues grouped by era"],
          ["Delivery", claimable, "open delivery work"],
          ["Catalog", catalog, "requirement anchors, not agent work"],
        ].map(([label, value, note]) => (
          <div key={String(label)} className="rounded-2xl border border-neutral-200 bg-white p-5">
            <p className="text-sm text-neutral-500">{label}</p>
            <p className="text-3xl font-semibold text-neutral-900 mt-1">{value}</p>
            <p className="text-xs text-neutral-500 mt-2">{note}</p>
          </div>
        ))}
      </section>

      <section className="space-y-3">
        <h2 className="text-lg font-semibold text-neutral-900">Delivery sequence</h2>
        <MermaidDiagram chart={SEQUENCE} />
        <div className="overflow-x-auto rounded-2xl border border-neutral-200 bg-white">
          <table className="min-w-full text-sm">
            <thead className="bg-neutral-50 text-left text-neutral-500">
              <tr>
                <th className="px-4 py-3 font-medium">Train</th>
                <th className="px-4 py-3 font-medium">Open</th>
              </tr>
            </thead>
            <tbody>
              {delivery.map((row) => (
                <tr key={row.title} className="border-t border-neutral-100">
                  <td className="px-4 py-3 text-neutral-900">{row.title}</td>
                  <td className="px-4 py-3 text-neutral-700">{row.count}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <section className="space-y-3">
        <h2 className="text-lg font-semibold text-neutral-900">How the project was organized</h2>
        <p className="text-sm text-neutral-600 max-w-3xl">
          Closed cohorts preserve the path from the first issue to the current
          delivery plan. Curated phase buckets remain distinct from date cohorts.
        </p>
        <ul className="space-y-2">
          {history.map((row) => (
            <li key={row.title} className="flex items-center justify-between gap-4 text-sm">
              <span className="text-neutral-800">{row.title}</span>
              <span className="font-mono text-neutral-500">{row.count}</span>
            </li>
          ))}
        </ul>
      </section>

      <section className="space-y-3">
        <h2 className="text-lg font-semibold text-neutral-900">Requirements catalog</h2>
        <p className="text-sm text-neutral-600 max-w-3xl">
          {catalog} RF, CU, and RNF anchors remain open for traceability. They
          have dedicated Project views and are never picked as implementation work.
        </p>
      </section>

      <section className="space-y-3">
        <h2 className="text-lg font-semibold text-neutral-900">Parallel lanes</h2>
        <p className="text-sm text-neutral-600 max-w-3xl">
          Lanes are derived from area and type labels. Cross work blocks backend
          and frontend; security also blocks backend. Catalog and epic lanes are
          not claimable.
        </p>
        <ul className="grid sm:grid-cols-2 gap-2">
          {lanes.map(([lane, count]) => (
            <li
              key={lane}
              className="flex items-center justify-between rounded-xl border border-neutral-200 bg-white px-4 py-3 text-sm"
            >
              <span className="font-mono text-neutral-800">{lane}</span>
              <span className="text-neutral-600">{count}</span>
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}
