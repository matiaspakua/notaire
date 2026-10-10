import { DOCS, REPO } from "@/lib/docs-content";

const controls = [
  {
    title: "Authentication & sessions",
    body: "JWT (HttpOnly cookie path), rate limiting, logout revocation. ADR-008, ADR-018.",
  },
  {
    title: "Business audit",
    body: "AuditoriaAspect records create/update/delete and logins from SecurityContext — not client headers.",
  },
  {
    title: "SAST / SCA / DAST",
    body: "CodeQL, dependency review, Trivy image/fs scans, ZAP DAST workflows run where the source lives.",
  },
  {
    title: "Repository protection",
    body: "security/ module holds rulesets-as-code and the control map; apply with admin PAT (#1040).",
  },
];

export default function DocsSecurityPage() {
  return (
    <div className="space-y-8">
      <header className="space-y-3">
        <h1 className="text-3xl font-semibold text-neutral-900" style={{ fontFamily: "var(--font-display)" }}>
          Security & DevSecOps
        </h1>
        <p className="text-neutral-600 max-w-3xl leading-relaxed">
          Security scanning stays with the source (ADR-024 critique of a standalone security repo).
          The <code className="text-xs bg-neutral-100 px-1 rounded">security/</code> module owns
          protection-as-code and the inventory of controls.
        </p>
      </header>

      <div className="grid sm:grid-cols-2 gap-4">
        {controls.map((c) => (
          <article key={c.title} className="rounded-2xl border border-neutral-200 bg-white p-5">
            <h2 className="font-semibold text-neutral-900">{c.title}</h2>
            <p className="mt-2 text-sm text-neutral-600 leading-relaxed">{c.body}</p>
          </article>
        ))}
      </div>

      <ul className="space-y-2 text-sm">
        <li>
          <a className="text-[#0A84FF] hover:underline" href={`${DOCS}/200-architecture/206-security/`}>
            Security architecture docs
          </a>
        </li>
        <li>
          <a className="text-[#0A84FF] hover:underline" href={`${DOCS}/200-architecture/208-devsecops/README.md`}>
            DevSecOps README
          </a>
        </li>
        <li>
          <a className="text-[#0A84FF] hover:underline" href={`${REPO}/blob/main/SECURITY.md`}>
            SECURITY.md
          </a>
        </li>
        <li>
          <a className="text-[#0A84FF] hover:underline" href={`${REPO}/tree/main/security`}>
            security/ module
          </a>
        </li>
      </ul>
    </div>
  );
}
