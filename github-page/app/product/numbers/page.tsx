import { VersionHistory } from "@/components/VersionHistory";
import { FunFacts } from "@/components/FunFacts";
import { Coverage } from "@/components/Coverage";
import { Stats } from "@/components/Stats";
import { coverageIsMeasured, loadMetrics } from "@/lib/metrics";
import { loadReleaseHistory } from "@/lib/releases";
import { pageMetadata } from "@/lib/site";
import { versions } from "@/lib/tech-facts";

export const metadata = pageMetadata("/product/numbers/");

export default function ProductNumbersPage() {
  const metrics = loadMetrics();
  return (
    <>
      <VersionHistory history={loadReleaseHistory()} />
      {!coverageIsMeasured(metrics.coverage) && (
        <p className="mx-auto max-w-3xl px-6 pt-10 text-sm text-slate-700">
          The coverage percentages on this page are not a JaCoCo or Vitest run. A shown 0% is the generator&apos;s missing-file fallback, not a measurement.
          Activity counts above the coverage charts are counted from the repository and GitHub.{" "}
          <code className="font-mono">lastUpdated: {metrics.coverage.lastUpdated}</code>
        </p>
      )}
      <Stats metrics={metrics} />
      <Coverage metrics={metrics} />
      <FunFacts metrics={metrics} java={versions().java} />
    </>
  );
}
