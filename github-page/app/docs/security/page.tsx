import Link from "next/link";
import { DOCS, REPO } from "@/lib/docs-content";
import { pageMetadata } from "@/lib/site";

export const metadata = pageMetadata("/docs/security/");

export default function DocsSecurityPage() {
  return (
    <div className="space-y-8">
      <header className="space-y-3">
        <h1 className="text-3xl font-semibold text-neutral-900" style={{ fontFamily: "var(--font-display)" }}>
          Security docs
        </h1>
        <p className="max-w-3xl leading-relaxed text-neutral-600">
          The interactive explanation — trust boundaries, STRIDE, the toolchain cadence, and the public snapshot —
          lives in the Security section. This page keeps the old route and points at the architecture notes on GitHub.
        </p>
      </header>
      <p>
        <Link href="/security/" className="font-semibold text-brand-700 underline decoration-brand-700/30 underline-offset-2">
          Open the Security section
        </Link>
      </p>
      <ul className="space-y-2 text-sm">
        <li>
          <a className="text-brand-700 underline decoration-brand-700/30 underline-offset-2 hover:decoration-current" href={`${DOCS}/200-architecture/206-security/ASSESSMENT-2026-11-05.md`}>
            Assessment 2026-11-05
          </a>
        </li>
        <li>
          <a className="text-brand-700 underline decoration-brand-700/30 underline-offset-2 hover:decoration-current" href={`${DOCS}/200-architecture/206-security/`}>
            Security architecture docs
          </a>
        </li>
        <li>
          <a className="text-brand-700 underline decoration-brand-700/30 underline-offset-2 hover:decoration-current" href={`${REPO}/blob/main/SECURITY.md`}>
            SECURITY.md
          </a>
        </li>
        <li>
          <a className="text-brand-700 underline decoration-brand-700/30 underline-offset-2 hover:decoration-current" href={`${REPO}/tree/main/security`}>
            security/ module
          </a>
        </li>
      </ul>
    </div>
  );
}
