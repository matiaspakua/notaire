"use client";

import type { KeyboardEvent, ReactNode } from "react";
import { translateNode } from "@/components/site/Es";
import { tr } from "@/lib/i18n/es";
import "@/lib/i18n/strings";
import { usePreferences } from "@/components/site/Preferences";
import type { Stepper } from "./useStepper";

export type Step = { title: string; body: ReactNode; source?: { label: string; href: string } };

/**
 * Previous / Play / Next, the list of steps, and a live region with the current one. The list is
 * also the small-screen and screen-reader version of the diagram, so nothing depends on the SVG.
 */
export function StepControls({ id, steps, s, label }: { id: string; steps: Step[]; s: Stepper; label: string }) {
  const { locale } = usePreferences();
  const t = (value: string) => tr(locale, value);
  const current = steps[s.index];
  const onKey = (e: KeyboardEvent) => {
    if (e.key === "ArrowRight") {
      e.preventDefault();
      s.stop();
      s.next();
    } else if (e.key === "ArrowLeft") {
      e.preventDefault();
      s.stop();
      s.prev();
    }
  };
  const btn =
    "inline-flex h-9 items-center gap-1.5 rounded-full border border-slate-300 bg-white px-3 text-sm font-medium text-slate-800 transition-colors duration-150 hover:border-[var(--accent-500)] hover:text-[var(--accent-700)] focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[var(--accent-500)] disabled:cursor-not-allowed disabled:opacity-50";
  return (
    <div className="space-y-4" onKeyDown={onKey}>
      <div role="group" aria-label={`${t(label)}: ${t("step controls")}`} className="flex flex-wrap items-center gap-2">
        <button type="button" className={btn} onClick={() => (s.stop(), s.prev())} disabled={s.index === 0} aria-label={t("Previous step")}>
          <span aria-hidden>←</span> {t("Prev")}
        </button>
        <button type="button" className={btn} onClick={s.toggle} aria-pressed={s.playing} aria-label={s.playing ? t("Pause") : t("Play all steps")}>
          <span aria-hidden>{s.playing ? "❚❚" : "▶"}</span> {s.playing ? t("Pause") : t("Play")}
        </button>
        <button type="button" className={btn} onClick={() => (s.stop(), s.next())} disabled={s.index === steps.length - 1} aria-label={t("Next step")}>
          {t("Next")} <span aria-hidden>→</span>
        </button>
        <span className="ml-auto font-mono text-xs text-slate-600">
          {t("Step")} {s.index + 1} {t("of")} {steps.length}
        </span>
      </div>
      <div aria-live="polite" id={`${id}-live`} className="rounded-2xl border border-[var(--accent-500)]/30 bg-[var(--accent-500)]/5 p-4">
        <p className="font-semibold text-slate-900">{t(current.title)}</p>
        <div className="mt-1 text-sm leading-relaxed text-slate-700">{locale === "es" ? translateNode(current.body) : current.body}</div>
        {current.source && (
          <a className="mt-2 inline-block font-mono text-[11px] text-[var(--accent-700)] underline underline-offset-2" href={current.source.href}>
            ↗ {current.source.label}
          </a>
        )}
      </div>
      <ol className="grid gap-1.5 sm:grid-cols-2" aria-label={`${t(label)}: ${t("all steps")}`}>
        {steps.map((st, i) => (
          <li key={st.title}>
            <button
              type="button"
              onClick={() => s.jump(i)}
              aria-current={i === s.index ? "step" : undefined}
              className={`flex w-full items-start gap-2 rounded-xl border px-3 py-2 text-left text-sm transition-colors duration-150 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[var(--accent-500)] ${
                i === s.index ? "border-[var(--accent-500)] bg-white text-slate-900" : "border-slate-200 bg-white/70 text-slate-700 hover:border-slate-400"
              }`}
            >
              <span
                className={`mt-0.5 flex h-5 w-5 shrink-0 items-center justify-center rounded-full font-mono text-[11px] ${
                  i === s.index ? "bg-[var(--accent-700)] text-white" : i < s.index ? "bg-slate-700 text-white" : "bg-slate-200 text-slate-700"
                }`}
              >
                {i + 1}
              </span>
              <span>{t(st.title)}</span>
            </button>
          </li>
        ))}
      </ol>
    </div>
  );
}
