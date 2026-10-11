"use client";

/**
 * Static app shell shown before the dashboard layout has mounted and read the
 * stored session (#1359), so a hard load never paints a blank page. Same
 * sidebar width (w-72, desktop only), header row and paddings as the real
 * layout, so nothing shifts when the shell mounts.
 */
import { useTranslations } from "next-intl";
import { Skeleton } from "@/components/ui/skeleton";
import { theme } from "@/theme/tokens";

export function AppShellSkeleton() {
  const tc = useTranslations("common");
  return (
    <div
      className="flex min-h-screen"
      style={{ backgroundColor: theme.colors.neutral[100] }}
      data-testid="app-shell-skeleton"
      role="status"
      aria-busy="true"
      aria-label={tc("loading")}
    >
      <div
        data-testid="app-shell-skeleton-sidebar"
        className="hidden md:flex flex-col w-72 min-h-screen gap-4 p-6 bg-[hsl(var(--sidebar))] border-r border-[hsl(var(--sidebar-border))]"
      >
        <Skeleton className="h-10 w-36 rounded-xl" />
        <Skeleton className="h-16 w-full rounded-2xl" />
        {Array.from({ length: 8 }).map((_, i) => (
          <Skeleton key={i} className="h-5 w-3/4" />
        ))}
      </div>
      <div className="flex-1 flex flex-col min-w-0">
        <div className="flex items-center gap-3 px-6 lg:px-8 pt-4 pb-0 h-9">
          <Skeleton className="h-4 w-40" />
        </div>
        <div className="flex-1 p-6 lg:p-8 flex flex-col gap-6">
          <Skeleton className="h-24 w-full rounded-[24px]" />
          <Skeleton className="h-80 w-full rounded-[24px]" />
        </div>
      </div>
    </div>
  );
}
