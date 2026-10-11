"use client";

import { Suspense, useState } from "react";
import { toast } from "sonner";
import { Eye, RotateCcw } from "lucide-react";
import { useTranslations } from "next-intl";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { useManagementsPage } from "@/hooks/useManagements";
import { useClampPage, useUrlPagination } from "@/hooks/useUrlPagination";
import { useReenterDocumentation, useDocumentReentry } from "@/hooks/useDocumentReentry";
import { extractApiError } from "@/lib/utils";
import type { RequiredDocument, DeedManagement, ProcedureRequiredDocumentation } from "@/types";

export default function DocumentReentryPage() {
  // useUrlPagination reads useSearchParams, which needs a Suspense boundary.
  return (
    <Suspense>
      <DocumentReentryList />
    </Suspense>
  );
}

function DocumentReentryList() {
  const t = useTranslations("reingresoDocumentacion");
  const tc = useTranslations("common");

  // One server page at a time, newest first, page and size in the URL (#1340):
  // the list used to load size=1000 and silently miss every later case.
  const paging = useUrlPagination();
  const { data: managementsPage, isLoading, isFetching } = useManagementsPage({ page: paging.page, size: paging.size });
  const managements = managementsPage?.content ?? [];
  useClampPage(paging, managementsPage?.totalPages);

  const [selectedManagementId, setSelectedManagementId] = useState<number | null>(null);
  const { data: detail, isLoading: isLoadingDetail } = useDocumentReentry(selectedManagementId ?? undefined);

  const reenterMutation = useReenterDocumentation();

  async function handleReenter(idProcedure: number, document: RequiredDocument) {
    if (!selectedManagementId) return;
    try {
      await reenterMutation.mutateAsync({
        managementId: selectedManagementId,
        reentry: { idProcedure: idProcedure, idDocumentType: document.idDocumentType },
      });
      toast.success(t("documentoReingresado"));
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorReingreso"));
    }
  }

  const managementColumns: Column<DeedManagement>[] = [
    { key: "number", header: t("fields.numero"), render: (g) => <span className="font-medium">{g.number}</span> },
    { key: "heading", header: t("fields.encabezado"), render: (g) => g.heading ?? "—" },
    { key: "currentStatus", header: tc("status"), render: (g) => g.statusActual ?? "—" },
    {
      key: "actions",
      header: "",
      className: "w-32",
      render: (g) => (
        <div className="flex justify-end">
          <Button
            size="sm"
            variant="ghost"
            onClick={() => setSelectedManagementId(g.idManagement!)}
            aria-label={t("verTramites")}
            data-testid={`btn-view-procedures-${g.idManagement}`}
          >
            <Eye className="h-4 w-4" />
          </Button>
        </div>
      ),
    },
  ];

  function documentColumns(idProcedure: number): Column<RequiredDocument>[] {
    return [
      { key: "name", header: t("fields.nombre"), render: (d) => d.name ?? "—" },
      {
        key: "expires",
        header: t("fields.vence"),
        render: (d) => <Badge variant={d.expires ? "secondary" : "outline"}>{d.expires ? tc("yes") : tc("no")}</Badge>,
      },
      {
        key: "dueDays",
        header: t("fields.diasVencimiento"),
        render: (d) => (d.expires ? (d.dueDays ?? "—") : "—"),
      },
      { key: "deliveredBy", header: t("fields.quienEntrega"), render: (d) => d.deliveredBy ?? "—" },
      {
        key: "actions",
        header: "",
        className: "w-32",
        render: (d) => (
          <div className="flex justify-end">
            <Button
              size="sm"
              variant="secondary"
              onClick={() => handleReenter(idProcedure, d)}
              disabled={reenterMutation.isPending}
              data-testid={`btn-reenter-${idProcedure}-${d.idDocumentType}`}
            >
              <RotateCcw className="h-4 w-4 mr-1" />
              {t("reingresar")}
            </Button>
          </div>
        ),
      },
    ];
  }

  function renderProcedure(procedure: ProcedureRequiredDocumentation) {
    return (
      <div key={procedure.idProcedure} className="mb-6" data-testid={`procedure-${procedure.idProcedure}`}>
        <h3 className="text-sm font-semibold mb-2">
          {t("fields.tramite")}: {procedure.typeProcedureName ?? "—"}
        </h3>
        {procedure.requiredDocuments.length === 0 ? (
          <p className="text-sm text-muted-foreground" data-testid={`empty-documents-${procedure.idProcedure}`}>
            {t("noDocumentosNecesarios")}
          </p>
        ) : (
          <DataTable
            data={procedure.requiredDocuments}
            columns={documentColumns(procedure.idProcedure)}
            keyExtractor={(d) => d.idDocumentType}
          />
        )}
      </div>
    );
  }

  return (
    <div>
      <AppHeader title={t("title")} />
      <DataTable
        data={managements}
        columns={managementColumns}
        isLoading={isLoading}
        isFetching={isFetching}
        keyExtractor={(g) => g.idManagement!}
        emptyMessage={t("noData")}
        pagination={{
          page: managementsPage?.number ?? paging.page,
          size: paging.size,
          totalElements: managementsPage?.totalElements ?? 0,
          onPageChange: paging.setPage,
          onSizeChange: paging.setSize,
        }}
      />

      <Dialog open={!!selectedManagementId} onOpenChange={(v) => !v && setSelectedManagementId(null)}>
        <DialogContent className="max-w-3xl max-h-[85vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>{t("detalleTitle")}</DialogTitle>
          </DialogHeader>
          {isLoadingDetail ? (
            <p className="text-sm text-muted-foreground">{tc("loading")}</p>
          ) : !detail || detail.procedures.length === 0 ? (
            <p className="text-sm text-muted-foreground" data-testid="empty-procedures">
              {t("noTramites")}
            </p>
          ) : (
            detail.procedures.map(renderProcedure)
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}
