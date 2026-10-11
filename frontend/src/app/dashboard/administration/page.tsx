"use client";

import Link from "next/link";
import { useTranslations } from "next-intl";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { AppHeader } from "@/components/layout/AppHeader";
import { NotaireIcon } from "@/components/ui/notaire-icon";

export default function AdministrationPage() {
  const t = useTranslations("administracion");

  const adminModules = [
    { key: "usuarios" as const, href: "/dashboard/administration/users", pngIcon: "/icons/admin/users.png" },
    { key: "conceptos" as const, href: "/dashboard/administration/concepts", pngIcon: "/icons/admin/concepts.png" },
    { key: "documentos" as const, href: "/dashboard/administration/documents", pngIcon: "/icons/admin/documents.png" },
    { key: "folios" as const, href: "/dashboard/administration/folios", pngIcon: "/icons/admin/folios.png" },
    { key: "tramites" as const, href: "/dashboard/administration/procedures", pngIcon: "/icons/admin/procedures.png" },
    { key: "estadosGestion" as const, href: "/dashboard/administration/management-statuses", pngIcon: "/icons/admin/management-statuses.png" },
    { key: "tiposIdentificacion" as const, href: "/dashboard/administration/identification-types", pngIcon: "/icons/admin/users.png" },
    { key: "plantillas" as const, href: "/dashboard/administration/templates", pngIcon: "/icons/admin/budget-templates.png" },
    { key: "roles" as const, href: "/dashboard/administration/roles", pngIcon: "/icons/admin/users.png" },
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
