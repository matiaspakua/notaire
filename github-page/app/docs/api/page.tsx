import { Code, Diagram, PageHeader, SectionTitle, Sources, Table } from "@/components/FactoryUI";
import { Callout, StatGrid } from "@/components/DocsUI";
import { src } from "@/lib/ai-sdlc-content";
import { pageMetadata } from "@/lib/site";
import { BRUNO_SAMPLE_FILE, bruno, brunoDefectsFixed, brunoSample, openapi, versions } from "@/lib/tech-facts";

export const metadata = pageMetadata("/docs/api/");

const CONTRACT_FLOW = `
flowchart LR
  CODE["Spring controllers<br/>+ springdoc annotations"] -->|export-openapi.sh on H2| SPEC["backend-api/openapi/<br/>openapi.yaml (committed)"]
  SPEC -->|openapi-typescript| TYPES["frontend/src/types/<br/>api.generated.ts"]
  TYPES --> HTTP["frontend adapters/http"]
  SPEC -->|oasdiff vs base branch| BREAK{"breaking change?"}
  BREAK -->|listed in accepted-breaking-changes.d| OK["allowed, with an issue"]
  BREAK -->|not listed| FAIL["PR fails"]
  SPEC --> REACH["contracts: every endpoint<br/>has a UI caller or a reason"]
  SPEC -. exercised by .-> BRU["Bruno suite<br/>backend-api/api-test"]
`;

export default function ApiPage() {
  const api = openapi();
  const b = bruno();
  const v = versions();
  return (
    <>
      <PageHeader
        eyebrow="Architecture internals"
        title="API contract & Bruno"
        lead={
          <>
            The REST API is code-first: controllers are the source, springdoc {v.springdoc ?? "—"} turns them into an
            OpenAPI document, and that document is committed. CI checks it is current, refuses unannounced breaking
            changes, generates the front end&apos;s types from it, and a Bruno suite calls the running API.
          </>
        }
      />

      <StatGrid
        items={[
          { value: api?.paths ?? "—", label: "API paths", hint: "openapi.yaml" },
          { value: api?.operations ?? "—", label: "Operations", hint: "operationIds" },
          { value: b.requests, label: "Bruno requests", hint: `in ${b.folders.length} folders` },
          { value: brunoDefectsFixed(), label: "Defects found", hint: "by the Bruno suite, all fixed" },
        ]}
      />

      <SectionTitle id="contract">One contract, four checks</SectionTitle>
      <Diagram chart={CONTRACT_FLOW} caption="The committed OpenAPI document sits between the backend and everything that depends on it." />
      <Table
        head={["Check", "Fails when", "Where"]}
        rows={[
          ["Export matches", "A controller changed but openapi.yaml was not regenerated.", <Code key="e">openapi-contract.yml</Code>],
          ["Breaking changes", "oasdiff finds an error-level break against the base branch that no accepted-breaking-changes.d file lists.", <Code key="o">openapi-contract.yml</Code>],
          ["Front-end types", "api.generated.ts differs from what openapi-typescript generates.", <Code key="f">frontend-ci.yml</Code>],
          ["Reachability", "An endpoint has no caller in the front end and no reason in the allowlist.", <Code key="r">contracts/tests</Code>],
        ]}
      />
      <Sources
        items={[
          { label: "openapi.yaml", href: src("backend-api/openapi/openapi.yaml") },
          { label: "export-openapi.sh", href: src("backend-api/tools/export-openapi.sh") },
          { label: "accepted-breaking-changes.d/", href: src("backend-api/openapi/accepted-breaking-changes.d/") },
          { label: "openapi-contract.yml", href: src(".github/workflows/openapi-contract.yml") },
          { label: "FRONTEND-OPENAPI-TYPES.md", href: src("docs/200-architecture/203-design/FRONTEND-OPENAPI-TYPES.md") },
          { label: "test_api_reachability.py", href: src("contracts/tests/test_api_reachability.py") },
          { label: "ADR-020 exposure", href: src("docs/200-architecture/202-ADR/ADR-020-openapi-exposure-policy.md") },
          { label: "ADR-023 naming", href: src("docs/200-architecture/202-ADR/ADR-023-rest-resource-naming.md") },
        ]}
      />
      <Callout title="Why code-first">
        The controllers already existed when the contract was introduced, so the spec is generated from them rather
        than written by hand. Committing the generated file turns every API change into a visible diff in the pull
        request, which is what the breaking-change and type checks compare.
      </Callout>

      <SectionTitle id="bruno">The Bruno API suite</SectionTitle>
      <p>
        Bruno keeps API tests as plain files in the repository, so they are reviewed like code. The collection uses
        the YAML OpenCollection format (Bruno CLI 4 or later): one file per request, with its assertions, grouped by
        resource. <Code>00-auth</Code> runs first: it logs in and stores the JWT that the other folders send. This is that first request, read from the repository:
      </p>
      <pre className="my-6 overflow-x-auto rounded-2xl bg-slate-900 p-5 text-[13px] leading-relaxed text-slate-100" tabIndex={0} aria-label={`Bruno request ${BRUNO_SAMPLE_FILE}`}>
        <code>{brunoSample()}</code>
      </pre>
      <Table
        head={["Where", "Command"]}
        rows={[
          ["Locally, backend on :8080", <Code key="l">cd backend-api/api-test && npx @usebruno/cli run . -r --env Development</Code>],
          ["CI", <span key="c">The E2E workflow starts the backend, then runs the suite with JUnit, JSON and HTML reporters and uploads the reports.</span>],
        ]}
      />
      <p className="mt-4 text-sm text-slate-600">Resource folders: {b.folders.join(", ")}.</p>
      <Sources
        items={[
          { label: "backend-api/api-test/", href: src("backend-api/api-test/") },
          { label: "COVERAGE.md", href: src("backend-api/api-test/COVERAGE.md") },
          { label: "00-auth/01-login.yml", href: src("backend-api/api-test/00-auth/01-login.yml") },
          { label: "opencollection.yml", href: src("backend-api/api-test/opencollection.yml") },
          { label: "playwright-e2e.yml", href: src(".github/workflows/playwright-e2e.yml") },
          { label: "api-contract-testing skill", href: src(".claude/skills/api-contract-testing/SKILL.md") },
        ]}
      />
    </>
  );
}
