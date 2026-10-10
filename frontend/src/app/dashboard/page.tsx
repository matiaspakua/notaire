"use client";

import Link from "next/link";
import {
  BookMarked,
  Building2,
  Calculator,
  Copy,
  CreditCard,
  FileBarChart,
  FileText,
  FolderKanban,
  ListTodo,
  ScrollText,
  Settings,
  ShieldCheck,
  UserRoundCog,
  Users,
} from "lucide-react";
import { Suspense } from "react";
import { useSearchParams } from "next/navigation";
import { useTranslations, useLocale } from "next-intl";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Stagger, StaggerItem, HoverLift } from "@/components/motion";
import { useAuthStore } from "@/store/auth-store";
import { useGestionesPage } from "@/hooks/useGestiones";
import { usePersonasPage } from "@/hooks/usePersonas";
import { usePresupuestosPage } from "@/hooks/usePresupuestos";
import { StatValue } from "@/components/dashboard/StatValue";
import { WorkflowHero } from "@/components/workflow/WorkflowHero";
import { theme } from "@/theme/tokens";
import type { ComponentType } from "react";

type LucideIcon = ComponentType<{ className?: string }>;

interface Module {
  labelKey: string;
  descKey: string;
  href: string;
  icon: LucideIcon;
  adminOnly: boolean;
}

const modules: Module[] = [
  { labelKey: "gestiones.label", descKey: "gestiones.description", href: "/dashboard/gestiones", icon: FolderKanban, adminOnly: false },
  { labelKey: "presupuestos.label", descKey: "presupuestos.description", href: "/dashboard/presupuestos", icon: Calculator, adminOnly: false },
  { labelKey: "personas.label", descKey: "personas.description", href: "/dashboard/personas", icon: Users, adminOnly: false },
  { labelKey: "escrituras.label", descKey: "escrituras.description", href: "/dashboard/escrituras", icon: ScrollText, adminOnly: false },
  { labelKey: "pagos.label", descKey: "pagos.description", href: "/dashboard/pagos", icon: CreditCard, adminOnly: false },
  { labelKey: "protocolo.label", descKey: "protocolo.description", href: "/dashboard/protocolo", icon: BookMarked, adminOnly: false },
  { labelKey: "inmuebles.label", descKey: "inmuebles.description", href: "/dashboard/inmuebles", icon: Building2, adminOnly: false },
  { labelKey: "copias.label", descKey: "copias.description", href: "/dashboard/copias", icon: Copy, adminOnly: false },
  { labelKey: "suplencias.label", descKey: "suplencias.description", href: "/dashboard/suplencias", icon: UserRoundCog, adminOnly: false },
  { labelKey: "reportes.label", descKey: "reportes.description", href: "/dashboard/reportes", icon: FileBarChart, adminOnly: false },
  { labelKey: "items.label", descKey: "items.description", href: "/dashboard/items", icon: ListTodo, adminOnly: false },
  { labelKey: "documentos.label", descKey: "documentos.description", href: "/dashboard/documentos", icon: FileText, adminOnly: false },
  { labelKey: "auditoria.label", descKey: "auditoria.description", href: "/dashboard/auditoria", icon: ShieldCheck, adminOnly: true },
  { labelKey: "administracion.label", descKey: "administracion.description", href: "/dashboard/administracion", icon: Settings, adminOnly: true },
];

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
  const locale = useLocale();
  const { user, isAdmin } = useAuthStore();
  // Only the totals are needed: size=1 pages, never the lists (#1340, #1358).
  const gestionesPage = useGestionesPage({ page: 0, size: 1 });
  const personasPage = usePersonasPage({ page: 0, size: 1 });
  const presupuestosPage = usePresupuestosPage({ page: 0, size: 1 });

  const visibleModules = modules.filter((m) => !m.adminOnly || isAdmin());
  const dateLocale = locale === "en" ? "en-US" : "es-AR";

  const stats = [
    { labelKey: "gestiones.label", query: gestionesPage, icon: FolderKanban, tint: "bg-primary/10", iconColor: "text-primary" },
    { labelKey: "personas.label", query: personasPage, icon: Users, tint: "bg-primary/10", iconColor: "text-primary" },
    { labelKey: "presupuestos.label", query: presupuestosPage, icon: Calculator, tint: "bg-primary/10", iconColor: "text-primary" },
  ] as const;

  return (
    <div className="max-w-[1600px] mx-auto space-y-12">
      <Suspense fallback={null}>
        <AccessDeniedBanner />
      </Suspense>
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div className="space-y-1.5">
          <h1 className="text-4xl font-semibold tracking-tight text-foreground">
            {td("hello")}, {user?.nombre?.split(" ")[0]}
          </h1>
          <p className="text-xl text-muted-foreground font-medium">{td("today")}</p>
        </div>
        <div className="bg-white/50 backdrop-blur-sm px-4 py-2 rounded-full border border-black/5 shadow-sm">
          <p
            className="text-sm font-semibold"
            style={{ color: theme.colors.neutral[800] }}
          >
            {new Date().toLocaleDateString(dateLocale, { weekday: "long", year: "numeric", month: "long", day: "numeric" })}
          </p>
        </div>
      </div>

      <Stagger className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {stats.map((stat) => {
          const StatIcon = stat.icon;
          return (
            <StaggerItem key={stat.labelKey}>
              <HoverLift lift={4}>
                <Card className="bg-white border-none apple-shadow rounded-[28px] overflow-hidden">
                  <CardContent className="p-8 flex items-center justify-between">
                    <div className="space-y-1">
                      <p className="text-[13px] font-bold uppercase tracking-widest text-muted-foreground">
                        {td(stat.labelKey as Parameters<typeof td>[0])}
                      </p>
                      <StatValue
                        value={stat.query.data?.totalElements}
                        isLoading={stat.query.isLoading}
                        isError={stat.query.isError}
                      />
                    </div>
                    <div className={`${stat.tint} p-5 rounded-3xl`}>
                      <StatIcon className={`h-8 w-8 ${stat.iconColor}`} />
                    </div>
                  </CardContent>
                </Card>
              </HoverLift>
            </StaggerItem>
          );
        })}
      </Stagger>

      <WorkflowHero />

      <div className="space-y-6">
        <div className="flex items-center justify-between px-2">
          <h2 className="text-2xl font-semibold tracking-tight text-foreground">{td("availableModules")}</h2>
        </div>

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
                          {td(mod.labelKey as Parameters<typeof td>[0])}
                        </CardTitle>
                      </CardHeader>
                      <CardContent className="px-8 pb-8 pt-0">
                        <CardDescription className="text-base text-muted-foreground leading-relaxed">
                          {td(mod.descKey as Parameters<typeof td>[0])}
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
    </div>
  );
}
