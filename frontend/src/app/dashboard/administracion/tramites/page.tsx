"use client";
import { useState } from "react";
import { toast } from "sonner";
import { NotaireIcon } from "@/components/ui/notaire-icon";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { FormContainer, FormSection, FormField, FormActions, CheckboxField } from "@/theme/form-patterns";
import { useTiposTramite, useCreateTipoTramite, useUpdateTipoTramite, useDeleteTipoTramite } from "@/hooks/useTiposTramite";
import type { TipoDeTramite } from "@/types";

const EMPTY: Partial<TipoDeTramite> = { nombre: "", descripcion: "" };

export default function TramitesPage() {
  const { data = [], isLoading } = useTiposTramite();
  const createMutation = useCreateTipoTramite();
  const updateMutation = useUpdateTipoTramite();
  const deleteMutation = useDeleteTipoTramite();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<TipoDeTramite>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);

  function openCreate() {
    setEditing(EMPTY);
    setIsEditMode(false);
    setModalOpen(true);
  }

  function openEdit(t: TipoDeTramite) {
    setEditing(t);
    setIsEditMode(true);
    setModalOpen(true);
  }

  async function handleSave() {
    if (!editing.nombre?.trim()) {
      toast.error("El nombre es requerido");
      return;
    }
    try {
      if (isEditMode && editing.idTipoDeTramite) {
        await updateMutation.mutateAsync({ id: editing.idTipoDeTramite, data: editing });
        toast.success("Tipo de trámite actualizado");
      } else {
        await createMutation.mutateAsync(editing);
        toast.success("Tipo de trámite creado");
      }
      setModalOpen(false);
    } catch {
      toast.error("Error al guardar el tipo de trámite");
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success("Tipo de trámite eliminado");
    } catch {
      toast.error("Error al eliminar");
    } finally {
      setDeleteId(null);
    }
  }

  const columns: Column<TipoDeTramite>[] = [
    { key: "id", header: "ID", render: (t) => <span className="text-xs text-muted-foreground">{t.idTipoDeTramite}</span>, className: "w-12" },
    { key: "nombre", header: "Nombre", render: (t) => <span className="font-medium">{t.nombre}</span> },
    { key: "desc", header: "Descripción", render: (t) => t.descripcion ?? "—" },
    {
      key: "actions", header: "", className: "w-24",
      render: (t) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(t)}>
            <NotaireIcon src="/icons/actions/generar.png" alt="Editar" size={16} />
          </Button>
          <Button size="sm" variant="ghost" className="text-destructive hover:text-destructive" onClick={() => setDeleteId(t.idTipoDeTramite!)}>
            <NotaireIcon src="/icons/actions/borrar.png" alt="Eliminar" size={16} />
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <AppHeader
        title="Tipos de Trámite"
        description="Catálogo de tipos de trámite disponibles para gestiones notariales"
        actions={
          <Button onClick={openCreate} data-testid="btn-nuevo-tipo-tramite">
            <NotaireIcon src="/icons/actions/agregar.png" alt="Agregar" size={16} className="mr-1" />
            Nuevo Tipo
          </Button>
        }
      />
      <DataTable
        data={data}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(t) => t.idTipoDeTramite!}
        emptyMessage="No hay tipos de trámite registrados"
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={isEditMode ? "Editar tipo de trámite" : "Nuevo tipo de trámite"}>
              <FormField label="Nombre" required>
                <Input
                  value={editing.nombre ?? ""}
                  onChange={(e) => setEditing({ ...editing, nombre: e.target.value })}
                  placeholder="Ej: Compraventa"
                  data-testid="input-nombre-tramite"
                />
              </FormField>
              <FormField label="Descripción">
                <Input
                  value={editing.descripcion ?? ""}
                  onChange={(e) => setEditing({ ...editing, descripcion: e.target.value })}
                  placeholder="Descripción del tipo de trámite"
                />
              </FormField>
              <CheckboxField
                label="Se archiva"
                checked={editing.seArchiva ?? false}
                onChange={(v) => setEditing({ ...editing, seArchiva: v })}
              />
              <CheckboxField
                label="Se inscribe"
                checked={editing.seInscribe ?? false}
                onChange={(v) => setEditing({ ...editing, seInscribe: v })}
              />
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                <NotaireIcon src="/icons/actions/cerrar.png" alt="Cancelar" size={16} className="mr-1" />
                Cancelar
              </Button>
              <Button onClick={handleSave} disabled={createMutation.isPending || updateMutation.isPending}>
                <NotaireIcon src="/icons/actions/guardar.png" alt="Guardar" size={16} className="mr-1 brightness-0 invert" />
                {isEditMode ? "Actualizar" : "Guardar"}
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
