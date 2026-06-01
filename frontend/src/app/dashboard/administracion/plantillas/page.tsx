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
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import { useQuery } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { PlantillaPresupuesto } from "@/types";

const EMPTY: Partial<PlantillaPresupuesto> = { nombre: "", descripcion: "" };

export default function PlantillasPage() {
  const t = useTranslations("administracion.plantillas");
  const tc = useTranslations("common");

  const { data = [], isLoading, refetch } = useQuery({
    queryKey: ["plantillas-presupuesto"],
    queryFn: () => apiGet<PlantillaPresupuesto[]>("/plantilla-presupuesto"),
  });

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Partial<PlantillaPresupuesto>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);

  function openCreate() {
    setEditing(EMPTY);
    setIsEditMode(false);
    setModalOpen(true);
  }

  function openEdit(plantilla: PlantillaPresupuesto) {
    setEditing(plantilla);
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
      const body = { nombre: editing.nombre.trim(), descripcion: editing.descripcion?.trim() };
      if (isEditMode && editing.idPlantillaPresupuesto) {
        await apiPut(`/plantilla-presupuesto/${editing.idPlantillaPresupuesto}`, body);
        toast.success(t("updated"));
      } else {
        await apiPost("/plantilla-presupuesto", body);
        toast.success(t("created"));
      }
      setModalOpen(false);
      refetch();
    } catch {
      toast.error(t("errorCreate"));
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    setDeleting(true);
    try {
      await apiDelete(`/plantilla-presupuesto/${deleteId}`);
      toast.success(t("deleted"));
      refetch();
    } catch {
      toast.error(t("errorDelete"));
    } finally {
      setDeleting(false);
      setDeleteId(null);
    }
  }

  const columns: Column<PlantillaPresupuesto>[] = [
    { key: "id", header: tc("id"), render: (p) => <span className="text-xs text-muted-foreground">{p.idPlantillaPresupuesto}</span>, className: "w-12" },
    { key: "nombre", header: t("fields.nombre"), render: (p) => <span className="font-medium">{p.nombre}</span> },
    { key: "desc", header: tc("description"), render: (p) => p.descripcion ?? "—" },
    { key: "items", header: t("fields.items"), render: (p) => p.itemList?.length ?? 0 },
    {
      key: "actions",
      header: "",
      className: "w-24",
      render: (p) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(p)} data-testid="btn-edit-plantilla" aria-label={tc("edit")}>
            <NotaireIcon src="/icons/actions/generar.png" alt={tc("edit")} size={16} />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(p.idPlantillaPresupuesto!)}
            data-testid="btn-delete-plantilla"
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
          <Button onClick={openCreate} data-testid="btn-nueva-plantilla">
            <NotaireIcon src="/icons/actions/agregar.png" alt={tc("add")} size={16} className="mr-1" />
            {t("newPlantilla")}
          </Button>
        }
      />
      <DataTable data={data} columns={columns} isLoading={isLoading} keyExtractor={(p) => p.idPlantillaPresupuesto!} emptyMessage={t("noData")} />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={isEditMode ? t("editPlantilla") : t("newPlantilla")}>
              <FormField label={t("fields.nombre")} required>
                <Input
                  value={editing.nombre ?? ""}
                  onChange={(e) => setEditing({ ...editing, nombre: e.target.value })}
                  placeholder={t("fields.namePlaceholder")}
                  data-testid="input-nombre-plantilla"
                />
              </FormField>
              <FormField label={tc("description")}>
                <Input value={editing.descripcion ?? ""} onChange={(e) => setEditing({ ...editing, descripcion: e.target.value })} />
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
