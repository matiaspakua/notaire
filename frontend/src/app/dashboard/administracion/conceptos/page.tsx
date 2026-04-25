"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Plus, Pencil, Trash2 } from "lucide-react";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
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
          <Button size="sm" variant="ghost" onClick={() => openEdit(c)}><Pencil className="h-4 w-4" /></Button>
          <Button size="sm" variant="ghost" className="text-destructive hover:text-destructive" onClick={() => setDeleteId(c.idConcepto!)}><Trash2 className="h-4 w-4" /></Button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <AppHeader title="Conceptos" description="CU29, CU66 — Catálogo de conceptos de presupuesto"
        actions={<Button onClick={openCreate}><Plus className="h-4 w-4" />Nuevo concepto</Button>}
      />
      <DataTable data={conceptos} columns={columns} isLoading={isLoading} keyExtractor={(c) => c.idConcepto!} emptyMessage="No hay conceptos" />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <DialogHeader><DialogTitle>{isEditMode ? "Editar concepto" : "Nuevo concepto"}</DialogTitle></DialogHeader>
          <div className="space-y-3 pt-2">
            <div className="space-y-1"><Label>Nombre *</Label><Input value={editing.nombre ?? ""} onChange={(e) => setEditing({ ...editing, nombre: e.target.value })} data-testid="input-nombre-concepto" /></div>
            <div className="space-y-1"><Label>Descripción</Label><Input value={editing.descripcion ?? ""} onChange={(e) => setEditing({ ...editing, descripcion: e.target.value })} /></div>
            <div className="space-y-1"><Label>Valor base ($)</Label><Input type="number" step="0.01" value={editing.valor ?? ""} onChange={(e) => setEditing({ ...editing, valor: parseFloat(e.target.value) })} /></div>
            <div className="flex justify-end gap-2 pt-2">
              <Button variant="outline" onClick={() => setModalOpen(false)}>Cancelar</Button>
              <Button onClick={handleSave} disabled={createMutation.isPending || updateMutation.isPending}>{isEditMode ? "Actualizar" : "Crear"}</Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      <ConfirmDialog open={!!deleteId} onOpenChange={(v) => !v && setDeleteId(null)} onConfirm={handleDelete} loading={deleteMutation.isPending} />
    </div>
  );
}
