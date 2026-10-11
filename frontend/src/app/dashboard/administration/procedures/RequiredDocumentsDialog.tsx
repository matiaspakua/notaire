"use client";
/**
 * Required-documents checklist of one procedure type (#1362, CU55): list, add, edit notes and
 * remove (with confirmation). ADMIN only, like the rest of the administration section (Administración).
 */
import { useState } from "react";
import { toast } from "sonner";
import { useTranslations } from "next-intl";
import { Check, Pencil, Plus, Trash2, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { FormContainer, FormSection, FormField } from "@/theme/form-patterns";
import { useDocumentTypes } from "@/hooks/useDocuments";
import { useDeleteError } from "@/hooks/useDeleteError";
import {
  useCreateProcedureTemplate,
  useDeleteProcedureTemplate,
  useProcedureTemplate,
  useUpdateProcedureTemplate,
} from "@/hooks/useProcedureTemplate";
import { ApiError } from "@/lib/api-client";
import type { ProcedureType, ProcedureTemplateRow } from "@/types";

interface Props {
  procedureType: Pick<ProcedureType, "idProcedureType" | "name"> | null;
  onClose: () => void;
}

export function RequiredDocumentsDialog({ procedureType, onClose }: Props) {
  const t = useTranslations("administracion.tramites.requiredDocs");
  const tc = useTranslations("common");
  const showDeleteError = useDeleteError();
  const procedureTypeId = procedureType?.idProcedureType;

  const { data: rows = [], isLoading } = useProcedureTemplate(procedureTypeId);
  const { data: documentTypes = [] } = useDocumentTypes();
  const createMutation = useCreateProcedureTemplate();
  const updateMutation = useUpdateProcedureTemplate();
  const deleteMutation = useDeleteProcedureTemplate();

  const [newDocTypeId, setNewDocTypeId] = useState("");
  const [newNotes, setNewNotes] = useState("");
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editNotes, setEditNotes] = useState("");
  const [deleting, setDeleting] = useState<ProcedureTemplateRow | null>(null);

  const listed = new Set(rows.map((r) => r.fkIdDocumentType));
  const available = documentTypes.filter((d) => d.idDocumentType !== undefined && !listed.has(d.idDocumentType));

  async function handleAdd() {
    if (!procedureTypeId) return;
    if (!newDocTypeId) { toast.error(t("documentRequired")); return; }
    try {
      await createMutation.mutateAsync({ procedureTypeId, documentTypeId: Number(newDocTypeId), notes: newNotes.trim() });
      toast.success(t("added"));
      setNewDocTypeId("");
      setNewNotes("");
    } catch (err) {
      toast.error(err instanceof ApiError && err.status === 409 ? t("duplicate") : t("errorSave"));
    }
  }

  async function handleSaveNotes(row: ProcedureTemplateRow) {
    try {
      await updateMutation.mutateAsync({ procedureTypeId: row.fkIdProcedureType, documentTypeId: row.fkIdDocumentType, notes: editNotes.trim() });
      toast.success(t("updated"));
      setEditingId(null);
    } catch {
      toast.error(t("errorSave"));
    }
  }

  async function handleDelete() {
    if (!deleting) return;
    try {
      await deleteMutation.mutateAsync({ procedureTypeId: deleting.fkIdProcedureType, documentTypeId: deleting.fkIdDocumentType });
      toast.success(t("removed"));
    } catch (err) {
      showDeleteError(err, t("errorDelete"));
    } finally {
      setDeleting(null);
    }
  }

  return (
    <>
      <Dialog open={procedureType !== null} onOpenChange={(open) => { if (!open) onClose(); }}>
        <DialogContent className="max-w-2xl">
          <FormContainer>
            <FormSection dialogTitle title={t("title", { name: procedureType?.name ?? "" })} subtitle={t("description")}>
              {isLoading ? (
                <p role="status" className="text-sm text-muted-foreground">{tc("loading")}</p>
              ) : rows.length === 0 ? (
                <p className="text-sm text-muted-foreground" data-testid="required-docs-empty">{t("empty")}</p>
              ) : (
                <ul className="flex flex-col divide-y divide-border/60" data-testid="required-docs-list">
                  {rows.map((row) => (
                    <li key={row.fkIdDocumentType} className="flex items-center gap-3 py-3" data-testid={`required-doc-row-${row.fkIdDocumentType}`}>
                      <div className="min-w-0 flex-1">
                        <p className="font-medium text-foreground break-words">{row.documentType?.name ?? `#${row.fkIdDocumentType}`}</p>
                        {editingId === row.fkIdDocumentType ? (
                          <Input
                            value={editNotes}
                            onChange={(e) => setEditNotes(e.target.value)}
                            aria-label={t("notes")}
                            className="mt-2"
                            data-testid="input-edit-required-doc-notes"
                          />
                        ) : row.notes ? (
                          <p className="text-sm text-muted-foreground break-words">{row.notes}</p>
                        ) : null}
                      </div>
                      {editingId === row.fkIdDocumentType ? (
                        <div className="flex shrink-0 gap-2">
                          <Button size="icon-sm" variant="ghost" onClick={() => handleSaveNotes(row)} aria-label={tc("save")} data-testid="btn-save-required-doc">
                            <Check aria-hidden />
                          </Button>
                          <Button size="icon-sm" variant="ghost" onClick={() => setEditingId(null)} aria-label={tc("cancel")}>
                            <X aria-hidden />
                          </Button>
                        </div>
                      ) : (
                        <div className="flex shrink-0 gap-2">
                          <Button
                            size="icon-sm"
                            variant="ghost"
                            onClick={() => { setEditingId(row.fkIdDocumentType); setEditNotes(row.notes ?? ""); }}
                            aria-label={tc("edit")}
                            data-testid="btn-edit-required-doc"
                          >
                            <Pencil aria-hidden />
                          </Button>
                          <Button
                            size="icon-sm"
                            variant="ghost"
                            className="text-destructive hover:text-destructive"
                            onClick={() => setDeleting(row)}
                            aria-label={tc("delete")}
                            data-testid="btn-delete-required-doc"
                          >
                            <Trash2 aria-hidden />
                          </Button>
                        </div>
                      )}
                    </li>
                  ))}
                </ul>
              )}
            </FormSection>
            <FormSection title={t("addTitle")}>
              <FormField label={t("documentType")} required>
                <select
                  className="w-full h-12 rounded-xl border border-input px-3 text-sm bg-background"
                  value={newDocTypeId}
                  onChange={(e) => setNewDocTypeId(e.target.value)}
                  data-testid="select-required-doc-type"
                >
                  <option value="">{t("selectDocument")}</option>
                  {available.map((d) => (
                    <option key={d.idDocumentType} value={String(d.idDocumentType)}>{d.name}</option>
                  ))}
                </select>
              </FormField>
              <FormField label={t("notes")}>
                <Input
                  value={newNotes}
                  onChange={(e) => setNewNotes(e.target.value)}
                  placeholder={t("notesPlaceholder")}
                  data-testid="input-required-doc-notes"
                />
              </FormField>
              <div className="flex justify-end">
                <Button onClick={handleAdd} disabled={createMutation.isPending} data-testid="btn-add-required-doc">
                  <Plus aria-hidden />
                  {t("add")}
                </Button>
              </div>
            </FormSection>
          </FormContainer>
        </DialogContent>
      </Dialog>
      <ConfirmDialog
        open={deleting !== null}
        onOpenChange={(open) => { if (!open) setDeleting(null); }}
        title={t("removeTitle")}
        description={t("removeDescription", { name: deleting?.documentType?.name ?? "" })}
        onConfirm={handleDelete}
        loading={deleteMutation.isPending}
      />
    </>
  );
}
