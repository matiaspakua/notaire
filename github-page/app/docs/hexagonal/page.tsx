import { Card, Code, Diagram, PageHeader, SectionTitle, Sources, Table } from "@/components/FactoryUI";
import { Callout, StatGrid } from "@/components/DocsUI";
import { src } from "@/lib/ai-sdlc-content";
import { pageMetadata } from "@/lib/site";
import { hexagon, versions } from "@/lib/tech-facts";

export const metadata = pageMetadata("/docs/hexagonal/");


const HEXAGON_CHART = `
flowchart LR
  subgraph IN [Inbound adapters]
    WEB["adapter.in.web<br/>controllers, web DTOs, mappers"]
    SEC["security filters<br/>JWT, role/module guard"]
  end
  subgraph APP [Application]
    PIN(["port.in<br/>one interface per use case"])
    UC["usecase<br/>@Service + @Transactional"]
    POUT(["port.out<br/>repositories, gateways, clock"])
  end
  DOM{{"domain<br/>entities, value objects, rules"}}
  subgraph OUT [Outbound adapters]
    PER["adapter.out.persistence<br/>JpaEntity, JpaRepository, Mapper"]
    PDF["adapter.out.pdf / report"]
  end
  DB[(PostgreSQL)]
  WEB --> PIN
  SEC --> PIN
  PIN --> UC
  UC --> DOM
  UC --> POUT
  PER -. implements .-> POUT
  PDF -. implements .-> POUT
  PER --> DB
`;

const PAYMENT_FLOW = `
sequenceDiagram
  autonumber
  participant C as PaymentController
  participant P as ProcessPaymentUseCase (port.in)
  participant S as ProcessPaymentService (usecase)
  participant D as domain.payment
  participant R as PaymentRepositoryPort (port.out)
  participant A as PaymentPersistenceAdapter
  C->>P: ProcessPaymentCommand
  P->>S: process(command)
  S->>D: BudgetCharges / Money rules
  S->>R: save(NewPayment)
  R->>A: implemented by
  A-->>S: PaymentDetails
  S-->>C: result, mapped by PaymentWebMapper
`;

const RULES: [string, string][] = [
  ["R1", "Inbound adapters do not reach repositories, legacy JPA or entities."],
  ["R2", "The application layer depends on domain and ports only, plus @Service and @Transactional."],
  ["R3", "The domain depends on nothing but the JDK and itself."],
  ["R4", "Outbound adapters do not depend on inbound adapters."],
  ["R5", "Inbound adapters do not depend on outbound adapters."],
  ["R6", "Nothing new depends on the legacy jpa and service packages."],
  ["R7", "Use-case slices do not form dependency cycles beyond the baseline."],
];

export default function HexagonalPage() {
  const h = hexagon();
  const v = versions();
  return (
    <>
      <PageHeader
        eyebrow="Architecture internals"
        title="Hexagonal architecture"
        lead={
          <>
            The backend and the front end are both built as ports and adapters: business rules sit in the middle, and
            HTTP, JPA, PDF and the browser are plugged in from the outside. The Owner made it the rule for all code in
            ADR-030, after a pilot on the payment slice (ADR-021), and tests keep the boundaries from eroding.
          </>
        }
      />

      <StatGrid
        items={[
          { value: h.inPorts, label: "Inbound port files", hint: "application/port/in" },
          { value: h.useCases, label: "Use-case files", hint: `in ${h.useCaseContexts} contexts` },
          { value: h.outPorts, label: "Outbound port files", hint: "application/port/out" },
          { value: h.baselineViolations, label: "Known violations", hint: "ArchUnit baseline" },
        ]}
      />

      <SectionTitle id="map">The map</SectionTitle>
      <p>
        Dependencies point inward only: <Code>adapter</Code> → <Code>application</Code> → <Code>domain</Code>. A
        controller never sees a JPA entity; a use case never sees HTTP. <Code>config</Code> is the composition root
        that wires the adapters to the ports.
      </p>
      <Diagram chart={HEXAGON_CHART} caption="Backend packages under com.licensis.notaire and the direction of their dependencies (ADR-030)." />
      <Table
        head={["Package", "Holds", "May depend on"]}
        rows={[
          [<Code key="d">domain.&lt;context&gt;</Code>, "Entities, value objects, domain services and exceptions. Plain Java.", "JDK only"],
          [<Code key="pi">application.port.in</Code>, "One interface per use case, with command and result records.", "domain"],
          [<Code key="po">application.port.out</Code>, "What use cases need from the outside, in domain terms.", "domain"],
          [<Code key="u">application.usecase</Code>, "Classes implementing inbound ports; they own the transaction.", "domain, ports"],
          [<Code key="w">adapter.in.web</Code>, "Controllers, web DTOs, mappers, request validation.", "inbound ports"],
          [<Code key="p">adapter.out.persistence</Code>, "JPA entities, Spring Data repositories, mappers, persistence adapters.", "outbound ports, domain"],
          [<Code key="c">config</Code>, "Spring wiring, security, OpenAPI.", "everything"],
        ]}
      />
      <Sources
        items={[
          { label: "ADR-030", href: src("docs/200-architecture/202-ADR/ADR-030-hexagonal-architecture-full-adoption.md") },
          { label: "ADR-021 pilot", href: src("docs/200-architecture/202-ADR/ADR-021-hexagonal-architecture-pilot.md") },
          { label: "application/", href: src("backend-api/src/main/java/com/licensis/notaire/application/") },
          { label: "adapter/", href: src("backend-api/src/main/java/com/licensis/notaire/adapter/") },
        ]}
      />

      <SectionTitle id="slice">One request through a slice</SectionTitle>
      <p>The payment slice, the first one migrated, shows the whole path of a request:</p>
      <Diagram chart={PAYMENT_FLOW} caption="POST a payment: controller → inbound port → use case → domain → outbound port → persistence adapter." />
      <Sources
        items={[
          { label: "PaymentController.java", href: src("backend-api/src/main/java/com/licensis/notaire/adapter/in/web/payment/PaymentController.java") },
          { label: "ProcessPaymentUseCase.java", href: src("backend-api/src/main/java/com/licensis/notaire/application/port/in/payment/ProcessPaymentUseCase.java") },
          { label: "ProcessPaymentService.java", href: src("backend-api/src/main/java/com/licensis/notaire/application/usecase/payment/ProcessPaymentService.java") },
          { label: "PaymentRepositoryPort.java", href: src("backend-api/src/main/java/com/licensis/notaire/application/port/out/payment/PaymentRepositoryPort.java") },
          { label: "PaymentPersistenceAdapter.java", href: src("backend-api/src/main/java/com/licensis/notaire/adapter/out/persistence/payment/PaymentPersistenceAdapter.java") },
          { label: "domain/payment/", href: src("backend-api/src/main/java/com/licensis/notaire/domain/payment/") },
        ]}
      />

      <SectionTitle id="enforcement">How the boundaries are enforced</SectionTitle>
      <p>
        ArchUnit {v.archunit ?? "—"} turns each rule into a test that runs with the backend suite. Violations that
        existed when a rule was introduced go into a baseline file that may only shrink; a new violation fails the
        build. Today the hexagon baseline holds {h.baselineViolations} entries.
      </p>
      <Table head={["Rule", "What it forbids"]} rows={RULES.map(([id, text]) => [id, text])} />
      <Sources
        items={[
          { label: "HexagonArchitectureTest.java", href: src("backend-api/src/test/java/com/licensis/notaire/architecture/HexagonArchitectureTest.java") },
          { label: "UseCaseSliceCycleArchitectureTest.java", href: src("backend-api/src/test/java/com/licensis/notaire/architecture/UseCaseSliceCycleArchitectureTest.java") },
          { label: "hexagon-baseline.txt", href: src("backend-api/src/test/resources/architecture/hexagon-baseline.txt") },
          { label: "hexagonal-arch skill", href: src(".claude/skills/hexagonal-arch/SKILL.md") },
        ]}
      />

      <SectionTitle id="frontend">The same shape in the front end</SectionTitle>
      <p>
        <Code>frontend/src</Code> follows the same idea: pages and hooks call use cases; only HTTP adapters may call
        the API client. ESLint rules stop UI code from importing <Code>lib/api-client</Code> or calling{" "}
        <Code>fetch</Code>, and stop <Code>core</Code> from importing React, Next or adapters.
      </p>
      <div className="my-6 grid gap-3 sm:grid-cols-2">
        <Card>
          <p className="font-semibold text-slate-900">core/&lt;context&gt;</p>
          <p className="mt-1 text-sm text-slate-600">
            Domain types and rules, use cases and their <Code>ports.ts</Code> — {h.frontendContexts} contexts, no React.
          </p>
        </Card>
        <Card>
          <p className="font-semibold text-slate-900">adapters/http/&lt;context&gt;</p>
          <p className="mt-1 text-sm text-slate-600">
            {h.frontendHttpAdapters} adapters implement the ports with the generated API types and map transport shapes to
            domain types.
          </p>
        </Card>
        <Card>
          <p className="font-semibold text-slate-900">app, components, hooks</p>
          <p className="mt-1 text-sm text-slate-600">The UI adapter: pages, components and React Query hooks that call use cases.</p>
        </Card>
        <Card>
          <p className="font-semibold text-slate-900">composition/</p>
          <p className="mt-1 text-sm text-slate-600">Builds each use case with its HTTP adapter, so tests swap an adapter without module mocks.</p>
        </Card>
      </div>
      <Sources
        items={[
          { label: "frontend/src/core/", href: src("frontend/src/core/") },
          { label: "frontend/src/adapters/http/", href: src("frontend/src/adapters/http/") },
          { label: "frontend/src/composition/", href: src("frontend/src/composition/") },
          { label: "frontend/eslint.config.mjs", href: src("frontend/eslint.config.mjs") },
        ]}
      />

      <SectionTitle id="legacy">Where it is not finished</SectionTitle>
      <Callout title="An honest status">
        <p>
          The domain has {h.domainContexts} extracted contexts while the use cases span {h.useCaseContexts}; the rest
          still work on legacy entities. The legacy packages are closed — rule R6 and ADR-030 rule 8 allow no new
          dependents — and are deleted context by context. The remaining legacy JPA package is tracked in #576.
        </p>
      </Callout>
      <Table head={["Legacy package", "Java files left"]} rows={h.legacy.map((l) => [<Code key={l.pkg}>{l.pkg}</Code>, String(l.files)])} />
    </>
  );
}
