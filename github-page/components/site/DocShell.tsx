"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import type { ReactNode } from "react";
import { actOf, groupsFor, REPORTS, sectionOf } from "@/lib/site";
import { Breadcrumbs } from "./Breadcrumbs";
import { PrevNext } from "./PrevNext";
import { ReadingProgress } from "./ReadingProgress";
import { Sidebar } from "./Sidebar";

/**
 * Long-form layout for the Technical docs and the AI Factory (#1970): breadcrumbs, a sidebar
 * of the section's pages (the factory's grouped as five acts), reading progress, a readable
 * measure and previous/next links. The factory gets the violet accent.
 */
export function DocShell({ children }: { children: ReactNode }) {
  const raw = usePathname() || "/";
  const pathname = raw.endsWith("/") ? raw : `${raw}/`;
  const section = sectionOf(pathname);
  const factory = section === "factory";
  const act = factory ? actOf(pathname) : undefined;

  return (
    <div className={factory ? "theme-factory" : "theme-docs"}>
      <ReadingProgress />
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:grid lg:grid-cols-[14rem_minmax(0,1fr)] lg:gap-12">
        <aside className="hidden lg:block">
          <div className="sticky top-[calc(var(--header-h)+1.5rem)] max-h-[calc(100vh-var(--header-h)-3rem)] overflow-y-auto py-10 pr-2">
            <p className="mb-5 px-3 text-sm font-semibold text-slate-900">{factory ? "The AI Factory" : "Technical docs"}</p>
            <Sidebar
              label={factory ? "AI Factory chapters" : "Technical docs pages"}
              groups={groupsFor(section)}
              pathname={pathname}
              footer={
                !factory && (
                  <div className="mb-6">
                    <p className="mb-2 px-3 text-[11px] font-semibold uppercase tracking-wider text-slate-500">Live reports</p>
                    <ul className="space-y-0.5 border-l border-slate-200">
                      {REPORTS.map((r) => (
                        <li key={r.href}>
                          <Link href={r.href} prefetch={false} className="-ml-px block border-l-2 border-transparent py-1.5 pl-3 pr-2 text-slate-600 hover:border-slate-400 hover:text-slate-900">
                            {r.title}
                          </Link>
                        </li>
                      ))}
                    </ul>
                  </div>
                )
              }
            />
          </div>
        </aside>
        <div className="min-w-0 py-8 sm:py-10">
          <Breadcrumbs pathname={pathname} />
          {act && (
            <p className="mb-3 inline-flex items-center gap-2 rounded-full bg-factory-50 px-3 py-1 text-xs font-semibold text-factory-700">
              <span aria-hidden="true" className="h-1.5 w-1.5 rounded-full bg-factory-600" />
              Act {act}
            </p>
          )}
          <div className="doc-body">{children}</div>
          <PrevNext pathname={pathname} />
        </div>
      </div>
    </div>
  );
}
