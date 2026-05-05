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
    <div className="space-y-8">
      {/* Welcome Header */}
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-foreground">
            Buenos días, {user?.nombre?.split(" ")[0]}
          </h1>
          <p className="text-muted-foreground mt-1 text-base">
            Sistema de Gestión de Escribanía — Panel principal
          </p>
        </div>
        <div className="text-right text-sm text-muted-foreground">
          <p>{new Date().toLocaleDateString("es-AR", { weekday: "long", year: "numeric", month: "long", day: "numeric" })}</p>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Card className="apple-shadow">
          <CardContent className="flex items-center gap-4 pt-6">
            <div className="bg-blue-50 p-3 rounded-xl">
              <ClipboardList className="h-5 w-5 text-blue-600" />
            </div>
            <div>
              <p className="text-2xl font-bold tracking-tight">{gestiones?.length ?? "—"}</p>
              <p className="text-sm text-muted-foreground">Gestiones activas</p>
            </div>
          </CardContent>
        </Card>
        <Card className="apple-shadow">
          <CardContent className="flex items-center gap-4 pt-6">
            <div className="bg-violet-50 p-3 rounded-xl">
              <Users className="h-5 w-5 text-violet-600" />
            </div>
            <div>
              <p className="text-2xl font-bold tracking-tight">{personas?.length ?? "—"}</p>
              <p className="text-sm text-muted-foreground">Personas registradas</p>
            </div>
          </CardContent>
        </Card>
        <Card className="apple-shadow">
          <CardContent className="flex items-center gap-4 pt-6">
            <div className="bg-emerald-50 p-3 rounded-xl">
              <TrendingUp className="h-5 w-5 text-emerald-600" />
            </div>
            <div>
              <p className="text-2xl font-bold tracking-tight">{presupuestos?.length ?? "—"}</p>
              <p className="text-sm text-muted-foreground">Presupuestos</p>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Module grid */}
      <div>
        <h2 className="text-xl font-semibold tracking-tight mb-4">Módulos del sistema</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {visibleModules.map((mod) => {
            const Icon = mod.icon;
            return (
              <Link key={mod.href} href={mod.href}>
                <Card className="hover:apple-shadow-lg transition-all duration-300 cursor-pointer h-full group border-border/50 hover:border-border">
                  <CardHeader className="pb-3">
                    <div className={`w-11 h-11 rounded-xl flex items-center justify-center mb-3 bg-gradient-to-br ${mod.gradient} text-white shadow-sm`}>
                      <Icon className="h-5 w-5" />
                    </div>
                    <CardTitle className="text-base flex items-center justify-between">
                      {mod.label}
                      <ArrowRight className="h-4 w-4 text-muted-foreground/50 group-hover:text-primary group-hover:translate-x-1 transition-all" />
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <CardDescription>{mod.description}</CardDescription>
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
