"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Plus, CheckCircle2, Download, Pencil, Trash2, History } from "lucide-react";
import { useTranslations } from "next-intl";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { StatusBadge } from "@/components/shared/StatusBadge";
import { testimonyStatus } from "@/lib/status-maps";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { FormContainer, FormSection, FormField, FormActions, CheckboxField } from "@/theme/form-patterns";
import { useThemeClasses } from "@/theme";
import { useTestimonies, useGenerateTestimony, useVerifyTestimony, useUpdateTestimony, useDeleteTestimony, downloadTestimonyCopy } from "@/hooks/useTestimonies";
import { Input } from "@/components/ui/input";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { useDeleteError } from "@/hooks/useDeleteError";
import { ApiError } from "@/lib/api-client";
import { TestimonyMovementsDialog } from "./TestimonyMovementsDialog";
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
  const updateMutation = useUpdateTestimony();
  const deleteMutation = useDeleteTestimony();
  const showDeleteError = useDeleteError();

  const [modalOpen, setModalOpen] = useState(false);
  const [idDeed, setIdDeed] = useState("");
  const [verifyId, setVerifyId] = useState<number | null>(null);
  const [flagged, setFlagged] = useState(false);
  const [notes, setNotes] = useState("");
  const [downloadingId, setDownloadingId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Testimony | null>(null);
  const [editNumber, setEditNumber] = useState("");
  const [editNotes, setEditNotes] = useState("");
  const [deleting, setDeleting] = useState<Testimony | null>(null);
  const [movementsOf, setMovementsOf] = useState<Testimony | null>(null);

  function openEdit(testimony: Testimony) {
    setEditing(testimony);
    setEditNumber(String(testimony.number ?? ""));
    setEditNotes(testimony.notes ?? "");
  }

  async function handleSaveEdit() {
    if (!editing?.idTestimony) return;
    const number = editNumber.trim();
    if (!/^[1-9]\d*$/.test(number)) {
      toast.error(t("numberRequired"));
      return;
    }
    try {
      await updateMutation.mutateAsync({
        id: editing.idTestimony,
        data: {
          number: Number(number),
          notes: editNotes.trim(),
          flagged: editing.flagged ?? false,
          verified: editing.verified ?? false,
          version: editing.version ?? 0,
        },
      });
      toast.success(t("updated"));
      setEditing(null);
    } catch (err) {
      toast.error(err instanceof ApiError && err.status === 409 ? t("staleVersion") : extractApiError(err) ?? t("errorUpdate"));
    }
  }

  async function handleDelete() {
    if (!deleting?.idTestimony) return;
    try {
      await deleteMutation.mutateAsync(deleting.idTestimony);
      toast.success(t("deleted"));
    } catch (err) {
      if (err instanceof ApiError && err.status === 409) toast.warning(t("deleteInUse"));
      else showDeleteError(err, t("errorDelete"));
    } finally {
      setDeleting(null);
    }
  }

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
      className: "w-52",
      render: (te) => (
        <div className="flex gap-2 justify-end">
          <Button
            size="icon-sm"
            variant="ghost"
            onClick={() => setMovementsOf(te)}
            aria-label={t("verMovimientos")}
            title={t("verMovimientos")}
            data-testid={`btn-movements-testimony-${te.idTestimony}`}
          >
            <History aria-hidden />
          </Button>
          <Button
            size="icon-sm"
            variant="ghost"
            onClick={() => openEdit(te)}
            aria-label={t("editTestimonio")}
            title={t("editTestimonio")}
            data-testid={`btn-edit-testimony-${te.idTestimony}`}
          >
            <Pencil aria-hidden />
          </Button>
          {!te.verified && (
            <Button
              size="icon-sm"
              variant="ghost"
              onClick={() => openVerify(te)}
              aria-label={t("verificarTestimonio")}
              data-testid={`btn-verify-testimony-${te.idTestimony}`}
            >
              <CheckCircle2 aria-hidden />
            </Button>
          )}
          {te.verified && (
            <Button
              size="icon-sm"
              variant="ghost"
              onClick={() => handleDownloadCopy(te)}
              disabled={downloadingId === te.idTestimony}
              aria-label={t("emitirCopia")}
              data-testid={`btn-issue-copy-${te.idTestimony}`}
            >
              <Download aria-hidden />
            </Button>
          )}
          <Button
            size="icon-sm"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleting(te)}
            aria-label={tc("delete")}
            title={tc("delete")}
            data-testid={`btn-delete-testimony-${te.idTestimony}`}
          >
            <Trash2 aria-hidden />
          </Button>
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

      <Dialog open={editing !== null} onOpenChange={(v) => !v && setEditing(null)}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={t("editTestimonio")}>
              <FormField label={t("fields.numero")} required>
                <Input
                  value={editNumber}
                  onChange={(e) => setEditNumber(e.target.value)}
                  inputMode="numeric"
                  data-testid="input-edit-testimony-number"
                />
              </FormField>
              <FormField label={t("fields.observaciones")}>
                <textarea
                  className={themeClass("textarea")}
                  value={editNotes}
                  onChange={(e) => setEditNotes(e.target.value)}
                  data-testid="input-edit-testimony-notes"
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setEditing(null)}>
                {tc("cancel")}
              </Button>
              <Button onClick={handleSaveEdit} disabled={updateMutation.isPending} data-testid="btn-save-edit-testimony">
                {tc("save")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <ConfirmDialog
        open={deleting !== null}
        onOpenChange={(v) => { if (!v) setDeleting(null); }}
        title={t("deleteTitle")}
        description={t("deleteDescription", { number: deleting?.number ?? "" })}
        onConfirm={handleDelete}
        loading={deleteMutation.isPending}
      />

      <TestimonyMovementsDialog testimony={movementsOf} onClose={() => setMovementsOf(null)} />
    </div>
  );
}
