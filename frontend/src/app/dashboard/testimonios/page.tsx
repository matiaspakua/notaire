"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Plus, CheckCircle2, Download } from "lucide-react";
import { useTranslations } from "next-intl";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { FormContainer, FormSection, FormField, FormActions, CheckboxField } from "@/theme/form-patterns";
import { useThemeClasses } from "@/theme";
import { useTestimonios, useGenerarTestimonio, useVerificarTestimonio, descargarCopiaTestimonio } from "@/hooks/useTestimonios";
import { useEscrituras } from "@/hooks/useEscrituras";
import { extractApiError } from "@/lib/utils";
import type { Testimonio } from "@/types";

const ESTADO_FIRMADA = "Firmada";

export default function TestimoniosPage() {
  const t = useTranslations("testimonios");
  const tc = useTranslations("common");
  const themeClass = useThemeClasses();

  const { data: testimonios = [], isLoading } = useTestimonios();
  const { data: escrituras = [] } = useEscrituras();
  const generarMutation = useGenerarTestimonio();
  const verificarMutation = useVerificarTestimonio();

  const [modalOpen, setModalOpen] = useState(false);
  const [idEscritura, setIdEscritura] = useState("");
  const [verificarId, setVerificarId] = useState<number | null>(null);
  const [observado, setObservado] = useState(false);
  const [observaciones, setObservaciones] = useState("");
  const [downloadingId, setDownloadingId] = useState<number | null>(null);

  const escriturasFirmadas = escrituras.filter((e) => e.estado === ESTADO_FIRMADA);

  function openGenerar() {
    setIdEscritura("");
    setModalOpen(true);
  }

  function openVerificar(testimonio: Testimonio) {
    setVerificarId(testimonio.idTestimonio!);
    setObservado(false);
    setObservaciones("");
  }

  async function handleGenerar() {
    try {
      await generarMutation.mutateAsync(Number(idEscritura));
      toast.success(t("generated"));
      setModalOpen(false);
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorGenerate"));
    }
  }

  async function handleVerificar() {
    if (!verificarId) return;
    try {
      await verificarMutation.mutateAsync({ id: verificarId, observado, observaciones: observaciones || undefined });
      toast.success(t("verified"));
      setVerificarId(null);
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorVerify"));
    }
  }

  async function handleDescargarCopia(testimonio: Testimonio) {
    try {
      setDownloadingId(testimonio.idTestimonio!);
      await descargarCopiaTestimonio(testimonio.idTestimonio!);
    } catch {
      toast.error(t("errorCopia"));
    } finally {
      setDownloadingId(null);
    }
  }

  function estadoLabel(testimonio: Testimonio): string {
    if (!testimonio.verificado) return t("estadoGenerado");
    return testimonio.observado ? t("estadoObservado") : t("estadoVerificado");
  }

  const columns: Column<Testimonio>[] = [
    { key: "id", header: tc("id"), render: (te) => <span className="text-xs text-muted-foreground">{te.idTestimonio}</span>, className: "w-12" },
    { key: "numero", header: t("fields.numero"), render: (te) => <span className="font-medium">{te.numero}</span> },
    { key: "escritura", header: t("fields.escritura"), render: (te) => te.escritura?.numero ?? "—" },
    { key: "estado", header: t("fields.estado"), render: (te) => estadoLabel(te) },
    {
      key: "actions",
      header: "",
      className: "w-32",
      render: (te) => (
        <div className="flex gap-2 justify-end">
          {!te.verificado && (
            <Button
              size="sm"
              variant="ghost"
              onClick={() => openVerificar(te)}
              aria-label={t("verificarTestimonio")}
              data-testid={`btn-verificar-testimonio-${te.idTestimonio}`}
            >
              <CheckCircle2 className="h-4 w-4" />
            </Button>
          )}
          {te.verificado && (
            <Button
              size="sm"
              variant="ghost"
              onClick={() => handleDescargarCopia(te)}
              disabled={downloadingId === te.idTestimonio}
              aria-label={t("emitirCopia")}
              data-testid={`btn-emitir-copia-${te.idTestimonio}`}
            >
              <Download className="h-4 w-4" />
            </Button>
          )}
        </div>
      ),
    },
  ];

  return (
    <div>
      <AppHeader
        title={t("title")}
        actions={
          <Button onClick={openGenerar} data-testid="btn-generar-testimonio">
            <Plus className="h-4 w-4" />
            {t("newTestimonio")}
          </Button>
        }
      />

      <DataTable data={testimonios} columns={columns} isLoading={isLoading} keyExtractor={(te) => te.idTestimonio!} emptyMessage={t("noData")} />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={t("newTestimonio")}>
              <FormField label={t("fields.escritura")} required>
                <Select value={idEscritura} onValueChange={setIdEscritura}>
                  <SelectTrigger data-testid="select-escritura-testimonio">
                    <SelectValue placeholder={t("selectEscritura")} />
                  </SelectTrigger>
                  <SelectContent>
                    {escriturasFirmadas.map((e) => (
                      <SelectItem key={e.idEscritura} value={String(e.idEscritura)}>
                        {t("fields.numero")} {e.numero}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                {tc("cancel")}
              </Button>
              <Button onClick={handleGenerar} disabled={!idEscritura || generarMutation.isPending} data-testid="btn-guardar-testimonio">
                {tc("create")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <Dialog open={!!verificarId} onOpenChange={(v) => !v && setVerificarId(null)}>
        <DialogContent>
          <FormContainer>
            <FormSection title={t("verificarTestimonio")}>
              <CheckboxField label={t("fields.observado")} checked={observado} onChange={setObservado} data-testid="checkbox-observado-testimonio" />
              <FormField label={t("fields.observaciones")}>
                <textarea
                  className={themeClass("textarea")}
                  value={observaciones}
                  onChange={(e) => setObservaciones(e.target.value)}
                  data-testid="input-observaciones-testimonio"
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setVerificarId(null)}>
                {tc("cancel")}
              </Button>
              <Button onClick={handleVerificar} disabled={verificarMutation.isPending} data-testid="btn-confirmar-verificar-testimonio">
                {t("verificarTestimonio")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>
    </div>
  );
}
