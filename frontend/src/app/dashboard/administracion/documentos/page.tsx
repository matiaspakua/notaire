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
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { TipoDeDocumento } from "@/types";

const EMPTY: Partial<TipoDeDocumento> = { nombre: "" };

export default function DocumentosPage() {
  const qc = useQueryClient();
  const { data: tipos = [], isLoading } = useQuery({
    queryKey: ["tiposDocumento"],
    queryFn: () => apiGet<TipoDeDocumento[]>("/tipo-de-documento"),
  });

  const createMutation = useMutation({
    mutationFn: (data: Partial<TipoDeDocumento>) => apiPost<void>("/tipo-de-documento", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["tiposDocumento"] }),
  });
  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<TipoDeDocumento> }) =>
      apiPut<void>(`/tipo-de-documento/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["tiposDocumento"] }),
  });
  const deleteMutation = useMutation({
    mutationFn: (id: number) => apiDelete(`/tipo-de-documento/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["tiposDocumento"] }),
  });

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<TipoDeDocumento>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);

  function openCreate() {
    setEditing(EMPTY);
    setIsEditMode(false);
    setModalOpen(true);
  }

  function openEdit(t: TipoDeDocumento) {
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
      if (isEditMode && editing.idTipoDocumento) {
        await updateMutation.mutateAsync({ id: editing.idTipoDocumento, data: editing });
        toast.success("Tipo de documento actualizado");
      } else {
        await createMutation.mutateAsync(editing);
        toast.success("Tipo de documento creado");
      }
      setModalOpen(false);
    } catch {
      toast.error("Error al guardar el tipo de documento");
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success("Tipo de documento eliminado");
    } catch {
      toast.error("No se puede eliminar: el tipo de documento está referenciado por otros registros.");
    } finally {
      setDeleteId(null);
    }
  }

  const columns: Column<TipoDeDocumento>[] = [
    { key: "id", header: "ID", render: (t) => <span className="text-xs text-muted-foreground">{t.idTipoDocumento}</span>, className: "w-12" },
    { key: "nombre", header: "Nombre", render: (t) => <span className="font-medium">{t.nombre}</span> },
    {
      key: "actions", header: "", className: "w-24",
      render: (t) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(t)}>
            <NotaireIcon src="/icons/actions/generar.png" alt="Editar" size={16} />
          </Button>
          <Button size="sm" variant="ghost" className="text-destructive hover:text-destructive" onClick={() => setDeleteId(t.idTipoDocumento!)}>
            <NotaireIcon src="/icons/actions/borrar.png" alt="Eliminar" size={16} />
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <AppHeader
        title="Tipos de Documento"
        description="Catálogo de tipos de documento notarial"
        actions={
          <Button onClick={openCreate} data-testid="btn-nuevo-tipo-documento">
            <NotaireIcon src="/icons/actions/agregar.png" alt="Agregar" size={16} className="mr-1" />
            Nuevo Tipo
          </Button>
        }
      />
      <DataTable
        data={tipos}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(t) => t.idTipoDocumento!}
        emptyMessage="No hay tipos de documento registrados"
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={isEditMode ? "Editar tipo de documento" : "Nuevo tipo de documento"}>
              <FormField label="Nombre" required>
                <Input
                  value={editing.nombre ?? ""}
                  onChange={(e) => setEditing({ ...editing, nombre: e.target.value })}
                  placeholder="Ej: DNI, Escritura"
                  data-testid="input-nombre-documento"
                />
              </FormField>
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
