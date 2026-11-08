import { Timeline } from "@/components/Timeline";
import { TechEvolution } from "@/components/TechEvolution";
import { AIEra } from "@/components/AIEra";
import { AITools } from "@/components/AITools";
import { pageMetadata } from "@/lib/site";
import { versions } from "@/lib/tech-facts";

export const metadata = pageMetadata("/product/story/");

export default function ProductStoryPage() {
  return (
    <>
      <Timeline />
      <TechEvolution versions={versions()} />
      <AIEra />
      <AITools />
    </>
  );
}
