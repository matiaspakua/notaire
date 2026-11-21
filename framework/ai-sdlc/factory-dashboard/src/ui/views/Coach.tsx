import { useState } from "react";

import { factory } from "../api.ts";
import { Icon, type IconName } from "../components/Icon.tsx";
import { Empty, Loaded, Section } from "../components/State.tsx";
import { useWorkspace } from "../context.ts";
import { useResource } from "../hooks.ts";
import { hasKey, useLang, useT } from "../i18n.ts";
import type { CoachReport } from "../model.ts";

const WINDOWS = [7, 30, 90, 0];
const SEVERITY_ICON: Record<string, IconName> = { high: "alert", medium: "clock", low: "dot" };

/** Single-series bars: thin marks, a 2px gap, rounded data ends on the baseline, a hover readout, a table twin. */
function HourChart({ hours }: { hours: number[] }) {
  const t = useT();
  const lang = useLang();
  const [hover, setHover] = useState<number>();
  const max = Math.max(1, ...hours);
  const width = 24 * 14;
  const height = 120;
  const bar = 12;
  return (
    <figure className="chart">
      <figcaption>{t("coach.hours")}</figcaption>
      <div className="chart-plot">
        <svg viewBox={`0 0 ${width} ${height + 18}`} role="img" aria-label={t("coach.hours")} preserveAspectRatio="none">
          <line x1="0" x2={width} y1={height} y2={height} className="chart-axis" />
          {hours.map((value, hour) => {
            const h = value === 0 ? 0 : Math.max(3, (value / max) * (height - 8));
            const x = hour * 14 + 1;
            return (
              <g key={hour} onMouseEnter={() => setHover(hour)} onMouseLeave={() => setHover(undefined)}>
                <rect x={hour * 14} y="0" width="14" height={height} className="chart-hit" />
                {h > 0 && (
                  <path
                    className={`chart-bar${hover === hour ? " chart-bar-on" : ""}`}
                    d={`M${x},${height} v${-(h - 4)} q0,-4 4,-4 h${bar - 8} q4,0 4,4 v${h - 4} z`}
                  />
                )}
                {hour % 6 === 0 && (
                  <text x={hour * 14 + 7} y={height + 14} className="chart-tick" textAnchor="middle">
                    {String(hour).padStart(2, "0")}
                  </text>
                )}
              </g>
            );
          })}
        </svg>
        {hover !== undefined && (
          <p className="chart-tip" role="status">
            <span className="num">{String(hover).padStart(2, "0")}:00</span>
            <strong className="num">{hours[hover].toLocaleString(lang)}</strong>
          </p>
        )}
      </div>
      <details className="disclosure">
        <summary>{t("coach.hours")} · {t("coach.prompts").toLowerCase()}</summary>
        <table className="compact">
          <tbody>
            {hours.map((value, hour) => (
              <tr key={hour}>
                <th scope="row" className="num">{String(hour).padStart(2, "0")}:00</th>
                <td className="num">{value}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </details>
    </figure>
  );
}

function Summary({ data }: { data: CoachReport }) {
  const t = useT();
  const lang = useLang();
  const n = (value: number) => value.toLocaleString(lang);
  return (
    <>
      <dl className="figures">
        <div><dt>{t("coach.sessions")}</dt><dd className="num">{n(data.totals.sessions)}</dd></div>
        <div><dt>{t("coach.prompts")}</dt><dd className="num">{n(data.totals.user_turns)}</dd></div>
        <div><dt>{t("coach.tools")}</dt><dd className="num">{n(data.totals.tool_calls)}</dd></div>
      </dl>
      <p className="muted">
        {data.totals.sessions > 0
          ? t("coach.healthy", { ok: n(data.totals.healthy_sessions), n: n(data.totals.sessions) })
          : t("coach.none")}
      </p>
    </>
  );
}

function Details({ data }: { data: CoachReport }) {
  const t = useT();
  const lang = useLang();
  const n = (value: number) => value.toLocaleString(lang);
  const active = data.harnesses.filter((row) => row.sessions > 0);
  return (
    <>
      <Section title={t("coach.findings")}>
        {data.anti_patterns.length === 0 ? <Empty>{t("coach.clean")}</Empty> : (
          <ul className="findings">
            {data.anti_patterns.map((item) => {
              const name = `pattern.${item.id}`;
              return (
                <li key={item.id} className={`finding finding-${item.severity}`}>
                  <span className={`severity severity-${item.severity}`}>
                    <Icon name={SEVERITY_ICON[item.severity]} size={14} />
                    {t(`severity.${item.severity}`)}
                  </span>
                  <div>
                    <p className="finding-title">{hasKey(name) ? t(name) : item.id}</p>
                    <p className="muted small">{t("coach.occurrences", { n: n(item.occurrences), s: n(item.sessions) })}</p>
                    <p>{item.recommendation}</p>
                  </div>
                </li>
              );
            })}
          </ul>
        )}
      </Section>
      <div className="split">
        <Section title={t("coach.harnesses")}>
          {active.length === 0 ? <Empty>{t("state.empty")}</Empty> : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th scope="col">{t("coach.harness")}</th>
                    <th scope="col" className="num">{t("coach.sessions")}</th>
                    <th scope="col" className="num">{t("coach.prompts")}</th>
                    <th scope="col" className="num">{t("coach.tools")}</th>
                    <th scope="col">{t("coach.models")}</th>
                  </tr>
                </thead>
                <tbody>
                  {active.map((row) => (
                    <tr key={row.harness}>
                      <th scope="row">{row.harness}</th>
                      <td className="num">{n(row.sessions)}</td>
                      <td className="num">{n(row.user_turns)}</td>
                      <td className="num">{row.tool_calls === null ? <span className="muted">{t("coach.notMeasured")}</span> : n(row.tool_calls)}</td>
                      <td className="mono small">{Object.keys(row.models).slice(0, 3).join(", ") || "—"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Section>
        <Section title={t("coach.hours")}>
          <HourChart hours={data.hours} />
        </Section>
      </div>
    </>
  );
}

export function Coach() {
  const t = useT();
  const { workspace, grant } = useWorkspace();
  const [days, setDays] = useState(30);
  const owner = grant.scope === "owner";
  const coach = useResource<CoachReport>(owner ? factory(workspace.id, `/coach${days ? `?days=${days}` : ""}`) : null);
  if (!owner) {
    return (
      <div className="note note-lock" role="note">
        <Icon name="lock" />
        <p>{t("coach.owner")}</p>
      </div>
    );
  }
  return (
    <div className="stack">
      <Section
        title={t("coach.summary")}
        actions={
          <div className="chips" role="group" aria-label={t("coach.window")}>
            {WINDOWS.map((value) => (
              <button key={value} type="button" className="chip" aria-pressed={days === value} onClick={() => setDays(value)}>
                {value ? t("coach.days", { n: value }) : t("coach.all")}
              </button>
            ))}
          </div>
        }
      >
        <p className="lede">{t("coach.privacy")}</p>
        <Loaded resource={coach} rows={3}>{(data) => <Summary data={data} />}</Loaded>
      </Section>
      {coach.data && coach.data.totals.sessions > 0 && <Details data={coach.data} />}
    </div>
  );
}
