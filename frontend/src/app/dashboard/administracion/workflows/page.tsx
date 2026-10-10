"use client";
import { useState } from "react";
import Link from "next/link";
import { toast } from "sonner";
import { useTranslations } from "next-intl";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import {
  useWorkflowDefinitions,
  useCreateWorkflowDefinition,
  useUpdateWorkflowDefinition,
  useDeleteWorkflowDefinition,
} from "@/hooks/useWorkflow";
import { presentMutationError } from "@/lib/mutation-error";
import type { WorkflowDefinition } from "@/types";

const EMPTY: Partial<WorkflowDefinition> = { name: "", description: "", active: false };

export default function WorkflowsPage() {
  const t = useTranslations("administracion.workflows");
  const tc = useTranslations("common");
  const { data = [], isLoading } = useWorkflowDefinitions();
  const createWf = useCreateWorkflowDefinition();
  const updateWf = useUpdateWorkflowDefinition();
  const deleteWf = useDeleteWorkflowDefinition();

  const [search, setSearch] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Partial<WorkflowDefinition>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [saving, setSaving] = useState(false);

  const filtered = search.trim()
    ? data.filter((wf) => wf.name?.toLowerCase().includes(search.toLowerCase()))
    : data;

  function openCreate() {
    setEditing(EMPTY);
    setIsEditMode(false);
    setModalOpen(true);
  }

  function openEdit(wf: WorkflowDefinition) {
    setEditing(wf);
    setIsEditMode(true);
    setModalOpen(true);
  }

  async function handleSave() {
    if (!editing.name?.trim()) {
      toast.error(t("nameRequired"));
      return;
    }
    setSaving(true);
    try {
      if (isEditMode && editing.id) {
        await updateWf.mutateAsync({ id: editing.id, data: editing });
        toast.success(t("updated"));
      } else {
        await createWf.mutateAsync(editing);
        toast.success(t("created"));
      }
      setModalOpen(false);
    } catch (err) {
      presentMutationError(err, { fallback: t("errorSave") });
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteWf.mutateAsync(deleteId);
      toast.success(t("deleted"));
    } catch (err) {
      presentMutationError(err, {
        fallback: t("errorDeleteHasNodes"),
      });
    } finally {
      setDeleteId(null);
    }
  }

  const columns: Column<WorkflowDefinition>[] = [
    { key: "id", header: tc("id"), render: (wf) => <span className="text-xs text-muted-foreground">{wf.id}</span>, className: "w-12" },
    { key: "nombre", header: tc("name"), render: (wf) => <span className="font-medium">{wf.name}</span> },
    { key: "desc", header: tc("description"), render: (wf) => wf.description ?? "—" },
    {
      key: "activo",
      header: tc("status"),
      render: (wf) => (
        <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${wf.active ? "bg-green-100 text-green-700" : "bg-neutral-100 text-neutral-500"}`}>
          {wf.active ? tc("active") : tc("inactive")}
        </span>
      ),
      className: "w-24",
    },
    {
      key: "actions",
      header: "",
      className: "w-32",
      render: (wf) => (
        <div className="flex gap-1 justify-end">
          <Button size="sm" variant="ghost" asChild data-testid={`btn-editor-${wf.id}`}>
            <Link href={`/dashboard/administracion/workflows/${wf.id}`}>{t("editGraph")}</Link>
          </Button>
          <Button size="sm" variant="ghost" onClick={() => openEdit(wf)} data-testid={`btn-edit-wf-${wf.id}`}>
            {t("editData")}
          </Button>
          <Button
            size="sm"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(wf.id!)}
            data-testid={`btn-delete-wf-${wf.id}`}
          >
            {t("deleteAction")}
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
          <Button onClick={openCreate} data-testid="btn-nuevo-workflow">
            + {t("newWorkflow")}
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
          data-testid="search-workflows"
        />
      </div>
      <DataTable data={filtered} columns={columns} isLoading={isLoading} keyExtractor={(wf) => wf.id!} emptyMessage={t("noData")} />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={isEditMode ? t("editWorkflow") : t("newWorkflow")}>
              <FormField label={tc("name")} required>
                <Input
                  value={editing.name ?? ""}
                  onChange={(e) => setEditing({ ...editing, name: e.target.value })}
                  placeholder={t("fields.namePlaceholder")}
                  data-testid="input-nombre-workflow"
                />
              </FormField>
              <FormField label={tc("description")}>
                <Input
                  value={editing.description ?? ""}
                  onChange={(e) => setEditing({ ...editing, description: e.target.value })}
                  placeholder={t("fields.descriptionPlaceholder")}
                />
              </FormField>
              <FormField label={tc("active")}>
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={editing.active ?? false}
                    onChange={(e) => setEditing({ ...editing, active: e.target.checked })}
                    data-testid="checkbox-activo-workflow"
                  />
                  <span className="text-sm">{t("enabledForAssignment")}</span>
                </label>
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>{tc("cancel")}</Button>
              <Button onClick={handleSave} disabled={saving} data-testid="btn-guardar-workflow">
                {isEditMode ? tc("update") : tc("create")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <ConfirmDialog open={!!deleteId} onOpenChange={(v) => !v && setDeleteId(null)} onConfirm={handleDelete} />
    </div>
  );
}
