import { Code, PageHeader, SectionTitle, Sources, Table } from "@/components/FactoryUI";
import { StatGrid } from "@/components/DocsUI";
import { TestPyramid } from "@/components/diagrams/TestPyramid";
import { src } from "@/lib/ai-sdlc-content";
import { pageMetadata } from "@/lib/site";
import { testCounts } from "@/lib/tech-facts";

export const metadata = pageMetadata("/docs/testing/");

const LAYERS: [string, string, string][] = [
  ["Unit", "backend-api, frontend", "mvn test · npm test (Vitest)"],
  ["Integration (H2, Testcontainers)", "backend-api", "mvn test -pl backend-api -am"],
  ["API (Bruno)", "backend-api/api-test", "npx @usebruno/cli run . -r --env Development"],
  ["E2E (Playwright)", "testing/e2e", "npx playwright test"],
  ["Database V&V", "testing, Flyway guard", "database-vv.yml"],
];

export default function DocsTestingPage() {
  const t = testCounts();
  return (
    <>
      <PageHeader
        eyebrow="Quality"
        title="Testing process"
        lead={
          <>
            Tests come first, by rule (CONSTITUTION §6, Gate 2): a change starts with tests that fail. The suites are
            layered from cheap static checks to full-stack browser journeys, and the heavy layers gate every merge.
          </>
        }
      />

      <StatGrid
        items={[
          { value: t.backend, label: "Backend test classes", hint: "backend-api/src/test" },
          { value: t.frontend, label: "Frontend test files", hint: "Vitest" },
          { value: t.bruno, label: "Bruno requests", hint: "backend-api/api-test" },
          { value: t.e2e, label: "Playwright specs", hint: "testing/e2e" },
        ]}
      />

      <SectionTitle id="pyramid">The layers</SectionTitle>
      <p>Step through the pyramid from the base. The counts are read from the repository when the site is built.</p>
      <TestPyramid
        facts={t}
        sources={{
          arch: src("backend-api/src/test/java/com/licensis/notaire/architecture/HexagonArchitectureTest.java"),
          backend: src("backend-api/src/test/"),
          frontend: src("frontend/src/"),
          bruno: src("backend-api/api-test/"),
          e2e: src("testing/e2e/"),
        }}
      />
      <Table
        head={["Layer", "Owner module", "Command"]}
        rows={LAYERS.map(([layer, owner, cmd]) => [layer, owner, <Code key={layer}>{cmd}</Code>])}
      />
      <p className="text-sm text-slate-600">Bruno and the PostgreSQL integration suite stay with the backend module (#1190).</p>

      <SectionTitle id="gates">Gates agents must run</SectionTitle>
      <ol className="list-decimal space-y-2 pl-5">
        <li>
          The local mirror of CI: <Code>bash workspace/sdlc/preflight.sh</Code> (see CI-PREFLIGHT).
        </li>
        <li>
          Merge authority: <Code>bash workspace/sdlc/check-heavy-ci.sh &lt;pr&gt;</Code>. Integration, coverage, Bruno
          and Playwright must be green (CI-MERGE-GATE).
        </li>
        <li>The test plan: TEST-PLAN.</li>
      </ol>
      <Sources
        items={[
          { label: "CI-PREFLIGHT.md", href: src("docs/300-development/CI-PREFLIGHT.md") },
          { label: "CI-MERGE-GATE.md", href: src("docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md") },
          { label: "TEST-PLAN.md", href: src("docs/300-development/303-testing/TEST-PLAN.md") },
          { label: "TESTING-PATTERNS.md", href: src("docs/300-development/303-testing/TESTING-PATTERNS.md") },
          { label: "A11Y-AXE.md", href: src("docs/300-development/303-testing/A11Y-AXE.md") },
        ]}
      />
    </>
  );
}
