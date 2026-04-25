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
  useTiposDocumento,
  useCreateTipoDocumento,
  useUpdateTipoDocumento,
  useDeleteTipoDocumento,
} from "@/hooks/useDocumentos";
import type { TipoDeDocumento } from "@/types";

/** CU27, CU65 — Gestionar tipos de documento presentado */
const EMPTY: Partial<TipoDeDocumento> = { nombre: "" };

export default function DocumentosPage() {
  const { data: tipos = [], isLoading } = useTiposDocumento();
  const createMutation = useCreateTipoDocumento();
  const updateMutation = useUpdateTipoDocumento();
  const deleteMutation = useDeleteTipoDocumento();

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
      toast.error("El nombre es obligatorio");
      return;
    }
    try {
      if (isEditMode && editing.idTipoDeDocumento) {
        await updateMutation.mutateAsync({
          id: editing.idTipoDeDocumento,
          data: editing,
        });
        toast.success("Tipo de documento actualizado");
      } else {
        await createMutation.mutateAsync(editing);
        toast.success("Tipo de documento creado");
      }
      setModalOpen(false);
    } catch {
      toast.error("Error al guardar");
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success("Tipo de documento eliminado");
    } catch {
      toast.error("Error al eliminar — puede estar en uso");
    } finally {
      setDeleteId(null);
    }
  }

  const columns: Column<TipoDeDocumento>[] = [
    {
      key: "id",
      header: "ID",
      render: (t) => (
        <span className="text-xs text-muted-foreground">
          {t.idTipoDeDocumento}
        </span>
      ),
      className: "w-16",
    },
    {
      key: "nombre",
      header: "Nombre",
      render: (t) => <span className="font-medium">{t.nombre ?? "—"}</span>,
    },
    {
      key: "actions",
      header: "",
      className: "w-24",
      render: (t) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(t)}>
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(t.idTipoDeDocumento!)}
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
        title="Tipos de Documento"
        description="CU27, CU65 — Administrar el catálogo de tipos de documento presentado"
        actions={
          <Button onClick={openCreate}>
            <Plus className="h-4 w-4" />
            Nuevo tipo
          </Button>
        }
      />

      <DataTable
        data={tipos}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(t) => t.idTipoDeDocumento!}
        emptyMessage="No hay tipos de documento registrados"
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {isEditMode ? "Editar tipo de documento" : "Nuevo tipo de documento"}
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
                placeholder="Ej: DNI, Escritura, Certificado de dominio…"
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
        description="Esto eliminará el tipo de documento. Los documentos presentados de este tipo no se eliminarán."
      />
    </div>
  );
}
