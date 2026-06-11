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

export default function Home() {
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
          <Coverage />
          <Stats />
        </main>
        <Footer />
      </SmoothScroll>
    </>
  );
}
