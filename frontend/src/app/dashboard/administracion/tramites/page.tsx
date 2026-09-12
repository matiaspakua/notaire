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
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import { useTiposTramite, useCreateTipoTramite, useUpdateTipoTramite, useDeleteTipoTramite, useAssignWorkflowToTipoTramite } from "@/hooks/useTiposTramite";
import { useWorkflowDefinitions } from "@/hooks/useWorkflow";
import { extractApiError } from "@/lib/utils";
import type { TipoDeTramite } from "@/types";

const EMPTY: Partial<TipoDeTramite> = { name: "", notes: "" };

export default function TramitesPage() {
  const t = useTranslations("administracion.tramites");
  const tc = useTranslations("common");

  const { data = [], isLoading } = useTiposTramite();
  const createMutation = useCreateTipoTramite();
  const updateMutation = useUpdateTipoTramite();
  const deleteMutation = useDeleteTipoTramite();
  const assignWorkflowMutation = useAssignWorkflowToTipoTramite();
  const { data: workflows = [] } = useWorkflowDefinitions();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<TipoDeTramite>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);
  const [search, setSearch] = useState("");
  const [selectedWorkflowId, setSelectedWorkflowId] = useState<string>("");

  const { data: filtered = data } = useQuery({
    queryKey: ["tiposTramite", "search", search, data],
    queryFn: () =>
      search.trim()
        ? apiGet<TipoDeTramite[]>(`/tipo-tramite/search?nombre=${encodeURIComponent(search.trim())}`)
        : Promise.resolve(data),
  });

  function openCreate() { setEditing(EMPTY); setIsEditMode(false); setSelectedWorkflowId(""); setModalOpen(true); }
  function openEdit(item: TipoDeTramite) { setEditing(item); setIsEditMode(true); setSelectedWorkflowId(item.workflowDefinitionId ? String(item.workflowDefinitionId) : ""); setModalOpen(true); }

  async function handleSave() {
    if (!editing.name?.trim()) { toast.error(t("nameRequired")); return; }
    try {
      let savedId = editing.idProcedureType;
      if (isEditMode && editing.idProcedureType) {
        await updateMutation.mutateAsync({ id: editing.idProcedureType, data: editing });
        toast.success(t("updated"));
      } else {
        const created = await createMutation.mutateAsync(editing) as TipoDeTramite | undefined;
        savedId = created?.idProcedureType ?? editing.idProcedureType;
        toast.success(t("created"));
      }
      if (savedId) {
        const wfId = selectedWorkflowId ? Number(selectedWorkflowId) : null;
        const currentWfId = editing.workflowDefinitionId ?? null;
        if (wfId !== currentWfId) {
          await assignWorkflowMutation.mutateAsync({ id: savedId, workflowDefinitionId: wfId });
        }
      }
      setModalOpen(false);
    } catch (err) {
      const apiError = extractApiError(err);
      toast.error(apiError ?? t("errorSave"));
    }
  }

  async function handleDeleteClick(item: TipoDeTramite) {
    try {
      const { inUse } = await apiGet<{ inUse: boolean }>(`/tipo-tramite/${item.idProcedureType}/in-use`);
      if (inUse) {
        toast.error(t("inUseCannotDelete"));
        return;
      }
      setDeleteId(item.idProcedureType!);
    } catch {
      toast.error(t("errorDelete"));
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success(t("deleted"));
    } catch (err) {
      const apiError = extractApiError(err);
      toast.error(apiError ?? t("errorDelete"));
    } finally {
      setDeleteId(null);
    }
  }

  const columns: Column<TipoDeTramite>[] = [
    { key: "id", header: tc("id"), render: (item) => <span className="text-xs text-muted-foreground">{item.idProcedureType}</span>, className: "w-12" },
    { key: "nombre", header: t("fields.nombre"), render: (item) => <span className="font-medium">{item.name}</span> },
    { key: "desc", header: t("fields.descripcion"), render: (item) => item.notes ?? "—" },
    { key: "workflow", header: "Workflow", render: (item) => item.workflowDefinitionName ? <span className="text-xs text-muted-foreground">{item.workflowDefinitionName}</span> : <span className="text-xs text-muted-foreground">—</span> },
    {
      key: "actions", header: "", className: "w-24",
      render: (item) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(item)}>
            <NotaireIcon src="/icons/actions/generar.png" alt={tc("edit")} size={16} />
          </Button>
          <Button size="sm" variant="ghost" className="text-destructive hover:text-destructive" onClick={() => handleDeleteClick(item)} data-testid="btn-delete-tramite">
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
          <Button onClick={openCreate} data-testid="btn-nuevo-tipo-tramite">
            <NotaireIcon src="/icons/actions/agregar.png" alt={tc("add")} size={16} className="mr-1" />
            {t("newTramite")}
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
          data-testid="input-search-tramite"
        />
      </div>
      <DataTable
        data={filtered}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(item) => item.idProcedureType!}
        emptyMessage={t("noData")}
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={isEditMode ? t("editTramite") : t("newTramite")}>
              <FormField label={t("fields.nombre")} required>
                <Input
                  value={editing.name ?? ""}
                  onChange={(e) => setEditing({ ...editing, name: e.target.value })}
                  placeholder={t("fields.namePlaceholder")}
                  data-testid="input-nombre-tramite"
                />
              </FormField>
              <FormField label={t("fields.descripcion")}>
                <Input
                  value={editing.notes ?? ""}
                  onChange={(e) => setEditing({ ...editing, notes: e.target.value })}
                  placeholder={t("fields.descripcionPlaceholder")}
                />
              </FormField>
              <CheckboxField
                label="Se archiva"
                checked={editing.isArchived ?? false}
                onChange={(v) => setEditing({ ...editing, isArchived: v })}
              />
              <CheckboxField
                label="Se inscribe"
                checked={editing.isRegistered ?? false}
                onChange={(v) => setEditing({ ...editing, isRegistered: v })}
              />
              <FormField label="Workflow">
                <select
                  className="w-full h-12 rounded-xl border border-neutral-300 px-3 text-sm bg-white"
                  value={selectedWorkflowId}
                  onChange={(e) => setSelectedWorkflowId(e.target.value)}
                  data-testid="select-workflow-tramite"
                >
                  <option value="">— Sin workflow —</option>
                  {workflows.filter((w) => w.active).map((w) => (
                    <option key={w.id} value={String(w.id)}>{w.name}</option>
                  ))}
                </select>
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
