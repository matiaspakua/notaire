import Link from "next/link";
import { PageHeader, SectionTitle, Table } from "@/components/FactoryUI";
import { pageMetadata } from "@/lib/site";

export const metadata = pageMetadata("/docs/glossary/");

const DOMAIN: [string, string][] = [
  ["Use case (CU)", "A written business scenario, numbered CU01 onward. Every change cites one."],
  ["Requirement (RF / RNF)", "A functional (RF) or non-functional (RNF) requirement. A traceability matrix links them to use cases."],
  ["Person, client", "Anyone the office deals with; a client is a person who buys services."],
  ["Budget (presupuesto)", "The priced list of a procedure for a client. Payments are collected against it."],
  ["Management (gestión)", "The case file for one procedure, with a status, a history and linked documents."],
  ["Procedure type", "The kind of procedure, such as a sale, a donation or a mortgage. Some have a workflow."],
  ["Workflow", "A data-driven graph of statuses and the allowed transitions between them."],
  ["Deed (escritura)", "The notarial instrument for the management. It needs a folio to be signed."],
  ["Folio", "A numbered page of the protocol book."],
  ["Testimony (testimonio)", "The authorised copy of a signed deed. It goes to the registry and may be re-entered."],
  ["Registry (registro)", "The public office where the deed is recorded."],
  ["Substitution (suplencia)", "A period in which one user covers another, so work is redirected."],
  ["Audit trail", "The record of who created, changed, deleted or logged in, taken from the security context."],
];

const ENGINEERING: [string, string][] = [
  ["ADR", "Architecture Decision Record: one decision, its context, options and consequences."],
  ["OpenSpec", "The specification method and CLI. A change has a proposal, traceability, specs, design and tasks."],
  ["Constitution", "CONSTITUTION.md: the mandatory process and the five quality gates for people and agents."],
  ["Gate", "A hard stop. Gate 1 specification, 2 tests first, 3 before a pull request, 4 before merge, 5 before close."],
  ["TDD", "Test-driven development: write a failing test, then the code that passes it."],
  ["Atomic commit", "One logical change per commit, in Conventional Commits form."],
  ["Flyway", "The tool that versions the database schema. A migration that ran is never edited."],
  ["Hexagonal architecture", "Ports and Adapters: rules in the middle, controllers and persistence at the edges."],
  ["JWT", "JSON Web Token, carried in an HttpOnly cookie, that proves who is calling."],
  ["Ratchet", "A threshold that may rise but never fall, used for coverage floors."],
  ["Preflight", "A local script that mirrors the gates so a push cannot fail for a known reason."],
  ["Version tag", "A release tag; the product workflows run on it, not on every pull request."],
  ["Testcontainers", "Starts a real PostgreSQL in a container for integration tests."],
  ["Bruno", "The API test tool; the collection lives with the backend."],
  ["Playwright", "The browser tool for end-to-end tests of the real UI."],
  ["SAST, SCA, DAST", "Static code analysis, dependency and image scanning, and scanning of a running app."],
  ["Owner, Foreman, Worker", "The human code owner; the supervising agent; the implementing agent."],
  ["Agent queue, lane", "The shared list of claimable work and the derived label that keeps agents from colliding."],
  ["Observability", "Metrics, logs and traces that show what the running system is doing."],
  ["Prometheus, Grafana, Loki, Tempo", "Metrics store, dashboards, log store and trace store."],
  ["SonarQube", "Code quality and static analysis with a gate on new code."],
  ["Smoke test", "A short check that a freshly deployed system answers correctly."],
];

export default function GlossaryPage() {
  return (
    <>
      <PageHeader
        eyebrow="Technical docs · Glossary"
        title="Every term, in plain words"
        lead={
          <>
            Notarial terms first, engineering and AI SDLC terms second. For the picture behind them, see{" "}
            <Link href="/product/anatomy/">how Notaire works</Link>, the <Link href="/docs/engineering/">engineering anatomy</Link> and the{" "}
            <Link href="/ai-sdlc/anatomy/">AI SDLC anatomy</Link>.
          </>
        }
      />
      <SectionTitle id="domain">The notary office</SectionTitle>
      <Table head={["Term", "Meaning"]} rows={DOMAIN} />
      <SectionTitle id="engineering">Engineering and the AI SDLC</SectionTitle>
      <Table head={["Term", "Meaning"]} rows={ENGINEERING} />
    </>
  );
}
