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
import type { EstadoDeGestion } from "@/types";

const EMPTY: Partial<EstadoDeGestion> = { nombre: "", observaciones: "" };

export default function EstadosGestionPage() {
  const t = useTranslations("administracion.estadosGestion");
  const tc = useTranslations("common");

  const { data = [], isLoading, refetch } = useQuery({
    queryKey: ["estados-gestion"],
    queryFn: () => apiGet<EstadoDeGestion[]>("/estado-gestion"),
  });

  const [search, setSearch] = useState("");
  const [selectedWorkflowId, setSelectedWorkflowId] = useState<number | undefined>(undefined);
  const { data: workflows = [] } = useWorkflowDefinitions();
  const { data: workflowNodes = [] } = useWorkflowNodes(selectedWorkflowId);
  const { data: workflowTransitions = [] } = useWorkflowTransitions(selectedWorkflowId);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Partial<EstadoDeGestion>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const { data: filtered = data } = useQuery({
    queryKey: ["estados-gestion", "search", search, data],
    queryFn: () =>
      search.trim()
        ? apiGet<EstadoDeGestion[]>(`/estado-gestion/search?nombre=${encodeURIComponent(search.trim())}`)
        : Promise.resolve(data),
  });

  function openCreate() {
    setEditing(EMPTY);
    setIsEditMode(false);
    setModalOpen(true);
  }

  function openEdit(estado: EstadoDeGestion) {
    setEditing(estado);
    setIsEditMode(true);
    setModalOpen(true);
  }

  async function handleSave() {
    if (!editing.nombre?.trim()) {
      toast.error(t("nameRequired"));
      return;
    }
    setSaving(true);
    try {
      const body = { nombre: editing.nombre.trim(), observaciones: editing.observaciones?.trim() };
      if (isEditMode && editing.idEstadoGestion) {
        await apiPut(`/estado-gestion/${editing.idEstadoGestion}`, { ...body, version: editing.version });
        toast.success(t("updated"));
      } else {
        await apiPost("/estado-gestion", body);
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

  async function handleDeleteClick(estado: EstadoDeGestion) {
    try {
      const { inUse } = await apiGet<{ inUse: boolean }>(`/estado-gestion/${estado.idEstadoGestion}/in-use`);
      if (inUse) {
        toast.error(t("inUseCannotDelete"));
        return;
      }
      setDeleteId(estado.idEstadoGestion!);
    } catch {
      toast.error(t("errorDelete"));
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    setDeleting(true);
    try {
      await apiDelete(`/estado-gestion/${deleteId}`);
      toast.success(t("deleted"));
      refetch();
    } catch (err) {
      const apiMsg = extractApiError(err);
      toast.error(apiMsg ?? t("errorDelete"));
    } finally {
      setDeleting(false);
      setDeleteId(null);
    }
  }

  const columns: Column<EstadoDeGestion>[] = [
    { key: "id", header: tc("id"), render: (e) => <span className="text-xs text-muted-foreground">{e.idEstadoGestion}</span>, className: "w-12" },
    { key: "nombre", header: t("fields.nombre"), render: (e) => <span className="font-medium">{e.nombre}</span> },
    { key: "desc", header: tc("description"), render: (e) => e.observaciones ?? "—" },
    {
      key: "actions",
      header: "",
      className: "w-24",
      render: (e) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(e)} data-testid="btn-edit-estado" aria-label={tc("edit")}>
            <NotaireIcon src="/icons/actions/generar.png" alt={tc("edit")} size={16} />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => handleDeleteClick(e)}
            data-testid="btn-delete-estado"
            aria-label={tc("delete")}
          >
            <NotaireIcon src="/icons/actions/borrar.png" alt={tc("delete")} size={16} />
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
          <Button onClick={openCreate} data-testid="btn-nuevo-estado">
            <NotaireIcon src="/icons/actions/agregar.png" alt={tc("add")} size={16} className="mr-1" />
            {t("newEstado")}
          </Button>
        }
      />
      <div className="mb-4">
        <Input
          placeholder={t("searchPlaceholder")}
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="max-w-sm"
          data-testid="search-estados"
        />
      </div>
      <DataTable data={filtered} columns={columns} isLoading={isLoading} keyExtractor={(e) => e.idEstadoGestion!} emptyMessage={t("noData")} />

      {workflows.length > 0 && (
        <div className="mt-8">
          <div className="flex items-center gap-3 mb-3">
            <h2 className="text-base font-semibold text-neutral-800">{t("workflowSection")}</h2>
            <select
              className="border border-neutral-300 rounded-lg px-3 py-1.5 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-primary-300"
              value={selectedWorkflowId ?? ""}
              onChange={(e) => setSelectedWorkflowId(e.target.value ? Number(e.target.value) : undefined)}
              data-testid="select-workflow"
            >
              <option value="">{t("selectWorkflow")}</option>
              {workflows.map((wf) => (
                <option key={wf.id} value={wf.id}>{wf.nombre}</option>
              ))}
            </select>
          </div>
          <WorkflowViewer nodes={workflowNodes} transitions={workflowTransitions} data-testid="workflow-viewer" />
        </div>
      )}

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={isEditMode ? t("editEstado") : t("newEstado")}>
              <FormField label={t("fields.nombre")} required>
                <Input
                  value={editing.nombre ?? ""}
                  onChange={(e) => setEditing({ ...editing, nombre: e.target.value })}
                  placeholder={t("fields.namePlaceholder")}
                  data-testid="input-nombre-estado"
                />
              </FormField>
              <FormField label={t("fields.descripcion")}>
                <Input
                  value={editing.observaciones ?? ""}
                  onChange={(e) => setEditing({ ...editing, observaciones: e.target.value })}
                  placeholder={t("fields.descripcionPlaceholder")}
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                <NotaireIcon src="/icons/actions/cerrar.png" alt={tc("cancel")} size={16} className="mr-1" />
                {tc("cancel")}
              </Button>
              <Button onClick={handleSave} disabled={saving}>
                <NotaireIcon src="/icons/actions/guardar.png" alt={tc("save")} size={16} className="mr-1 brightness-0 invert" />
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
