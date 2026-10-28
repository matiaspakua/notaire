import { SmoothScroll } from "@/components/SmoothScroll";
import { Hero } from "@/components/Hero";
import { Timeline } from "@/components/Timeline";
import { VersionHistory } from "@/components/VersionHistory";
import { loadReleaseHistory } from "@/lib/releases";
import { TechEvolution } from "@/components/TechEvolution";
import { AIEra } from "@/components/AIEra";
import { AITools } from "@/components/AITools";
import { Architecture } from "@/components/Architecture";
import { InfraStack } from "@/components/InfraStack";
import { FunFacts } from "@/components/FunFacts";
import { Stats } from "@/components/Stats";
import { Coverage } from "@/components/Coverage";
import { loadMetrics } from "@/lib/metrics";

export default function Home() {
  const metrics = loadMetrics();
  const releases = loadReleaseHistory();

  return (
    <SmoothScroll>
      <Hero />
      <Timeline />
      <VersionHistory history={releases} />
      <TechEvolution />
      <AIEra />
      <AITools />
      <Architecture />
      <InfraStack />
      <FunFacts metrics={metrics} />
      <Coverage metrics={metrics} />
      <Stats metrics={metrics} />
    </SmoothScroll>
  );
}
