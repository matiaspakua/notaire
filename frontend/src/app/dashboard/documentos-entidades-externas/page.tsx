"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Eye, Pencil } from "lucide-react";
import { useTranslations } from "next-intl";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { FormContainer, FormSection, FormField, FormActions, CheckboxField } from "@/theme/form-patterns";
import { useThemeClasses } from "@/theme";
import { useGestiones } from "@/hooks/useGestiones";
import {
  useDocumentosEntidadExterna,
  useRegistrarMovimientoDocumentoEntidadExterna,
} from "@/hooks/useDocumentosEntidadExterna";
import { extractApiError, formatDate } from "@/lib/utils";
import type { DocumentoEntidadExterna, GestionDeEscritura, MovimientoDocumentoEntidadExternaInput } from "@/types";

const emptyMovimiento: MovimientoDocumentoEntidadExternaInput = {
  prepared: false,
  cardNumber: undefined,
  dateEntry: "",
  dateExit: "",
  flagged: false,
  amountToPay: undefined,
  datePayment: "",
  dateReleased: "",
  notes: "",
  delivered: false,
};

export default function DocumentosEntidadesExternasPage() {
  const t = useTranslations("documentosEntidadesExternas");
  const tc = useTranslations("common");
  const themeClass = useThemeClasses();

  const { data: gestiones = [], isLoading } = useGestiones();

  const [selectedGestionId, setSelectedGestionId] = useState<number | null>(null);
  const { data: detalle, isLoading: isLoadingDetalle } = useDocumentosEntidadExterna(selectedGestionId ?? undefined);

  const [editingDocumento, setEditingDocumento] = useState<DocumentoEntidadExterna | null>(null);
  const [movimiento, setMovimiento] = useState<MovimientoDocumentoEntidadExternaInput>(emptyMovimiento);

  const registrarMutation = useRegistrarMovimientoDocumentoEntidadExterna();

  function openEditar(documento: DocumentoEntidadExterna) {
    setEditingDocumento(documento);
    setMovimiento({
      prepared: documento.prepared ?? false,
      cardNumber: documento.cardNumber,
      dateEntry: documento.dateEntry ?? "",
      dateExit: documento.dateExit ?? "",
      flagged: documento.flagged ?? false,
      amountToPay: documento.amountToPay,
      datePayment: documento.datePayment ?? "",
      dateReleased: documento.dateReleased ?? "",
      notes: documento.notes ?? "",
      delivered: documento.delivered ?? false,
    });
  }

  async function handleGuardarMovimiento() {
    if (!selectedGestionId || !editingDocumento) return;
    try {
      await registrarMutation.mutateAsync({
        gestionId: selectedGestionId,
        idDocumentoPresentado: editingDocumento.idSubmittedDocument,
        movimiento,
      });
      toast.success(t("movimientoRegistrado"));
      setEditingDocumento(null);
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorMovimiento"));
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
            aria-label={t("verDocumentos")}
            data-testid={`btn-ver-documentos-${g.idManagement}`}
          >
            <Eye className="h-4 w-4" />
          </Button>
        </div>
      ),
    },
  ];

  const documentoColumns: Column<DocumentoEntidadExterna>[] = [
    { key: "nombre", header: t("fields.nombre"), render: (d) => d.name ?? "—" },
    {
      key: "delivered",
      header: t("fields.entregado"),
      render: (d) => (
        <Badge variant={d.delivered ? "success" : "secondary"}>
          {d.delivered ? tc("yes") : tc("no")}
        </Badge>
      ),
    },
    { key: "dateEntry", header: t("fields.fechaIngreso"), render: (d) => formatDate(d.dateEntry) },
    { key: "dateExit", header: t("fields.fechaSalida"), render: (d) => formatDate(d.dateExit) },
    {
      key: "actions",
      header: "",
      className: "w-24",
      render: (d) => (
        <div className="flex justify-end">
          <Button
            size="sm"
            variant="ghost"
            onClick={() => openEditar(d)}
            aria-label={t("registrarMovimiento")}
            data-testid={`btn-editar-documento-${d.idSubmittedDocument}`}
          >
            <Pencil className="h-4 w-4" />
          </Button>
        </div>
      ),
    },
  ];

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
          {detalle && (
            <div className="mb-6 grid grid-cols-2 gap-4 text-sm">
              <div>
                <span className="font-semibold">{t("fields.numero")}:</span> {detalle.number ?? "—"}
              </div>
              <div>
                <span className="font-semibold">{t("fields.escribano")}:</span> {detalle.notary ?? "—"}
              </div>
              <div>
                <span className="font-semibold">{t("fields.encabezado")}:</span> {detalle.encabezado ?? "—"}
              </div>
              <div>
                <span className="font-semibold">{t("fields.nomenclaturaCatastral")}:</span>{" "}
                {detalle.cadastralDesignation ?? "—"}
              </div>
            </div>
          )}
          <DataTable
            data={detalle?.documents ?? []}
            columns={documentoColumns}
            isLoading={isLoadingDetalle}
            keyExtractor={(d) => d.idSubmittedDocument}
            emptyMessage={t("noDocumentos")}
          />
        </DialogContent>
      </Dialog>

      <Dialog open={!!editingDocumento} onOpenChange={(v) => !v && setEditingDocumento(null)}>
        <DialogContent className="max-h-[85vh] overflow-y-auto" data-testid="dialog-movimiento">
          <FormContainer>
            <FormSection title={t("registrarMovimiento")}>
              <FormField label={t("fields.numeroCarton")}>
                <Input
                  type="number"
                  value={movimiento.cardNumber ?? ""}
                  onChange={(e) =>
                    setMovimiento((m) => ({ ...m, cardNumber: e.target.value ? Number(e.target.value) : undefined }))
                  }
                  data-testid="input-numero-carton"
                />
              </FormField>
              <FormField label={t("fields.fechaIngreso")}>
                <Input
                  type="date"
                  value={movimiento.dateEntry ?? ""}
                  onChange={(e) => setMovimiento((m) => ({ ...m, dateEntry: e.target.value }))}
                  data-testid="input-fecha-ingreso"
                />
              </FormField>
              <FormField label={t("fields.fechaSalida")}>
                <Input
                  type="date"
                  value={movimiento.dateExit ?? ""}
                  onChange={(e) => setMovimiento((m) => ({ ...m, dateExit: e.target.value }))}
                  data-testid="input-fecha-salida"
                />
              </FormField>
              <FormField label={t("fields.importeAPagar")}>
                <Input
                  type="number"
                  value={movimiento.amountToPay ?? ""}
                  onChange={(e) =>
                    setMovimiento((m) => ({ ...m, amountToPay: e.target.value ? Number(e.target.value) : undefined }))
                  }
                  data-testid="input-importe-a-pagar"
                />
              </FormField>
              <FormField label={t("fields.fechaPago")}>
                <Input
                  type="date"
                  value={movimiento.datePayment ?? ""}
                  onChange={(e) => setMovimiento((m) => ({ ...m, datePayment: e.target.value }))}
                  data-testid="input-fecha-pago"
                />
              </FormField>
              <FormField label={t("fields.fechaLiberado")}>
                <Input
                  type="date"
                  value={movimiento.dateReleased ?? ""}
                  onChange={(e) => setMovimiento((m) => ({ ...m, dateReleased: e.target.value }))}
                  data-testid="input-fecha-liberado"
                />
              </FormField>
              <FormField label={t("fields.observaciones")}>
                <textarea
                  className={themeClass("textarea")}
                  value={movimiento.notes ?? ""}
                  onChange={(e) => setMovimiento((m) => ({ ...m, notes: e.target.value }))}
                  data-testid="input-notes"
                />
              </FormField>
              <CheckboxField
                label={t("fields.preparado")}
                checked={!!movimiento.prepared}
                onChange={(checked) => setMovimiento((m) => ({ ...m, prepared: checked }))}
                data-testid="checkbox-prepared"
              />
              <CheckboxField
                label={t("fields.observado")}
                checked={!!movimiento.flagged}
                onChange={(checked) => setMovimiento((m) => ({ ...m, flagged: checked }))}
                data-testid="checkbox-flagged"
              />
              <CheckboxField
                label={t("fields.entregado")}
                checked={!!movimiento.delivered}
                onChange={(checked) => setMovimiento((m) => ({ ...m, delivered: checked }))}
                data-testid="checkbox-delivered"
              />
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setEditingDocumento(null)}>
                {tc("cancel")}
              </Button>
              <Button
                onClick={handleGuardarMovimiento}
                disabled={registrarMutation.isPending}
                data-testid="btn-guardar-movimiento"
              >
                {tc("save")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>
    </div>
  );
}
