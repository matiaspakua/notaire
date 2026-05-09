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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import { useUsuarios, useCreateUsuario, useUpdateUsuario, useDeleteUsuario } from "@/hooks/useUsuarios";
import type { Usuario } from "@/types";

const EMPTY: Partial<Usuario> = { nombre: "", contrasenia: "", tipo: "EMPLEADO", activo: true };

export default function UsuariosPage() {
  const { data: usuarios = [], isLoading } = useUsuarios();
  const createMutation = useCreateUsuario();
  const updateMutation = useUpdateUsuario();
  const deleteMutation = useDeleteUsuario();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<Usuario>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);

  function openCreate() { setEditing(EMPTY); setIsEditMode(false); setModalOpen(true); }
  function openEdit(u: Usuario) { setEditing({ ...u, contrasenia: "" }); setIsEditMode(true); setModalOpen(true); }

  async function handleSave() {
    if (!editing.nombre?.trim()) { toast.error("El nombre es obligatorio"); return; }
    try {
      if (isEditMode && editing.idUsuario) {
        await updateMutation.mutateAsync({ id: editing.idUsuario, data: editing });
        toast.success("Usuario actualizado");
      } else {
        await createMutation.mutateAsync(editing);
        toast.success("Usuario creado");
      }
      setModalOpen(false);
    } catch { toast.error("Error al guardar el usuario"); }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success("Usuario eliminado");
    } catch { toast.error("Error al eliminar"); }
    finally { setDeleteId(null); }
  }

  const tipoVariant = (tipo?: string) => {
    const t = tipo?.toUpperCase();
    if (t === "ADMIN" || t === "ADMINISTRADOR" || t === "ESCRIBANO") return "default" as const;
    return "secondary" as const;
  };

  const columns: Column<Usuario>[] = [
    { key: "id", header: "ID", render: (u) => <span className="text-xs text-muted-foreground">{u.idUsuario}</span>, className: "w-12" },
    { key: "nombre", header: "Nombre de usuario", render: (u) => <span className="font-medium">{u.nombre}</span> },
    { key: "tipo", header: "Rol", render: (u) => <Badge variant={tipoVariant(u.tipo)}>{u.tipo ?? "—"}</Badge> },
    { key: "activo", header: "Estado", render: (u) => u.activo ? <Badge variant="success">Activo</Badge> : <Badge variant="secondary">Inactivo</Badge> },
    {
      key: "actions", header: "", className: "w-24",
      render: (u) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(u)}><Pencil className="h-4 w-4" /></Button>
          <Button size="sm" variant="ghost" className="text-destructive hover:text-destructive" onClick={() => setDeleteId(u.idUsuario!)}><Trash2 className="h-4 w-4" /></Button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <AppHeader
        title="Usuarios"
        description="Gestión de usuarios y roles del sistema"
        actions={<Button onClick={openCreate} data-testid="btn-nuevo-usuario"><Plus className="h-4 w-4" />Nuevo usuario</Button>}
      />
      <DataTable data={usuarios} columns={columns} isLoading={isLoading} keyExtractor={(u) => u.idUsuario!} emptyMessage="No hay usuarios" />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={isEditMode ? "Editar usuario" : "Nuevo usuario"}>
              <FormField label="Nombre de usuario" required>
                <Input
                  value={editing.nombre ?? ""}
                  onChange={(e) => setEditing({ ...editing, nombre: e.target.value })}
                  data-testid="input-nombre-usuario"
                />
              </FormField>
              <FormField
                label={isEditMode ? "Nueva contraseña" : "Contraseña"}
                required={!isEditMode}
                helperText={isEditMode ? "Dejar vacío para no cambiar" : undefined}
              >
                <Input
                  type="password"
                  value={editing.contrasenia ?? ""}
                  onChange={(e) => setEditing({ ...editing, contrasenia: e.target.value })}
                />
              </FormField>
              <FormField label="Tipo / Rol">
                <Select
                  value={editing.tipo ?? "EMPLEADO"}
                  onValueChange={(v) => setEditing({ ...editing, tipo: v })}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="EMPLEADO">EMPLEADO</SelectItem>
                    <SelectItem value="ADMIN">ADMIN</SelectItem>
                    <SelectItem value="ESCRIBANO">ESCRIBANO</SelectItem>
                  </SelectContent>
                </Select>
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
