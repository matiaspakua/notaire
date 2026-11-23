import { factory } from "../api.ts";
import { Kpi, Loaded, Section } from "../components/State.tsx";
import { useWorkspace } from "../context.ts";
import {
  changeLabel,
  costLabel,
  type CostSummary,
  type FactoryMetrics,
  liveSamples,
  minutes,
  totalTokens,
} from "../factory-metrics.ts";
import { useResource } from "../hooks.ts";
import { type Key, useLang, useT } from "../i18n.ts";

function Cost({ cost }: { cost: CostSummary }) {
  const t = useT();
  const lang = useLang();
  const label = costLabel(cost, lang);
  return (
    <span className="cost-cell">
      <span className="mono">{label.text}</span>
      {label.note !== "priced" && <span className="small muted"> {t(`fm.cost.${label.note}` as Key)}</span>}
    </span>
  );
}

function Overall({ data }: { data: FactoryMetrics }) {
  const t = useT();
  const lang = useLang();
  const o = data.overall;
  return (
    <div className="kpi-row" data-testid="factory-overall">
      <Kpi label={t("fm.timePerIntent")} tone="plan" value={minutes(o.per_intent.median_active_s, lang)}
        sub={t("fm.wallMedian", { wall: minutes(o.per_intent.median_wall_s, lang) })} />
      <Kpi label={t("fm.costPerIntent")} tone="ship"
        value={o.per_intent.median_usd === null ? "—" : costLabel({ ...o.cost, usd: o.per_intent.median_usd }, lang).text}
        sub={t("fm.median")} />
      <Kpi label={t("fm.costTotal")} tone="publish" value={<Cost cost={o.cost} />}
        sub={o.overhead?.cost.usd ? t("fm.overhead", { usd: costLabel(o.overhead.cost, lang).text }) : t("fm.allRuns", { n: o.runs })} />
      <Kpi label={t("fm.tokens")} value={totalTokens(o.tokens).toLocaleString(lang)}
        sub={t("fm.tokensSplit", { cache: o.tokens.cache_read.toLocaleString(lang) })} />
      <Kpi label={t("fm.repolensCalls")} tone="prove" value={o.repolens.calls.toLocaleString(lang)}
        sub={o.repolens.p50_ms === null ? undefined : t("fm.p50", { ms: o.repolens.p50_ms })} />
    </div>
  );
}

function Intents({ data }: { data: FactoryMetrics }) {
  const t = useT();
  const lang = useLang();
  if (data.intents.length === 0) return <p className="muted">{t("fm.noIntents")}</p>;
  return (
    <div className="table-wrap" role="region" tabIndex={0} aria-label={t("fm.title")}>
      <table className="table">
        <caption className="sr-only">{t("fm.title")}</caption>
        <thead>
          <tr>
            <th scope="col">{t("fm.col.intent")}</th>
            <th scope="col" className="num">{t("fm.col.active")}</th>
            <th scope="col" className="num">{t("fm.col.wall")}</th>
            <th scope="col" className="num">{t("fm.col.corrections")}</th>
            <th scope="col" className="num">{t("fm.col.tokens")}</th>
            <th scope="col" className="num">{t("fm.col.cost")}</th>
            <th scope="col" className="num">{t("fm.col.repolens")}</th>
          </tr>
        </thead>
        <tbody>
          {data.intents.map((intent) => (
            <tr key={intent.id}>
              <th scope="row">
                {intent.title ?? intent.id}
                <span className="small muted mono"> {intent.runs.join(", ")}</span>
              </th>
              <td className="num mono">{minutes(intent.time.active_s, lang)}</td>
              <td className="num mono">{minutes(intent.time.wall_s, lang)}</td>
              <td className="num mono">{intent.time.corrections}</td>
              <td className="num mono">{totalTokens(intent.tokens).toLocaleString(lang)}</td>
              <td className="num"><Cost cost={intent.cost} /></td>
              <td className="num mono">{intent.repolens.calls}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function RepolensEffect({ data }: { data: FactoryMetrics }) {
  const t = useT();
  const live = liveSamples(data.repolens_effect);
  return (
    <div className="stack-tight" data-testid="repolens-effect">
      <p className="small muted">{t("fm.benchHint")}</p>
      {data.repolens_effect.bench.length === 0 ? (
        <p className="muted">{t("fm.noBench")}</p>
      ) : (
        <div className="table-wrap" role="region" tabIndex={0} aria-label={t("fm.effect")}>
          <table className="table">
            <caption className="sr-only">{t("fm.effect")}</caption>
            <thead>
              <tr>
                <th scope="col">{t("fm.col.bench")}</th>
                <th scope="col" className="num">{t("fm.col.n")}</th>
                <th scope="col" className="num">{t("fm.col.costChange")}</th>
                <th scope="col" className="num">{t("fm.col.tokensChange")}</th>
                <th scope="col" className="num">{t("fm.col.toolsChange")}</th>
                <th scope="col" className="num">{t("fm.col.timeChange")}</th>
                <th scope="col" className="num">{t("fm.col.recall")}</th>
              </tr>
            </thead>
            <tbody>
              {data.repolens_effect.bench.map((b) => (
                <tr key={b.file}>
                  <th scope="row">{b.model ?? b.file}<span className="small muted mono"> {b.file}</span></th>
                  <td className="num mono">{b.runs_per_arm}</td>
                  <td className="num mono">{changeLabel(b.cost_usd_change_pct)}</td>
                  <td className="num mono">{changeLabel(b.input_tokens_change_pct)}</td>
                  <td className="num mono">{changeLabel(b.tool_calls_change_pct)}</td>
                  <td className="num mono">{changeLabel(b.duration_change_pct)}</td>
                  <td className="num mono">{b.recall_change === null ? "—" : `${b.recall_change >= 0 ? "+" : "−"}${Math.abs(b.recall_change * 100).toFixed(1)} pt`}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      <p className="small">{t("fm.live", { a: live.withRepolens, b: live.without })}</p>
    </div>
  );
}

export function IntentMetrics() {
  const t = useT();
  const { workspace } = useWorkspace();
  const metrics = useResource<FactoryMetrics>(factory(workspace.id, "/metrics/factory"), 60_000);
  return (
    <section className="phase-group" data-testid="intent-metrics" aria-labelledby="fm-title">
      <header className="phase-header">
        <span className="phase-indicator ship" aria-hidden="true" />
        <h2 className="phase-name" id="fm-title">{t("fm.title")}</h2>
      </header>
      <Loaded resource={metrics} rows={3}>
        {(data) => (
          <>
            <Overall data={data} />
            <Section title={t("fm.perIntent")} hint={t("fm.perIntentHint")}><Intents data={data} /></Section>
            <Section title={t("fm.effect")}><RepolensEffect data={data} /></Section>
          </>
        )}
      </Loaded>
    </section>
  );
}
