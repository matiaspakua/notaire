import { type FormEvent, useState } from "react";

import { knowledge } from "../api.ts";
import { Icon } from "../components/Icon.tsx";
import { Empty, Loaded, Section } from "../components/State.tsx";
import { useWorkspace } from "../context.ts";
import { useResource } from "../hooks.ts";
import { useLang, useT } from "../i18n.ts";
import type { Hit, KnowledgeMetrics } from "../model.ts";

function Figures({ items }: { items: [string, string][] }) {
  return (
    <dl className="figures">
      {items.map(([label, value]) => (
        <div key={label}>
          <dt>{label}</dt>
          <dd className="num">{value}</dd>
        </div>
      ))}
    </dl>
  );
}

function Metrics() {
  const t = useT();
  const lang = useLang();
  const { workspace } = useWorkspace();
  const metrics = useResource<KnowledgeMetrics>(knowledge(workspace.id, "metrics"), 60_000);
  const [listing, setListing] = useState(false);
  const orphans = useResource<{ isolated: { id: string; path: string; project?: string }[] }>(listing ? knowledge(workspace.id, "orphans") : null);
  const n = (value: unknown) => (typeof value === "number" ? value.toLocaleString(lang) : "—");
  const pct = (value: unknown) => (typeof value === "number" ? `${(value * 100).toLocaleString(lang, { maximumFractionDigits: 1 })}%` : "—");
  return (
    <Loaded resource={metrics} rows={4}>
      {(data) => (
        <div className="stack">
          <Section title={t("knowledge.index")}>
            <Figures items={[
              [t("knowledge.files"), n(data.index.files)],
              [t("knowledge.nodes"), n(data.index.nodes)],
              [t("knowledge.edges"), n(data.index.edges)],
            ]} />
            {Array.isArray(data.index.projects) && data.index.projects.length > 0 && (
              <ul className="chips" aria-label={t("knowledge.repos")}>
                {(data.index.projects as (string | { name: string })[]).map((item) => {
                  const name = typeof item === "string" ? item : item.name;
                  return <li key={name} className="tag tag-repo">{name}</li>;
                })}
              </ul>
            )}
          </Section>
          <Section title={t("knowledge.orphans")}>
            <Figures items={[
              [t("knowledge.isolated"), n(data.orphans.isolated)],
              [t("knowledge.historyOnly"), n(data.orphans.history_only)],
            ]} />
            {listing ? (
              <Loaded resource={orphans} rows={3}>
                {(list) => (
                  <ul className="paths">
                    {list.isolated.slice(0, 200).map((item) => (
                      <li key={item.id} className="mono small">
                        {item.project && <span className="tag tag-repo">{item.project}</span>} {item.path}
                      </li>
                    ))}
                  </ul>
                )}
              </Loaded>
            ) : (
              <button type="button" className="btn btn-quiet" onClick={() => setListing(true)}>{t("knowledge.listOrphans")}</button>
            )}
          </Section>
          <Section title={t("knowledge.usage")}>
            <Figures items={[
              [t("knowledge.calls"), n(data.usage.server?.calls)],
              [t("knowledge.p95"), typeof data.usage.server?.p95_ms === "number" ? `${n(data.usage.server.p95_ms)} ms` : "—"],
              [t("knowledge.zero"), pct(data.usage.server?.zero_result_rate)],
              [t("knowledge.adoption"), pct(data.usage.sessions?.adoption)],
            ]} />
          </Section>
        </div>
      )}
    </Loaded>
  );
}

export function Knowledge() {
  const t = useT();
  const { workspace } = useWorkspace();
  const [query, setQuery] = useState("");
  const [submitted, setSubmitted] = useState("");
  const hits = useResource<Hit[]>(
    submitted ? knowledge(workspace.id, "search", new URLSearchParams({ q: submitted, k: "20" }).toString()) : null,
  );
  const submit = (event: FormEvent) => {
    event.preventDefault();
    setSubmitted(query.trim());
  };
  if (!workspace.knowledge) {
    return <Empty>{t("error.knowledge_not_configured")}</Empty>;
  }
  return (
    <div className="split split-wide">
      <Section title={t("knowledge.submit")}>
        <form className="search" role="search" onSubmit={submit}>
          <label htmlFor="kq" className="sr-only">{t("knowledge.search")}</label>
          <Icon name="search" />
          <input id="kq" type="search" value={query} onChange={(e) => setQuery(e.target.value)} placeholder={t("knowledge.placeholder")} />
          <button type="submit" className="btn btn-primary" disabled={!query.trim()}>{t("knowledge.submit")}</button>
        </form>
        {submitted && (
          <Loaded resource={hits} rows={5}>
            {(data) =>
              data.length === 0 ? <Empty>{t("knowledge.none")}</Empty> : (
                <>
                  <p className="muted small" role="status">{t("knowledge.results", { n: data.length })}</p>
                  <ol className="hits">
                    {data.map((hit) => (
                      <li key={hit.id} className="hit">
                        <div className="hit-head">
                          <span className="hit-name">{hit.name}</span>
                          <span className="tag">{hit.kind}</span>
                          {hit.project && <span className="tag tag-repo">{hit.project}</span>}
                        </div>
                        <span className="mono small">{hit.path}{hit.start ? `:${hit.start}` : ""}</span>
                        {hit.why && <p className="muted small">{hit.why}</p>}
                      </li>
                    ))}
                  </ol>
                </>
              )
            }
          </Loaded>
        )}
      </Section>
      <Metrics />
    </div>
  );
}
