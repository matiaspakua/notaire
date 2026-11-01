import { Code, Diagram, PageHeader, SectionTitle, Sources, Table } from "@/components/FactoryUI";
import { Callout, StatGrid } from "@/components/DocsUI";
import { src } from "@/lib/ai-sdlc-content";
import { pageMetadata } from "@/lib/site";
import { flywaySettings, migrations, rollbackScripts, versions } from "@/lib/tech-facts";

export const metadata = pageMetadata("/docs/database/");

/** What each Flyway setting in application.properties means for Notaire. */
const SETTING_MEANING: Record<string, string> = {
  enabled: "Flyway runs at every backend start-up.",
  "baseline-on-migrate": "An existing database without history is baselined instead of rejected.",
  "baseline-version": "The baseline sits below V1, so every migration still runs on a fresh database.",
  locations: "Migrations are packaged inside the backend jar.",
  "out-of-order": "A migration numbered below the latest applied one is refused.",
  "validate-on-migrate": "Start-up fails if an applied migration's checksum changed.",
  "clean-disabled": "Nobody can wipe a database with flyway clean.",
  "connect-retries": "Start-up waits for PostgreSQL to accept connections.",
};

/** Phases of the schema history, by migration number (named from the migration files themselves). */
const PHASES = [
  { from: 1, to: 2, title: "Baseline", body: "The schema carried over from the original system, and its reference data." },
  { from: 3, to: 24, title: "Fixes and features", body: "Missing columns, workflow tables, roles and permissions, notebooks, registration drafts." },
  { from: 25, to: 37, title: "English names", body: "Tables renamed from Spanish to English, one bounded group per migration." },
  { from: 38, to: 53, title: "Hardening", body: "Numeric money, unique identities, revoked tokens, login attempts, append-only audit, bcrypt, role templates." },
];

const LIFECYCLE = `
flowchart LR
  DEV["Developer adds<br/>V{n}__change.sql"] --> CI["CI: Testcontainers PostgreSQL<br/>migrate + validate entities"]
  CI --> REL["product_release.py --write<br/>records the schema version"]
  REL --> BOOT["Backend start-up<br/>Flyway migrate + validate"]
  BOOT --> HIST[("flyway_schema_history")]
  BOOT -->|checksum changed| FAIL["start-up refused"]
`;

export default function DatabasePage() {
  const list = migrations();
  const latest = list.at(-1);
  const rollbacks = rollbackScripts();
  const settings = flywaySettings();
  const v = versions();

  return (
    <>
      <PageHeader
        eyebrow="Architecture internals"
        title="Database & Flyway"
        lead={
          <>
            Notaire stores everything in PostgreSQL {v.postgres ?? "—"}. The schema has exactly one source: the
            versioned SQL migrations that Flyway applies when the backend starts. Hibernate never creates or alters
            tables in production, and a test checks that the entities match the migrated schema (ADR-004, ADR-007).
          </>
        }
      />

      <StatGrid
        items={[
          { value: list.length, label: "Migrations", hint: "V1 … latest" },
          { value: latest ? `V${latest.version}` : "—", label: "Schema version", hint: latest?.name },
          { value: rollbacks.length, label: "Rollback scripts", hint: "applied by hand only" },
          { value: settings.length, label: "Flyway settings", hint: "application.properties" },
        ]}
      />

      <SectionTitle id="lifecycle">How a schema change ships</SectionTitle>
      <Diagram chart={LIFECYCLE} caption="A migration is tested in CI against a real PostgreSQL, recorded in the release manifest, and applied at start-up." />
      <ul className="my-4 list-disc space-y-2 pl-6">
        <li>
          One change, one new file: <Code>V&#123;n&#125;__short_description.sql</Code>. An applied migration is never
          edited; Flyway&apos;s checksum check refuses to start if one is.
        </li>
        <li>
          After a migration, <Code>workspace/release/product_release.py --write</Code> records the schema version in{" "}
          <Code>release/product-release.json</Code>, so each product release names its database version.
        </li>
        <li>
          <Code>R&#123;n&#125;__*.sql</Code> files are hand-applied rollback scripts for the migration with the same
          number. Their names are not valid repeatable migrations, so Flyway skips them on purpose.
        </li>
      </ul>
      <Sources
        items={[
          { label: "db/migration/", href: src("backend-api/src/main/resources/db/migration/") },
          { label: "database-migrations rule", href: src(".claude/rules/database-migrations.md") },
          { label: "flyway skill", href: src(".claude/skills/flyway/SKILL.md") },
          { label: "ADR-007", href: src("docs/200-architecture/202-ADR/ADR-007-database-schema-versioning-flyway.md") },
          { label: "product_release.py", href: src("workspace/release/product_release.py") },
        ]}
      />

      <SectionTitle id="settings">The Flyway settings</SectionTitle>
      <Table
        head={["Setting", "Value", "Why"]}
        rows={settings.map((s) => [<Code key={s.key}>spring.flyway.{s.key}</Code>, <Code key={`${s.key}-v`}>{s.value}</Code>, SETTING_MEANING[s.key] ?? ""])}
      />
      <Sources items={[{ label: "application.properties", href: src("backend-api/src/main/resources/application.properties") }]} />

      <SectionTitle id="history">The schema history</SectionTitle>
      <p>Every migration on main, grouped by what it did. The list is read from the migration folder when the site is built.</p>
      <div className="my-6 space-y-6">
        {PHASES.map((phase) => {
          const items = list.filter((m) => m.version >= phase.from && m.version <= phase.to);
          if (items.length === 0) return null;
          return (
            <section key={phase.title} aria-labelledby={`phase-${phase.from}`} className="rounded-2xl border border-slate-200 bg-white p-5">
              <h3 id={`phase-${phase.from}`} className="font-semibold text-slate-900">
                {phase.title}{" "}
                <span className="font-mono text-sm font-normal text-slate-500">
                  V{items[0].version}–V{items.at(-1)?.version}
                </span>
              </h3>
              <p className="mt-1 text-sm text-slate-600">{phase.body}</p>
              <ol className="mt-3 grid gap-x-6 gap-y-1 text-sm sm:grid-cols-2">
                {items.map((m) => (
                  <li key={m.file} className="flex gap-2">
                    <span className="w-10 shrink-0 font-mono text-xs leading-5 text-slate-500">V{m.version}</span>
                    <span className="text-slate-700">{m.name}</span>
                  </li>
                ))}
              </ol>
            </section>
          );
        })}
      </div>

      <SectionTitle id="guards">What keeps the schema honest</SectionTitle>
      <Table
        head={["Check", "What it proves", "Where"]}
        rows={[
          ["Schema validation test", "The migrated schema matches every JPA entity (no drift).", <Code key="f">FlywaySchemaValidationIntegrationTest</Code>],
          ["Integration tests", "Run on a real PostgreSQL through Testcontainers, migrations included.", <Code key="t">backend-api/src/test</Code>],
          ["Database V&V", "The database suite on its own workflow.", <Code key="d">database-vv.yml</Code>],
          ["Backup and restore (not yet active)", "Skips itself until the backup tooling of #256 exists, rather than reporting a false green.", <Code key="b">backup-restore-smoke.yml</Code>],
        ]}
      />
      <Sources
        items={[
          { label: "FlywaySchemaValidationIntegrationTest.java", href: src("backend-api/src/test/java/com/licensis/notaire/integration/FlywaySchemaValidationIntegrationTest.java") },
          { label: "database-vv.yml", href: src(".github/workflows/database-vv.yml") },
          { label: "backup-restore-smoke.yml", href: src(".github/workflows/backup-restore-smoke.yml") },
        ]}
      />
      <Callout title="Known gap">
        Some CI jobs still start the backend with Hibernate&apos;s <Code>ddl-auto=update</Code>, which can hide a
        missing migration in those runs; production uses <Code>none</Code>. It is tracked as its own issue.
      </Callout>
    </>
  );
}
