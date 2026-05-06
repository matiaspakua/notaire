"use client";

import Link from "next/link";
import {
  ClipboardList,
  Calculator,
  Users,
  FileText,
  CreditCard,
  BookOpen,
  Settings,
  TrendingUp,
  Building2,
  Copy,
  ListTodo,
  FileCheck,
  Shield,
  ArrowRight,
} from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { useAuthStore } from "@/store/auth-store";
import { useGestiones } from "@/hooks/useGestiones";
import { usePersonas } from "@/hooks/usePersonas";
import { usePresupuestos } from "@/hooks/usePresupuestos";

const modules = [
  {
    label: "Gestiones",
    href: "/dashboard/gestiones",
    icon: ClipboardList,
    description: "Gestionar trámites y escrituras",
    gradient: "from-blue-500 to-blue-600",
    bgLight: "bg-blue-50",
    adminOnly: false,
  },
  {
    label: "Presupuestos",
    href: "/dashboard/presupuestos",
    icon: Calculator,
    description: "Crear y administrar presupuestos",
    gradient: "from-emerald-500 to-emerald-600",
    bgLight: "bg-emerald-50",
    adminOnly: false,
  },
  {
    label: "Personas",
    href: "/dashboard/personas",
    icon: Users,
    description: "Clientes y personas del sistema",
    gradient: "from-violet-500 to-violet-600",
    bgLight: "bg-violet-50",
    adminOnly: false,
  },
  {
    label: "Escrituras",
    href: "/dashboard/escrituras",
    icon: FileText,
    description: "Protocolo de escrituras",
    gradient: "from-orange-500 to-orange-600",
    bgLight: "bg-orange-50",
    adminOnly: false,
  },
  {
    label: "Pagos",
    href: "/dashboard/pagos",
    icon: CreditCard,
    description: "Gestionar cobros y pagos",
    gradient: "from-pink-500 to-pink-600",
    bgLight: "bg-pink-50",
    adminOnly: false,
  },
  {
    label: "Protocolo",
    href: "/dashboard/protocolo",
    icon: BookOpen,
    description: "Folios e índices",
    gradient: "from-teal-500 to-teal-600",
    bgLight: "bg-teal-50",
    adminOnly: false,
  },
  {
    label: "Inmuebles",
    href: "/dashboard/inmuebles",
    icon: Building2,
    description: "Gestión de propiedades",
    gradient: "from-cyan-500 to-cyan-600",
    bgLight: "bg-cyan-50",
    adminOnly: false,
  },
  {
    label: "Copias",
    href: "/dashboard/copias",
    icon: Copy,
    description: "Gestión de copias de documentos",
    gradient: "from-indigo-500 to-indigo-600",
    bgLight: "bg-indigo-50",
    adminOnly: false,
  },
  {
    label: "Items",
    href: "/dashboard/items",
    icon: ListTodo,
    description: "Items de presupuestos",
    gradient: "from-amber-500 to-amber-600",
    bgLight: "bg-amber-50",
    adminOnly: false,
  },
  {
    label: "Documentos",
    href: "/dashboard/documentos",
    icon: FileCheck,
    description: "Documentos presentados",
    gradient: "from-rose-500 to-rose-600",
    bgLight: "bg-rose-50",
    adminOnly: false,
  },
  {
    label: "Auditoría",
    href: "/dashboard/auditoria",
    icon: Shield,
    description: "Registro de actividades",
    gradient: "from-slate-500 to-slate-600",
    bgLight: "bg-slate-50",
    adminOnly: true,
  },
  {
    label: "Administración",
    href: "/dashboard/administracion",
    icon: Settings,
    description: "Usuarios, conceptos y catálogos",
    gradient: "from-gray-500 to-gray-600",
    bgLight: "bg-gray-50",
    adminOnly: true,
  },
];

export default function DashboardPage() {
  const { user, isAdmin } = useAuthStore();
  const { data: gestiones } = useGestiones();
  const { data: personas } = usePersonas();
  const { data: presupuestos } = usePresupuestos();

  const visibleModules = modules.filter((m) => !m.adminOnly || isAdmin());

  return (
    <div className="max-w-[1600px] mx-auto space-y-12 animate-in fade-in slide-in-from-bottom-4 duration-1000">
      {/* Welcome Header */}
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div className="space-y-1.5">
          <h1 className="text-4xl font-semibold tracking-tight text-[#1d1d1f]">
            Hola, {user?.nombre?.split(" ")[0]}
          </h1>
          <p className="text-xl text-[#86868b] font-medium">
            Resumen de actividad para hoy
          </p>
        </div>
        <div className="bg-white/50 backdrop-blur-sm px-4 py-2 rounded-full border border-black/5 shadow-sm">
          <p className="text-sm font-semibold text-[#424245]">
            {new Date().toLocaleDateString("es-AR", { weekday: "long", year: "numeric", month: "long", day: "numeric" })}
          </p>
        </div>
      </div>

      {/* Stats - Apple Bento Grid style */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card className="bg-white border-none apple-shadow rounded-[28px] overflow-hidden group hover:scale-[1.01] transition-transform duration-500">
          <CardContent className="p-8 flex items-center justify-between">
            <div className="space-y-1">
              <p className="text-[13px] font-bold uppercase tracking-widest text-[#86868b]">Gestiones</p>
              <p className="text-5xl font-semibold tracking-tighter text-[#1d1d1f]">{gestiones?.length ?? "0"}</p>
            </div>
            <div className="bg-blue-500/10 p-5 rounded-3xl text-blue-600">
              <ClipboardList className="h-8 w-8" />
            </div>
          </CardContent>
        </Card>
        
        <Card className="bg-white border-none apple-shadow rounded-[28px] overflow-hidden group hover:scale-[1.01] transition-transform duration-500">
          <CardContent className="p-8 flex items-center justify-between">
            <div className="space-y-1">
              <p className="text-[13px] font-bold uppercase tracking-widest text-[#86868b]">Personas</p>
              <p className="text-5xl font-semibold tracking-tighter text-[#1d1d1f]">{personas?.length ?? "0"}</p>
            </div>
            <div className="bg-violet-500/10 p-5 rounded-3xl text-violet-600">
              <Users className="h-8 w-8" />
            </div>
          </CardContent>
        </Card>

        <Card className="bg-white border-none apple-shadow rounded-[28px] overflow-hidden group hover:scale-[1.01] transition-transform duration-500">
          <CardContent className="p-8 flex items-center justify-between">
            <div className="space-y-1">
              <p className="text-[13px] font-bold uppercase tracking-widest text-[#86868b]">Presupuestos</p>
              <p className="text-5xl font-semibold tracking-tighter text-[#1d1d1f]">{presupuestos?.length ?? "0"}</p>
            </div>
            <div className="bg-emerald-500/10 p-5 rounded-3xl text-emerald-600">
              <TrendingUp className="h-8 w-8" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Module grid */}
      <div className="space-y-6">
        <div className="flex items-center justify-between px-2">
          <h2 className="text-2xl font-semibold tracking-tight text-[#1d1d1f]">Módulos disponibles</h2>
          <Button variant="link" className="text-[#0071e3] font-semibold text-sm group">
            Ver todos los servicios <ArrowRight className="ml-1 h-4 w-4 group-hover:translate-x-1 transition-transform" />
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
                      <Icon className="h-7 w-7" />
                    </div>
                    <CardTitle className="text-xl font-semibold text-[#1d1d1f] group-hover:text-[#0071e3] transition-colors duration-300">
                      {mod.label}
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="px-8 pb-8 pt-0">
                    <CardDescription className="text-base text-[#86868b] leading-relaxed">
                      {mod.description}
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
