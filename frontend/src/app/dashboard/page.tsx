"use client";

import Link from "next/link";
import {
  Building2,
  Copy,
  ListTodo,
  Shield,
  ArrowRight,
} from "lucide-react";
import { useTranslations, useLocale } from "next-intl";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { NotaireIcon } from "@/components/ui/notaire-icon";
import { useAuthStore } from "@/store/auth-store";
import { useGestiones } from "@/hooks/useGestiones";
import { usePersonas } from "@/hooks/usePersonas";
import { usePresupuestos } from "@/hooks/usePresupuestos";
import type { ComponentType } from "react";

type LucideIcon = ComponentType<{ className?: string }>;

interface Module {
  labelKey: string;
  descKey: string;
  href: string;
  icon?: LucideIcon;
  pngIcon?: string;
  gradient: string;
  adminOnly: boolean;
}

const modules: Module[] = [
  { labelKey: "gestiones.label", descKey: "gestiones.description", href: "/dashboard/gestiones", pngIcon: "/icons/modulos/gestion.png", gradient: "from-blue-500 to-blue-600", adminOnly: false },
  { labelKey: "presupuestos.label", descKey: "presupuestos.description", href: "/dashboard/presupuestos", pngIcon: "/icons/modulos/presupuestos.png", gradient: "from-emerald-500 to-emerald-600", adminOnly: false },
  { labelKey: "personas.label", descKey: "personas.description", href: "/dashboard/personas", pngIcon: "/icons/modulos/clientes.png", gradient: "from-violet-500 to-violet-600", adminOnly: false },
  { labelKey: "escrituras.label", descKey: "escrituras.description", href: "/dashboard/escrituras", pngIcon: "/icons/modulos/escrituras.png", gradient: "from-orange-500 to-orange-600", adminOnly: false },
  { labelKey: "pagos.label", descKey: "pagos.description", href: "/dashboard/pagos", pngIcon: "/icons/modulos/pagos.png", gradient: "from-pink-500 to-pink-600", adminOnly: false },
  { labelKey: "protocolo.label", descKey: "protocolo.description", href: "/dashboard/protocolo", pngIcon: "/icons/modulos/protocolo.png", gradient: "from-teal-500 to-teal-600", adminOnly: false },
  { labelKey: "inmuebles.label", descKey: "inmuebles.description", href: "/dashboard/inmuebles", icon: Building2, gradient: "from-cyan-500 to-cyan-600", adminOnly: false },
  { labelKey: "copias.label", descKey: "copias.description", href: "/dashboard/copias", icon: Copy, gradient: "from-indigo-500 to-indigo-600", adminOnly: false },
  { labelKey: "items.label", descKey: "items.description", href: "/dashboard/items", icon: ListTodo, gradient: "from-amber-500 to-amber-600", adminOnly: false },
  { labelKey: "documentos.label", descKey: "documentos.description", href: "/dashboard/documentos", pngIcon: "/icons/admin/documentos.png", gradient: "from-rose-500 to-rose-600", adminOnly: false },
  { labelKey: "auditoria.label", descKey: "auditoria.description", href: "/dashboard/auditoria", icon: Shield, gradient: "from-slate-500 to-slate-600", adminOnly: true },
  { labelKey: "administracion.label", descKey: "administracion.description", href: "/dashboard/administracion", pngIcon: "/icons/modulos/admin.png", gradient: "from-gray-500 to-gray-600", adminOnly: true },
];

export default function DashboardPage() {
  const td = useTranslations("dashboard");
  const locale = useLocale();
  const { user, isAdmin } = useAuthStore();
  const { data: gestiones } = useGestiones();
  const { data: personas } = usePersonas();
  const { data: presupuestos } = usePresupuestos();

  const visibleModules = modules.filter((m) => !m.adminOnly || isAdmin());
  const dateLocale = locale === "en" ? "en-US" : "es-AR";

  return (
    <div className="max-w-[1600px] mx-auto space-y-12 animate-in fade-in slide-in-from-bottom-4 duration-1000">
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div className="space-y-1.5">
          <h1 className="text-4xl font-semibold tracking-tight text-[#1d1d1f]">
            {td("hello")}, {user?.nombre?.split(" ")[0]}
          </h1>
          <p className="text-xl text-[#86868b] font-medium">
            {td("today")}
          </p>
        </div>
        <div className="bg-white/50 backdrop-blur-sm px-4 py-2 rounded-full border border-black/5 shadow-sm">
          <p className="text-sm font-semibold text-[#424245]">
            {new Date().toLocaleDateString(dateLocale, { weekday: "long", year: "numeric", month: "long", day: "numeric" })}
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card className="bg-white border-none apple-shadow rounded-[28px] overflow-hidden group hover:scale-[1.01] transition-transform duration-500">
          <CardContent className="p-8 flex items-center justify-between">
            <div className="space-y-1">
              <p className="text-[13px] font-bold uppercase tracking-widest text-[#86868b]">{td("gestiones.label")}</p>
              <p className="text-5xl font-semibold tracking-tighter text-[#1d1d1f]">{gestiones?.length ?? "0"}</p>
            </div>
            <div className="bg-blue-500/10 p-5 rounded-3xl">
              <NotaireIcon src="/icons/modulos/gestion.png" alt={td("gestiones.label")} size={32} />
            </div>
          </CardContent>
        </Card>

        <Card className="bg-white border-none apple-shadow rounded-[28px] overflow-hidden group hover:scale-[1.01] transition-transform duration-500">
          <CardContent className="p-8 flex items-center justify-between">
            <div className="space-y-1">
              <p className="text-[13px] font-bold uppercase tracking-widest text-[#86868b]">{td("personas.label")}</p>
              <p className="text-5xl font-semibold tracking-tighter text-[#1d1d1f]">{personas?.length ?? "0"}</p>
            </div>
            <div className="bg-violet-500/10 p-5 rounded-3xl">
              <NotaireIcon src="/icons/modulos/clientes.png" alt={td("personas.label")} size={32} />
            </div>
          </CardContent>
        </Card>

        <Card className="bg-white border-none apple-shadow rounded-[28px] overflow-hidden group hover:scale-[1.01] transition-transform duration-500">
          <CardContent className="p-8 flex items-center justify-between">
            <div className="space-y-1">
              <p className="text-[13px] font-bold uppercase tracking-widest text-[#86868b]">{td("presupuestos.label")}</p>
              <p className="text-5xl font-semibold tracking-tighter text-[#1d1d1f]">{presupuestos?.length ?? "0"}</p>
            </div>
            <div className="bg-emerald-500/10 p-5 rounded-3xl">
              <NotaireIcon src="/icons/modulos/presupuestos.png" alt={td("presupuestos.label")} size={32} />
            </div>
          </CardContent>
        </Card>
      </div>

      <div className="space-y-6">
        <div className="flex items-center justify-between px-2">
          <h2 className="text-2xl font-semibold tracking-tight text-[#1d1d1f]">{td("availableModules")}</h2>
          <Button variant="link" className="text-[#0071e3] font-semibold text-sm group">
            {td("viewAll")} <ArrowRight className="ml-1 h-4 w-4 group-hover:translate-x-1 transition-transform" />
          </Button>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {visibleModules.map((mod) => {
            const Icon = mod.icon;
            return (
              <Link key={mod.href} href={mod.href} className="group">
                <Card className="h-full bg-white border-none apple-shadow rounded-[28px] hover:apple-shadow-lg transition-all duration-500 relative overflow-hidden">
                  <div className={`absolute top-0 left-0 w-1.5 h-full bg-gradient-to-b ${mod.gradient} opacity-0 group-hover:opacity-100 transition-opacity duration-500`} />
                  <CardHeader className="pb-4 p-8">
                    <div className={`w-14 h-14 rounded-2xl flex items-center justify-center mb-6 bg-gradient-to-br ${mod.gradient} text-white shadow-lg shadow-blue-500/10 transition-transform duration-500 group-hover:scale-110`}>
                      {mod.pngIcon ? (
                        <NotaireIcon src={mod.pngIcon} alt={td(mod.labelKey as Parameters<typeof td>[0])} size={36} className="brightness-0 invert" />
                      ) : Icon ? (
                        <Icon className="h-7 w-7" />
                      ) : null}
                    </div>
                    <CardTitle className="text-xl font-semibold text-[#1d1d1f] group-hover:text-[#0071e3] transition-colors duration-300">
                      {td(mod.labelKey as Parameters<typeof td>[0])}
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="px-8 pb-8 pt-0">
                    <CardDescription className="text-base text-[#86868b] leading-relaxed">
                      {td(mod.descKey as Parameters<typeof td>[0])}
                    </CardDescription>
                  </CardContent>
                </Card>
              </Link>
            );
          })}
        </div>
      </div>
    </div>
  );
}
