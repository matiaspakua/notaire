import { CadenceBoard } from "@/components/security/CadenceBoard";
import { pageMetadata } from "@/lib/site";

export const metadata = pageMetadata("/security/toolchain/");

export default function SecurityToolchainPage() {
  return (
    <div>
      <header className="mb-8 space-y-3">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-brand-700">Security · toolchain</p>
        <h1 className="font-display text-4xl font-semibold tracking-tight text-slate-900">What runs, and when</h1>
        <p className="max-w-3xl text-lg leading-relaxed text-slate-600">
          Filter the catalog. A version tag runs the release security job. Monday runs CodeQL and ZAP. A pull request
          does not start those workflows. The local preflight does.
        </p>
      </header>
      <CadenceBoard />
    </div>
  );
}
