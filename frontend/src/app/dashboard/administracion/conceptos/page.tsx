"use client";

import { useState } from "react";
import { toast } from "sonner";
import { NotaireIcon } from "@/components/ui/notaire-icon";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import { useConceptos, useCreateConcepto, useUpdateConcepto, useDeleteConcepto } from "@/hooks/useConceptos";
import { formatCurrency } from "@/lib/utils";
import type { Concepto } from "@/types";

const EMPTY: Partial<Concepto> = { nombre: "", descripcion: "", valor: undefined };

export default function ConceptosPage() {
  const { data: conceptos = [], isLoading } = useConceptos();
  const createMutation = useCreateConcepto();
  const updateMutation = useUpdateConcepto();
  const deleteMutation = useDeleteConcepto();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<Concepto>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);

  function openCreate() { setEditing(EMPTY); setIsEditMode(false); setModalOpen(true); }
  function openEdit(c: Concepto) { setEditing(c); setIsEditMode(true); setModalOpen(true); }

  async function handleSave() {
    if (!editing.nombre?.trim()) { toast.error("El nombre es obligatorio"); return; }
    try {
      if (isEditMode && editing.idConcepto) {
        await updateMutation.mutateAsync({ id: editing.idConcepto, data: editing });
        toast.success("Concepto actualizado");
      } else {
        await createMutation.mutateAsync(editing);
        toast.success("Concepto creado");
      }
      setModalOpen(false);
    } catch { toast.error("Error al guardar"); }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success("Concepto eliminado");
    } catch { toast.error("Error al eliminar"); }
    finally { setDeleteId(null); }
  }

  const columns: Column<Concepto>[] = [
    { key: "id", header: "ID", render: (c) => <span className="text-xs text-muted-foreground">{c.idConcepto}</span>, className: "w-12" },
    { key: "nombre", header: "Nombre", render: (c) => <span className="font-medium">{c.nombre}</span> },
    { key: "descripcion", header: "Descripción", render: (c) => c.descripcion ?? "—" },
    { key: "valor", header: "Valor base", render: (c) => formatCurrency(c.valor) },
    {
      key: "actions", header: "", className: "w-24",
      render: (c) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(c)}><NotaireIcon src="/icons/actions/generar.png" alt="Editar" size={16} /></Button>
          <Button size="sm" variant="ghost" className="text-destructive hover:text-destructive" onClick={() => setDeleteId(c.idConcepto!)}><NotaireIcon src="/icons/actions/borrar.png" alt="Eliminar" size={16} /></Button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <AppHeader
        title="Conceptos"
        description="CU29, CU66 — Catálogo de conceptos de presupuesto"
        actions={<Button onClick={openCreate}><NotaireIcon src="/icons/actions/agregar.png" alt="Agregar" size={16} className="mr-1" />Nuevo concepto</Button>}
      />
      <DataTable data={conceptos} columns={columns} isLoading={isLoading} keyExtractor={(c) => c.idConcepto!} emptyMessage="No hay conceptos" />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={isEditMode ? "Editar concepto" : "Nuevo concepto"}>
              <FormField label="Nombre" required>
                <Input
                  value={editing.nombre ?? ""}
                  onChange={(e) => setEditing({ ...editing, nombre: e.target.value })}
                  data-testid="input-nombre-concepto"
                />
              </FormField>
              <FormField label="Descripción">
                <Input
                  value={editing.descripcion ?? ""}
                  onChange={(e) => setEditing({ ...editing, descripcion: e.target.value })}
                />
              </FormField>
              <FormField label="Valor base ($)">
                <Input
                  type="number"
                  step="0.01"
                  value={editing.valor ?? ""}
                  onChange={(e) => setEditing({ ...editing, valor: parseFloat(e.target.value) })}
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                <NotaireIcon src="/icons/actions/cerrar.png" alt="Cancelar" size={16} className="mr-1" />
                Cancelar
              </Button>
              <Button onClick={handleSave} disabled={createMutation.isPending || updateMutation.isPending}>
                <NotaireIcon src="/icons/actions/guardar.png" alt="Guardar" size={16} className="mr-1 brightness-[100] invert" />
                {isEditMode ? "Actualizar" : "Guardar"}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <ConfirmDialog open={!!deleteId} onOpenChange={(v) => !v && setDeleteId(null)} onConfirm={handleDelete} loading={deleteMutation.isPending} />
    </div>
  );
}
