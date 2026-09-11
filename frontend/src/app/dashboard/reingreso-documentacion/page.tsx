"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Eye, RotateCcw } from "lucide-react";
import { useTranslations } from "next-intl";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { useGestiones } from "@/hooks/useGestiones";
import { useReingresarDocumentacion, useReingresoDocumentacion } from "@/hooks/useReingresoDocumentacion";
import { extractApiError } from "@/lib/utils";
import type { DocumentoNecesario, GestionDeEscritura, TramiteDocumentacionNecesaria } from "@/types";

export default function ReingresoDocumentacionPage() {
  const t = useTranslations("reingresoDocumentacion");
  const tc = useTranslations("common");

  const { data: gestiones = [], isLoading } = useGestiones();

  const [selectedGestionId, setSelectedGestionId] = useState<number | null>(null);
  const { data: detalle, isLoading: isLoadingDetalle } = useReingresoDocumentacion(selectedGestionId ?? undefined);

  const reingresarMutation = useReingresarDocumentacion();

  async function handleReingresar(idTramite: number, documento: DocumentoNecesario) {
    if (!selectedGestionId) return;
    try {
      await reingresarMutation.mutateAsync({
        gestionId: selectedGestionId,
        reingreso: { idTramite, idTipoDocumento: documento.idDocumentType },
      });
      toast.success(t("documentoReingresado"));
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorReingreso"));
    }
  }

  const gestionColumns: Column<GestionDeEscritura>[] = [
    { key: "numero", header: t("fields.numero"), render: (g) => <span className="font-medium">{g.number}</span> },
    { key: "encabezado", header: t("fields.encabezado"), render: (g) => g.encabezado ?? "—" },
    { key: "estadoActual", header: tc("status"), render: (g) => g.statusActual ?? "—" },
    {
      key: "actions",
      header: "",
      className: "w-32",
      render: (g) => (
        <div className="flex justify-end">
          <Button
            size="sm"
            variant="ghost"
            onClick={() => setSelectedGestionId(g.idManagement!)}
            aria-label={t("verTramites")}
            data-testid={`btn-ver-tramites-${g.idManagement}`}
          >
            <Eye className="h-4 w-4" />
          </Button>
        </div>
      ),
    },
  ];

  function documentoColumns(idTramite: number): Column<DocumentoNecesario>[] {
    return [
      { key: "nombre", header: t("fields.nombre"), render: (d) => d.name ?? "—" },
      {
        key: "vence",
        header: t("fields.vence"),
        render: (d) => <Badge variant={d.expires ? "secondary" : "outline"}>{d.expires ? tc("yes") : tc("no")}</Badge>,
      },
      {
        key: "diasVencimiento",
        header: t("fields.diasVencimiento"),
        render: (d) => (d.expires ? (d.dueDays ?? "—") : "—"),
      },
      { key: "quienEntrega", header: t("fields.quienEntrega"), render: (d) => d.deliveredBy ?? "—" },
      {
        key: "actions",
        header: "",
        className: "w-32",
        render: (d) => (
          <div className="flex justify-end">
            <Button
              size="sm"
              variant="secondary"
              onClick={() => handleReingresar(idTramite, d)}
              disabled={reingresarMutation.isPending}
              data-testid={`btn-reingresar-${idTramite}-${d.idDocumentType}`}
            >
              <RotateCcw className="h-4 w-4 mr-1" />
              {t("reingresar")}
            </Button>
          </div>
        ),
      },
    ];
  }

  function renderTramite(tramite: TramiteDocumentacionNecesaria) {
    return (
      <div key={tramite.idProcedure} className="mb-6" data-testid={`tramite-${tramite.idProcedure}`}>
        <h3 className="text-sm font-semibold mb-2">
          {t("fields.tramite")}: {tramite.typeProcedureName ?? "—"}
        </h3>
        {tramite.documentsNecesarios.length === 0 ? (
          <p className="text-sm text-muted-foreground" data-testid={`empty-documentos-${tramite.idProcedure}`}>
            {t("noDocumentosNecesarios")}
          </p>
        ) : (
          <DataTable
            data={tramite.documentsNecesarios}
            columns={documentoColumns(tramite.idProcedure)}
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
        data={gestiones}
        columns={gestionColumns}
        isLoading={isLoading}
        keyExtractor={(g) => g.idManagement!}
        emptyMessage={t("noData")}
      />

      <Dialog open={!!selectedGestionId} onOpenChange={(v) => !v && setSelectedGestionId(null)}>
        <DialogContent className="max-w-3xl max-h-[85vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>{t("detalleTitle")}</DialogTitle>
          </DialogHeader>
          {isLoadingDetalle ? (
            <p className="text-sm text-muted-foreground">{tc("loading")}</p>
          ) : !detalle || detalle.procedures.length === 0 ? (
            <p className="text-sm text-muted-foreground" data-testid="empty-tramites">
              {t("noTramites")}
            </p>
          ) : (
            detalle.procedures.map(renderTramite)
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}
