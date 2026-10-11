"use client";
import { useState } from "react";
import { toast } from "sonner";
import { useTranslations } from "next-intl";
import { NotaireIcon } from "@/components/ui/notaire-icon";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { FormContainer, FormSection, FormField, FormActions, CheckboxField } from "@/theme/form-patterns";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import { extractApiError } from "@/lib/utils";
import { EMPTY_DOCUMENT_TYPE } from "@/lib/document-type-form";
import type { DocumentType } from "@/types";
import { useDeleteError } from "@/hooks/useDeleteError";
import { documentsKeys } from "@/hooks/useDocuments";

export default function DocumentsPage() {
  const t = useTranslations("administracion.documentos");
  const showDeleteError = useDeleteError();
  const tc = useTranslations("common");

  const qc = useQueryClient();
  const { data: documentTypes = [], isLoading } = useQuery({
    queryKey: documentsKeys.all,
    queryFn: () => apiGet<DocumentType[]>("/document-types"),
  });

  const createMutation = useMutation({
    mutationFn: (data: Partial<DocumentType>) => apiPost<void>("/document-types", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: documentsKeys.all }),
  });
  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<DocumentType> }) =>
      apiPut<void>(`/document-types/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: documentsKeys.all }),
  });
  const deleteMutation = useMutation({
    mutationFn: (id: number) => apiDelete(`/document-types/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: documentsKeys.all }),
  });

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<DocumentType>>(EMPTY_DOCUMENT_TYPE);
  const [isEditMode, setIsEditMode] = useState(false);
  const [search, setSearch] = useState("");

  const { data: filtered = documentTypes } = useQuery({
    queryKey: [...documentsKeys.all, "search", search, documentTypes],
    queryFn: () =>
      search.trim()
        ? apiGet<DocumentType[]>(`/document-types/search?name=${encodeURIComponent(search.trim())}`)
        : Promise.resolve(documentTypes),
  });

  function openCreate() {
    setEditing({ ...EMPTY_DOCUMENT_TYPE });
    setIsEditMode(false);
    setModalOpen(true);
  }
  function openEdit(documentType: DocumentType) {
    setEditing({
      ...documentType,
      enabled: documentType.enabled ?? true,
      returned: documentType.returned ?? false,
    });
    setIsEditMode(true);
    setModalOpen(true);
  }

  async function handleSave() {
    if (!editing.name?.trim()) { toast.error(t("nameRequired")); return; }
    if (editing.expires && !editing.dueDays) { toast.error(t("diasVencimientoRequired")); return; }
    try {
      if (isEditMode && editing.idDocumentType) {
        await updateMutation.mutateAsync({ id: editing.idDocumentType, data: editing });
        toast.success(t("updated"));
      } else {
        await createMutation.mutateAsync(editing);
        toast.success(t("created"));
      }
      setModalOpen(false);
    } catch (err) {
      const apiError = extractApiError(err);
      toast.error(apiError ?? t("errorSave"));
    }
  }

  async function handleDeleteClick(documentType: DocumentType) {
    try {
      const { inUse } = await apiGet<{ inUse: boolean }>(`/document-types/${documentType.idDocumentType}/in-use`);
      if (inUse) {
        toast.warning(t("inUseCannotDelete"));
        return;
      }
      setDeleteId(documentType.idDocumentType!);
    } catch (err) {
      showDeleteError(err, t("errorDelete"));
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

  const columns: Column<DocumentType>[] = [
    { key: "id", header: tc("id"), render: (documentType) => <span className="text-xs text-muted-foreground">{documentType.idDocumentType}</span>, className: "w-12" },
    { key: "name", header: tc("name"), render: (documentType) => <span className="font-medium">{documentType.name}</span> },
    {
      key: "actions", header: "", className: "w-24",
      render: (documentType) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(documentType)} aria-label={tc("edit")}>
            <NotaireIcon src="/icons/actions/generate.png" alt="" size={16} />
          </Button>
          <Button size="sm" variant="ghost" className="text-destructive hover:text-destructive" onClick={() => handleDeleteClick(documentType)} data-testid="btn-delete-document" aria-label={tc("delete")}>
            <NotaireIcon src="/icons/actions/borrar.png" alt="" size={16} />
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <AppHeader
        title={t("title")}
        description={t("description")}
        actions={
          <Button onClick={openCreate} data-testid="btn-new-document-type">
            <NotaireIcon src="/icons/actions/agregar.png" alt={tc("add")} size={16} className="mr-1" />
            {t("newDoc")}
          </Button>
        }
      />
      <div className="mb-4">
        <Input
          placeholder={t("searchPlaceholder")}
          aria-label={t("searchPlaceholder")}
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="max-w-sm"
          data-testid="input-search-document"
        />
      </div>
      <DataTable
        data={filtered}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(documentType) => documentType.idDocumentType!}
        emptyMessage={t("noData")}
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={isEditMode ? t("editDoc") : t("newDoc")}>
              <FormField label={tc("name")} required>
                <Input
                  value={editing.name ?? ""}
                  onChange={(e) => setEditing({ ...editing, name: e.target.value })}
                  placeholder={t("namePlaceholder")}
                  data-testid="input-name-document"
                />
              </FormField>
              <CheckboxField
                label={t("fields.enabled")}
                checked={editing.enabled ?? true}
                onChange={(checked) => setEditing({ ...editing, enabled: checked })}
                data-testid="checkbox-enabled-document"
              />
              <CheckboxField
                label={t("fields.returned")}
                checked={editing.returned ?? false}
                onChange={(checked) => setEditing({ ...editing, returned: checked })}
                data-testid="checkbox-returned-document"
              />
              <CheckboxField
                label={t("fields.vence")}
                checked={editing.expires ?? false}
                onChange={(checked) =>
                  setEditing({ ...editing, expires: checked, dueDays: checked ? editing.dueDays : null })
                }
                data-testid="checkbox-expires-document"
              />
              {editing.expires && (
                <FormField label={t("fields.diasVencimiento")} required>
                  <Input
                    type="number"
                    min={1}
                    value={editing.dueDays ?? ""}
                    onChange={(e) =>
                      setEditing({ ...editing, dueDays: e.target.value ? parseInt(e.target.value) : null })
                    }
                    data-testid="input-due-days-document"
                  />
                </FormField>
              )}
              <FormField label={t("fields.quienEntrega")}>
                <Input
                  value={editing.deliveredBy ?? ""}
                  onChange={(e) => setEditing({ ...editing, deliveredBy: e.target.value })}
                  data-testid="input-delivered-by-document"
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                <NotaireIcon src="/icons/actions/cerrar.png" alt={tc("cancel")} size={16} className="mr-1" />
                {tc("cancel")}
              </Button>
              <Button onClick={handleSave} disabled={createMutation.isPending || updateMutation.isPending}>
                <NotaireIcon src="/icons/actions/guardar.png" alt={tc("save")} size={16} className="mr-1 brightness-0 invert" />
                {isEditMode ? tc("update") : tc("save")}
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
