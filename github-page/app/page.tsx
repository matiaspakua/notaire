import { CustomCursor } from "@/components/CustomCursor";
import { SmoothScroll } from "@/components/SmoothScroll";
import { Nav } from "@/components/Nav";
import { Hero } from "@/components/Hero";
import { Timeline } from "@/components/Timeline";
import { TechEvolution } from "@/components/TechEvolution";
import { AIEra } from "@/components/AIEra";
import { Architecture } from "@/components/Architecture";
import { Stats } from "@/components/Stats";
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
          <Architecture />
          <Stats />
        </main>
        <Footer />
      </SmoothScroll>
    </>
  );
}
