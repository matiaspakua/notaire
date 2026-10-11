"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Plus, Pencil, Trash2, CheckCircle, XCircle } from "lucide-react";
import { useTranslations } from "next-intl";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { FormContainer, FormSection, FormField, FormActions, CheckboxField } from "@/theme/form-patterns";
import { presentMutationError } from "@/lib/mutation-error";
import {
  useSubmittedDocuments,
  useCreateSubmittedDocument,
  useUpdateSubmittedDocument,
  useDeleteSubmittedDocument,
} from "@/hooks/useSubmittedDocuments";
import { useDocumentTypes } from "@/hooks/useDocuments";
import { ManagementPicker } from "@/components/shared/ManagementPicker";
import {
  EMPTY_DOCUMENT_FORM,
  missingDocumentFields,
  showsProcedureLink,
  toDocumentRequest,
  type DocumentForm,
} from "@/lib/submitted-document-form";
import { useDocumentReentry } from "@/hooks/useDocumentReentry";
import type { SubmittedDocument } from "@/types";
import { useDeleteError } from "@/hooks/useDeleteError";
import { toDateInputValue, todayInputValue } from "@/lib/dates";
import { useFormat } from "@/hooks/useFormat";

export default function DocumentsPage() {
  const fmt = useFormat();
  const t = useTranslations("documentos");
  const showDeleteError = useDeleteError();
  const tc = useTranslations("common");

  const { data: documents = [], isLoading } = useSubmittedDocuments();
  const { data: tiposDoc = [] } = useDocumentTypes();
  const createMutation = useCreateSubmittedDocument();
  const updateMutation = useUpdateSubmittedDocument();
  const deleteMutation = useDeleteSubmittedDocument();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<SubmittedDocument | null>(null);
  const [form, setForm] = useState<DocumentForm>(EMPTY_DOCUMENT_FORM);
  const { data: managementWithProcedures } = useDocumentReentry(form.managementId ? Number(form.managementId) : undefined);
  const procedures = managementWithProcedures?.procedures ?? [];

  function openCreate() {
    setEditing(null);
    setForm({ ...EMPTY_DOCUMENT_FORM, date: todayInputValue() });
    setModalOpen(true);
  }

  function openEdit(d: SubmittedDocument) {
    setEditing(d);
    setForm({
      typeId: d.type?.idDocumentType?.toString() ?? "",
      date: toDateInputValue(d.date),
      delivered: d.delivered ?? false,
      managementId: "",
      procedureId: d.procedureId?.toString() ?? "",
    });
    setModalOpen(true);
  }

  async function handleSave() {
    const data = toDocumentRequest(form);
    try {
      if (editing?.idSubmittedDocument) {
        await updateMutation.mutateAsync({ id: editing.idSubmittedDocument, data });
        toast.success(t("updated"));
      } else {
        await createMutation.mutateAsync(data);
        toast.success(t("registered"));
      }
      setModalOpen(false);
    } catch (err) {
      presentMutationError(err, { fallback: t("errorSave") });
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success(t("deleted"));
    } catch (err) {
      showDeleteError(err, t("errorDelete"));
    } finally {
      setDeleteId(null);
    }
  }

  const columns: Column<SubmittedDocument>[] = [
    {
      key: "id",
      header: tc("id"),
      render: (d) => <span className="text-muted-foreground text-xs">{d.idSubmittedDocument}</span>,
      className: "w-16",
    },
    {
      key: "type",
      header: tc("type"),
      render: (d) => <span className="font-medium">{d.type?.name ?? "—"}</span>,
    },
    {
      key: "procedure",
      header: t("tramite"),
      render: (d) => (d.procedureId ? `#${d.procedureId}` : "—"),
      className: "w-24",
    },
    {
      key: "date",
      header: tc("date"),
      render: (d) => fmt.date(d.date),
    },
    {
      key: "delivered",
      header: t("delivered"),
      render: (d) => (
        <span className="flex items-center gap-1.5">
          {d.delivered ? (
            <CheckCircle className="h-4 w-4 text-success" />
          ) : (
            <XCircle className="h-4 w-4 text-muted-foreground/40" />
          )}
          <span className="text-sm">{d.delivered ? tc("yes") : tc("no")}</span>
        </span>
      ),
      className: "w-28",
    },
    {
      key: "actions",
      header: "",
      render: (d) => (
        <div className="flex gap-1 justify-end">
          <Button size="icon" variant="ghost" onClick={() => openEdit(d)} aria-label={tc("edit")}>
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            size="icon"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(d.idSubmittedDocument!)}
            aria-label={tc("delete")}
          >
            <Trash2 className="h-4 w-4" />
          </Button>
        </div>
      ),
      className: "w-24",
    },
  ];

  return (
    <div>
      <AppHeader
        title={t("title")}
        actions={
          <Button onClick={openCreate} data-testid="btn-new-document">
            <Plus className="h-4 w-4" />
            {t("newDocumento")}
          </Button>
        }
      />

      <DataTable
        data={documents}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(d) => d.idSubmittedDocument!}
        emptyMessage={t("noData")}
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={editing ? t("editDocumento") : t("newDocumento")}>
              <FormField label={tc("type")} required={!editing}>
                <Select value={form.typeId} onValueChange={(v) => setForm({ ...form, typeId: v })}>
                  <SelectTrigger data-testid="select-document-type">
                    <SelectValue placeholder={t("selectTipo")} />
                  </SelectTrigger>
                  <SelectContent>
                    {tiposDoc.map((tipo) => (
                      <SelectItem key={tipo.idDocumentType} value={tipo.idDocumentType!.toString()}>
                        {tipo.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
              <FormField label={tc("date")}>
                <Input
                  type="date"
                  value={form.date}
                  onChange={(e) => setForm({ ...form, date: e.target.value })}
                />
              </FormField>
              {showsProcedureLink(editing) && (
                <>
                  <FormField label={t("gestion")} helperText={t("gestionHelper")} required={!editing}>
                    <ManagementPicker
                      value={form.managementId ? Number(form.managementId) : undefined}
                      onChange={(g) => setForm({ ...form, managementId: g?.idManagement?.toString() ?? "", procedureId: "" })}
                      aria-label={t("gestion")}
                      placeholder={t("selectGestion")}
                      data-testid="select-management-document"
                    />
                  </FormField>
                  {form.managementId && (
                    <FormField
                      label={t("tramite")}
                      helperText={procedures.length === 0 ? t("sinTramites") : undefined}
                      required={!editing}
                    >
                      <Select value={form.procedureId} onValueChange={(v) => setForm({ ...form, procedureId: v })}>
                        <SelectTrigger data-testid="select-procedure-document" disabled={procedures.length === 0}>
                          <SelectValue placeholder={t("selectTramite")} />
                        </SelectTrigger>
                        <SelectContent>
                          {procedures.map((tr) => (
                            <SelectItem key={tr.idProcedure} value={tr.idProcedure.toString()}>
                              #{tr.idProcedure} — {tr.typeProcedureName}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </FormField>
                  )}
                </>
              )}
              <CheckboxField
                label={t("delivered")}
                checked={form.delivered}
                onChange={(v) => setForm({ ...form, delivered: v })}
              />
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                {tc("cancel")}
              </Button>
              <Button
                onClick={handleSave}
                disabled={
                  createMutation.isPending ||
                  updateMutation.isPending ||
                  missingDocumentFields(form, !!editing).length > 0
                }
                data-testid="btn-save-document"
              >
                {editing ? tc("update") : tc("create")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <ConfirmDialog
        open={!!deleteId}
        onOpenChange={(v) => !v && setDeleteId(null)}
        onConfirm={handleDelete}
        loading={deleteMutation.isPending}
      />
    </div>
  );
}
