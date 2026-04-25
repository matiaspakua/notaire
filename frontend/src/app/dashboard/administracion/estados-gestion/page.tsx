"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Plus, Pencil, Trash2 } from "lucide-react";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  useEstadosGestion,
  useCreateEstadoGestion,
  useUpdateEstadoGestion,
  useDeleteEstadoGestion,
} from "@/hooks/useEstadosGestion";
import type { EstadoDeGestion } from "@/types";

/** CU67 — Gestionar estados de gestión */
const EMPTY: Partial<EstadoDeGestion> = { nombre: "" };

export default function EstadosGestionPage() {
  const { data: estados = [], isLoading } = useEstadosGestion();
  const createMutation = useCreateEstadoGestion();
  const updateMutation = useUpdateEstadoGestion();
  const deleteMutation = useDeleteEstadoGestion();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<EstadoDeGestion>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);

  function openCreate() {
    setEditing(EMPTY);
    setIsEditMode(false);
    setModalOpen(true);
  }

  function openEdit(e: EstadoDeGestion) {
    setEditing(e);
    setIsEditMode(true);
    setModalOpen(true);
  }

  async function handleSave() {
    if (!editing.nombre?.trim()) {
      toast.error("El nombre es obligatorio");
      return;
    }
    try {
      if (isEditMode && editing.idEstadoGestion) {
        await updateMutation.mutateAsync({
          id: editing.idEstadoGestion,
          data: editing,
        });
        toast.success("Estado actualizado");
      } else {
        await createMutation.mutateAsync(editing);
        toast.success("Estado creado");
      }
      setModalOpen(false);
    } catch {
      toast.error("Error al guardar el estado");
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success("Estado eliminado");
    } catch {
      toast.error("Error al eliminar el estado");
    } finally {
      setDeleteId(null);
    }
  }

  const columns: Column<EstadoDeGestion>[] = [
    {
      key: "id",
      header: "ID",
      render: (e) => (
        <span className="text-xs text-muted-foreground">{e.idEstadoGestion}</span>
      ),
      className: "w-16",
    },
    {
      key: "nombre",
      header: "Nombre",
      render: (e) => <span className="font-medium">{e.nombre ?? "—"}</span>,
    },
    {
      key: "actions",
      header: "",
      className: "w-24",
      render: (e) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(e)}>
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(e.idEstadoGestion!)}
          >
            <Trash2 className="h-4 w-4" />
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <AppHeader
        title="Estados de Gestión"
        description="CU67 — Administrar los estados del ciclo de vida de una gestión"
        actions={
          <Button onClick={openCreate}>
            <Plus className="h-4 w-4" />
            Nuevo estado
          </Button>
        }
      />

      <DataTable
        data={estados}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(e) => e.idEstadoGestion!}
        emptyMessage="No hay estados de gestión registrados"
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {isEditMode ? "Editar estado" : "Nuevo estado de gestión"}
            </DialogTitle>
          </DialogHeader>
          <div className="space-y-3 pt-2">
            <div className="space-y-1">
              <Label>Nombre *</Label>
              <Input
                value={editing.nombre ?? ""}
                onChange={(e) =>
                  setEditing({ ...editing, nombre: e.target.value })
                }
                placeholder="Ej: En proceso, Finalizado, Cancelado…"
              />
            </div>
            <div className="flex justify-end gap-2 pt-2">
              <Button variant="outline" onClick={() => setModalOpen(false)}>
                Cancelar
              </Button>
              <Button
                onClick={handleSave}
                disabled={
                  createMutation.isPending || updateMutation.isPending
                }
              >
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
