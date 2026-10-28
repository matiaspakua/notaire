import Link from "next/link";
import { prevNext } from "@/lib/site";
import { ArrowIcon } from "./icons";

export function PrevNext({ pathname }: { pathname: string }) {
  const { prev, next } = prevNext(pathname);
  if (!prev && !next) return null;
  const card = "group flex flex-col gap-1 rounded-2xl border border-slate-200 bg-white p-4 transition-colors hover:border-[var(--accent-500)]";
  return (
    <nav aria-label="Previous and next" className="mt-16 grid gap-3 border-t border-slate-200 pt-8 sm:grid-cols-2">
      {prev ? (
        <Link href={prev.href} className={card}>
          <span className="flex items-center gap-1.5 text-xs font-medium uppercase tracking-wider text-slate-500">
            <ArrowIcon left className="h-3.5 w-3.5" /> Previous
          </span>
          <span className="font-semibold text-slate-900 group-hover:text-[var(--accent-700)]">{prev.title}</span>
          <span className="text-sm text-slate-600">{prev.summary}</span>
        </Link>
      ) : (
        <span className="hidden sm:block" />
      )}
      {next && (
        <Link href={next.href} className={`${card} sm:items-end sm:text-right`}>
          <span className="flex items-center gap-1.5 text-xs font-medium uppercase tracking-wider text-slate-500">
            Next <ArrowIcon className="h-3.5 w-3.5" />
          </span>
          <span className="font-semibold text-slate-900 group-hover:text-[var(--accent-700)]">{next.title}</span>
          <span className="text-sm text-slate-600">{next.summary}</span>
        </Link>
      )}
    </nav>
  );
}
