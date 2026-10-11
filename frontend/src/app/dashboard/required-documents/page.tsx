"use client";

import { useState } from "react";
import { Printer } from "lucide-react";
import { useTranslations } from "next-intl";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { FormContainer, FormSection, FormField } from "@/theme/form-patterns";
import { useProcedureTypes } from "@/hooks/useProcedureTypes";
import { useProcedureTemplate } from "@/hooks/useProcedureTemplate";
import type { ProcedureTemplateRow } from "@/types";

export default function DocumentsRequiredPage() {
  const t = useTranslations("documentosNecesarios");
  const tc = useTranslations("common");

  const { data: procedureTypes = [] } = useProcedureTypes();
  const [procedureId, setProcedureId] = useState("");
  const idTypeProcedure = procedureId ? Number(procedureId) : undefined;
  const { data: documents = [], isLoading, isFetched } = useProcedureTemplate(idTypeProcedure);

  const columns: Column<ProcedureTemplateRow>[] = [
    {
      key: "name",
      header: t("fields.nombre"),
      render: (p) => <span className="font-medium">{p.documentType?.name}</span>,
    },
    {
      key: "expires",
      header: t("fields.vence"),
      render: (p) => (p.documentType?.expires ? tc("yes") : tc("no")),
    },
    {
      key: "dueDays",
      header: t("fields.diasVencimiento"),
      render: (p) => (p.documentType?.expires ? (p.documentType?.dueDays ?? "—") : "—"),
    },
    {
      key: "deliveredBy",
      header: t("fields.quienEntrega"),
      render: (p) => p.documentType?.deliveredBy ?? "—",
    },
  ];

  const selectedProcedureName = procedureTypes.find((tt) => tt.idProcedureType === idTypeProcedure)?.name ?? "";

  return (
    <div>
      <AppHeader
        title={t("title")}
        description={t("description")}
        actions={
          idTypeProcedure && documents.length > 0 ? (
            <Button onClick={() => window.print()} data-testid="btn-print">
              <Printer size={16} className="mr-1" />
              {t("print")}
            </Button>
          ) : undefined
        }
      />

      <div className="max-w-[1600px] mx-auto p-6 lg:p-10 space-y-6">
        <FormContainer>
          <FormSection title={t("selectorTitle")}>
            <FormField label={t("fields.tramite")} required>
              <Select value={procedureId} onValueChange={setProcedureId}>
                <SelectTrigger data-testid="select-procedure">
                  <SelectValue placeholder={t("fields.tramitePlaceholder")} />
                </SelectTrigger>
                <SelectContent>
                  {procedureTypes.map((tt) => (
                    <SelectItem key={tt.idProcedureType} value={String(tt.idProcedureType)}>
                      {tt.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </FormField>
          </FormSection>
        </FormContainer>

        {idTypeProcedure !== undefined && (
          <div data-printable="true">
            <h2 className="text-lg font-semibold mb-4">
              {t("resultsTitle", { tramite: selectedProcedureName })}
            </h2>
            {isFetched && documents.length === 0 ? (
              <p className="text-sm text-muted-foreground" data-testid="empty-state">
                {t("noDocuments")}
              </p>
            ) : (
              <DataTable
                data={documents}
                columns={columns}
                isLoading={isLoading}
                emptyMessage={t("noDocuments")}
                keyExtractor={(p) => `${p.fkIdProcedureType}-${p.fkIdDocumentType}`}
              />
            )}
          </div>
        )}
      </div>
    </div>
  );
}
