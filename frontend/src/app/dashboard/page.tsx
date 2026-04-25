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
} from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
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
    color: "bg-blue-50 text-blue-600",
    adminOnly: false,
  },
  {
    label: "Presupuestos",
    href: "/dashboard/presupuestos",
    icon: Calculator,
    description: "Crear y administrar presupuestos",
    color: "bg-green-50 text-green-600",
    adminOnly: false,
  },
  {
    label: "Personas",
    href: "/dashboard/personas",
    icon: Users,
    description: "Clientes y personas del sistema",
    color: "bg-purple-50 text-purple-600",
    adminOnly: false,
  },
  {
    label: "Escrituras",
    href: "/dashboard/escrituras",
    icon: FileText,
    description: "Protocolo de escrituras",
    color: "bg-orange-50 text-orange-600",
    adminOnly: false,
  },
  {
    label: "Pagos",
    href: "/dashboard/pagos",
    icon: CreditCard,
    description: "Gestionar cobros y pagos",
    color: "bg-pink-50 text-pink-600",
    adminOnly: false,
  },
  {
    label: "Protocolo",
    href: "/dashboard/protocolo",
    icon: BookOpen,
    description: "Folios e índices",
    color: "bg-teal-50 text-teal-600",
    adminOnly: false,
  },
  {
    label: "Administración",
    href: "/dashboard/administracion",
    icon: Settings,
    description: "Usuarios, conceptos y catálogos",
    color: "bg-slate-100 text-slate-600",
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
    <div>
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-2xl font-bold">
          Bienvenido, {user?.nombre}
        </h1>
        <p className="text-muted-foreground mt-1">
          Sistema de Gestión de Escribanía — Panel principal
        </p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">
        <Card>
          <CardContent className="flex items-center gap-4 pt-6">
            <div className="bg-blue-100 p-3 rounded-lg">
              <ClipboardList className="h-5 w-5 text-blue-600" />
            </div>
            <div>
              <p className="text-2xl font-bold">{gestiones?.length ?? "—"}</p>
              <p className="text-sm text-muted-foreground">Gestiones</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center gap-4 pt-6">
            <div className="bg-purple-100 p-3 rounded-lg">
              <Users className="h-5 w-5 text-purple-600" />
            </div>
            <div>
              <p className="text-2xl font-bold">{personas?.length ?? "—"}</p>
              <p className="text-sm text-muted-foreground">Personas</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center gap-4 pt-6">
            <div className="bg-green-100 p-3 rounded-lg">
              <TrendingUp className="h-5 w-5 text-green-600" />
            </div>
            <div>
              <p className="text-2xl font-bold">{presupuestos?.length ?? "—"}</p>
              <p className="text-sm text-muted-foreground">Presupuestos</p>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Module grid */}
      <h2 className="text-lg font-semibold mb-4">Módulos del sistema</h2>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {visibleModules.map((mod) => {
          const Icon = mod.icon;
          return (
            <Link key={mod.href} href={mod.href}>
              <Card className="hover:shadow-md transition-shadow cursor-pointer h-full">
                <CardHeader className="pb-2">
                  <div className={`w-10 h-10 rounded-lg flex items-center justify-center mb-2 ${mod.color}`}>
                    <Icon className="h-5 w-5" />
                  </div>
                  <CardTitle className="text-base">{mod.label}</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-sm text-muted-foreground">{mod.description}</p>
                </CardContent>
              </Card>
            </Link>
          );
        })}
      </div>
    </div>
  );
}
