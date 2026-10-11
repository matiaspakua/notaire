"use client";

import { useState } from "react";
import { toast } from "sonner";
import { useTranslations } from "next-intl";
import { NotaireIcon } from "@/components/ui/notaire-icon";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import { useConcepts, useCreateConcept, useUpdateConcept, useDeleteConcept } from "@/hooks/useConcepts";
import { formatCurrency, extractApiError } from "@/lib/utils";
import type { Concept } from "@/types";
import { useDeleteError } from "@/hooks/useDeleteError";

const EMPTY: Partial<Concept> = { name: "", value: undefined };

export default function ConceptsPage() {
  const t = useTranslations("administracion.conceptos");
  const showDeleteError = useDeleteError();
  const tc = useTranslations("common");

  const { data: concepts = [], isLoading } = useConcepts();
  const createMutation = useCreateConcept();
  const updateMutation = useUpdateConcept();
  const deleteMutation = useDeleteConcept();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<Concept>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);
  const [search, setSearch] = useState("");

  const { data: filtered = concepts } = useQuery({
    queryKey: ["concepts", "search", search, concepts],
    queryFn: () =>
      search
        ? apiGet<Concept[]>(`/concepts/search?name=${encodeURIComponent(search)}`)
        : Promise.resolve(concepts),
  });

  function openCreate() { setEditing(EMPTY); setIsEditMode(false); setModalOpen(true); }
  function openEdit(c: Concept) { setEditing(c); setIsEditMode(true); setModalOpen(true); }

  async function handleSave() {
    if (!editing.name?.trim()) { toast.error(t("nameRequired")); return; }
    try {
      if (isEditMode && editing.idConcept) {
        await updateMutation.mutateAsync({ id: editing.idConcept, data: editing });
        toast.success(t("updated"));
      } else {
        await createMutation.mutateAsync(editing);
        toast.success(t("created"));
      }
      setModalOpen(false);
    } catch (err) {
      const conflict = extractApiError(err);
      if (conflict) {
        toast.error(conflict);
      } else {
        toast.error(t("errorSave"));
      }
    }
  }

  async function handleDeleteClick(c: Concept) {
    try {
      const { inUse } = await apiGet<{ inUse: boolean }>(`/concepts/${c.idConcept}/in-use`);
      if (inUse) {
        toast.warning(t("inUseCannotDelete"));
        return;
      }
      setDeleteId(c.idConcept!);
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
    } finally { setDeleteId(null); }
  }

  const columns: Column<Concept>[] = [
    { key: "id", header: tc("id"), render: (c) => <span className="text-xs text-muted-foreground">{c.idConcept}</span>, className: "w-12" },
    { key: "name", header: t("fields.nombre"), render: (c) => <span className="font-medium">{c.name}</span> },
    { key: "value", header: t("fields.valorBase"), render: (c) => formatCurrency(c.value) },
    {
      key: "actions", header: "", className: "w-24",
      render: (c) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(c)} aria-label={tc("edit")}><NotaireIcon src="/icons/actions/generate.png" alt="" size={16} /></Button>
          <Button size="sm" variant="ghost" className="text-destructive hover:text-destructive" onClick={() => handleDeleteClick(c)} data-testid="btn-delete-concept" aria-label={tc("delete")}><NotaireIcon src="/icons/actions/delete.png" alt="" size={16} /></Button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <AppHeader
        title={t("title")}
        description={t("description")}
        actions={<Button onClick={openCreate}><NotaireIcon src="/icons/actions/add.png" alt={tc("add")} size={16} className="mr-1" />{t("newConcepto")}</Button>}
      />

      <div className="px-4 pb-4">
        <Input
          placeholder={t("searchPlaceholder")}
          aria-label={t("searchPlaceholder")}
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="w-64"
          data-testid="input-search-concept"
        />
      </div>

      <DataTable data={filtered} columns={columns} isLoading={isLoading} keyExtractor={(c) => c.idConcept!} emptyMessage={t("noData")} />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={isEditMode ? t("editConcepto") : t("newConcepto")}>
              <FormField label={t("fields.nombre")} required>
                <Input
                  value={editing.name ?? ""}
                  onChange={(e) => setEditing({ ...editing, name: e.target.value })}
                  data-testid="input-name-concept"
                />
              </FormField>
              <FormField label={t("fields.valorBaseInput")}>
                <Input
                  type="number"
                  step="0.01"
                  value={editing.value ?? ""}
                  onChange={(e) => setEditing({ ...editing, value: parseFloat(e.target.value) })}
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                <NotaireIcon src="/icons/actions/cancel.png" alt={tc("cancel")} size={16} className="mr-1" />
                {tc("cancel")}
              </Button>
              <Button onClick={handleSave} disabled={createMutation.isPending || updateMutation.isPending}>
                <NotaireIcon src="/icons/actions/save.png" alt={tc("save")} size={16} className="mr-1 brightness-0 invert" />
                {isEditMode ? tc("update") : tc("save")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <ConfirmDialog open={!!deleteId} onOpenChange={(v) => !v && setDeleteId(null)} onConfirm={handleDelete} loading={deleteMutation.isPending} />
    </div>
  );
}
