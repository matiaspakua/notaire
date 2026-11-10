"use client";

import Link from "next/link";
import { chrome, factoryPageTitle } from "@/lib/i18n";
import { breadcrumbs, PRIMARY_NAV } from "@/lib/site";
import { usePreferences } from "./Preferences";

export function Breadcrumbs({ pathname }: { pathname: string }) {
  const { locale } = usePreferences();
  const t = chrome(locale);
  const crumbs = breadcrumbs(pathname).map((crumb) => {
    if (crumb.href === "/") return { ...crumb, label: t.home };
    const nav = PRIMARY_NAV.find((item) => item.href === crumb.href);
    if (nav) return { ...crumb, label: t.nav[nav.key] };
    return { ...crumb, label: factoryPageTitle(crumb.href, locale, crumb.label) };
  });
  if (crumbs.length === 0) return null;
  return (
    <nav aria-label={t.breadcrumb} className="mb-6 text-sm">
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
