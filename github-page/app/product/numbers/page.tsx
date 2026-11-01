import { VersionHistory } from "@/components/VersionHistory";
import { FunFacts } from "@/components/FunFacts";
import { Coverage } from "@/components/Coverage";
import { Stats } from "@/components/Stats";
import { loadMetrics } from "@/lib/metrics";
import { loadReleaseHistory } from "@/lib/releases";
import { pageMetadata } from "@/lib/site";
import { versions } from "@/lib/tech-facts";

export const metadata = pageMetadata("/product/numbers/");

export default function ProductNumbersPage() {
  const metrics = loadMetrics();
  return (
    <>
      <VersionHistory history={loadReleaseHistory()} />
      <Stats metrics={metrics} />
      <Coverage metrics={metrics} />
      <FunFacts metrics={metrics} java={versions().java} />
    </>
  );
}
