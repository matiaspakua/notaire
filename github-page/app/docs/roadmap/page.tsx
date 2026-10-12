import { MermaidDiagram } from "@/components/MermaidDiagram";
import { DOCS } from "@/lib/docs-content";
import snapshot from "@/lib/roadmap-snapshot.json";

const SEQUENCE = `
flowchart LR
  P4["Phase 4 Frontend"] --> V10["v1.0 Production"]
  P6["Phase 6 Deprecation"] --> V10
  V10 --> V11["v1.1 Hardening"]
  V11 --> BL["Backlog"]
`;

type Milestone = {
  title: string;
  state: string;
  kind: string;
  open: number;
  closed: number;
};

const data = snapshot as {
  as_of: string;
  issues: number;
  open: number;
  closed: number;
  current_unassigned: number;
  moves: number;
  claimable_open: number;
  by_milestone: Milestone[];
  by_lane_claimable_open: Record<string, number>;
};

function share(row: Milestone): number {
  const total = row.open + row.closed;
  if (total === 0) return 0;
  return Math.round((100 * row.closed) / total);
}

export default function DocsRoadmapPage() {
  const delivery = data.by_milestone.filter((row) => row.kind === "delivery");
  const history = data.by_milestone.filter((row) => row.kind === "history");
  const scope = data.by_milestone.filter((row) => row.kind === "scope");
  const lanes = Object.entries(data.by_lane_claimable_open).sort((a, b) => b[1] - a[1]);

  return (
    <div className="space-y-10">
      <header className="space-y-3">
        <p className="text-xs uppercase tracking-[0.2em] text-[#0A84FF] font-semibold">
          Project chart · {data.as_of}
        </p>
        <h1
          className="text-3xl font-semibold text-neutral-900"
          style={{ fontFamily: "var(--font-display)" }}
        >
          Roadmap and progress
        </h1>
        <p className="text-neutral-600 max-w-3xl leading-relaxed">
          Target layout for {data.issues} issues ({data.open} open, {data.closed} closed).
          {data.current_unassigned} issues had no milestone on this date. The plan assigns{" "}
          {data.moves} of them onto a history cohort, a delivery train, or the requirements
          catalog. {data.claimable_open} open issues are work agents may pick.
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
          ["History", history.reduce((sum, row) => sum + row.closed, 0), "closed issues grouped by era"],
          ["Delivery", delivery.reduce((sum, row) => sum + row.open, 0), "open issues on a train"],
          ["Scope", scope.reduce((sum, row) => sum + row.open, 0), "RF, CU, and RNF catalog rows"],
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
                <th className="px-4 py-3 font-medium">Closed</th>
                <th className="px-4 py-3 font-medium">Closed share</th>
              </tr>
            </thead>
            <tbody>
              {delivery.map((row) => (
                <tr key={row.title} className="border-t border-neutral-100">
                  <td className="px-4 py-3 text-neutral-900">{row.title}</td>
                  <td className="px-4 py-3 text-neutral-700">{row.open}</td>
                  <td className="px-4 py-3 text-neutral-700">{row.closed}</td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-2">
                      <div className="h-2 w-28 rounded-full bg-neutral-100 overflow-hidden">
                        <div
                          className="h-full bg-[#0A84FF]"
                          style={{ width: `${share(row)}%` }}
                        />
                      </div>
                      <span className="text-neutral-600">{share(row)}%</span>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <section className="space-y-3">
        <h2 className="text-lg font-semibold text-neutral-900">How the project was organized</h2>
        <p className="text-sm text-neutral-600 max-w-3xl">
          Closed cohorts for issues that had no milestone. Older milestones that already
          had a title stay in the table with their original names.
        </p>
        <ul className="space-y-2">
          {history.map((row) => (
            <li key={row.title} className="flex items-center justify-between gap-4 text-sm">
              <span className="text-neutral-800">{row.title}</span>
              <span className="font-mono text-neutral-500">{row.closed}</span>
            </li>
          ))}
        </ul>
      </section>

      <section className="space-y-3">
        <h2 className="text-lg font-semibold text-neutral-900">Requirements catalog</h2>
        <p className="text-sm text-neutral-600 max-w-3xl">
          These stay open for traceability. They are not unfinished features, and agents do not pick them.
        </p>
        <ul className="space-y-2">
          {scope.map((row) => (
            <li key={row.title} className="flex items-center justify-between gap-4 text-sm">
              <span className="text-neutral-800">{row.title}</span>
              <span className="font-mono text-neutral-500">{row.open} open</span>
            </li>
          ))}
        </ul>
      </section>

      <section className="space-y-3">
        <h2 className="text-lg font-semibold text-neutral-900">Parallel lanes</h2>
        <p className="text-sm text-neutral-600 max-w-3xl">
          One in-progress issue per lane. Fullstack also blocks backend and frontend.
          lane:owner waits for a person. lane:security is exclusive.
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
