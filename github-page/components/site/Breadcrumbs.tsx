import Link from "next/link";
import { breadcrumbs } from "@/lib/site";

export function Breadcrumbs({ pathname }: { pathname: string }) {
  const crumbs = breadcrumbs(pathname);
  if (crumbs.length === 0) return null;
  return (
    <nav aria-label="Breadcrumb" className="mb-6 text-sm">
      <ol className="flex flex-wrap items-center gap-1.5 text-slate-600">
        {crumbs.map((c, i) => {
          const last = i === crumbs.length - 1;
          return (
            <li key={c.href} className="flex items-center gap-1.5">
              {last ? (
                <span aria-current="page" className="font-medium text-slate-900">
                  {c.label}
                </span>
              ) : (
                <>
                  <Link href={c.href} className="rounded hover:text-slate-900 hover:underline">
                    {c.label}
                  </Link>
                  <span aria-hidden="true" className="text-slate-400">/</span>
                </>
              )}
            </li>
          );
        })}
      </ol>
    </nav>
  );
}
