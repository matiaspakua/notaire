"use client";

import Link from "next/link";
import { chrome, factoryPageSummary, factoryPageTitle } from "@/lib/i18n";
import { prevNext } from "@/lib/site";
import { ArrowIcon } from "./icons";
import { usePreferences } from "./Preferences";

export function PrevNext({ pathname }: { pathname: string }) {
  const { locale } = usePreferences();
  const t = chrome(locale);
  const { prev, next } = prevNext(pathname);
  if (!prev && !next) return null;
  const card = "group flex flex-col gap-1 rounded-2xl border border-slate-200 bg-white p-4 transition-colors hover:border-[var(--accent-500)]";
  return (
    <nav aria-label={t.pager} className="mt-16 grid gap-3 border-t border-slate-200 pt-8 sm:grid-cols-2">
      {prev ? (
        <Link href={prev.href} className={card}>
          <span className="flex items-center gap-1.5 text-xs font-medium uppercase tracking-wider text-slate-500">
            <ArrowIcon left className="h-3.5 w-3.5" /> {t.previous}
          </span>
          <span className="font-semibold text-slate-900 group-hover:text-[var(--accent-700)]">{factoryPageTitle(prev.href, locale, prev.title)}</span>
          <span className="text-sm text-slate-600">{factoryPageSummary(prev.href, locale, prev.summary)}</span>
        </Link>
      ) : (
        <span className="hidden sm:block" />
      )}
      {next && (
        <Link href={next.href} className={`${card} sm:items-end sm:text-right`}>
          <span className="flex items-center gap-1.5 text-xs font-medium uppercase tracking-wider text-slate-500">
            {t.next} <ArrowIcon className="h-3.5 w-3.5" />
          </span>
          <span className="font-semibold text-slate-900 group-hover:text-[var(--accent-700)]">{factoryPageTitle(next.href, locale, next.title)}</span>
          <span className="text-sm text-slate-600">{factoryPageSummary(next.href, locale, next.summary)}</span>
        </Link>
      )}
    </nav>
  );
}
