"use client";

import Link from "next/link";
import { useTranslations } from "next-intl";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { AppHeader } from "@/components/layout/AppHeader";
import { NotaireIcon } from "@/components/ui/notaire-icon";

export default function AdministracionPage() {
  const t = useTranslations("administracion");

  const adminModules = [
    { key: "usuarios" as const, href: "/dashboard/administracion/usuarios", pngIcon: "/icons/admin/usuarios.png" },
    { key: "conceptos" as const, href: "/dashboard/administracion/conceptos", pngIcon: "/icons/admin/conceptos.png" },
    { key: "documentos" as const, href: "/dashboard/administracion/documentos", pngIcon: "/icons/admin/documentos.png" },
    { key: "folios" as const, href: "/dashboard/administracion/folios", pngIcon: "/icons/admin/folios.png" },
    { key: "tramites" as const, href: "/dashboard/administracion/tramites", pngIcon: "/icons/admin/tramites.png" },
    { key: "estadosGestion" as const, href: "/dashboard/administracion/estados-gestion", pngIcon: "/icons/admin/estadosGestion.png" },
    { key: "plantillas" as const, href: "/dashboard/administracion/plantillas", pngIcon: "/icons/admin/plantillaPresupuesto.png" },
  ];

  return (
    <div>
      <AppHeader title={t("title")} description={t("description")} />
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {adminModules.map((mod) => (
          <Link key={mod.href} href={mod.href}>
            <Card className="hover:shadow-md transition-shadow cursor-pointer h-full">
              <CardHeader className="pb-2">
                <div className="w-14 h-14 rounded-xl flex items-center justify-center mb-2 bg-primary/5">
                  <NotaireIcon src={mod.pngIcon} alt={t(`modules.${mod.key}.label`)} size={48} />
                </div>
                <CardTitle className="text-sm">{t(`modules.${mod.key}.label`)}</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-xs text-muted-foreground">{t(`modules.${mod.key}.description`)}</p>
              </CardContent>
            </Card>
          </Link>
        ))}
      </div>
    </div>
  );
}
