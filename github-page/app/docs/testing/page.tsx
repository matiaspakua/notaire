import { DOCS, REPO } from "@/lib/docs-content";

const pyramid = [
  { layer: "Unit", owner: "backend-api / frontend", cmd: "mvn test · npm test (Vitest)" },
  { layer: "Integration (H2)", owner: "backend-api", cmd: "mvn test · integration suite" },
  { layer: "API (Bruno)", owner: "backend-api/api-test", cmd: "bru run (stays with backend per #1190)" },
  { layer: "E2E (Playwright)", owner: "testing/e2e", cmd: "npx playwright test" },
  { layer: "DB V&V", owner: "testing + Flyway guard", cmd: "testing/scripts · pg-integration" },
];

export default function DocsTestingPage() {
  return (
    <div className="space-y-8">
      <header className="space-y-3">
        <h1 className="text-3xl font-semibold text-neutral-900" style={{ fontFamily: "var(--font-display)" }}>
          Testing process
        </h1>
        <p className="text-neutral-600 max-w-3xl leading-relaxed">
          Constitution-mandated TDD, layered suites, and the heavy-CI merge gate. High-level tests
          stay PR-blocking; Bruno and pg-integration remain with the backend module (#1190).
        </p>
      </header>

      <section className="overflow-x-auto rounded-2xl border border-neutral-200 bg-white">
        <table className="min-w-full text-sm">
          <thead className="bg-neutral-50 text-left text-neutral-500">
            <tr>
              <th className="px-4 py-3 font-medium">Layer</th>
              <th className="px-4 py-3 font-medium">Owner module</th>
              <th className="px-4 py-3 font-medium">Command</th>
            </tr>
          </thead>
          <tbody>
            {pyramid.map((r) => (
              <tr key={r.layer} className="border-t border-neutral-100">
                <td className="px-4 py-3 font-medium text-neutral-900">{r.layer}</td>
                <td className="px-4 py-3 text-neutral-600">{r.owner}</td>
                <td className="px-4 py-3 font-mono text-xs text-neutral-500">{r.cmd}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="space-y-3 text-sm text-neutral-700 leading-relaxed">
        <h2 className="text-lg font-semibold text-neutral-900">Gates agents must run</h2>
        <ol className="list-decimal pl-5 space-y-2">
          <li>
            Local mirror of CI:{" "}
            <code className="bg-neutral-100 px-1 rounded text-xs">bash workspace/sdlc/preflight.sh</code>{" "}
            (<a className="text-[#0A84FF] hover:underline" href={`${DOCS}/300-development/CI-PREFLIGHT.md`}>
              (CI-PREFLIGHT)
            </a>
          </li>
          <li>
            Merge authority:{" "}
            <code className="bg-neutral-100 px-1 rounded text-xs">
              bash workspace/sdlc/check-heavy-ci.sh &lt;pr&gt;
            </code>{" "}
            — Integration + Coverage + Bruno + Playwright must be green (
            <a
              className="text-[#0A84FF] hover:underline"
              href={`${DOCS}/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md`}
            >
              CI-MERGE-GATE
            </a>
            ).
          </li>
          <li>
            Test plan:{" "}
            <a className="text-[#0A84FF] hover:underline" href={`${DOCS}/300-development/303-testing/TEST-PLAN.md`}>
              TEST-PLAN.md
            </a>
          </li>
        </ol>
      </section>

      <a className="text-sm text-[#0A84FF] hover:underline" href={`${REPO}/tree/main/testing`}>
        testing/ module on GitHub →
      </a>
    </div>
  );
}
