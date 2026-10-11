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
import { useTestimonies, useGenerateTestimony, useVerifyTestimony, downloadTestimonyCopy } from "@/hooks/useTestimonies";
import { useDeeds } from "@/hooks/useDeeds";
import { extractApiError } from "@/lib/utils";
import type { Testimony } from "@/types";

const STATUS_SIGNED = "Firmada";

export default function TestimoniesPage() {
  const t = useTranslations("testimonios");
  const tc = useTranslations("common");
  const themeClass = useThemeClasses();

  const { data: testimonies = [], isLoading } = useTestimonies();
  const { data: deeds = [] } = useDeeds();
  const generateMutation = useGenerateTestimony();
  const verifyMutation = useVerifyTestimony();

  const [modalOpen, setModalOpen] = useState(false);
  const [idDeed, setIdDeed] = useState("");
  const [verifyId, setVerifyId] = useState<number | null>(null);
  const [flagged, setFlagged] = useState(false);
  const [notes, setNotes] = useState("");
  const [downloadingId, setDownloadingId] = useState<number | null>(null);

  const deedsSigned = deeds.filter((e) => e.status === STATUS_SIGNED);

  function openGenerate() {
    setIdDeed("");
    setModalOpen(true);
  }

  function openVerify(testimony: Testimony) {
    setVerifyId(testimony.idTestimony!);
    setFlagged(false);
    setNotes("");
  }

  async function handleGenerate() {
    try {
      await generateMutation.mutateAsync(Number(idDeed));
      toast.success(t("generated"));
      setModalOpen(false);
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorGenerate"));
    }
  }

  async function handleVerify() {
    if (!verifyId) return;
    try {
      await verifyMutation.mutateAsync({ id: verifyId, flagged, notes: notes || undefined });
      toast.success(t("verified"));
      setVerifyId(null);
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorVerify"));
    }
  }

  async function handleDescargarCopia(testimony: Testimony) {
    try {
      setDownloadingId(testimony.idTestimony!);
      await downloadTestimonyCopy(testimony.idTestimony!);
    } catch {
      toast.error(t("errorCopia"));
    } finally {
      setDownloadingId(null);
    }
  }

  function estadoLabel(testimony: Testimony): string {
    if (!testimony.verified) return t("estadoGenerado");
    return testimony.flagged ? t("estadoObservado") : t("estadoVerificado");
  }

  const columns: Column<Testimony>[] = [
    { key: "id", header: tc("id"), render: (te) => <span className="text-xs text-muted-foreground">{te.idTestimony}</span>, className: "w-12" },
    { key: "number", header: t("fields.numero"), render: (te) => <span className="font-medium">{te.number}</span> },
    { key: "escritura", header: t("fields.escritura"), render: (te) => te.deed?.number ?? "—" },
    { key: "estado", header: t("fields.estado"), render: (te) => estadoLabel(te) },
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
              onClick={() => openVerify(te)}
              aria-label={t("verificarTestimonio")}
              data-testid={`btn-verify-testimony-${te.idTestimony}`}
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
              data-testid={`btn-issue-copy-${te.idTestimony}`}
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
          <Button onClick={openGenerate} data-testid="btn-generate-testimony">
            <Plus className="h-4 w-4" />
            {t("newTestimonio")}
          </Button>
        }
      />

      <DataTable data={testimonies} columns={columns} isLoading={isLoading} keyExtractor={(te) => te.idTestimony!} emptyMessage={t("noData")} />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={t("newTestimonio")}>
              <FormField label={t("fields.escritura")} required>
                <Select value={idDeed} onValueChange={setIdDeed}>
                  <SelectTrigger data-testid="select-deed-testimony">
                    <SelectValue placeholder={t("selectEscritura")} />
                  </SelectTrigger>
                  <SelectContent>
                    {deedsSigned.map((e) => (
                      <SelectItem key={e.idDeed} value={String(e.idDeed)}>
                        {t("fields.numero")} {e.number}
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
              <Button onClick={handleGenerate} disabled={!idDeed || generateMutation.isPending} data-testid="btn-save-testimony">
                {tc("create")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <Dialog open={!!verifyId} onOpenChange={(v) => !v && setVerifyId(null)}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={t("verificarTestimonio")}>
              <CheckboxField label={t("fields.observado")} checked={flagged} onChange={setFlagged} data-testid="checkbox-observed-testimony" />
              <FormField label={t("fields.observaciones")}>
                <textarea
                  className={themeClass("textarea")}
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  data-testid="input-notes-testimony"
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setVerifyId(null)}>
                {tc("cancel")}
              </Button>
              <Button onClick={handleVerify} disabled={verifyMutation.isPending} data-testid="btn-confirm-verify-testimony">
                {t("verificarTestimonio")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>
    </div>
  );
}
