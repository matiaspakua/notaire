import { loadReleaseHistory } from "@/lib/releases";

export default function ReleasesDocPage() {
  const history = loadReleaseHistory();

  return (
    <div className="space-y-8">
      <header className="space-y-3">
        <p className="text-xs uppercase tracking-[0.2em] text-[#0A84FF] font-semibold">Versions</p>
        <h1
          className="text-4xl font-semibold tracking-tight text-neutral-900"
          style={{ fontFamily: "var(--font-display)" }}
        >
          Release history
        </h1>
        <p className="text-neutral-600 max-w-2xl leading-relaxed">
          One product version covers the backend, the frontend, and the Flyway schema recorded
          with that cut. The schema number is not the semver. Delivery trains
          ({history.train} for {history.productVersion}) are milestones, not tags.
        </p>
      </header>

      <dl className="grid sm:grid-cols-3 gap-4">
        <div className="rounded-2xl border border-neutral-200 bg-white p-5">
          <dt className="text-xs uppercase tracking-wider text-neutral-500">Backend</dt>
          <dd className="mt-1 font-mono text-lg text-neutral-900">{history.components.backend}</dd>
        </div>
        <div className="rounded-2xl border border-neutral-200 bg-white p-5">
          <dt className="text-xs uppercase tracking-wider text-neutral-500">Frontend</dt>
          <dd className="mt-1 font-mono text-lg text-neutral-900">{history.components.frontend}</dd>
        </div>
        <div className="rounded-2xl border border-neutral-200 bg-white p-5">
          <dt className="text-xs uppercase tracking-wider text-neutral-500">Database</dt>
          <dd className="mt-1 font-mono text-lg text-neutral-900">V{history.components.databaseSchema}</dd>
        </div>
      </dl>

      <table className="w-full text-sm">
        <thead>
          <tr className="text-left text-neutral-500 border-b border-neutral-200">
            <th className="py-2 pr-4 font-medium">Tag</th>
            <th className="py-2 pr-4 font-medium">Date</th>
            <th className="py-2 font-medium">Train</th>
          </tr>
        </thead>
        <tbody>
          {history.releases.map((release) => (
            <tr key={release.version} className="border-b border-neutral-100">
              <td className="py-3 pr-4">
                <a className="text-[#0A84FF] hover:underline font-mono" href={release.url}>
                  {release.tag}
                </a>
              </td>
              <td className="py-3 pr-4 text-neutral-700">{release.date}</td>
              <td className="py-3 text-neutral-700">{release.train}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
