"use client";

import Link from "next/link";
import {
  Users,
  Tag,
  FileCheck,
  BookOpen,
  AlignLeft,
  ListChecks,
  LayoutTemplate,
  ShieldCheck,
} from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { AppHeader } from "@/components/layout/AppHeader";

const adminModules = [
  {
    label: "Usuarios",
    href: "/dashboard/administracion/usuarios",
    icon: Users,
    description: "CU20, CU21, CU23 — Gestionar cuentas de usuario",
  },
  {
    label: "Conceptos",
    href: "/dashboard/administracion/conceptos",
    icon: Tag,
    description: "CU29, CU66 — Catálogo de conceptos de cobro",
  },
  {
    label: "Tipos de Documento",
    href: "/dashboard/administracion/documentos",
    icon: FileCheck,
    description: "CU27, CU65 — Tipos de documentos presentados",
  },
  {
    label: "Folios",
    href: "/dashboard/administracion/folios",
    icon: BookOpen,
    description: "CU28, CU40, CU58, CU68 — Gestión del protocolo de folios",
  },
  {
    label: "Tipos de Trámite",
    href: "/dashboard/administracion/tramites",
    icon: AlignLeft,
    description: "CU26, CU57, CU64 — Catálogo de tipos de trámite",
  },
  {
    label: "Estados de Gestión",
    href: "/dashboard/administracion/estados-gestion",
    icon: ListChecks,
    description: "CU67 — Ciclo de vida de las gestiones",
  },
  {
    label: "Plantillas Presupuesto",
    href: "/dashboard/administracion/plantillas",
    icon: LayoutTemplate,
    description: "CU39, CU49, CU55 — Conceptos por tipo de trámite",
  },
  {
    label: "Auditoría",
    href: "/dashboard/administracion/auditoria",
    icon: ShieldCheck,
    description: "Registro de acciones del sistema",
  },
];

export default function AdministracionPage() {
  return (
    <div>
      <AppHeader
        title="Administración"
        description="Catálogos y configuración del sistema — acceso exclusivo para administradores"
      />
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
        {adminModules.map((mod) => {
          const Icon = mod.icon;
          return (
            <Link key={mod.href} href={mod.href}>
              <Card className="hover:shadow-md transition-shadow cursor-pointer h-full">
                <CardHeader className="pb-2">
                  <div className="w-9 h-9 bg-slate-100 rounded-lg flex items-center justify-center mb-2">
                    <Icon className="h-4 w-4 text-slate-600" />
                  </div>
                  <CardTitle className="text-sm">{mod.label}</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-xs text-muted-foreground">
                    {mod.description}
                  </p>
                </CardContent>
              </Card>
            </Link>
          );
        })}
      </div>
    </div>
  );
}
