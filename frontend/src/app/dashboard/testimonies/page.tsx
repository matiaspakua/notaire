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
import { useTestimonies, useGenerateTestimony, useVerifyTestimony, downloadTestimonyCopy } from "@/hooks/useTestimonies";
import { DeedPicker } from "@/components/shared/DeedPicker";
import { extractApiError } from "@/lib/utils";
import type { Testimony } from "@/types";

export default function TestimoniesPage() {
  const t = useTranslations("testimonios");
  const tc = useTranslations("common");
  const themeClass = useThemeClasses();

  const { data: testimonies = [], isLoading } = useTestimonies();
  const generateMutation = useGenerateTestimony();
  const verifyMutation = useVerifyTestimony();

  const [modalOpen, setModalOpen] = useState(false);
  const [idDeed, setIdDeed] = useState("");
  const [verifyId, setVerifyId] = useState<number | null>(null);
  const [flagged, setFlagged] = useState(false);
  const [notes, setNotes] = useState("");
  const [downloadingId, setDownloadingId] = useState<number | null>(null);

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

  async function handleDownloadCopy(testimony: Testimony) {
    try {
      setDownloadingId(testimony.idTestimony!);
      await downloadTestimonyCopy(testimony.idTestimony!);
    } catch {
      toast.error(t("errorCopia"));
    } finally {
      setDownloadingId(null);
    }
  }

  const columns: Column<Testimony>[] = [
    { key: "id", header: tc("id"), render: (te) => <span className="text-xs text-muted-foreground">{te.idTestimony}</span>, className: "w-12" },
    { key: "number", header: t("fields.numero"), render: (te) => <span className="font-medium">{te.number}</span> },
    { key: "deed", header: t("fields.escritura"), render: (te) => te.deed?.number ?? "—" },
    { key: "status", header: t("fields.estado"), render: (te) => <StatusBadge domain="testimony" value={testimonyStatus(te)} /> },
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
              onClick={() => handleDownloadCopy(te)}
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
                <DeedPicker
                  value={idDeed ? Number(idDeed) : undefined}
                  onChange={(deed) => setIdDeed(deed?.idDeed != null ? String(deed.idDeed) : "")}
                  aria-label={t("fields.escritura")}
                  data-testid="select-deed-testimony"
                />
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
