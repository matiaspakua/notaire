import type { ReactNode } from "react";

import { useT } from "../i18n.ts";
import type { WorkflowStep } from "../line-model.ts";
import type { LineStage, RunStep, StepIo } from "../model.ts";
import { commitUrl, fileUrl, isEmptyIo, safeUrl, stepsAtStage } from "../step-io.ts";
import { StatusBadge } from "./State.tsx";

function Ref({ href, children }: { href: string | null; children: ReactNode }) {
  const t = useT();
  if (!href) return <span className="mono">{children}</span>;
  return (
    <a className="mono" href={href} target="_blank" rel="noopener noreferrer" title={t("io.newTab")}>{children}</a>
  );
}

function Side({ label, io, base, fileRef }: { label: string; io: StepIo | undefined; base: string | null; fileRef?: string }) {
  const t = useT();
  return (
    <section className="pl-io-side" aria-label={label}>
      <p className="section-label">{label}</p>
      {!io || isEmptyIo(io) ? (
        <p className="faint small">{t("io.nothing")}</p>
      ) : (
        <>
        {io.prompt && (
          <details className="pl-io-prompt">
            <summary>{t("io.prompt")}</summary>
            <pre>{io.prompt}</pre>
          </details>
        )}
        <dl className="pl-io-facts">
          {io.request && (<><dt>{t("io.request")}</dt><dd>{io.request}</dd></>)}
          {io.summary && (<><dt>{t("io.summary")}</dt><dd>{io.summary}</dd></>)}
          {io.files?.length ? (
            <><dt>{t("io.files")}</dt><dd><ul className="pl-io-list">
              {io.files.map((f) => <li key={f}><Ref href={fileUrl(base, f, fileRef)}>{f}</Ref></li>)}
            </ul></dd></>
          ) : null}
          {io.commits?.length ? (
            <><dt>{t("io.commits")}</dt><dd><ul className="pl-io-list inline">
              {io.commits.map((c) => <li key={c}><Ref href={commitUrl(base, c)}>{c.slice(0, 12)}</Ref></li>)}
            </ul></dd></>
          ) : null}
          {io.links?.length ? (
            <><dt>{t("io.links")}</dt><dd><ul className="pl-io-list">
              {io.links.map((l) => <li key={l.url}><Ref href={safeUrl(l.url)}>{l.label || l.url}</Ref></li>)}
            </ul></dd></>
          ) : null}
        </dl>
        </>
      )}
    </section>
  );
}

export function StepIoPanel({ stage, steps, workflow, stages, repo, hasRun }: {
  stage: LineStage;
  steps: RunStep[];
  workflow: WorkflowStep[];
  stages: LineStage[];
  repo: string | null;
  hasRun: boolean;
}) {
  const t = useT();
  const mine = stepsAtStage(steps, stage.id, workflow, stages).filter((s) => s.status !== "pending" || !isEmptyIo(s.inputs));
  return (
    <section className="pl-io" aria-labelledby="pl-io-title">
      <h4 id="pl-io-title" className="pl-io-title">{t("io.title")}</h4>
      <p className="muted small">{t("io.hint")}</p>
      {mine.length === 0 ? (
        <p className="faint" data-testid="step-io-empty">{hasRun ? t("io.empty") : t("io.noRun")}</p>
      ) : (
        mine.map((step) => (
          <article key={step.id} className="pl-io-step" data-testid="step-io" aria-label={step.id}>
            <header className="pl-io-head">
              <h5 className="mono">{step.id}</h5>
              <StatusBadge status={step.status} />
            </header>
            <div className="pl-io-sides">
              <Side label={t("io.received")} io={step.inputs} base={repo} />
              <Side label={t("io.produced")} io={step.outputs} base={repo} fileRef={step.outputs?.commits?.[0]} />
            </div>
          </article>
        ))
      )}
    </section>
  );
}
