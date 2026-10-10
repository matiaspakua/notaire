"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Plus, CheckCircle2, Download } from "lucide-react";
import { useTranslations } from "next-intl";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { StatusBadge } from "@/components/shared/StatusBadge";
import { testimonyStatus } from "@/lib/status-maps";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { FormContainer, FormSection, FormField, FormActions, CheckboxField } from "@/theme/form-patterns";
import { useThemeClasses } from "@/theme";
import { useTestimonios, useGenerarTestimonio, useVerificarTestimonio, descargarCopiaTestimonio } from "@/hooks/useTestimonios";
import { DeedPicker } from "@/components/shared/DeedPicker";
import { extractApiError } from "@/lib/utils";
import type { Testimonio } from "@/types";

export default function TestimoniosPage() {
  const t = useTranslations("testimonios");
  const tc = useTranslations("common");
  const themeClass = useThemeClasses();

  const { data: testimonios = [], isLoading } = useTestimonios();
  const generarMutation = useGenerarTestimonio();
  const verificarMutation = useVerificarTestimonio();

  const [modalOpen, setModalOpen] = useState(false);
  const [idEscritura, setIdEscritura] = useState("");
  const [verificarId, setVerificarId] = useState<number | null>(null);
  const [observado, setObservado] = useState(false);
  const [observaciones, setObservaciones] = useState("");
  const [downloadingId, setDownloadingId] = useState<number | null>(null);

  function openGenerar() {
    setIdEscritura("");
    setModalOpen(true);
  }

  function openVerificar(testimonio: Testimonio) {
    setVerificarId(testimonio.idTestimony!);
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
      setDownloadingId(testimonio.idTestimony!);
      await descargarCopiaTestimonio(testimonio.idTestimony!);
    } catch {
      toast.error(t("errorCopia"));
    } finally {
      setDownloadingId(null);
    }
  }

  const columns: Column<Testimonio>[] = [
    { key: "id", header: tc("id"), render: (te) => <span className="text-xs text-muted-foreground">{te.idTestimony}</span>, className: "w-12" },
    { key: "numero", header: t("fields.numero"), render: (te) => <span className="font-medium">{te.number}</span> },
    { key: "escritura", header: t("fields.escritura"), render: (te) => te.deed?.number ?? "—" },
    { key: "estado", header: t("fields.estado"), render: (te) => <StatusBadge domain="testimony" value={testimonyStatus(te)} /> },
    {
      key: "actions",
      header: "",
      className: "w-32",
      render: (te) => (
        <div className="flex gap-2 justify-end">
          {!te.verified && (
            <Button
              size="sm"
              variant="ghost"
              onClick={() => openVerificar(te)}
              aria-label={t("verificarTestimonio")}
              data-testid={`btn-verificar-testimonio-${te.idTestimony}`}
            >
              <CheckCircle2 className="h-4 w-4" />
            </Button>
          )}
          {te.verified && (
            <Button
              size="sm"
              variant="ghost"
              onClick={() => handleDescargarCopia(te)}
              disabled={downloadingId === te.idTestimony}
              aria-label={t("emitirCopia")}
              data-testid={`btn-emitir-copia-${te.idTestimony}`}
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

      <DataTable data={testimonios} columns={columns} isLoading={isLoading} keyExtractor={(te) => te.idTestimony!} emptyMessage={t("noData")} />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={t("newTestimonio")}>
              <FormField label={t("fields.escritura")} required>
                <DeedPicker
                  value={idEscritura ? Number(idEscritura) : undefined}
                  onChange={(deed) => setIdEscritura(deed?.idDeed != null ? String(deed.idDeed) : "")}
                  aria-label={t("fields.escritura")}
                  data-testid="select-escritura-testimonio"
                />
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
            <FormSection dialogTitle title={t("verificarTestimonio")}>
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
