import Link from "next/link";
import { PostureBoard } from "@/components/security/PostureBoard";
import { T } from "@/components/site/T";
import { pageMetadata } from "@/lib/site";
import { CADENCE } from "@/lib/security-content";

export const metadata = pageMetadata("/security/");

export default function SecurityOverviewPage() {
  return (
    <div>
      <header className="mb-10 space-y-4">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-brand-700"><T text="Security · public snapshot" /> {CADENCE.assessed}</p>
        <h1 className="font-display text-4xl font-semibold tracking-tight text-slate-900 sm:text-5xl"><T text="Protect the notarial record" /></h1>
        <p className="max-w-3xl text-lg leading-relaxed text-slate-600">
          <T text="Notaire holds identity documents and deeds. The controls are in the repository: a signed session, a module grant on each API path, scanners that fail a release, and a weekly dynamic scan. This section explains them. It does not show a live scan." />
        </p>
      </header>
      <PostureBoard />
      <ul className="mt-8 grid gap-3 sm:grid-cols-2">
        {[
          ["/security/architecture/", "Trust boundaries", "Step from the browser to the database."],
          ["/security/threats/", "STRIDE", "Six properties, and the privacy notes beside them."],
          ["/security/toolchain/", "Toolchain", "Local gate, version tag, or Monday schedule."],
          ["/security/posture/", "Reporting", "Where a finding goes, and where it does not."],
        ].map(([href, title, body]) => (
          <li key={href}>
            <Link href={href} className="block h-full rounded-2xl border border-slate-200 bg-white p-5 hover:border-brand-500">
              <span className="font-semibold text-slate-900"><T text={title} /></span>
              <span className="mt-1 block text-sm text-slate-600"><T text={body} /></span>
            </Link>
          </li>
        ))}
      </ul>
    </div>
  );
}
