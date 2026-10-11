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
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import { useRoles, useCreateRole, useUpdateRole, useDeleteRole } from "@/hooks/useRoles";
import { presentMutationError } from "@/lib/mutation-error";
import type { Role } from "@/types";
import { useDeleteError } from "@/hooks/useDeleteError";

const MODULE_VALUES = [
  "administracion",
  "usuarios",
  "tramites",
  "presupuestos",
  "escrituras",
  "personas",
  "auditoria",
  "workflows",
] as const;

const EMPTY: Partial<Role> = { name: "", description: "", active: true, modulos: [] };

export default function RolesPage() {
  const t = useTranslations("administracion.roles");
  const showDeleteError = useDeleteError();
  const tc = useTranslations("common");
  const { data: roles = [], isLoading } = useRoles();
  const createMutation = useCreateRole();
  const updateMutation = useUpdateRole();
  const deleteMutation = useDeleteRole();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<Role>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  function openCreate() {
    setEditing(EMPTY);
    setIsEditMode(false);
    setFieldErrors({});
    setModalOpen(true);
  }
  function openEdit(r: Role) {
    setEditing({ ...r });
    setIsEditMode(true);
    setFieldErrors({});
    setModalOpen(true);
  }

  function toggleModule(module: string) {
    const current = editing.modulos ?? [];
    const updated = current.includes(module)
      ? current.filter((m) => m !== module)
      : [...current, module];
    setEditing({ ...editing, modulos: updated });
  }

  async function handleSave() {
    if (!editing.name?.trim()) { toast.error(t("nameRequired")); return; }
    setFieldErrors({});
    try {
      if (isEditMode && editing.idRole) {
        await updateMutation.mutateAsync({ id: editing.idRole, data: editing });
        toast.success(t("updated"));
      } else {
        await createMutation.mutateAsync(editing);
        toast.success(t("created"));
      }
      setModalOpen(false);
    } catch (err) {
      presentMutationError(err, {
        fallback: t("errorSave"),
        fieldNames: ["name", "description"],
        setFieldErrors,
      });
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success(t("deleted"));
    } catch (err) {
      showDeleteError(err, t("errorDelete"));
    } finally {
      setDeleteId(null);
    }
  }

  const columns: Column<Role>[] = [
    { key: "id", header: tc("id"), render: (r) => <span className="text-xs text-muted-foreground">{r.idRole}</span>, className: "w-12" },
    { key: "name", header: tc("name"), render: (r) => <span className="font-medium">{r.name}</span> },
    { key: "description", header: tc("description"), render: (r) => <span className="text-sm text-muted-foreground">{r.description ?? "—"}</span> },
    { key: "modules", header: t("fields.modulos"), render: (r) => (
      <div className="flex flex-wrap gap-1">
        {(r.modulos ?? []).map((m) => <Badge key={m} variant="outline" className="text-xs">{m}</Badge>)}
        {(r.modulos ?? []).length === 0 && <span className="text-xs text-muted-foreground">{t("noPermissions")}</span>}
      </div>
    )},
    { key: "active", header: tc("status"), render: (r) => r.active ? <Badge variant="success">{tc("active")}</Badge> : <Badge variant="secondary">{tc("inactive")}</Badge> },
    {
      key: "actions", header: "", className: "w-24",
      render: (r) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(r)} data-testid={`btn-edit-role-${r.idRole}`} aria-label={tc("edit")}><Pencil className="h-4 w-4" /></Button>
          <Button size="sm" variant="ghost" className="text-destructive hover:text-destructive" onClick={() => setDeleteId(r.idRole!)} aria-label={tc("delete")}><Trash2 className="h-4 w-4" /></Button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <AppHeader
        title={t("title")}
        description={t("description")}
        actions={<Button onClick={openCreate} data-testid="btn-new-role"><Plus className="h-4 w-4" />{t("newRol")}</Button>}
      />
      <DataTable data={roles} columns={columns} isLoading={isLoading} keyExtractor={(r) => r.idRole!} emptyMessage={t("noData")} />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={isEditMode ? t("editRol") : t("newRol")}>
              <FormField label={tc("name")} required error={fieldErrors.name}>
                <Input
                  value={editing.name ?? ""}
                  onChange={(e) => setEditing({ ...editing, name: e.target.value })}
                  data-testid="input-name-role"
                  aria-invalid={!!fieldErrors.name}
                />
              </FormField>
              <FormField label={tc("description")} error={fieldErrors.description}>
                <Input
                  value={editing.description ?? ""}
                  onChange={(e) => setEditing({ ...editing, description: e.target.value })}
                  aria-invalid={!!fieldErrors.description}
                />
              </FormField>
              <FormField label={t("fields.modulosPermitidos")}>
                <div className="grid grid-cols-2 gap-2 pt-1">
                  {MODULE_VALUES.map((mod) => (
                    <label key={mod} className="flex items-center gap-2 cursor-pointer text-sm">
                      <input
                        type="checkbox"
                        checked={(editing.modulos ?? []).includes(mod)}
                        onChange={() => toggleModule(mod)}
                        data-testid={`check-modulo-${mod}`}
                        className="rounded"
                      />
                      {t(`modules.${mod}`)}
                    </label>
                  ))}
                </div>
              </FormField>
              <FormField label={t("fields.activo")}>
                <label className="flex items-center gap-2 cursor-pointer text-sm">
                  <input
                    type="checkbox"
                    checked={editing.active ?? true}
                    onChange={(e) => setEditing({ ...editing, active: e.target.checked })}
                    className="rounded"
                  />
                  {t("fields.activoHint")}
                </label>
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
