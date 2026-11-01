"use client";

import { motion } from "framer-motion";
import { StepControls, type Step } from "./StepControls";
import { transition, useStepper } from "./useStepper";

export type WorkflowTrigger = { file: string; name: string; triggers: string[] };

const STAGES: { key: string; label: string; match: string[]; body: string }[] = [
  {
    key: "pr",
    label: "Pull request",
    match: ["pull_request", "pull_request_target"],
    body: "Every pull request runs these before Matias can merge: builds, tests, the API contract, database checks, security scans and the SDLC gates.",
  },
  {
    key: "push",
    label: "Push",
    match: ["push"],
    body: "A push to main (or a release tag for cd.yml) re-runs the main checks on the merged code and lets release-please open the next release PR.",
  },
  {
    key: "after",
    label: "After CI on main",
    match: ["workflow_run"],
    body: "When “CI - Build, Test & Security” finishes on main, these chain on: publishing images and SBOMs, and deploying this site.",
  },
  {
    key: "schedule",
    label: "Scheduled",
    match: ["schedule"],
    body: "Cron jobs catch what a single pull request cannot: DAST scans, performance runs, backup checks, coverage trends and housekeeping.",
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
                {st.label}
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
