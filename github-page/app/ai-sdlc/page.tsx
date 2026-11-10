import { FactoryHomeView } from "@/components/factory/FactoryHomeView";
import { LESSONS } from "@/lib/ai-sdlc-content";
import { fmt, loadFactoryFacts } from "@/lib/ai-sdlc-facts";
import { factoryFlow } from "@/lib/diagram-flows";
import { pageMetadata } from "@/lib/site";

export const metadata = pageMetadata("/ai-sdlc/");

const PROBLEM_TITLES = [
  "Light green is not mergeable",
  "An invented Issue number passed Gate 1",
  "Models report success they have not earned",
  "Too much always-loaded context",
];

export default function FactoryHome() {
  const f = loadFactoryFacts();
  const problems = PROBLEM_TITLES.map((title) => LESSONS.find((lesson) => lesson.title === title)).filter((lesson) => lesson !== undefined);
  return (
    <FactoryHomeView
      stats={[
        { key: "skills", value: fmt(f.skills) },
        { key: "agents", value: fmt(f.agents) },
        { key: "workflows", value: fmt(f.workflows) },
        { key: "archived", value: fmt(f.archivedChanges) },
        { key: "specs", value: fmt(f.livingSpecs) },
        { key: "scripts", value: fmt(f.sdlcScripts) },
        { key: "e2e", value: fmt(f.e2eSuites) },
        { key: "adrs", value: fmt(f.adrs) },
      ]}
      problems={problems.map((lesson) => ({ title: lesson.title, happened: lesson.happened, rule: lesson.rule }))}
      flow={factoryFlow()}
    />
  );
}
