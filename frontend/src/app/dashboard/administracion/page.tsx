"use client";

import Link from "next/link";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { AppHeader } from "@/components/layout/AppHeader";
import { NotaireIcon } from "@/components/ui/notaire-icon";

const adminModules = [
  { label: "Usuarios", href: "/dashboard/administracion/usuarios", pngIcon: "/icons/admin/usuarios.png", description: "CU20, CU21, CU23" },
  { label: "Conceptos", href: "/dashboard/administracion/conceptos", pngIcon: "/icons/admin/conceptos.png", description: "CU29, CU66" },
  { label: "Tipos de Documento", href: "/dashboard/administracion/documentos", pngIcon: "/icons/admin/documentos.png", description: "CU27, CU65" },
  { label: "Folios", href: "/dashboard/administracion/folios", pngIcon: "/icons/admin/folios.png", description: "CU28, CU40, CU58, CU63, CU68" },
  { label: "Tipos de Trámite", href: "/dashboard/administracion/tramites", pngIcon: "/icons/admin/tramites.png", description: "CU26, CU57, CU64" },
  { label: "Estados de Gestión", href: "/dashboard/administracion/estados-gestion", pngIcon: "/icons/admin/estadosGestion.png", description: "CU67" },
  { label: "Plantillas Presupuesto", href: "/dashboard/administracion/plantillas", pngIcon: "/icons/admin/plantillaPresupuesto.png", description: "CU39, CU49, CU55" },
];

export default function AdministracionPage() {
  return (
    <div>
      <AppHeader title="Administración" description="Catálogos y configuración del sistema" />
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {adminModules.map((mod) => (
          <Link key={mod.href} href={mod.href}>
            <Card className="hover:shadow-md transition-shadow cursor-pointer h-full">
              <CardHeader className="pb-2">
                <div className="w-14 h-14 rounded-xl flex items-center justify-center mb-2 bg-primary/5">
                  <NotaireIcon src={mod.pngIcon} alt={mod.label} size={48} />
                </div>
                <CardTitle className="text-sm">{mod.label}</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-xs text-muted-foreground">{mod.description}</p>
              </CardContent>
            </Card>
          </Link>
        ))}
      </div>
    </div>
  );
}
