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
import {
  usePresupuestos,
  useCreatePresupuesto,
  useUpdatePresupuesto,
  useDeletePresupuesto,
} from "@/hooks/usePresupuestos";
import { formatDate, formatCurrency } from "@/lib/utils";
import type { Presupuesto } from "@/types";

const EMPTY: Partial<Presupuesto> = { fecha: "", monto: undefined, estado: "BORRADOR" };

export default function PresupuestosPage() {
  const { data: presupuestos = [], isLoading } = usePresupuestos();
  const createMutation = useCreatePresupuesto();
  const updateMutation = useUpdatePresupuesto();
  const deleteMutation = useDeletePresupuesto();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<Presupuesto>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);

  function openCreate() {
    setEditing(EMPTY);
    setIsEditMode(false);
    setModalOpen(true);
  }

  function openEdit(p: Presupuesto) {
    setEditing(p);
    setIsEditMode(true);
    setModalOpen(true);
  }

  async function handleSave() {
    try {
      if (isEditMode && editing.idPresupuesto) {
        await updateMutation.mutateAsync({ id: editing.idPresupuesto, data: editing });
        toast.success("Presupuesto actualizado");
      } else {
        await createMutation.mutateAsync(editing);
        toast.success("Presupuesto creado");
      }
      setModalOpen(false);
    } catch {
      toast.error("Error al guardar el presupuesto");
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success("Presupuesto eliminado");
    } catch {
      toast.error("Error al eliminar");
    } finally {
      setDeleteId(null);
    }
  }

  const columns: Column<Presupuesto>[] = [
    {
      key: "id",
      header: "ID",
      render: (p) => <span className="text-xs text-muted-foreground">{p.idPresupuesto}</span>,
      className: "w-12",
    },
    {
      key: "fecha",
      header: "Fecha",
      render: (p) => formatDate(p.fecha),
    },
    {
      key: "monto",
      header: "Monto",
      render: (p) => <span className="font-medium">{formatCurrency(p.monto)}</span>,
    },
    {
      key: "estado",
      header: "Estado",
      render: (p) => p.estado ?? "—",
    },
    {
      key: "actions",
      header: "",
      render: (p) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(p)}>
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(p.idPresupuesto!)}
          >
            <Trash2 className="h-4 w-4" />
          </Button>
        </div>
      ),
      className: "w-24",
    },
  ];

  return (
    <div>
      <AppHeader
        title="Presupuestos"
        description="CU01, CU39, CU45, CU49, CU55, CU60 — Preparar y gestionar presupuestos"
        actions={
          <Button onClick={openCreate} data-testid="btn-nuevo-presupuesto">
            <Plus className="h-4 w-4" />
            Nuevo presupuesto
          </Button>
        }
      />

      <DataTable
        data={presupuestos}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(p) => p.idPresupuesto!}
        emptyMessage="No hay presupuestos"
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{isEditMode ? "Editar presupuesto" : "Nuevo presupuesto"}</DialogTitle>
          </DialogHeader>
          <div className="space-y-3 pt-2">
            <div className="space-y-1">
              <Label>Fecha</Label>
              <Input
                type="date"
                value={editing.fecha ?? ""}
                onChange={(e) => setEditing({ ...editing, fecha: e.target.value })}
              />
            </div>
            <div className="space-y-1">
              <Label>Monto ($)</Label>
              <Input
                type="number"
                step="0.01"
                value={editing.monto ?? ""}
                onChange={(e) => setEditing({ ...editing, monto: parseFloat(e.target.value) })}
                data-testid="input-monto"
              />
            </div>
            <div className="space-y-1">
              <Label>Estado</Label>
              <Input
                value={editing.estado ?? ""}
                onChange={(e) => setEditing({ ...editing, estado: e.target.value })}
                placeholder="BORRADOR, APROBADO, etc."
              />
            </div>
            <div className="flex justify-end gap-2 pt-2">
              <Button variant="outline" onClick={() => setModalOpen(false)}>
                Cancelar
              </Button>
              <Button onClick={handleSave} disabled={createMutation.isPending || updateMutation.isPending}>
                {isEditMode ? "Actualizar" : "Crear"}
              </Button>
            </div>
          </div>
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
