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
import type { Usuario } from "@/types";

const EMPTY: Partial<Usuario> = { nombre: "", contrasenia: "", tipo: "EMPLEADO", activo: true };

export default function UsuariosPage() {
  const t = useTranslations("administracion.usuarios");
  const tc = useTranslations("common");

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
    if (!editing.nombre?.trim()) { toast.error(t("fields.nombre") + " " + tc("required")); return; }
    try {
      if (isEditMode && editing.idUsuario) {
        await updateMutation.mutateAsync({ id: editing.idUsuario, data: editing });
        toast.success(t("updated"));
      } else {
        await createMutation.mutateAsync(editing);
        toast.success(t("created"));
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
    { key: "id", header: tc("id"), render: (u) => <span className="text-xs text-muted-foreground">{u.idUsuario}</span>, className: "w-12" },
    { key: "nombre", header: t("fields.nombre"), render: (u) => <span className="font-medium">{u.nombre}</span> },
    { key: "tipo", header: t("fields.tipo"), render: (u) => <Badge variant={tipoVariant(u.tipo)}>{u.tipo ?? "—"}</Badge> },
    { key: "activo", header: tc("status"), render: (u) => u.activo ? <Badge variant="success">Activo</Badge> : <Badge variant="secondary">Inactivo</Badge> },
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
        title={t("title")}
        actions={<Button onClick={openCreate} data-testid="btn-nuevo-usuario"><Plus className="h-4 w-4" />{t("newUsuario")}</Button>}
      />
      <DataTable data={usuarios} columns={columns} isLoading={isLoading} keyExtractor={(u) => u.idUsuario!} emptyMessage={t("noData")} />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={isEditMode ? t("editUsuario") : t("newUsuario")}>
              <FormField label={t("fields.nombre")} required>
                <Input
                  value={editing.nombre ?? ""}
                  onChange={(e) => setEditing({ ...editing, nombre: e.target.value })}
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
                  value={editing.contrasenia ?? ""}
                  onChange={(e) => setEditing({ ...editing, contrasenia: e.target.value })}
                />
              </FormField>
              <FormField label={t("fields.tipo")}>
                <Select
                  value={editing.tipo ?? "EMPLEADO"}
                  onValueChange={(v) => setEditing({ ...editing, tipo: v })}
                >
                  <SelectTrigger>
                    <SelectValue label={{ EMPLEADO: t("roles.empleado"), ADMIN: t("roles.admin"), ESCRIBANO: t("roles.escribano") }[editing.tipo ?? "EMPLEADO"]} />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="EMPLEADO">{t("roles.empleado")}</SelectItem>
                    <SelectItem value="ADMIN">{t("roles.admin")}</SelectItem>
                    <SelectItem value="ESCRIBANO">{t("roles.escribano")}</SelectItem>
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
