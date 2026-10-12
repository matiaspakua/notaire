"use client";
/**
 * #1554 / CU79: overview of every required-document template (GET /procedure-templates),
 * filterable client-side; each row links to that procedure type's checklist editor.
 */
import { useMemo, useState } from "react";
import Link from "next/link";
import { useTranslations } from "next-intl";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Input } from "@/components/ui/input";
import { useAllProcedureTemplates } from "@/hooks/useProcedureTemplate";
import { useProcedureTypes } from "@/hooks/useProcedureTypes";
import { useDocumentTypes } from "@/hooks/useDocuments";

interface OverviewRow {
  procedureTypeId: number;
  documentTypeId: number;
  procedureName: string;
  documentName: string;
  notes: string;
}

export function AllProcedureTemplates() {
  const t = useTranslations("administracion.plantillas.todas");
  const { data: templates = [], isLoading } = useAllProcedureTemplates();
  const { data: procedureTypes = [] } = useProcedureTypes();
  const { data: documentTypes = [] } = useDocumentTypes();
  const [procedureFilter, setProcedureFilter] = useState("");
  const [documentFilter, setDocumentFilter] = useState("");

  const rows = useMemo<OverviewRow[]>(() => {
    const ptName = new Map(procedureTypes.map((p) => [p.idProcedureType, p.name ?? ""]));
    const dtName = new Map(documentTypes.map((d) => [d.idDocumentType, d.name ?? ""]));
    return templates.map((tpl) => ({
      procedureTypeId: tpl.fkIdProcedureType,
      documentTypeId: tpl.fkIdDocumentType,
      procedureName: ptName.get(tpl.fkIdProcedureType) || `#${tpl.fkIdProcedureType}`,
      documentName: dtName.get(tpl.fkIdDocumentType) || `#${tpl.fkIdDocumentType}`,
      notes: tpl.notes ?? "",
    }));
  }, [templates, procedureTypes, documentTypes]);

  const filtered = useMemo(() => {
    const p = procedureFilter.trim().toLowerCase();
    const d = documentFilter.trim().toLowerCase();
    return rows
      .filter((r) => (!p || r.procedureName.toLowerCase().includes(p)) && (!d || r.documentName.toLowerCase().includes(d)))
      .sort((a, b) => a.procedureName.localeCompare(b.procedureName) || a.documentName.localeCompare(b.documentName));
  }, [rows, procedureFilter, documentFilter]);

  const columns: Column<OverviewRow>[] = [
    {
      key: "procedure",
      header: t("tipoTramite"),
      render: (r) => (
        <Link
          href={`/dashboard/administration/procedures?checklist=${r.procedureTypeId}`}
          className="font-medium text-primary underline-offset-2 hover:underline"
          data-testid={`all-templates-open-${r.procedureTypeId}-${r.documentTypeId}`}
        >
          {r.procedureName}
        </Link>
      ),
    },
    { key: "document", header: t("tipoDocumento"), render: (r) => r.documentName },
    { key: "required", header: t("condicion"), render: () => t("requerido") },
    { key: "notes", header: t("notas"), render: (r) => r.notes || "—" },
  ];

  return (
    <section className="mt-8 space-y-4" data-testid="all-templates">
      <div>
        <h2 className="text-lg font-semibold">{t("title")}</h2>
        <p className="text-sm text-muted-foreground">{t("description")}</p>
      </div>
      <div className="flex flex-col gap-2 sm:flex-row">
        <Input
          placeholder={t("filtroTramite")}
          aria-label={t("filtroTramite")}
          value={procedureFilter}
          onChange={(e) => setProcedureFilter(e.target.value)}
          className="sm:max-w-xs"
          data-testid="all-templates-filter-procedure"
        />
        <Input
          placeholder={t("filtroDocumento")}
          aria-label={t("filtroDocumento")}
          value={documentFilter}
          onChange={(e) => setDocumentFilter(e.target.value)}
          className="sm:max-w-xs"
          data-testid="all-templates-filter-document"
        />
      </div>
      <div data-testid="all-templates-table">
        {!isLoading && filtered.length === 0 ? (
          <p className="text-sm text-muted-foreground" data-testid="all-templates-empty">
            {t("noData")}
          </p>
        ) : (
          <DataTable
            data={filtered}
            columns={columns}
            isLoading={isLoading}
            emptyMessage={t("noData")}
            keyExtractor={(r) => `${r.procedureTypeId}-${r.documentTypeId}`}
          />
        )}
      </div>
    </section>
  );
}
