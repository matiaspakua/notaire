"use client";

import Link from "next/link";
import { Calculator, FolderKanban, Users } from "lucide-react";
import { Suspense } from "react";
import { useSearchParams } from "next/navigation";
import { useTranslations } from "next-intl";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Stagger, StaggerItem, HoverLift } from "@/components/motion";
import { PageHeader } from "@/components/shared/PageHeader";
import { PageSection } from "@/components/shared/PageSection";
import { ErrorState } from "@/components/shared/ErrorState";
import { MetricCard } from "@/components/shared/MetricCard";
import { Button } from "@/components/ui/button";
import { useAuthStore } from "@/store/auth-store";
import { useManagementsPage } from "@/hooks/useManagements";
import { usePeoplePage } from "@/hooks/usePeople";
import { useBudgetsPage } from "@/hooks/useBudgets";
import { StatValue } from "@/components/dashboard/StatValue";
import { WorkflowHero } from "@/components/workflow/WorkflowHero";
import { ExpirationsAlertCard } from "@/components/dashboard/ExpirationsAlertCard";
import { theme } from "@/theme/tokens";
import { DASHBOARD_MODULES } from "@/lib/dashboard-nav";
import { useFormat } from "@/hooks/useFormat";

function AccessDeniedBanner() {
  const td = useTranslations("dashboard");
  const searchParams = useSearchParams();
  if (searchParams.get("forbidden") !== "1") {
    return null;
  }
  return (
    <p
      data-testid="access-denied-message"
      role="status"
      className="rounded-[16px] border border-warning/30 bg-warning/10 px-4 py-3 text-sm text-warning font-medium"
    >
      {td("accessDenied")}
    </p>
  );
}

export default function DashboardPage() {
  const td = useTranslations("dashboard");
  const tc = useTranslations("common");
  const { user, isAdmin } = useAuthStore();
  // Only the totals are needed: size=1 pages, never the lists (#1340, #1358).
  const managementsPage = useManagementsPage({ page: 0, size: 1 });
  const peoplePage = usePeoplePage({ page: 0, size: 1 });
  const budgetsPage = useBudgetsPage({ page: 0, size: 1 });

  // One config for the sidebar and this grid (#1367).
  const visibleModules = DASHBOARD_MODULES.filter((m) => !m.adminOnly || isAdmin());
  const fmt = useFormat();
  const homeLoadError =
    managementsPage.isError || peoplePage.isError || budgetsPage.isError;

  const stats = [
    { labelKey: "gestiones.label", query: managementsPage, icon: FolderKanban, tint: "bg-primary/10", iconColor: "text-primary" },
    { labelKey: "personas.label", query: peoplePage, icon: Users, tint: "bg-primary/10", iconColor: "text-primary" },
    { labelKey: "presupuestos.label", query: budgetsPage, icon: Calculator, tint: "bg-primary/10", iconColor: "text-primary" },
  ] as const;

  return (
    <div className="max-w-[1600px] mx-auto space-y-12">
      <Suspense fallback={null}>
        <AccessDeniedBanner />
      </Suspense>
      <PageHeader
        title={`${td("hello")}, ${user?.name?.split(" ")[0] ?? ""}`.trim()}
        description={td("today")}
        size="display"
        className="items-end"
        actions={
          <div className="bg-white/50 backdrop-blur-sm px-4 py-2 rounded-full border border-black/5 shadow-sm">
            <p className="text-sm font-semibold" style={{ color: theme.colors.neutral[800] }}>
              {fmt.longDate(new Date())}
            </p>
          </div>
        }
      />

      <ExpirationsAlertCard />

      {homeLoadError ? (
        <div className="rounded-[24px] border border-border/40 bg-white apple-shadow">
          <ErrorState
            title={td("loadError")}
            action={
              <Button
                type="button"
                variant="secondary"
                data-testid="btn-retry-dashboard-stats"
                onClick={() => {
                  void managementsPage.refetch();
                  void peoplePage.refetch();
                  void budgetsPage.refetch();
                }}
              >
                {tc("retry")}
              </Button>
            }
          />
        </div>
      ) : null}

      <Stagger className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {stats.map((stat) => {
          const StatIcon = stat.icon;
          return (
            <StaggerItem key={stat.labelKey}>
              <HoverLift lift={4}>
                <MetricCard
                  label={td(stat.labelKey as Parameters<typeof td>[0])}
                  icon={<StatIcon className={`h-8 w-8 ${stat.iconColor}`} />}
                  iconWellClassName={stat.tint}
                >
                  <StatValue
                    value={stat.query.data?.totalElements}
                    isLoading={stat.query.isLoading}
                    isError={stat.query.isError}
                  />
                </MetricCard>
              </HoverLift>
            </StaggerItem>
          );
        })}
      </Stagger>

      <WorkflowHero />

      <PageSection title={td("availableModules")}>
        <div data-testid="dashboard-modules">
          <Stagger className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {visibleModules.map((mod) => {
              const Icon = mod.icon;
              return (
                <StaggerItem key={mod.href}>
                  <HoverLift className="h-full">
                    <Link href={mod.href} className="group block h-full">
                      <Card className="h-full bg-white border-none apple-shadow rounded-[28px] hover:apple-shadow-lg transition-shadow duration-base ease-standard relative overflow-hidden">
                        <div className="absolute top-0 left-0 w-1.5 h-full bg-primary opacity-0 group-hover:opacity-100 transition-opacity duration-base ease-standard" />
                        <CardHeader className="pb-4 p-8">
                          <div className="w-14 h-14 rounded-2xl flex items-center justify-center mb-6 bg-primary/10 text-primary transition-transform duration-base ease-standard group-hover:scale-110">
                            <Icon className="h-7 w-7" />
                          </div>
                          <CardTitle className="text-xl font-semibold text-foreground group-hover:text-primary transition-colors duration-fast">
                            {td(`${mod.labelKey}.label` as Parameters<typeof td>[0])}
                          </CardTitle>
                        </CardHeader>
                        <CardContent className="px-8 pb-8 pt-0">
                          <CardDescription className="text-base text-muted-foreground leading-relaxed">
                            {td(`${mod.labelKey}.description` as Parameters<typeof td>[0])}
                          </CardDescription>
                        </CardContent>
                      </Card>
                    </Link>
                  </HoverLift>
                </StaggerItem>
              );
            })}
          </Stagger>
        </div>
      </PageSection>
    </div>
  );
}
