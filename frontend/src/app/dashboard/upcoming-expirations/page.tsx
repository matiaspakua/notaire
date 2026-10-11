"use client";
import { useFormat } from "@/hooks/useFormat";

import { useState } from "react";
import { useTranslations } from "next-intl";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { FormField } from "@/theme/form-patterns";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useUpcomingExpirations } from "@/hooks/useUpcomingExpirations";
import type { UpcomingExpiration } from "@/types";

const WINDOW_OPTIONS = [7, 15, 30, 60, 90];
const DEFAULT_WINDOW = 30;

export default function UpcomingExpirationsPage() {
  const fmt = useFormat();
  const t = useTranslations("proximosVencimientos");
  const [days, setDays] = useState(DEFAULT_WINDOW);
  const { data: expirations = [], isLoading } = useUpcomingExpirations(days);

  const columns: Column<UpcomingExpiration>[] = [
    { key: "document", header: t("fields.documento"), render: (v) => <span className="font-medium">{v.documentName ?? "—"}</span> },
    {
      key: "management",
      header: t("fields.gestion"),
      render: (v) => (v.managementNumber != null ? `${v.managementNumber} — ${v.managementHeading ?? ""}` : "—"),
    },
    { key: "entryDate", header: t("fields.fechaIngreso"), render: (v) => fmt.date(v.dateEntry) },
    { key: "dueDate", header: t("fields.fechaVencimiento"), render: (v) => fmt.date(v.dateDue) },
    { key: "daysRemaining", header: t("fields.diasRestantes"), render: (v) => v.daysRemaining },
    { key: "prepared", header: t("fields.preparado"), render: (v) => (v.prepared ? t("si") : t("no")) },
    { key: "observed", header: t("fields.observado"), render: (v) => (v.observed ? t("si") : t("no")) },
    { key: "amountDue", header: t("fields.montoDeuda"), render: (v) => fmt.currency(v.amountToPay) },
    { key: "notes", header: t("fields.observaciones"), render: (v) => v.notes ?? "—" },
  ];

  return (
    <div>
      <AppHeader title={t("title")} description={t("description")} />
      <div className="px-6 lg:px-10 py-6 max-w-[1600px] mx-auto space-y-6">
        <FormField label={t("fields.ventana")}>
          <Select value={String(days)} onValueChange={(value) => setDays(Number(value))}>
            <SelectTrigger data-testid="select-expiration-window">
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
          data={expirations}
          columns={columns}
          isLoading={isLoading}
          keyExtractor={(v) => v.idSubmittedDocument}
          emptyMessage={t("noData")}
        />
      </div>
    </div>
  );
}
