"use client";

import { motion } from "framer-motion";
import { tr } from "@/lib/i18n/es";
import "@/lib/i18n/strings";
import { usePreferences } from "@/components/site/Preferences";
import { StepControls, type Step } from "./StepControls";
import { transition, useStepper } from "./useStepper";

export type WorkflowTrigger = { file: string; name: string; triggers: string[] };

const STAGES: { key: string; label: string; match: string[]; body: string }[] = [
  {
    key: "pr",
    label: "Pull request",
    match: ["pull_request", "pull_request_target"],
    body: "A pull_request trigger is rare. Product builds, tests and security scans do not start here. The local preflight is the proof before merge.",
  },
  {
    key: "push",
    label: "Push",
    match: ["push"],
    body: "A version tag starts the product suite and image publish. A push to main starts Release Please and the portfolio. It does not publish images.",
  },
  {
    key: "after",
    label: "After CI on main",
    match: ["workflow_run"],
    body: "A workflow_run chain would start after another workflow. Product publish is a tag trigger, not a chain from main.",
  },
  {
    key: "schedule",
    label: "Scheduled",
    match: ["schedule"],
    body: "Scheduled jobs cover what a tag does not: the Monday ZAP baseline, k6, the Sunday backup smoke, and artifact cleanup. CodeQL also has a Monday run.",
  },
  {
    key: "comment",
    label: "Comment",
    match: ["issue_comment", "pull_request_review_comment"],
    body: "An AI agent workflow wakes up when someone mentions it in an issue or review comment.",
  },
];

/**
 * The GitHub Actions workflows grouped by what starts them (#1977). The grouping is read from each
 * workflow's `on:` block at build time, so a new workflow lands in the right column on its own.
 */
export function PipelineMap({ workflows, repoHref }: { workflows: WorkflowTrigger[]; repoHref: string }) {
  const { locale } = usePreferences();
  const text = (value: string) => tr(locale, value);
  const stages = STAGES.map((st) => ({ ...st, items: workflows.filter((w) => w.triggers.some((t) => st.match.includes(t))) })).filter(
    (st) => st.items.length > 0,
  );
  const steps: Step[] = stages.map((st) => ({
    title: `${st.label}: ${st.items.length} workflow${st.items.length === 1 ? "" : "s"}`,
    body: st.body,
    source: { label: ".github/workflows", href: `${repoHref}/tree/main/.github/workflows` },
  }));
  const s = useStepper(steps.length);
  const t = transition(s.reduced, 0.25);

  return (
    <figure className="not-prose my-8 rounded-3xl border border-slate-200 bg-white p-4 shadow-[var(--shadow-card)] sm:p-6" data-diagram="pipeline">
      <div className="grid gap-3 md:grid-cols-5">
        {stages.map((st, i) => {
          const on = i === s.index;
          return (
            <motion.div
              key={st.key}
              onClick={() => s.jump(i)}
              className={`relative cursor-pointer rounded-2xl border p-3 ${on ? "border-[var(--accent-700)] bg-[var(--accent-500)]/5" : "border-slate-200 bg-slate-50"}`}
              initial={false}
              animate={{ y: on && !s.reduced ? -3 : 0 }}
              transition={t}
            >
              <p className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wide text-slate-700">
                <span className={`flex h-5 w-5 items-center justify-center rounded-full font-mono text-[11px] ${on ? "bg-[var(--accent-700)] text-white" : "bg-slate-200 text-slate-700"}`}>
                  {i + 1}
                </span>
                {text(st.label)}
              </p>
              <ul className="mt-2 flex flex-wrap gap-1.5 md:flex-col">
                {st.items.map((w) => (
                  <li
                    key={w.file}
                    className={`rounded-lg border px-2 py-1 font-mono text-[11px] leading-tight ${on ? "border-[var(--accent-500)]/40 bg-white text-slate-900" : "border-slate-200 bg-white text-slate-700"}`}
                  >
                    {w.file.replace(/\.ya?ml$/, "")}
                  </li>
                ))}
              </ul>
            </motion.div>
          );
        })}
      </div>
      <div className="mt-5">
        <StepControls id="pipeline" steps={steps} s={s} label="CI/CD pipeline" />
      </div>
      <figcaption className="mt-3 text-xs text-slate-600">
        {workflows.length} workflows, grouped by the triggers in their <code>on:</code> block. A workflow with several triggers shows in each
        column; manual <code>workflow_dispatch</code> runs are left out.
      </figcaption>
    </figure>
  );
}
