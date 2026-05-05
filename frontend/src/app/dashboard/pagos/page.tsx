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
import { usePagos, useCreatePago, useUpdatePago, useDeletePago } from "@/hooks/usePagos";
import { formatDate, formatCurrency } from "@/lib/utils";
import type { Pago } from "@/types";

const EMPTY: Partial<Pago> = { idPresupuesto: undefined, monto: undefined, fecha: "", metodoPago: "", observaciones: "" };

export default function PagosPage() {
  const { data: pagos = [], isLoading } = usePagos();
  const createMutation = useCreatePago();
  const updateMutation = useUpdatePago();
  const deleteMutation = useDeletePago();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<Pago>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);

  function openCreate() { setEditing(EMPTY); setIsEditMode(false); setModalOpen(true); }
  function openEdit(p: Pago) { setEditing(p); setIsEditMode(true); setModalOpen(true); }

  async function handleSave() {
    try {
      if (isEditMode && editing.idPago) {
        await updateMutation.mutateAsync({ id: editing.idPago, data: editing });
        toast.success("Pago actualizado");
      } else {
        await createMutation.mutateAsync(editing);
        toast.success("Pago registrado");
      }
      setModalOpen(false);
    } catch { toast.error("Error al guardar"); }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success("Pago eliminado");
    } catch { toast.error("Error al eliminar"); }
    finally { setDeleteId(null); }
  }

  const columns: Column<Pago>[] = [
    { key: "id", header: "ID", render: (p) => <span className="text-xs text-muted-foreground">{p.idPago}</span>, className: "w-12" },
    { key: "presupuesto", header: "Presupuesto", render: (p) => <span className="text-xs text-muted-foreground">#{p.idPresupuesto ?? p.presupuesto?.idPresupuesto ?? "—"}</span>, className: "w-20" },
    { key: "fecha", header: "Fecha", render: (p) => formatDate(p.fecha) },
    { key: "monto", header: "Monto", render: (p) => <span className="font-medium">{formatCurrency(p.monto)}</span> },
    { key: "metodo", header: "Método", render: (p) => p.metodoPago ?? "—" },
    {
      key: "actions", header: "", className: "w-24",
      render: (p) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(p)}><Pencil className="h-4 w-4" /></Button>
          <Button size="sm" variant="ghost" className="text-destructive hover:text-destructive" onClick={() => setDeleteId(p.idPago!)}><Trash2 className="h-4 w-4" /></Button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <AppHeader
        title="Pagos"
        description="CU15, CU47 — Procesar y consultar pagos"
        actions={<Button onClick={openCreate}><Plus className="h-4 w-4" />Registrar pago</Button>}
      />
      <DataTable data={pagos} columns={columns} isLoading={isLoading} keyExtractor={(p) => p.idPago!} emptyMessage="No hay pagos registrados" />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <DialogHeader><DialogTitle>{isEditMode ? "Editar pago" : "Registrar pago"}</DialogTitle></DialogHeader>
          <div className="space-y-3 pt-2">
            <div className="space-y-1"><Label>Presupuesto ID</Label><Input type="number" value={editing.idPresupuesto ?? ""} onChange={(e) => setEditing({ ...editing, idPresupuesto: parseInt(e.target.value) })} placeholder="ID del presupuesto" /></div>
            <div className="space-y-1"><Label>Fecha</Label><Input type="date" value={editing.fecha ?? ""} onChange={(e) => setEditing({ ...editing, fecha: e.target.value })} /></div>
            <div className="space-y-1"><Label>Monto ($)</Label><Input type="number" step="0.01" value={editing.monto ?? ""} onChange={(e) => setEditing({ ...editing, monto: parseFloat(e.target.value) })} /></div>
            <div className="space-y-1"><Label>Método de pago</Label><Input value={editing.metodoPago ?? ""} onChange={(e) => setEditing({ ...editing, metodoPago: e.target.value })} placeholder="Efectivo, transferencia, etc." /></div>
            <div className="space-y-1"><Label>Observaciones</Label><Input value={editing.observaciones ?? ""} onChange={(e) => setEditing({ ...editing, observaciones: e.target.value })} placeholder="Notas opcionales" /></div>
            <div className="flex justify-end gap-2 pt-2">
              <Button variant="outline" onClick={() => setModalOpen(false)}>Cancelar</Button>
              <Button onClick={handleSave} disabled={createMutation.isPending || updateMutation.isPending}>{isEditMode ? "Actualizar" : "Registrar"}</Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      <ConfirmDialog open={!!deleteId} onOpenChange={(v) => !v && setDeleteId(null)} onConfirm={handleDelete} loading={deleteMutation.isPending} />
    </div>
  );
}
