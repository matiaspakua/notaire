import { DOCS } from "@/lib/docs-content";
import snapshot from "@/lib/milestone-snapshot.json";

const TRAINS: { name: string; span: string; tone: string }[] = [
  { name: "Release blockers", span: "13 Oct 2026 – 30 Nov 2026", tone: "#b60205" },
  { name: "Frontend product", span: "13 Oct 2026 – 31 Dec 2026", tone: "#0A84FF" },
  { name: "Quality & traceability", span: "1 Jan 2027 – 31 Jan 2027", tone: "#0077B6" },
  { name: "Platform & operability", span: "1 Feb 2027 – 28 Feb 2027", tone: "#0096C7" },
  { name: "Legacy retirement", span: "1 Mar 2027 – 31 Mar 2027", tone: "#7c6f64" },
  { name: "Backlog", span: "1 Apr 2027 – 30 Jun 2027", tone: "#94a3b8" },
];

type Counts = Record<string, number>;

const data = snapshot as {
  generated: string;
  applyStatus?: string;
  applyNote?: string;
  liveOpen?: Counts;
  plannedOpen?: Counts;
  plannedClosed?: Counts;
  plannedLanes?: Counts;
  openTotal?: number;
  closedTotal?: number;
};

function Bars({ rows, tone }: { rows: [string, number][]; tone: string }) {
  const max = Math.max(...rows.map(([, n]) => n), 1);
  return (
    <div className="space-y-2">
      {rows.map(([name, count]) => (
        <div key={name}>
          <div className="flex justify-between text-xs text-neutral-600 mb-1 gap-3">
            <span className="truncate">{name}</span>
            <span className="tabular-nums">{count}</span>
          </div>
          <div className="h-2 rounded-full bg-neutral-100 overflow-hidden">
            <div
              className="h-full rounded-full"
              style={{ width: `${Math.max(4, (count / max) * 100)}%`, background: tone }}
            />
          </div>
        </div>
      ))}
    </div>
  );
}

function pairs(counts: Counts | undefined): [string, number][] {
  return Object.entries(counts ?? {}).sort((a, b) => b[1] - a[1]);
}

export default function DocsManagementPage() {
  return (
    <div className="space-y-10">
      <header className="space-y-3">
        <p className="text-xs uppercase tracking-[0.2em] text-[#0A84FF] font-semibold">
          Snapshot {data.generated}
          {data.applyStatus ? ` · ${data.applyStatus}` : ""}
        </p>
        <h1
          className="text-3xl sm:text-4xl font-semibold tracking-tight text-neutral-900"
          style={{ fontFamily: "var(--font-display)" }}
        >
          Project schedule
        </h1>
        <p className="text-neutral-600 max-w-3xl leading-relaxed">
          {data.openTotal ?? "—"} open issues and {data.closedTotal ?? "—"} closed issues.
          History is grouped by the era it shipped. Forward work sits on six trains so a
          project manager can see scope, urgency, and which agents can run together.
        </p>
        {data.applyNote ? (
          <p className="text-sm text-neutral-700 max-w-3xl leading-relaxed rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3">
            {data.applyNote}
          </p>
        ) : null}
        <a className="inline-block text-sm text-[#0A84FF] hover:underline" href={`${DOCS}/github/PROJECT-MANAGEMENT.md`}>
          Project management handbook →
        </a>
      </header>

      <section className="space-y-3">
        <h2 className="text-lg font-semibold text-neutral-900">Trains</h2>
        <ol className="rounded-2xl border border-neutral-200 bg-white divide-y divide-neutral-100">
          {TRAINS.map((train) => (
            <li key={train.name} className="flex items-center justify-between gap-4 px-4 py-3">
              <span className="flex items-center gap-3 text-sm font-medium text-neutral-900">
                <span className="h-2.5 w-2.5 rounded-full" style={{ background: train.tone }} />
                {train.name}
              </span>
              <span className="text-xs text-neutral-500 tabular-nums">{train.span}</span>
            </li>
          ))}
        </ol>
      </section>

      <section className="grid lg:grid-cols-2 gap-6">
        <div className="rounded-2xl border border-neutral-200 bg-white p-5 space-y-4">
          <h2 className="text-lg font-semibold text-neutral-900">Open issues today</h2>
          <p className="text-sm text-neutral-500">Live milestones before the schedule is applied.</p>
          <Bars rows={pairs(data.liveOpen)} tone="#94a3b8" />
        </div>
        <div className="rounded-2xl border border-neutral-200 bg-white p-5 space-y-4">
          <h2 className="text-lg font-semibold text-neutral-900">Open issues on the schedule</h2>
          <p className="text-sm text-neutral-500">
            Frontend product includes the requirements catalog. The roadmap hides those anchors.
          </p>
          <Bars rows={pairs(data.plannedOpen)} tone="#0A84FF" />
        </div>
      </section>

      <section className="grid lg:grid-cols-2 gap-6">
        <div className="rounded-2xl border border-neutral-200 bg-white p-5 space-y-4">
          <h2 className="text-lg font-semibold text-neutral-900">Closed issues by history</h2>
          <Bars rows={pairs(data.plannedClosed)} tone="#0077B6" />
        </div>
        <div className="rounded-2xl border border-neutral-200 bg-white p-5 space-y-4">
          <h2 className="text-lg font-semibold text-neutral-900">Open issues by lane</h2>
          <p className="text-sm text-neutral-500">
            Catalog and epic lanes are not pickable. One in-progress issue per other lane.
          </p>
          <Bars rows={pairs(data.plannedLanes)} tone="#0096C7" />
        </div>
      </section>

      <section className="rounded-2xl border border-neutral-200 bg-white p-5 space-y-2 text-sm text-neutral-700">
        <h2 className="text-lg font-semibold text-neutral-900">Parallel lanes</h2>
        <p>One in-progress issue per lane. Catalog anchors and epics are not pickable.</p>
        <p>Security blocks backend. A cross-cutting issue blocks frontend and backend. Quality and platform can run beside a product lane.</p>
      </section>
    </div>
  );
}
