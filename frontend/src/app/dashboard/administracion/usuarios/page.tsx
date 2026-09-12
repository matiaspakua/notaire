"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Plus, Pencil, Trash2 } from "lucide-react";
import { useTranslations } from "next-intl";
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
import { useRoles, useAssignRolToUsuario, useUnassignRolFromUsuario } from "@/hooks/useRoles";
import type { Usuario } from "@/types";

const EMPTY: Partial<Usuario> = { name: "", password: "", type: "EMPLEADO", active: true };

export default function UsuariosPage() {
  const t = useTranslations("administracion.usuarios");
  const tc = useTranslations("common");

  const { data: usuarios = [], isLoading } = useUsuarios();
  const { data: roles = [] } = useRoles();
  const createMutation = useCreateUsuario();
  const updateMutation = useUpdateUsuario();
  const deleteMutation = useDeleteUsuario();
  const assignRolMutation = useAssignRolToUsuario();
  const unassignRolMutation = useUnassignRolFromUsuario();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<Usuario>>(EMPTY);
  const [selectedRolId, setSelectedRolId] = useState<string>("none");
  const [isEditMode, setIsEditMode] = useState(false);

  function openCreate() { setEditing(EMPTY); setSelectedRolId("none"); setIsEditMode(false); setModalOpen(true); }
  function openEdit(u: Usuario) {
    setEditing({ ...u, password: "" });
    setSelectedRolId(u.role?.idRole?.toString() ?? "none");
    setIsEditMode(true);
    setModalOpen(true);
  }

  async function handleSave() {
    if (!editing.name?.trim()) { toast.error(t("fields.nombre") + " " + tc("required")); return; }
    try {
      let savedId: number | undefined;
      if (isEditMode && editing.idUser) {
        await updateMutation.mutateAsync({ id: editing.idUser, data: editing });
        savedId = editing.idUser;
        toast.success(t("updated"));
      } else {
        const created = await createMutation.mutateAsync(editing);
        savedId = (created as Usuario)?.idUser;
        toast.success(t("created"));
      }
      if (savedId) {
        if (selectedRolId && selectedRolId !== "none") {
          await assignRolMutation.mutateAsync({ idRol: Number(selectedRolId), idUsuario: savedId });
        } else if (isEditMode && editing.role) {
          await unassignRolMutation.mutateAsync(savedId);
        }
      }
      setModalOpen(false);
    } catch { toast.error(t("errorSave")); }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success(t("deleted"));
    } catch { toast.error(t("errorDelete")); }
    finally { setDeleteId(null); }
  }

  const tipoVariant = (tipo?: string) => {
    const role = tipo?.toUpperCase();
    if (role === "ADMIN" || role === "ADMINISTRADOR" || role === "ESCRIBANO") return "default" as const;
    return "secondary" as const;
  };

  const columns: Column<Usuario>[] = [
    { key: "id", header: tc("id"), render: (u) => <span className="text-xs text-muted-foreground">{u.idUser}</span>, className: "w-12" },
    { key: "nombre", header: t("fields.nombre"), render: (u) => <span className="font-medium">{u.name}</span> },
    { key: "tipo", header: t("fields.tipo"), render: (u) => <Badge variant={tipoVariant(u.type)}>{u.type ?? "—"}</Badge> },
    { key: "rol", header: "Rol", render: (u) => u.role ? <Badge variant="outline">{u.role.name}</Badge> : <span className="text-xs text-muted-foreground">—</span> },
    { key: "activo", header: tc("status"), render: (u) => u.active ? <Badge variant="success">Activo</Badge> : <Badge variant="secondary">Inactivo</Badge> },
    {
      key: "actions", header: "", className: "w-24",
      render: (u) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(u)}><Pencil className="h-4 w-4" /></Button>
          <Button size="sm" variant="ghost" className="text-destructive hover:text-destructive" onClick={() => setDeleteId(u.idUser!)}><Trash2 className="h-4 w-4" /></Button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <AppHeader
        title={t("title")}
        actions={<Button onClick={openCreate} data-testid="btn-nuevo-usuario"><Plus className="h-4 w-4" />{t("newUsuario")}</Button>}
      />
      <DataTable data={usuarios} columns={columns} isLoading={isLoading} keyExtractor={(u) => u.idUser!} emptyMessage={t("noData")} />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={isEditMode ? t("editUsuario") : t("newUsuario")}>
              <FormField label={t("fields.nombre")} required>
                <Input
                  value={editing.name ?? ""}
                  onChange={(e) => setEditing({ ...editing, name: e.target.value })}
                  data-testid="input-nombre-usuario"
                />
              </FormField>
              <FormField
                label={isEditMode ? "Nueva contraseña" : t("fields.contrasenia")}
                required={!isEditMode}
                helperText={isEditMode ? t("fields.contraseniaHint") : undefined}
              >
                <Input
                  type="password"
                  value={editing.password ?? ""}
                  onChange={(e) => setEditing({ ...editing, password: e.target.value })}
                />
              </FormField>
              <FormField label={t("fields.tipo")}>
                <Select
                  value={editing.type ?? "EMPLEADO"}
                  onValueChange={(v) => setEditing({ ...editing, type: v })}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="EMPLEADO">{t("roles.empleado")}</SelectItem>
                    <SelectItem value="ADMIN">{t("roles.admin")}</SelectItem>
                    <SelectItem value="ESCRIBANO">{t("roles.escribano")}</SelectItem>
                  </SelectContent>
                </Select>
              </FormField>
              <FormField label="Rol de acceso" helperText="Permisos de módulos del sistema">
                <Select value={selectedRolId} onValueChange={setSelectedRolId}>
                  <SelectTrigger data-testid="select-rol-usuario">
                    <SelectValue placeholder="Sin rol asignado" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="none">Sin rol</SelectItem>
                    {roles.filter((r) => r.active).map((r) => (
                      <SelectItem key={r.idRole} value={String(r.idRole)}>{r.name}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>{tc("cancel")}</Button>
              <Button onClick={handleSave} disabled={createMutation.isPending || updateMutation.isPending}>
                {isEditMode ? tc("update") : tc("create")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <ConfirmDialog open={!!deleteId} onOpenChange={(v) => !v && setDeleteId(null)} onConfirm={handleDelete} loading={deleteMutation.isPending} />
    </div>
  );
}
