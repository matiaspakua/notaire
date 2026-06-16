import { CustomCursor } from "@/components/CustomCursor";
import { SmoothScroll } from "@/components/SmoothScroll";
import { Nav } from "@/components/Nav";
import { Hero } from "@/components/Hero";
import { Timeline } from "@/components/Timeline";
import { TechEvolution } from "@/components/TechEvolution";
import { AIEra } from "@/components/AIEra";
import { AITools } from "@/components/AITools";
import { Architecture } from "@/components/Architecture";
import { InfraStack } from "@/components/InfraStack";
import { FunFacts } from "@/components/FunFacts";
import { Stats } from "@/components/Stats";
import { Coverage } from "@/components/Coverage";
import { Footer } from "@/components/Footer";
import { loadMetrics } from "@/lib/metrics";

export default function Home() {
  const metrics = loadMetrics();

  return (
    <>
      <CustomCursor />
      <SmoothScroll>
        <Nav />
        <main>
          <Hero />
          <Timeline />
          <TechEvolution />
          <AIEra />
          <AITools />
          <Architecture />
          <InfraStack />
          <FunFacts />
          <Coverage metrics={metrics} />
          <Stats metrics={metrics} />
        </main>
        <Footer />
      </SmoothScroll>
    </>
  );
}
