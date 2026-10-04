"use client";

import { useState } from "react";
import { useTranslations } from "next-intl";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { FormField } from "@/theme/form-patterns";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useProximosVencimientos } from "@/hooks/useProximosVencimientos";
import { formatCurrency, formatDate } from "@/lib/utils";
import type { ProximoVencimiento } from "@/types";

const WINDOW_OPTIONS = [7, 15, 30, 60, 90];
const DEFAULT_WINDOW = 30;

export default function ProximosVencimientosPage() {
  const t = useTranslations("proximosVencimientos");
  const [dias, setDias] = useState(DEFAULT_WINDOW);
  const { data: vencimientos = [], isLoading } = useProximosVencimientos(dias);

  const columns: Column<ProximoVencimiento>[] = [
    { key: "documento", header: t("fields.documento"), render: (v) => <span className="font-medium">{v.documentName ?? "—"}</span> },
    {
      key: "gestion",
      header: t("fields.gestion"),
      render: (v) => (v.managementNumber != null ? `${v.managementNumber} — ${v.managementHeading ?? ""}` : "—"),
    },
    { key: "fechaIngreso", header: t("fields.fechaIngreso"), render: (v) => formatDate(v.dateEntry) },
    { key: "fechaVencimiento", header: t("fields.fechaVencimiento"), render: (v) => formatDate(v.dateDue) },
    { key: "diasRestantes", header: t("fields.diasRestantes"), render: (v) => v.daysRemaining },
    { key: "preparado", header: t("fields.preparado"), render: (v) => (v.prepared ? t("si") : t("no")) },
    { key: "observado", header: t("fields.observado"), render: (v) => (v.observed ? t("si") : t("no")) },
    { key: "montoDeuda", header: t("fields.montoDeuda"), render: (v) => formatCurrency(v.amountToPay) },
    { key: "observaciones", header: t("fields.observaciones"), render: (v) => v.notes ?? "—" },
  ];

  return (
    <div>
      <AppHeader title={t("title")} description={t("description")} />
      <div className="px-6 lg:px-10 py-6 max-w-[1600px] mx-auto space-y-6">
        <FormField label={t("fields.ventana")}>
          <Select value={String(dias)} onValueChange={(value) => setDias(Number(value))}>
            <SelectTrigger data-testid="select-ventana-vencimientos">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {WINDOW_OPTIONS.map((option) => (
                <SelectItem key={option} value={String(option)}>
                  {t("ventanaDias", { dias: option })}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </FormField>
        <DataTable
          data={vencimientos}
          columns={columns}
          isLoading={isLoading}
          keyExtractor={(v) => v.idSubmittedDocument}
          emptyMessage={t("noData")}
        />
      </div>
    </div>
  );
}
