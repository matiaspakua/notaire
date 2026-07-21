"use client";
import { useState } from "react";
import Link from "next/link";
import { toast } from "sonner";
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
import type { WorkflowDefinition } from "@/types";

const EMPTY: Partial<WorkflowDefinition> = { nombre: "", descripcion: "", activo: false };

export default function WorkflowsPage() {
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
    ? data.filter((wf) => wf.nombre?.toLowerCase().includes(search.toLowerCase()))
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
    if (!editing.nombre?.trim()) {
      toast.error("El nombre es requerido");
      return;
    }
    setSaving(true);
    try {
      if (isEditMode && editing.id) {
        await updateWf.mutateAsync({ id: editing.id, data: editing });
        toast.success("Workflow actualizado");
      } else {
        await createWf.mutateAsync(editing);
        toast.success("Workflow creado");
      }
      setModalOpen(false);
    } catch {
      toast.error("Error al guardar");
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteWf.mutateAsync(deleteId);
      toast.success("Workflow eliminado");
    } catch {
      toast.error("No se puede eliminar: el workflow tiene nodos");
    } finally {
      setDeleteId(null);
    }
  }

  const columns: Column<WorkflowDefinition>[] = [
    { key: "id", header: "ID", render: (wf) => <span className="text-xs text-muted-foreground">{wf.id}</span>, className: "w-12" },
    { key: "nombre", header: "Nombre", render: (wf) => <span className="font-medium">{wf.nombre}</span> },
    { key: "desc", header: "Descripción", render: (wf) => wf.descripcion ?? "—" },
    {
      key: "activo",
      header: "Estado",
      render: (wf) => (
        <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${wf.activo ? "bg-green-100 text-green-700" : "bg-neutral-100 text-neutral-500"}`}>
          {wf.activo ? "Activo" : "Inactivo"}
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
            <Link href={`/dashboard/administracion/workflows/${wf.id}`}>Editar grafo</Link>
          </Button>
          <Button size="sm" variant="ghost" onClick={() => openEdit(wf)} data-testid={`btn-edit-wf-${wf.id}`}>
            Datos
          </Button>
          <Button
            size="sm"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(wf.id!)}
            data-testid={`btn-delete-wf-${wf.id}`}
          >
            Borrar
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <AppHeader
        title="Workflows de Estados"
        description="Gestión de workflows para el ciclo de vida de las gestiones notariales"
        actions={
          <Button onClick={openCreate} data-testid="btn-nuevo-workflow">
            + Nuevo Workflow
          </Button>
        }
      />
      <div className="mb-4">
        <Input
          placeholder="Buscar por nombre..."
          aria-label="Buscar por nombre..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="max-w-sm"
          data-testid="search-workflows"
        />
      </div>
      <DataTable data={filtered} columns={columns} isLoading={isLoading} keyExtractor={(wf) => wf.id!} emptyMessage="No hay workflows" />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={isEditMode ? "Editar Workflow" : "Nuevo Workflow"}>
              <FormField label="Nombre" required>
                <Input
                  value={editing.nombre ?? ""}
                  onChange={(e) => setEditing({ ...editing, nombre: e.target.value })}
                  placeholder="Ej: Workflow Compraventa"
                  data-testid="input-nombre-workflow"
                />
              </FormField>
              <FormField label="Descripción">
                <Input
                  value={editing.descripcion ?? ""}
                  onChange={(e) => setEditing({ ...editing, descripcion: e.target.value })}
                  placeholder="Descripción opcional"
                />
              </FormField>
              <FormField label="Activo">
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={editing.activo ?? false}
                    onChange={(e) => setEditing({ ...editing, activo: e.target.checked })}
                    data-testid="checkbox-activo-workflow"
                  />
                  <span className="text-sm">Habilitado para asignación</span>
                </label>
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>Cancelar</Button>
              <Button onClick={handleSave} disabled={saving} data-testid="btn-guardar-workflow">
                {isEditMode ? "Actualizar" : "Crear"}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <ConfirmDialog open={!!deleteId} onOpenChange={(v) => !v && setDeleteId(null)} onConfirm={handleDelete} />
    </div>
  );
}
