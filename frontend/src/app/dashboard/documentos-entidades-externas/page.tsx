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
  preparado: false,
  numeroCarton: undefined,
  fechaIngreso: "",
  fechaSalida: "",
  observado: false,
  importeAPagar: undefined,
  fechaPago: "",
  fechaLiberado: "",
  observaciones: "",
  entregado: false,
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
      preparado: documento.preparado ?? false,
      numeroCarton: documento.numeroCarton,
      fechaIngreso: documento.fechaIngreso ?? "",
      fechaSalida: documento.fechaSalida ?? "",
      observado: documento.observado ?? false,
      importeAPagar: documento.importeAPagar,
      fechaPago: documento.fechaPago ?? "",
      fechaLiberado: documento.fechaLiberado ?? "",
      observaciones: documento.observaciones ?? "",
      entregado: documento.entregado ?? false,
    });
  }

  async function handleGuardarMovimiento() {
    if (!selectedGestionId || !editingDocumento) return;
    try {
      await registrarMutation.mutateAsync({
        gestionId: selectedGestionId,
        idDocumentoPresentado: editingDocumento.idDocumentoPresentado,
        movimiento,
      });
      toast.success(t("movimientoRegistrado"));
      setEditingDocumento(null);
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorMovimiento"));
    }
  }

  const gestionColumns: Column<GestionDeEscritura>[] = [
    { key: "numero", header: t("fields.numero"), render: (g) => <span className="font-medium">{g.numero}</span> },
    { key: "encabezado", header: t("fields.encabezado"), render: (g) => g.encabezado ?? "—" },
    { key: "estadoActual", header: tc("status"), render: (g) => g.estadoActual ?? "—" },
    {
      key: "actions",
      header: "",
      className: "w-32",
      render: (g) => (
        <div className="flex justify-end">
          <Button
            size="sm"
            variant="ghost"
            onClick={() => setSelectedGestionId(g.idGestion!)}
            aria-label={t("verDocumentos")}
            data-testid={`btn-ver-documentos-${g.idGestion}`}
          >
            <Eye className="h-4 w-4" />
          </Button>
        </div>
      ),
    },
  ];

  const documentoColumns: Column<DocumentoEntidadExterna>[] = [
    { key: "nombre", header: t("fields.nombre"), render: (d) => d.nombre ?? "—" },
    {
      key: "entregado",
      header: t("fields.entregado"),
      render: (d) => (
        <Badge variant={d.entregado ? "success" : "secondary"}>
          {d.entregado ? tc("yes") : tc("no")}
        </Badge>
      ),
    },
    { key: "fechaIngreso", header: t("fields.fechaIngreso"), render: (d) => formatDate(d.fechaIngreso) },
    { key: "fechaSalida", header: t("fields.fechaSalida"), render: (d) => formatDate(d.fechaSalida) },
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
            data-testid={`btn-editar-documento-${d.idDocumentoPresentado}`}
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
        keyExtractor={(g) => g.idGestion!}
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
                <span className="font-semibold">{t("fields.numero")}:</span> {detalle.numero ?? "—"}
              </div>
              <div>
                <span className="font-semibold">{t("fields.escribano")}:</span> {detalle.escribano ?? "—"}
              </div>
              <div>
                <span className="font-semibold">{t("fields.encabezado")}:</span> {detalle.encabezado ?? "—"}
              </div>
              <div>
                <span className="font-semibold">{t("fields.nomenclaturaCatastral")}:</span>{" "}
                {detalle.nomenclaturaCatastral ?? "—"}
              </div>
            </div>
          )}
          <DataTable
            data={detalle?.documentos ?? []}
            columns={documentoColumns}
            isLoading={isLoadingDetalle}
            keyExtractor={(d) => d.idDocumentoPresentado}
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
                  value={movimiento.numeroCarton ?? ""}
                  onChange={(e) =>
                    setMovimiento((m) => ({ ...m, numeroCarton: e.target.value ? Number(e.target.value) : undefined }))
                  }
                  data-testid="input-numero-carton"
                />
              </FormField>
              <FormField label={t("fields.fechaIngreso")}>
                <Input
                  type="date"
                  value={movimiento.fechaIngreso ?? ""}
                  onChange={(e) => setMovimiento((m) => ({ ...m, fechaIngreso: e.target.value }))}
                  data-testid="input-fecha-ingreso"
                />
              </FormField>
              <FormField label={t("fields.fechaSalida")}>
                <Input
                  type="date"
                  value={movimiento.fechaSalida ?? ""}
                  onChange={(e) => setMovimiento((m) => ({ ...m, fechaSalida: e.target.value }))}
                  data-testid="input-fecha-salida"
                />
              </FormField>
              <FormField label={t("fields.importeAPagar")}>
                <Input
                  type="number"
                  value={movimiento.importeAPagar ?? ""}
                  onChange={(e) =>
                    setMovimiento((m) => ({ ...m, importeAPagar: e.target.value ? Number(e.target.value) : undefined }))
                  }
                  data-testid="input-importe-a-pagar"
                />
              </FormField>
              <FormField label={t("fields.fechaPago")}>
                <Input
                  type="date"
                  value={movimiento.fechaPago ?? ""}
                  onChange={(e) => setMovimiento((m) => ({ ...m, fechaPago: e.target.value }))}
                  data-testid="input-fecha-pago"
                />
              </FormField>
              <FormField label={t("fields.fechaLiberado")}>
                <Input
                  type="date"
                  value={movimiento.fechaLiberado ?? ""}
                  onChange={(e) => setMovimiento((m) => ({ ...m, fechaLiberado: e.target.value }))}
                  data-testid="input-fecha-liberado"
                />
              </FormField>
              <FormField label={t("fields.observaciones")}>
                <textarea
                  className={themeClass("textarea")}
                  value={movimiento.observaciones ?? ""}
                  onChange={(e) => setMovimiento((m) => ({ ...m, observaciones: e.target.value }))}
                  data-testid="input-observaciones"
                />
              </FormField>
              <CheckboxField
                label={t("fields.preparado")}
                checked={!!movimiento.preparado}
                onChange={(checked) => setMovimiento((m) => ({ ...m, preparado: checked }))}
                data-testid="checkbox-preparado"
              />
              <CheckboxField
                label={t("fields.observado")}
                checked={!!movimiento.observado}
                onChange={(checked) => setMovimiento((m) => ({ ...m, observado: checked }))}
                data-testid="checkbox-observado"
              />
              <CheckboxField
                label={t("fields.entregado")}
                checked={!!movimiento.entregado}
                onChange={(checked) => setMovimiento((m) => ({ ...m, entregado: checked }))}
                data-testid="checkbox-entregado"
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
