"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Plus, Pencil, Trash2 } from "lucide-react";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import { useRoles, useCreateRol, useUpdateRol, useDeleteRol } from "@/hooks/useRoles";
import type { Rol } from "@/types";

const MODULOS_DISPONIBLES = [
  { value: "administracion", label: "Administración" },
  { value: "usuarios", label: "Usuarios" },
  { value: "tramites", label: "Trámites" },
  { value: "presupuestos", label: "Presupuestos" },
  { value: "escrituras", label: "Escrituras" },
  { value: "personas", label: "Personas" },
  { value: "auditoria", label: "Auditoría" },
  { value: "workflows", label: "Workflows" },
];

const EMPTY: Partial<Rol> = { nombre: "", descripcion: "", activo: true, modulos: [] };

export default function RolesPage() {
  const { data: roles = [], isLoading } = useRoles();
  const createMutation = useCreateRol();
  const updateMutation = useUpdateRol();
  const deleteMutation = useDeleteRol();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<Rol>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);

  function openCreate() { setEditing(EMPTY); setIsEditMode(false); setModalOpen(true); }
  function openEdit(r: Rol) { setEditing({ ...r }); setIsEditMode(true); setModalOpen(true); }

  function toggleModulo(modulo: string) {
    const current = editing.modulos ?? [];
    const updated = current.includes(modulo)
      ? current.filter((m) => m !== modulo)
      : [...current, modulo];
    setEditing({ ...editing, modulos: updated });
  }

  async function handleSave() {
    if (!editing.nombre?.trim()) { toast.error("El nombre es obligatorio"); return; }
    try {
      if (isEditMode && editing.idRol) {
        await updateMutation.mutateAsync({ id: editing.idRol, data: editing });
        toast.success("Rol actualizado");
      } else {
        await createMutation.mutateAsync(editing);
        toast.success("Rol creado");
      }
      setModalOpen(false);
    } catch { toast.error("Error al guardar el rol"); }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success("Rol eliminado");
    } catch { toast.error("Error al eliminar"); }
    finally { setDeleteId(null); }
  }

  const columns: Column<Rol>[] = [
    { key: "id", header: "ID", render: (r) => <span className="text-xs text-muted-foreground">{r.idRol}</span>, className: "w-12" },
    { key: "nombre", header: "Nombre", render: (r) => <span className="font-medium">{r.nombre}</span> },
    { key: "descripcion", header: "Descripción", render: (r) => <span className="text-sm text-muted-foreground">{r.descripcion ?? "—"}</span> },
    { key: "modulos", header: "Módulos", render: (r) => (
      <div className="flex flex-wrap gap-1">
        {(r.modulos ?? []).map((m) => <Badge key={m} variant="outline" className="text-xs">{m}</Badge>)}
        {(r.modulos ?? []).length === 0 && <span className="text-xs text-muted-foreground">Sin permisos</span>}
      </div>
    )},
    { key: "activo", header: "Estado", render: (r) => r.activo ? <Badge variant="success">Activo</Badge> : <Badge variant="secondary">Inactivo</Badge> },
    {
      key: "actions", header: "", className: "w-24",
      render: (r) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(r)} data-testid={`btn-edit-rol-${r.idRol}`}><Pencil className="h-4 w-4" /></Button>
          <Button size="sm" variant="ghost" className="text-destructive hover:text-destructive" onClick={() => setDeleteId(r.idRol!)}><Trash2 className="h-4 w-4" /></Button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <AppHeader
        title="Roles y Permisos"
        description="Gestión de roles de usuario y sus accesos a módulos"
        actions={<Button onClick={openCreate} data-testid="btn-nuevo-rol"><Plus className="h-4 w-4" />Nuevo Rol</Button>}
      />
      <DataTable data={roles} columns={columns} isLoading={isLoading} keyExtractor={(r) => r.idRol!} emptyMessage="No hay roles registrados" />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={isEditMode ? "Editar Rol" : "Nuevo Rol"}>
              <FormField label="Nombre" required>
                <Input
                  value={editing.nombre ?? ""}
                  onChange={(e) => setEditing({ ...editing, nombre: e.target.value })}
                  data-testid="input-nombre-rol"
                />
              </FormField>
              <FormField label="Descripción">
                <Input
                  value={editing.descripcion ?? ""}
                  onChange={(e) => setEditing({ ...editing, descripcion: e.target.value })}
                />
              </FormField>
              <FormField label="Módulos permitidos">
                <div className="grid grid-cols-2 gap-2 pt-1">
                  {MODULOS_DISPONIBLES.map((mod) => (
                    <label key={mod.value} className="flex items-center gap-2 cursor-pointer text-sm">
                      <input
                        type="checkbox"
                        checked={(editing.modulos ?? []).includes(mod.value)}
                        onChange={() => toggleModulo(mod.value)}
                        data-testid={`check-modulo-${mod.value}`}
                        className="rounded"
                      />
                      {mod.label}
                    </label>
                  ))}
                </div>
              </FormField>
              <FormField label="Activo">
                <label className="flex items-center gap-2 cursor-pointer text-sm">
                  <input
                    type="checkbox"
                    checked={editing.activo ?? true}
                    onChange={(e) => setEditing({ ...editing, activo: e.target.checked })}
                    className="rounded"
                  />
                  Rol activo
                </label>
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>Cancelar</Button>
              <Button onClick={handleSave} disabled={createMutation.isPending || updateMutation.isPending}>
                {isEditMode ? "Actualizar" : "Crear"}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <ConfirmDialog open={!!deleteId} onOpenChange={(v) => !v && setDeleteId(null)} onConfirm={handleDelete} loading={deleteMutation.isPending} />
    </div>
  );
}
