import type { ReleaseHistory } from "@/lib/releases";

export function VersionHistory({ history }: { history: ReleaseHistory }) {
  const { components } = history;

  return (
    <section id="versions" className="py-24 px-6" style={{ background: "var(--bg-dark)" }}>
      <div className="max-w-4xl mx-auto">
        <div className="text-center mb-12">
          <p className="text-[#0A84FF] text-sm font-mono font-bold tracking-widest mb-4 uppercase">
            Version history
          </p>
          <h2
            className="text-4xl sm:text-5xl font-bold text-neutral-900 mb-4"
            style={{ fontFamily: "var(--font-display)", letterSpacing: "-0.02em" }}
          >
            Product <span className="grad-cyan">cuts</span>
          </h2>
          <p className="text-neutral-600 max-w-2xl mx-auto">
            Semver releases of Notaire. The timeline above is the narrative. This list is the
            tag history. A delivery-train milestone is not a git tag.
          </p>
        </div>

        <div className="glass rounded-2xl p-6 border border-neutral-200/80 mb-8">
          <p className="text-xs uppercase tracking-widest text-neutral-500 mb-2">Current product</p>
          <p className="text-3xl font-semibold text-neutral-900" style={{ fontFamily: "var(--font-mono)" }}>
            v{history.productVersion}
          </p>
          <p className="mt-2 text-sm text-neutral-600">{history.train}</p>
          <dl className="mt-6 grid sm:grid-cols-3 gap-4 text-sm">
            <div>
              <dt className="text-neutral-500">Backend</dt>
              <dd className="font-mono text-neutral-900">{components.backend}</dd>
            </div>
            <div>
              <dt className="text-neutral-500">Frontend</dt>
              <dd className="font-mono text-neutral-900">{components.frontend}</dd>
            </div>
            <div>
              <dt className="text-neutral-500">Database schema</dt>
              <dd className="font-mono text-neutral-900">Flyway V{components.databaseSchema}</dd>
            </div>
          </dl>
        </div>

        <ol className="space-y-3">
          {history.releases.map((release) => (
            <li key={release.version} className="glass rounded-xl px-5 py-4 border border-neutral-200/70">
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
                <div>
                  <a
                    href={release.url}
                    className="font-mono font-semibold text-neutral-900 hover:text-[#0A84FF]"
                  >
                    {release.tag}
                  </a>
                  <p className="text-sm text-neutral-500">{release.date}</p>
                </div>
                <p className="text-sm text-neutral-600">{release.train}</p>
              </div>
            </li>
          ))}
        </ol>
      </div>
    </section>
  );
}
