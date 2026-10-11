"use client";
import { useState } from "react";
import { toast } from "sonner";
import { useTranslations } from "next-intl";
import { NotaireIcon } from "@/components/ui/notaire-icon";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { WorkflowViewer } from "@/components/shared/WorkflowViewer";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import { useQuery } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import { useWorkflowDefinitions, useWorkflowNodes, useWorkflowTransitions } from "@/hooks/useWorkflow";
import { extractApiError } from "@/lib/utils";
import type { ManagementStatus } from "@/types";
import { useDeleteError } from "@/hooks/useDeleteError";

const EMPTY: Partial<ManagementStatus> = { name: "", notes: "" };

export default function StatusesManagementPage() {
  const t = useTranslations("administracion.estadosGestion");
  const showDeleteError = useDeleteError();
  const tc = useTranslations("common");

  const { data = [], isLoading, refetch } = useQuery({
    queryKey: ["managementStatuses"],
    queryFn: () => apiGet<ManagementStatus[]>("/management-statuses"),
  });

  const [search, setSearch] = useState("");
  const [selectedWorkflowId, setSelectedWorkflowId] = useState<number | undefined>(undefined);
  const { data: workflows = [] } = useWorkflowDefinitions();
  const { data: workflowNodes = [] } = useWorkflowNodes(selectedWorkflowId);
  const { data: workflowTransitions = [] } = useWorkflowTransitions(selectedWorkflowId);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Partial<ManagementStatus>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const { data: filtered = data } = useQuery({
    queryKey: ["managementStatuses", "search", search, data],
    queryFn: () =>
      search.trim()
        ? apiGet<ManagementStatus[]>(`/management-statuses/search?name=${encodeURIComponent(search.trim())}`)
        : Promise.resolve(data),
  });

  function openCreate() {
    setEditing(EMPTY);
    setIsEditMode(false);
    setModalOpen(true);
  }

  function openEdit(status: ManagementStatus) {
    setEditing(status);
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
      const body = { name: editing.name.trim(), notes: editing.notes?.trim() };
      if (isEditMode && editing.idManagementStatus) {
        await apiPut(`/management-statuses/${editing.idManagementStatus}`, { ...body, version: editing.version });
        toast.success(t("updated"));
      } else {
        await apiPost("/management-statuses", body);
        toast.success(t("created"));
      }
      setModalOpen(false);
      refetch();
    } catch (err) {
      const apiMsg = extractApiError(err);
      toast.error(apiMsg ?? t("errorSave"));
    } finally {
      setSaving(false);
    }
  }

  async function handleDeleteClick(status: ManagementStatus) {
    try {
      const { inUse } = await apiGet<{ inUse: boolean }>(`/management-statuses/${status.idManagementStatus}/in-use`);
      if (inUse) {
        toast.warning(t("inUseCannotDelete"));
        return;
      }
      setDeleteId(status.idManagementStatus!);
    } catch (err) {
      showDeleteError(err, t("errorDelete"));
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    setDeleting(true);
    try {
      await apiDelete(`/management-statuses/${deleteId}`);
      toast.success(t("deleted"));
      refetch();
    } catch (err) {
      showDeleteError(err, t("errorDelete"));
    } finally {
      setDeleting(false);
      setDeleteId(null);
    }
  }

  const columns: Column<ManagementStatus>[] = [
    { key: "id", header: tc("id"), render: (e) => <span className="text-xs text-muted-foreground">{e.idManagementStatus}</span>, className: "w-12" },
    { key: "name", header: t("fields.nombre"), render: (e) => <span className="font-medium">{e.name}</span> },
    { key: "desc", header: tc("description"), render: (e) => e.notes ?? "—" },
    {
      key: "actions",
      header: "",
      className: "w-24",
      render: (e) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(e)} data-testid="btn-edit-status" aria-label={tc("edit")}>
            <NotaireIcon src="/icons/actions/generate.png" alt={tc("edit")} size={16} />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => handleDeleteClick(e)}
            data-testid="btn-delete-status"
            aria-label={tc("delete")}
          >
            <NotaireIcon src="/icons/actions/delete.png" alt={tc("delete")} size={16} />
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
          <Button onClick={openCreate} data-testid="btn-new-status">
            <NotaireIcon src="/icons/actions/add.png" alt={tc("add")} size={16} className="mr-1" />
            {t("newEstado")}
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
          data-testid="search-statuses"
        />
      </div>
      <DataTable data={filtered} columns={columns} isLoading={isLoading} keyExtractor={(e) => e.idManagementStatus!} emptyMessage={t("noData")} />

      {workflows.length > 0 && (
        <div className="mt-8">
          <div className="flex items-center gap-3 mb-3">
            <h2 className="text-base font-semibold text-foreground">{t("workflowSection")}</h2>
            <select
              aria-label={t("workflowFilter")}
              className="border border-input rounded-lg px-3 py-1.5 text-sm bg-background focus:outline-none focus:ring-2 focus:ring-ring/40"
              value={selectedWorkflowId ?? ""}
              onChange={(e) => setSelectedWorkflowId(e.target.value ? Number(e.target.value) : undefined)}
              data-testid="select-workflow"
            >
              <option value="">{t("selectWorkflow")}</option>
              {workflows.map((wf) => (
                <option key={wf.id} value={wf.id}>{wf.name}</option>
              ))}
            </select>
          </div>
          <WorkflowViewer nodes={workflowNodes} transitions={workflowTransitions} data-testid="workflow-viewer" />
        </div>
      )}

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={isEditMode ? t("editEstado") : t("newEstado")}>
              <FormField label={t("fields.nombre")} required>
                <Input
                  value={editing.name ?? ""}
                  onChange={(e) => setEditing({ ...editing, name: e.target.value })}
                  placeholder={t("fields.namePlaceholder")}
                  data-testid="input-name-status"
                />
              </FormField>
              <FormField label={t("fields.descripcion")}>
                <Input
                  value={editing.notes ?? ""}
                  onChange={(e) => setEditing({ ...editing, notes: e.target.value })}
                  placeholder={t("fields.descripcionPlaceholder")}
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                <NotaireIcon src="/icons/actions/cancel.png" alt={tc("cancel")} size={16} className="mr-1" />
                {tc("cancel")}
              </Button>
              <Button onClick={handleSave} disabled={saving}>
                <NotaireIcon src="/icons/actions/save.png" alt={tc("save")} size={16} className="mr-1 brightness-0 invert" />
                {isEditMode ? tc("update") : tc("save")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <ConfirmDialog open={!!deleteId} onOpenChange={(v) => !v && setDeleteId(null)} onConfirm={handleDelete} loading={deleting} />
    </div>
  );
}
