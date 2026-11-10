"use client";

import Link from "next/link";
import { readingCopy } from "@/lib/i18n";
import { FACTORY_READING_PATH } from "@/lib/reading-path";
import { usePreferences } from "./Preferences";

/** Numbered because the sequence is the point: basics, then one change, then the kit. */
export function ReadingPath({ compact = false }: { compact?: boolean }) {
  const { locale } = usePreferences();
  const copy = readingCopy(locale);
  return (
    <section aria-labelledby="read-order-title" className={compact ? "pt-16" : "mt-10"}>
      <h2 id="read-order-title" className="font-display text-3xl font-semibold text-slate-900">
        {copy.title}
      </h2>
      <p className="mt-3 max-w-3xl text-slate-700">{copy.lead}</p>
      <ol className="mt-6 grid gap-3 sm:grid-cols-2">
        {FACTORY_READING_PATH.map((step, index) => {
          const text = copy.steps[step.href];
          return (
            <li key={step.href}>
              <Link href={step.href} className="flex h-full gap-3 rounded-2xl border border-slate-200 bg-white p-4 transition-colors duration-200 hover:border-brand-500">
                <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-slate-900 font-mono text-sm font-semibold text-white">
                  {index + 1}
                </span>
                <span>
                  <span className="block font-semibold text-slate-900">{text.title}</span>
                  <span className="mt-1 block text-sm leading-relaxed text-slate-600">{text.body}</span>
                </span>
              </Link>
            </li>
          );
        })}
      </ol>
    </section>
  );
}
